/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.model.actors;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkDirectionNotNull;
import static de.amr.games.pacman.lib.Globals.checkLevelNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;
import static de.amr.games.pacman.lib.steering.Direction.DOWN;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.RIGHT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.world.World.tileAt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.tinylog.Logger;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.Portal;

/**
 * Base class for all creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public abstract class Creature extends Entity {

	protected static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	private static class MoveResult {
		private boolean moved;
		private boolean tunnelEntered;
		private boolean teleported;
		private List<String> messages;

		public MoveResult() {
			moved = false;
			tunnelEntered = false;
			teleported = false;
			messages = new ArrayList<>(3);
		}

		public String summary() {
			return messages.stream().collect(Collectors.joining(", "));
		}

		/*@Override
		public String toString() {
			var sb = new StringBuilder("");
			sb.append(tunnelEntered ? " entered tunnel" : "");
			sb.append(teleported ? " teleported" : "");
			sb.append(moved ? " moved" : "");
			return sb.length() == 0 ? "" : "[" + sb.toString().trim() + "]";
		}*/
	}

	private final String name;
	private Direction moveDir;
	private Direction wishDir;
	private Vector2i targetTile;

	private MoveResult moveResult;
	protected boolean newTileEntered; // TODO put this into move result but currently it has another lifetime
	protected boolean gotReverseCommand;
	protected boolean canTeleport;

	protected float corneringSpeedUp = 0;

	protected Creature(String name) {
		this.name = (name != null) ? name : "%s@%d"/*.formatted(getClass().getSimpleName(), hashCode())*/;
	}

	public void reset() {
		// entity
		visible = false;
		position = Vector2f.ZERO;
		velocity = Vector2f.ZERO;
		acceleration = Vector2f.ZERO;

		moveDir = RIGHT;
		wishDir = RIGHT;
		targetTile = null;

		gotReverseCommand = false;
		canTeleport = true;

		moveResult = null;
		newTileEntered = true;
	}

	/**
	 * @param level game level
	 * @return if the creature can reverse its direction
	 */
	public abstract boolean canReverse(GameLevel level);

	/*@Override
	public String toString() {
		return "%s: position=%s, tile=%s (%s), velocity=%s, moveDir=%s, wishDir=%s".formatted(name, position, tile(),
				offset(), velocity, moveDir, wishDir);
	}*/

	/** Readable name, for display and logging purposes. */
	public String name() {
		return name;
	}

	/** Tells if the creature entered a new tile with its last move or placement. */
	public boolean isNewTileEntered() {
		return newTileEntered;
	}

	/**
	 * Set teleport capability for this creature.
	 * 
	 * @param canTeleport if this creature can teleport
	 */
	public void setCanTeleport(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	/**
	 * @return if this creature can teleport
	 */
	public boolean canTeleport() {
		return canTeleport;
	}

	/**
	 * Sets the tile this creature tries to reach. May be an unreachable tile or <code>null</code>.
	 * 
	 * @param tile some tile or <code>null</code>
	 */
	public void setTargetTile(Vector2i tile) {
		targetTile = tile;
	}

	/**
	 * @return (Optional) target tile. Can be inaccessible or outside of the world.
	 */
	public Optional<Vector2i> targetTile() {
		return Optional.ofNullable(targetTile);
	}

	/**
	 * Places this creature at the given tile coordinate with the given tile offsets. Updates the
	 * <code>newTileEntered</code> state.
	 * 
	 * @param tx tile x-coordinate (grid column)
	 * @param ty tile y-coordinate (grid row)
	 * @param ox x-offset inside tile
	 * @param oy y-offset inside tile
	 */
	public void placeAtTile(int tx, int ty, float ox, float oy) {
		var prevTile = tile();
		setPosition(tx * TS + ox, ty * TS + oy);
		newTileEntered = !tile().equals(prevTile);
	}

	/**
	 * Places this creature at the given tile coordinate with the given tile offsets. Updates the
	 * <code>newTileEntered</code> state.
	 * 
	 * @param tile tile
	 * @param ox   x-offset inside tile
	 * @param oy   y-offset inside tile
	 */
	public void placeAtTile(Vector2i tile, float ox, float oy) {
		checkTileNotNull(tile);
		placeAtTile(tile.x(), tile.y(), ox, oy);
	}

	/**
	 * Places this creature exactly (no offsets) at the given tile coordinate. Updates the <code>newTileEntered</code>
	 * state.
	 * 
	 * @param tile tile
	 */
	public void placeAtTile(Vector2i tile) {
		checkTileNotNull(tile);
		placeAtTile(tile.x(), tile.y(), 0, 0);
	}

	/**
	 * Simulates the overflow bug from the original Arcade version.
	 * 
	 * @param numTiles number of tiles
	 * @return the tile located the given number of tiles in front of the creature (towards move direction). In case
	 *         creature looks up, additional n tiles are added towards left. This simulates an overflow error in the
	 *         original Arcade game.
	 */
	public Vector2i tilesAheadBuggy(int numTiles) {
		Vector2i ahead = tile().plus(moveDir().vector().scaled(numTiles));
		return moveDir() == Direction.UP ? ahead.minus(numTiles, 0) : ahead;
	}

	/**
	 * @param tile  some tile inside or outside of the world
	 * @param level the game level (tile access can depend on the game level where the creature exists)
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(Vector2i tile, GameLevel level) {
		checkTileNotNull(tile);
		checkLevelNotNull(level);
		if (level.world().insideBounds(tile)) {
			return !level.world().isWall(tile) && !level.world().house().door().occupies(tile);
		}
		return level.world().belongsToPortal(tile);
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		checkDirectionNotNull(dir);
		if (moveDir != dir) {
			moveDir = dir;
			Logger.trace("{}: New moveDir: {}. {}", name, moveDir, this);
			velocity = moveDir.vector().toFloatVec().scaled(velocity.length());
		}
	}

	/** @return The current move direction. */
	public Direction moveDir() {
		return moveDir;
	}

	/**
	 * Sets the wish direction and updates the velocity vector.
	 * 
	 * @param dir the new wish direction
	 */
	public void setWishDir(Direction dir) {
		checkDirectionNotNull(dir);
		if (wishDir != dir) {
			wishDir = dir;
			Logger.trace("{}: New wishDir: {}. {}", name, wishDir, this);
		}
	}

	/** @return The wish direction. Will be taken as soon as possible. */
	public Direction wishDir() {
		return wishDir;
	}

	/**
	 * Sets both directions at once.
	 * 
	 * @param dir the new wish and move direction
	 */
	public void setMoveAndWishDir(Direction dir) {
		setWishDir(dir);
		setMoveDir(dir);
	}

	/**
	 * Signals that this creature should reverse its move direction as soon as possible.
	 */
	public void reverseAsSoonAsPossible() {
		gotReverseCommand = true;
		newTileEntered = false;
		Logger.trace("{} (moveDir={}, wishDir={}) got command to reverse direction", name, moveDir, wishDir);
	}

	/**
	 * Sets the speed as a fraction of the base speed (1.25 pixels/sec).
	 * 
	 * @param fraction fraction of base speed
	 */
	public void setRelSpeed(float fraction) {
		if (fraction < 0) {
			throw new IllegalArgumentException("Negative speed fraction: " + fraction);
		}
		setPixelSpeed(fraction * GameModel.SPEED_PX_100_PERCENT);
	}

	/**
	 * Sets the absolute speed and updates the velocity vector.
	 * 
	 * @param pixelSpeed speed in pixels
	 */
	public void setPixelSpeed(float pixelSpeed) {
		if (pixelSpeed < 0) {
			throw new IllegalArgumentException("Negative pixel speed: " + pixelSpeed);
		}
		velocity = pixelSpeed == 0 ? Vector2f.ZERO : moveDir.vector().toFloatVec().scaled(pixelSpeed);
	}

	/**
	 * Sets the new wish direction for reaching the target tile.
	 * 
	 * @param level the game level
	 */
	public void navigateTowardsTarget(GameLevel level) {
		checkLevelNotNull(level);
		if (!newTileEntered && moved()) {
			return; // we don't need no navigation, dim dit diddit diddit dim dit diddit diddit...
		}
		if (targetTile == null) {
			return;
		}
		if (level.world().belongsToPortal(tile())) {
			return; // inside portal, no navigation happens
		}
		computeTargetDirection(level).ifPresent(this::setWishDir);
	}

	private Optional<Direction> computeTargetDirection(GameLevel level) {
		final var currentTile = tile();
		Direction targetDir = null;
		float minDistance = Float.MAX_VALUE;
		for (var dir : DIRECTION_PRIORITY) {
			if (dir == moveDir.opposite()) {
				continue; // reversing the move direction is not allowed
			}
			final var neighborTile = currentTile.plus(dir.vector());
			if (canAccessTile(neighborTile, level)) {
				final var distance = neighborTile.euclideanDistance(targetTile);
				if (distance < minDistance) {
					minDistance = distance;
					targetDir = dir;
				}
			}
		}
		return Optional.ofNullable(targetDir);
	}

	public boolean moved() {
		return moveResult != null && moveResult.moved;
	}

	public boolean teleported() {
		return moveResult != null && moveResult.teleported;
	}

	public boolean enteredTunnel() {
		return moveResult != null && moveResult.tunnelEntered;
	}

	/**
	 * Tries moving through the given game level.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
	 * possible, it keeps moving to its current move direction.
	 * 
	 * @param level the game level
	 */
	public void tryMoving(GameLevel level) {
		checkLevelNotNull(level);
		moveResult = new MoveResult();
		tryTeleport(level.world().portals());
		if (!moveResult.teleported) {
			checkReverseCommand(level);
			tryMoving(wishDir, level);
			if (moveResult.moved) {
				setMoveDir(wishDir);
			} else {
				tryMoving(moveDir, level);
			}
		}
		if (moveResult.teleported || moveResult.moved) {
			Logger.trace("{}: {} {} {}", name, moveResult, moveResult.summary(), this);
		}
	}

	private void checkReverseCommand(GameLevel level) {
		if (gotReverseCommand && canReverse(level)) {
			setWishDir(moveDir.opposite());
			gotReverseCommand = false;
			Logger.trace("{}: [turned around]", name);
		}
	}

	private void tryTeleport(List<Portal> portals) {
		if (canTeleport) {
			for (var portal : portals) {
				teleport(portal);
				if (moveResult.teleported) {
					return;
				}
			}
		}
	}

	private void teleport(Portal portal) {
		var tile = tile();
		var oldPosition = position;
		if (tile.y() == portal.leftTunnelEnd().y() && position.x() < (portal.leftTunnelEnd().x() - portal.depth()) * TS) {
			placeAtTile(portal.rightTunnelEnd());
			moveResult.teleported = true;
			moveResult.messages.add("%s: Teleported from %s to %s"/*.formatted(name, oldPosition, position)*/);
		} else if (tile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
			placeAtTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
			moveResult.teleported = true;
			moveResult.messages.add("%s: Teleported from %s to %s"/*.formatted(name, oldPosition, position)*/);
		}
	}

	private void tryMoving(Direction dir, GameLevel level) {
		final var tileBeforeMove = tile();
		final var aroundCorner = !dir.sameOrientation(moveDir);
		final var dirVector = dir.vector().toFloatVec();
		final var newVelocity = dirVector.scaled(velocity.length());
		final var touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);
		final var touchedTile = tileAt(touchPosition);

		if (!canAccessTile(touchedTile, level)) {
			if (!aroundCorner) {
				placeAtTile(tile()); // adjust if blocked and moving forward
			}
			moveResult.messages.add("Cannot move %s into tile %s"/*.formatted(dir, touchedTile)*/);
			return;
		}

		if (aroundCorner) {
			var offset = dir.isHorizontal() ? offset().y() : offset().x();
			boolean atTurnPosition = Math.abs(offset) <= 1; // TODO <= pixelspeed?
			if (atTurnPosition) {
				placeAtTile(tile()); // adjust if moving around corner
			} else {
				moveResult.messages.add("Wants to take corner towards %s but not at turn position"/*.formatted(dir)*/);
				return;
			}
		}

		if (aroundCorner && corneringSpeedUp > 0) {
			setVelocity(newVelocity.plus(dirVector.scaled(corneringSpeedUp)));
			Logger.trace("{} velocity around corner: {}", name(), velocity.length());
			move();
		} else {
			setVelocity(newVelocity);
			move();
		}
		setVelocity(newVelocity);

		newTileEntered = !tileBeforeMove.equals(tile());
		moveResult.moved = true;
		moveResult.tunnelEntered = !level.world().isTunnel(tileBeforeMove) && level.world().isTunnel(tile());
		moveResult.messages.add("%5s (%.2f pixels)"/*.formatted(dir, newVelocity.length())*/);
	}
}