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
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.TickTimer.secToTicks;
import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeGhostHouse;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	/** Speed in pixels/tick at 100%. */
	public static final double BASE_SPEED = 1.25;

	protected static final long[][] HUNTING_TIMES = {
	//@formatter:off
		{ 7*60, 20*60, 7*60, 20*60, 5*60,   20*60, 5*60, TickTimer.INDEFINITE },
		{ 7*60, 20*60, 7*60, 20*60, 5*60, 1033*60,    1, TickTimer.INDEFINITE },
		{ 5*60, 20*60, 5*60, 20*60, 5*60, 1037*60,    1, TickTimer.INDEFINITE }
	//@formatter:on
	};

	public static final int MAX_CREDIT = 99;
	public static final int PELLET_VALUE = 10;
	public static final int PELLET_RESTING_TICKS = 1;
	public static final int ENERGIZER_VALUE = 50;
	public static final int ENERGIZER_RESTING_TICKS = 3;
	public static final int INITIAL_LIFES = 3;
	public static final int ALL_GHOSTS_KILLED_POINTS = 12_000;
	public static final int EXTRA_LIFE = 10_000;

	// not sure exactly how long Pac-Man is losing power
	public static final long PAC_POWER_FADING_TICKS = secToTicks(2);

	protected static final int[] GHOST_VALUES = { 200, 400, 800, 1600 };

	/** The game variant respresented by this model. */
	public final GameVariant variant;

	/** Credit for playing. */
	protected int credit;

	/** Tells if the game play is active. */
	public boolean playing;

	/** Pac-Man or Ms. Pac-Man. */
	public final Pac pac;

	/** Controls the time Pac has power. */
	public final TickTimer powerTimer = new TickTimer("Pac-power-timer");

	/** Tells if Pac-Man can be killed by ghosts. */
	public boolean isPacImmune;

	/** If Pac-Man is controlled by autopilot. */
	public boolean isPacAutoControlled;

	/** The ghosts in order RED, PINK, CYAN, ORANGE. */
	public final Ghost[] theGhosts;

	/** "Cruise Elroy" state. Values: <code>0, 1, 2, -1, -2 (0= "off", negative value = "disabled")</code>. */
	public byte cruiseElroyState;

	/** The position of the ghosts when the game starts. */
	public final V2d[] homePosition = new V2d[4];

	/** The tiles inside the house where the ghosts get revived. Amen. */
	public final V2d[] revivalPosition = new V2d[4];

	/** The (unreachable) tiles in the corners targeted during the scatter phase. */
	public final V2i[] scatterTile = new V2i[4];

	/** Timer used to control hunting phases. */
	public final HuntingTimer huntingTimer = new HuntingTimer();

	/** Current level. */
	public GameLevel level;

	/** Number of lives remaining. */
	public int lives;

	/** If lives or one less is displayed in lives counter. */
	public boolean livesOneLessShown;

	/** Number of ghosts killed at the current level. */
	public int numGhostsKilledInLevel;

	/** Ghosts killed using the same energizer are indexed in order <code>0..4</code>. */
	public final int[] killedIndex = new int[4];

	/** Number of ghosts killed by current energizer. */
	public int ghostsKilledByEnergizer;

	/** List of collected level symbols. */
	public final LevelCounter levelCounter = new LevelCounter(7);

	/** Energizer animation. */
	public final SingleEntityAnimation<Boolean> energizerPulse = SingleEntityAnimation.pulse(10);

	/** Counters used by ghost house logic. */
	protected final int[] ghostDotCounter = new int[4];

	/** Counter used by ghost house logic. */
	protected int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	protected boolean globalDotCounterEnabled;

	/** Max number of clock ticks Pac can be starving until ghost gets unlocked. */
	protected int pacStarvingTimeLimit;

	protected int[] globalDotLimits;

	protected byte[] privateDotLimits;

	/** Number of current intermission scene in test mode. */
	public int intermissionTestNumber;

	public final Score gameScore;

	public final Score highScore;

	private File hiscoreFile;

	private boolean scoresEnabled;

	public final Memo memo = new Memo();

	protected GameModel(GameVariant variant, String pacName, String redGhostName, String pinkGhostName,
			String cyanGhostName, String orangeGhostName) {

		this.variant = variant;

		pac = new Pac(pacName);

		var redGhost = new Ghost(RED_GHOST, redGhostName);
		redGhost.setChasingTarget(pac::tile);

		var pinkGhost = new Ghost(PINK_GHOST, pinkGhostName);
		pinkGhost.setChasingTarget(() -> tilesAhead(pac, 4));

		var cyanGhost = new Ghost(CYAN_GHOST, cyanGhostName);
		cyanGhost.setChasingTarget(() -> tilesAhead(pac, 2).scaled(2).minus(redGhost.tile()));

		var orangeGhost = new Ghost(ORANGE_GHOST, orangeGhostName);
		orangeGhost.setChasingTarget(
				() -> orangeGhost.tile().euclideanDistance(pac.tile()) < 8 ? scatterTile[ORANGE_GHOST] : pac.tile());

		theGhosts = new Ghost[] { redGhost, pinkGhost, cyanGhost, orangeGhost };

		gameScore = new Score("SCORE");
		highScore = new Score("HIGH SCORE");
	}

	private void loadScoreFromFile(Score score, File file) {
		try (var in = new FileInputStream(file)) {
			var props = new Properties();
			props.loadFromXML(in);
			// parse
			var points = Integer.parseInt(props.getProperty("points"));
			var levelNumber = Integer.parseInt(props.getProperty("level"));
			var date = LocalDate.parse(props.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
			// parsing ok
			score.points = points;
			score.levelNumber = levelNumber;
			score.date = date;
			LOGGER.info("Score loaded. File: '%s' Points: %d Level: %d", file.getAbsolutePath(), score.points,
					score.levelNumber);
		} catch (Exception x) {
			LOGGER.info("Score could not be loaded. File '%s' Reason: %s", file, x.getMessage());
		}
	}

	public void setHiscoreFile(File hiscoreFile) {
		this.hiscoreFile = hiscoreFile;
		loadScoreFromFile(highScore, hiscoreFile);
	}

	public void enableScores(boolean enabled) {
		this.scoresEnabled = enabled;
	}

	public void reloadScores() {
		loadScoreFromFile(highScore, hiscoreFile);
	}

	public void scorePoints(int points) {
		if (!scoresEnabled) {
			return;
		}
		int scoreBeforeAddingPoints = gameScore.points;
		gameScore.points += points;
		if (gameScore.points > highScore.points) {
			highScore.points = gameScore.points;
			highScore.levelNumber = level.number();
			highScore.date = LocalDate.now();
		}
		if (scoreBeforeAddingPoints < GameModel.EXTRA_LIFE && gameScore.points >= GameModel.EXTRA_LIFE) {
			lives++;
			GameEvents.publish(new GameEvent(this, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, pac.tile()));
		}
	}

	public void saveHiscore() {
		Score latestHiscore = new Score("");
		loadScoreFromFile(latestHiscore, hiscoreFile);
		if (highScore.points <= latestHiscore.points) {
			return;
		}
		var props = new Properties();
		props.setProperty("points", String.valueOf(highScore.points));
		props.setProperty("level", String.valueOf(highScore.levelNumber));
		props.setProperty("date", highScore.date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		try (var out = new FileOutputStream(hiscoreFile)) {
			props.storeToXML(out, "");
			LOGGER.info("New hiscore saved. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(), highScore.points,
					highScore.levelNumber);
		} catch (Exception x) {
			LOGGER.info("Highscore could not be saved. File '%s' Reason: %s", hiscoreFile, x.getMessage());
		}
	}

	// simulates the overflow bug from the original Arcade version
	private static V2i tilesAhead(Creature guy, int n) {
		var ahead = guy.tile().plus(guy.moveDir().vec.scaled(n));
		return guy.moveDir() == UP ? ahead.minus(n, 0) : ahead;
	}

	public int getCredit() {
		return credit;
	}

	public void setCredit(int credit) {
		if (credit <= MAX_CREDIT) {
			this.credit = credit;
		}
	}

	public boolean addCredit() {
		if (credit < MAX_CREDIT) {
			++credit;
			return true;
		}
		return false;
	}

	public void consumeCredit() {
		if (credit > 0) {
			--credit;
		}
	}

	public boolean hasCredit() {
		return credit > 0;
	}

	public World world() {
		return level.world();
	}

	public void reset() {
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
		playing = false;
		lives = INITIAL_LIFES;
		livesOneLessShown = false;
		intermissionTestNumber = 1;
		reloadScores();
		gameScore.reset();
	}

	protected void initGhosts() {
		for (var ghost : theGhosts) {
			ghostDotCounter[ghost.id] = 0;
		}
		cruiseElroyState = 0;
		if (world() instanceof ArcadeWorld) {
			homePosition[RED_GHOST] = seatPosition(ArcadeGhostHouse.ENTRY_TILE);
			revivalPosition[RED_GHOST] = seatPosition(ArcadeGhostHouse.SEAT_TILE_CENTER);
			scatterTile[RED_GHOST] = ArcadeWorld.RIGHT_UPPER_CORNER;

			homePosition[PINK_GHOST] = seatPosition(ArcadeGhostHouse.SEAT_TILE_CENTER);
			revivalPosition[PINK_GHOST] = seatPosition(ArcadeGhostHouse.SEAT_TILE_CENTER);
			scatterTile[PINK_GHOST] = ArcadeWorld.LEFT_UPPER_CORNER;

			homePosition[CYAN_GHOST] = seatPosition(ArcadeGhostHouse.SEAT_TILE_LEFT);
			revivalPosition[CYAN_GHOST] = seatPosition(ArcadeGhostHouse.SEAT_TILE_LEFT);
			scatterTile[CYAN_GHOST] = ArcadeWorld.RIGHT_LOWER_CORNER;

			homePosition[ORANGE_GHOST] = seatPosition(ArcadeGhostHouse.SEAT_TILE_RIGHT);
			revivalPosition[ORANGE_GHOST] = seatPosition(ArcadeGhostHouse.SEAT_TILE_RIGHT);
			scatterTile[ORANGE_GHOST] = ArcadeWorld.LEFT_LOWER_CORNER;
		}
	}

	private V2d seatPosition(V2i seatTile) {
		return new V2d(seatTile).scaled(TS).plus(HTS, 0);
	}

	public void resetGuys() {
		Arrays.fill(killedIndex, -1);
		powerTimer.reset(0);
		energizerPulse.reset();
		pac.reset();
		pac.placeAtTile(v(13, 26), HTS, 0);
		pac.setMoveAndWishDir(Direction.LEFT);
		pac.show();
		pac.selectAndRunAnimation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::reset);
		ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setMoveAndWishDir(switch (ghost.id) {
			case Ghost.RED_GHOST -> Direction.LEFT;
			case Ghost.PINK_GHOST -> Direction.DOWN;
			case Ghost.CYAN_GHOST, Ghost.ORANGE_GHOST -> Direction.UP;
			default -> throw new IllegalArgumentException("Ghost ID: " + ghost.id);
			});
			ghost.setPosition(homePosition[ghost.id]);
			ghost.show();
			ghost.enterStateLocked();
		});
	}

	public Stream<Creature> guys() {
		return Stream.of(pac, theGhosts[RED_GHOST], theGhosts[PINK_GHOST], theGhosts[CYAN_GHOST], theGhosts[ORANGE_GHOST]);
	}

	public Stream<Ghost> ghosts(GhostState... states) {
		if (states.length == 0) {
			return Stream.of(theGhosts); // because is() would return an empty stream
		}
		return ghosts().filter(ghost -> ghost.is(states));
	}

	/**
	 * @param ghost a ghost
	 * @param dir   a direction
	 * @return tells if the ghost can currently move towards the given direction
	 */
	public boolean isGhostAllowedMoving(Ghost ghost, Direction dir) {
		return true;
	}

	protected int ghostValue(int ghostKillIndex) {
		return GHOST_VALUES[ghostKillIndex];
	}

	/**
	 * Initializes the model for given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public abstract void setLevel(int levelNumber);

	// Hunting

	/**
	 * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
	 * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
	 * ghosts attack Pac-Man.
	 * 
	 * @param phase hunting phase (0..7)
	 */
	public void startHuntingPhase(int phase) {
		huntingTimer.startPhase(phase, huntingPhaseTicks(phase));
	}

	/**
	 * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
	 * phases, the living ghosts outside of the ghosthouse reverse their move direction.
	 */
	private void advanceHunting() {
		huntingTimer.advance();
		if (huntingTimer.hasExpired()) {
			startHuntingPhase(huntingTimer.phase() + 1);
			ghosts(HUNTING_PAC, FRIGHTENED, LOCKED, LEAVING_HOUSE).forEach(Ghost::forceTurningBack);
		}
	}

	/**
	 * @param phase hunting phase (0, ... 7)
	 * @return hunting (scattering or chasing) ticks for current level and given phase
	 */
	private long huntingPhaseTicks(int phase) {
		if (phase < 0 || phase > 7) {
			throw new IllegalArgumentException("Hunting phase must be 0..7, but is " + phase);
		}
		return switch (level.number()) {
		case 1 -> HUNTING_TIMES[0][phase];
		case 2, 3, 4 -> HUNTING_TIMES[1][phase];
		default -> HUNTING_TIMES[2][phase];
		};
	}

	/**
	 * @param levelNumber game level number
	 * @return 1-based intermission (cut scene) number that is played after given level or <code>0</code> if no
	 *         intermission is played after given level.
	 */
	public int intermissionNumber(int levelNumber) {
		return switch (levelNumber) {
		case 2 -> 1;
		case 5 -> 2;
		case 9, 13, 17 -> 3;
		default -> 0;
		};
	}

	// Bonus stuff

	public abstract Bonus bonus();

	protected abstract void onBonusReached();

	// Game logic

	public void update() {
		pac.update(this);
		updateGhosts();
		bonus().update(this);
		advanceHunting();
		energizerPulse.advance();
		powerTimer.advance();
	}

	public static class Memo {
		public boolean allFoodEaten;
		public boolean foodFound;
		public boolean energizerFound;
		public boolean bonusReached;
		public boolean pacMetKiller;
		public boolean pacGotPower;
		public boolean pacPowerLost;
		public boolean pacPowerFading;
		public boolean ghostsKilled;
		public Ghost[] edibleGhosts;
		public Optional<Ghost> unlockedGhost;
		public String unlockReason;

		public Memo() {
			forgetEverything();
		}

		public void forgetEverything() {
			allFoodEaten = false;
			foodFound = false;
			energizerFound = false;
			bonusReached = false;
			pacMetKiller = false;
			pacGotPower = false;
			pacPowerLost = false;
			pacPowerFading = false;
			ghostsKilled = false;
			edibleGhosts = new Ghost[0];
			unlockedGhost = Optional.empty();
			unlockReason = null;
		}
	}

	public void whatHappenedWithFood() {
		checkFoodFound();
		if (memo.foodFound) {
			onFoodFound();
			if (memo.bonusReached) {
				onBonusReached();
			}
		} else {
			pac.starve();
		}
	}

	public void whatHappenedWithTheGuys() {
		if (memo.pacGotPower) {
			onPacGetsPower();
		}

		memo.pacMetKiller = isPacMeetingKiller();
		if (memo.pacMetKiller) {
			onPacMetKiller();
			return; // enter new game state
		}

		checkEdibleGhosts();
		if (memo.edibleGhosts.length > 0) {
			killGhosts(memo.edibleGhosts);
			memo.ghostsKilled = true;
			return; // enter new game state
		}
		checkPacPower();
		if (memo.pacPowerFading) {
			GameEvents.publish(GameEventType.PAC_STARTS_LOSING_POWER, pac.tile());
		}
		if (memo.pacPowerLost) {
			onPacPowerLost();
		}
	}

	private boolean isPacMeetingKiller() {
		return !isPacImmune && !powerTimer.isRunning() && ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
	}

	private void onPacMetKiller() {
		pac.die();
		var redGhost = theGhosts[RED_GHOST];
		if (cruiseElroyState > 0) {
			LOGGER.info("Cruise Elroy mode %d for %s disabled", cruiseElroyState, redGhost.name);
			cruiseElroyState = (byte) -cruiseElroyState; // negative value means "disabled"
		}
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		LOGGER.info("Global dot counter got reset and enabled because %s died", pac.name);
	}

	private void checkEdibleGhosts() {
		memo.edibleGhosts = ghosts(FRIGHTENED).filter(pac::sameTile).toArray(Ghost[]::new);
	}

	/**
	 * Cheat.
	 */
	public void killAllPossibleGhosts() {
		var prey = ghosts(GhostState.HUNTING_PAC, GhostState.FRIGHTENED).toArray(Ghost[]::new);
		ghostsKilledByEnergizer = 0;
		killGhosts(prey);
	}

	private void killGhosts(Ghost[] prey) {
		Stream.of(prey).forEach(this::killGhost);
		numGhostsKilledInLevel += prey.length;
		if (numGhostsKilledInLevel == 16) {
			LOGGER.info("All ghosts killed at level %d, Pac-Man wins additional %d points", level.number(),
					ALL_GHOSTS_KILLED_POINTS);
			scorePoints(ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		killedIndex[ghost.id] = ghostsKilledByEnergizer;
		ghostsKilledByEnergizer++;
		ghost.enterStateEaten(this);
		int value = ghostValue(killedIndex[ghost.id]);
		scorePoints(value);
		LOGGER.info("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), value);
	}

	private void startPowerTimer(double seconds) {
		powerTimer.resetSeconds(seconds);
		powerTimer.start();
		LOGGER.info("Power timer started: %s", powerTimer);
	}

	private void checkPacPower() {
		memo.pacPowerFading = powerTimer.remaining() == PAC_POWER_FADING_TICKS;
		memo.pacPowerLost = powerTimer.hasExpired();
	}

	public boolean isPacPowerFading() {
		return powerTimer.isRunning() && powerTimer.remaining() <= PAC_POWER_FADING_TICKS;
	}

	private void onPacPowerLost() {
		LOGGER.info("%s lost power, timer=%s", pac.name, powerTimer);
		// leave state EXPIRED to avoid repetitions:
		powerTimer.resetIndefinitely();
		huntingTimer.start();
		ghosts(FRIGHTENED).forEach(ghost -> ghost.enterStateHuntingPac(this));
		GameEvents.publish(GameEventType.PAC_LOSES_POWER, pac.tile());
	}

	private void checkFoodFound() {
		if (level.world().containsFood(pac.tile())) {
			memo.foodFound = true;
			memo.allFoodEaten = level.world().foodRemaining() == 1;
			if (level.world().isEnergizerTile(pac.tile())) {
				memo.energizerFound = true;
				if (level.ghostFrightenedSeconds() > 0) {
					memo.pacGotPower = true;
				}
			}
			memo.bonusReached = level.world().eatenFoodCount() == 70 || level.world().eatenFoodCount() == 170;
		}
	}

	private void onFoodFound() {
		if (memo.energizerFound) {
			ghostsKilledByEnergizer = 0;
			eatFood(ENERGIZER_VALUE, ENERGIZER_RESTING_TICKS);
		} else {
			eatFood(PELLET_VALUE, PELLET_RESTING_TICKS);
		}
	}

	private void eatFood(int value, int restingTicks) {
		pac.endStarving();
		pac.rest(restingTicks);
		level.world().removeFood(pac.tile());
		checkIfRedGhostBecomesCruiseElroy();
		updateGhostDotCounters();
		scorePoints(value);
		GameEvents.publish(GameEventType.PAC_FINDS_FOOD, pac.tile());
	}

	private void checkIfRedGhostBecomesCruiseElroy() {
		var redGhost = theGhosts[RED_GHOST];
		var foodRemaining = world().foodRemaining();
		if (foodRemaining == level.elroy1DotsLeft()) {
			cruiseElroyState = 1;
			LOGGER.info("%s becomes Cruise Elroy 1", redGhost.name);
		} else if (foodRemaining == level.elroy2DotsLeft()) {
			cruiseElroyState = 2;
			LOGGER.info("%s becomes Cruise Elroy 2", redGhost.name);
		}
	}

	private void onPacGetsPower() {
		huntingTimer.stop();
		startPowerTimer(level.ghostFrightenedSeconds());
		ghosts(HUNTING_PAC).forEach(ghost -> {
			ghost.enterStateFrightened(this);
			ghost.forceTurningBack();
		});
		GameEvents.publish(GameEventType.PAC_GETS_POWER, pac.tile());
	}

	// Ghosts

	private void updateGhosts() {
		checkGhostCanBeUnlocked(memo);
		memo.unlockedGhost.ifPresent(ghost -> {
			unlockGhost(ghost, memo.unlockReason);
			GameEvents.publish(new GameEvent(this, GameEventType.GHOST_STARTS_LEAVING_HOUSE, ghost, ghost.tile()));
		});
		ghosts().forEach(ghost -> ghost.update(this));
	}

	// Ghost house rules, see Pac-Man dossier

	private void checkGhostCanBeUnlocked(Memo result) {
		ghosts(LOCKED).findFirst().ifPresent(ghost -> {
			if (ghost.id == RED_GHOST) {
				result.unlockedGhost = Optional.of(theGhosts[RED_GHOST]);
				result.unlockReason = "Blinky is always unlocked immediately";
				return;
			}
			// first check private dot counter
			if (!globalDotCounterEnabled && ghostDotCounter[ghost.id] >= privateDotLimits[ghost.id]) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "Private dot counter at limit (%d)".formatted(privateDotLimits[ghost.id]);
				return;
			}
			// check global dot counter
			if (globalDotCounter >= globalDotLimits[ghost.id]) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "Global dot counter at limit (%d)".formatted(globalDotLimits[ghost.id]);
			} else if (pac.starvingTime() >= pacStarvingTimeLimit) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "%s at starving limit (%d ticks)".formatted(pac.name, pacStarvingTimeLimit);
				pac.endStarving();
			}
		});
	}

	private void unlockGhost(Ghost unlockedGhost, String reason) {
		LOGGER.info("Unlock ghost %s (%s)", unlockedGhost.name, reason);
		var redGhost = theGhosts[RED_GHOST];
		if (unlockedGhost.id == ORANGE_GHOST && cruiseElroyState < 0) {
			cruiseElroyState = (byte) -cruiseElroyState; // resume Elroy mode
			LOGGER.info("%s Elroy mode %d resumed", redGhost.name, cruiseElroyState);
		}
		if (unlockedGhost == redGhost) {
			unlockedGhost.setMoveAndWishDir(LEFT);
			unlockedGhost.enterStateHuntingPac(this);
		} else {
			unlockedGhost.enterStateLeavingHouse(this);
		}
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (theGhosts[ORANGE_GHOST].is(LOCKED) && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				LOGGER.info("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				globalDotCounter++;
			}
		} else {
			ghosts(LOCKED).filter(ghost -> ghost.id != RED_GHOST).findFirst().ifPresent(ghost -> ++ghostDotCounter[ghost.id]);
		}
	}
}