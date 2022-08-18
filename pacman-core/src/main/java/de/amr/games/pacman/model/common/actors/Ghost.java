/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.common.actors;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.actors.GhostState.RETURNING_TO_HOUSE;
import static de.amr.games.pacman.model.common.world.World.tileAt;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.AnimatedEntity;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimatedEntity {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	@Override
	protected Logger logger() {
		return LOGGER;
	}

	public static final int RED_GHOST = 0;
	public static final int PINK_GHOST = 1;
	public static final int CYAN_GHOST = 2;
	public static final int ORANGE_GHOST = 3;

	/** The ID of the ghost, see {@link #RED_GHOST} etc. */
	public final int id;

	/** The current state of this ghost. */
	private GhostState state;

	/** Function computing the chasing target of this ghost. */
	private Supplier<V2i> fnChasingTarget = () -> null;

	private EntityAnimationSet animationSet;

	private int attractRouteIndex;

	public Ghost(int id, String name) {
		super(name);
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException("Ghost ID must be in range 0..3");
		}
		this.id = id;
		reset();
	}

	@Override
	public boolean canAccessTile(V2i tile, GameModel game) {
		if (tile.equals(tile().plus(UP.vec)) && !game.isGhostAllowedMoving(this, UP)) {
			LOGGER.trace("%s cannot access tile %s", this, tile);
			return false;
		}
		if (game.world().ghostHouse().isDoorTile(tile)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		return super.canAccessTile(tile, game);
	}

	public void setChasingTarget(Supplier<V2i> targetTileSupplier) {
		this.fnChasingTarget = Objects.requireNonNull(targetTileSupplier);
	}

	@Override
	public String toString() {
		return "Ghost[%-6s %s tile=%s pos=%s offset=%s velocity=%s dir=%s wishDir=%s stuck=%s reverse=%s]".formatted(name,
				state, tile(), position, offset(), velocity, moveDir(), wishDir(), stuck, reverse);
	}

	// Here begins the state machine part

	public GhostState getState() {
		return state;
	}

	public boolean is(GhostState... alternatives) {
		return U.oneOf(state, alternatives);
	}

	/**
	 * Executes a single simulation step for this ghost in the specified game.
	 * 
	 * @param game the game
	 */
	public void update(GameModel game) {
		switch (state) {
		case LOCKED -> updateStateLocked(game);
		case LEAVING_HOUSE -> updateStateLeavingHouse(game);
		case HUNTING_PAC -> updateStateHuntingPac(game);
		case FRIGHTENED -> updateStateFrightened(game);
		case EATEN -> updateStateEaten();
		case RETURNING_TO_HOUSE -> updateStateReturningToHouse(game);
		case ENTERING_HOUSE -> updateStateEnteringHouse(game);
		}
		updateAnimation();
	}

	// --- LOCKED ---

	public void enterStateLocked() {
		state = LOCKED;
		setAbsSpeed(0.0);
		selectAndResetAnimation(AnimKeys.GHOST_COLOR);
	}

	/**
	 * In locked state, ghosts inside the house are bouncing up and down. They become blue and blink if Pac-Man gets/loses
	 * power. After that, they return to their normal color.
	 * 
	 * @param game the game
	 */
	private void updateStateLocked(GameModel game) {
		if (game.world().ghostHouse().contains(tile())) {
			setAbsSpeed(0.5);
			bounce(game.homePosition[id].y(), World.HTS);
			updateGhostInHouseAnimation(game);
		}
	}

	private void updateGhostInHouseAnimation(GameModel game) {
		if (game.powerTimer.isRunning()) {
			if (!isAnimationSelected(AnimKeys.GHOST_FLASHING)) {
				selectAndRunAnimation(AnimKeys.GHOST_BLUE);
			}
			ensureFlashingWhenPowerCeases(game);
		} else {
			selectAndRunAnimation(AnimKeys.GHOST_COLOR);
		}
	}

	// --- LEAVING_HOUSE ---

	public void enterStateLeavingHouse(GameModel game) {
		state = LEAVING_HOUSE;
		setAbsSpeed(0.5);
		selectAndRunAnimation(
				game.powerTimer.isRunning() && game.killedIndex[id] == -1 ? AnimKeys.GHOST_BLUE : AnimKeys.GHOST_COLOR);
		GameEvents.publish(new GameEvent(game, GameEventType.GHOST_STARTS_LEAVING_HOUSE, this, tile()));
	}

	/**
	 * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit. This
	 * logic is house-specific so it is placed in the house implementation. In the Arcade versions of Pac-Man and Ms.
	 * Pac-Man, the ghost first moves towards the vertical center of the house and then raises up until he has passed the
	 * door on top of the house.
	 * <p>
	 * The ghost speed is slower than outside but I do not know yet the exact value.
	 * 
	 * @param game the game
	 */
	private void updateStateLeavingHouse(GameModel game) {
		var outOfHouse = game.world().ghostHouse().leadGuyOutOfHouse(this);
		if (outOfHouse) {
			newTileEntered = false;
			setMoveAndWishDir(LEFT);
			if (game.powerTimer.isRunning() && game.killedIndex[id] == -1) {
				enterStateFrightened(game);
			} else {
				enterStateHuntingPac(game);
			}
			game.killedIndex[id] = -1;
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_COMPLETES_LEAVING_HOUSE, this, tile()));
		} else {
			updateGhostInHouseAnimation(game);
		}
	}

	// --- HUNTING_PAC ---

	/**
	 * @param game the game model
	 */
	public void enterStateHuntingPac(GameModel game) {
		state = HUNTING_PAC;
		selectAndRunAnimation(AnimKeys.GHOST_COLOR);
	}

	/**
	 * There are 4 hunting phases of different duration at each level. A hunting phase always starts with a "scatter"
	 * phase where the ghosts retreat to their maze corners. After some time they start chasing Pac-Man according to their
	 * character ("Shadow", "Speedy", "Bashful", "Pokey"). The 4th hunting phase at each level has an "infinite" chasing
	 * phase.
	 * <p>
	 * 
	 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the original intention
	 * had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only the scatter
	 * target of Blinky and Pinky would have been affected. Who knows?
	 */
	private void updateStateHuntingPac(GameModel game) {
		if (insideTunnel(game)) {
			setRelSpeed(game.level.ghostSpeedTunnel);
		} else if (id == RED_GHOST && game.cruiseElroyState == 1) {
			setRelSpeed(game.level.elroy1Speed);
		} else if (id == RED_GHOST && game.cruiseElroyState == 2) {
			setRelSpeed(game.level.elroy2Speed);
		} else {
			setRelSpeed(game.level.ghostSpeed);
		}
		if (game.variant == MS_PACMAN && game.huntingTimer.scatterPhase() == 0 && (id == RED_GHOST || id == PINK_GHOST)) {
			roam(game);
		} else if (game.huntingTimer.inChasingPhase() || id == RED_GHOST && game.cruiseElroyState > 0) {
			setTargetTile(fnChasingTarget.get());
			navigateTowardsTarget(game);
			tryMoving(game);
		} else {
			setTargetTile(game.scatterTile[id]);
			navigateTowardsTarget(game);
			tryMoving(game);
		}
	}

	// --- FRIGHTENED ---

	/**
	 * @param game the game model
	 */
	public void enterStateFrightened(GameModel game) {
		state = FRIGHTENED;
		selectAndRunAnimation(AnimKeys.GHOST_BLUE);
		attractRouteIndex = 0;
	}

	/**
	 * When frightened, a ghost moves randomly through the world, at each new tile he randomly decides where to move next.
	 * Reversing the move direction is not allowed in this state either.
	 * <p>
	 * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
	 * his power.
	 * <p>
	 * Speed is about half of the normal speed.
	 * 
	 * @param game the game
	 */
	private void updateStateFrightened(GameModel game) {
		setRelSpeed(insideTunnel(game) ? game.level.ghostSpeedTunnel : game.level.ghostSpeedFrightened);
		roam(game);
		ensureFlashingWhenPowerCeases(game);
	}

	private void roam(GameModel game) {
		if (game.hasCredit()) {
			moveRandomly(game);
		} else {
			movePseudoRandomly(game);
		}
	}

	private List<NavigationPoint> getAttractRoute(GameVariant variant) {
		return switch (variant) {
		case PACMAN -> switch (id) {
		case RED_GHOST -> PacManGame.ATTRACT_FRIGHTENED_RED_GHOST;
		case PINK_GHOST -> PacManGame.ATTRACT_FRIGHTENED_PINK_GHOST;
		case CYAN_GHOST -> PacManGame.ATTRACT_FRIGHTENED_CYAN_GHOST;
		case ORANGE_GHOST -> PacManGame.ATTRACT_FRIGHTENED_ORANGE_GHOST;
		default -> throw new IllegalArgumentException();
		};
		case MS_PACMAN -> List.of();
		};
	}

	private void movePseudoRandomly(GameModel game) {
		var route = getAttractRoute(game.variant);
		if (route.isEmpty()) {
			moveRandomly(game);
		} else if (tile().equals(route.get(attractRouteIndex).tile())) {
			var navPoint = route.get(attractRouteIndex);
			if (atTurnPositionTo(navPoint.dir())) {
				setWishDir(navPoint.dir());
				LOGGER.trace("New wish dir %s at nav point %s for %s", navPoint.dir(), navPoint.tile(), this);
				++attractRouteIndex;
			}
			tryMoving(game);
		} else {
			navigateTowardsTarget(game);
			tryMoving(game);
		}
	}

	private void moveRandomly(GameModel game) {
		if (newTileEntered || stuck) {
			Direction.shuffled().stream()//
					.filter(dir -> dir != moveDir().opposite())//
					.filter(dir -> canAccessTile(tile().plus(dir.vec), game))//
					.findAny()//
					.ifPresent(this::setWishDir);
		}
		tryMoving(game);
	}

	// --- EATEN ---

	/**
	 * After a ghost is eaten by Pac-Man he is displayed for a short time as the number of points earned for eating him.
	 * The value doubles for each ghost eaten using the power of the same energizer.
	 * 
	 * @param game the game
	 */
	public void enterStateEaten(GameModel game) {
		state = EATEN;
		// display ghost value (200, 400, 800, 1600)
		selectAndRunAnimation(AnimKeys.GHOST_VALUE).ifPresent(anim -> anim.setFrameIndex(game.killedIndex[id]));
	}

	private void updateStateEaten() {
		// nothing to do
	}

	// --- RETURNING_HOUSE ---

	public void enterStateReturningToHouse(GameModel game) {
		state = RETURNING_TO_HOUSE;
		setTargetTile(game.world().ghostHouse().entryTile());
		selectAndRunAnimation(AnimKeys.GHOST_EYES);
	}

	/**
	 * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns to the
	 * ghost house to be revived. Hallelujah!
	 * 
	 * @param game the game
	 */
	private void updateStateReturningToHouse(GameModel game) {
		if (game.world().ghostHouse().atHouseEntry(this)) {
			enterStateEnteringHouse(game);
		} else {
			setRelSpeed(2 * game.level.ghostSpeed); // not sure
			navigateTowardsTarget(game);
			tryMoving(game);
		}
	}

	// ENTERING_HOUSE state

	public void enterStateEnteringHouse(GameModel game) {
		state = ENTERING_HOUSE;
		setTargetTile(tileAt(game.revivalPosition[id]));
		GameEvents.publish(new GameEvent(game, GameEventType.GHOST_ENTERS_HOUSE, this, tile()));
	}

	/**
	 * When an eaten ghost reaches the ghost house, he enters and moves to his revival position. Because the exact route
	 * from the house entry to the revival tile is house-specific, this logic is in the house implementation.
	 * 
	 * @param game the game
	 */
	private void updateStateEnteringHouse(GameModel game) {
		boolean atRevivalTile = game.world().ghostHouse().leadGuyInside(this, game.revivalPosition[id]);
		if (atRevivalTile) {
			enterStateLeavingHouse(game);
		}
	}

	// Animations

	public void setAnimationSet(EntityAnimationSet animationSet) {
		this.animationSet = animationSet;
	}

	@Override
	public Optional<EntityAnimationSet> animationSet() {
		return Optional.ofNullable(animationSet);
	}

	private void ensureFlashingWhenPowerCeases(GameModel game) {
		if (game.powerTimer.isRunning() && game.powerTimer.remaining() <= GameModel.PAC_POWER_FADING_TICKS) {
			animationSet().ifPresent(anims -> {
				if (isAnimationSelected(AnimKeys.GHOST_FLASHING)) {
					anims.selectedAnimation().ensureRunning();
				} else {
					anims.select(AnimKeys.GHOST_FLASHING);
					var flashing = anims.selectedAnimation();
					var numFlashes = game.level.numFlashes;
					long frameTicks = GameModel.PAC_POWER_FADING_TICKS / (numFlashes * flashing.numFrames());
					flashing.setFrameDuration(frameTicks);
					flashing.setRepetions(numFlashes);
					flashing.restart();
				}
			});
		}
	}

	public void pauseFlashing(boolean paused) {
		animation(AnimKeys.GHOST_FLASHING).ifPresent(flashing -> {
			if (paused) {
				flashing.stop();
				// this is dependent on the animation implementation: display white with red eyes
				flashing.setFrameIndex(2);
			} else {
				flashing.run();
			}
		});
	}
}