/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.model;

import static de.amr.games.pacman.lib.Globals.RND;
import static de.amr.games.pacman.lib.steering.NavigationPoint.np;
import static de.amr.games.pacman.model.world.World.halfTileRightOf;

import java.util.ArrayList;
import java.util.Optional;

import org.tinylog.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.world.World;

/**
 * @author Armin Reichert
 */
public class BonusManagement {

	private final GameLevel level;
	private final BonusInfo[] bonusInfo = new BonusInfo[2];
	private Bonus bonus;

	public BonusManagement(GameLevel level) {
		this.level = level;
	}

	public void onLevelStart() {
		bonusInfo[0] = createNextBonusInfo();
		bonusInfo[1] = createNextBonusInfo();
	}

	public void onLevelEnd() {
		deactivateBonus();
	}

	private BonusInfo createNextBonusInfo() {
		if (level.game().variant() == GameVariant.MS_PACMAN) {
			return nextMsPacManBonusInfo();
		} else {
			// In the Pac-Man game, each level has two boni with the same symbol and value
			switch (level.number()) {
			//@formatter:off
				case 1: return      GameModel.PACMAN_CHERRIES;
				case 2: return      GameModel.PACMAN_STRAWBERRY;
				case 3:
				case 4: return   GameModel.PACMAN_PEACH_ORANGE;
				case 5:
				case 6: return   GameModel.PACMAN_APPLE;
				case 7:
				case 8: return   GameModel.PACMAN_GRAPES;
				case 9:
				case 10: return  GameModel.PACMAN_GALAXIAN;
				case 11:
				case 12: return GameModel.PACMAN_BELL;
				default: return     GameModel.PACMAN_KEY;
			}
			//@formatter:on
		}
	}

	/**
	 * (From Reddit user <em>damselindis</em>, see
	 * https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/)
	 * <p>
	 * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
	 * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
	 * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
	 * spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
	 * <p>
	 * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
	 * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
	 * the following table:
	 * 
	 * <table>
	 * <tr>
	 * <th>Cherry
	 * <th>Strawberry
	 * <th>Peach
	 * <th>Pretzel
	 * <th>Apple
	 * <th>Pear
	 * <th>Banana
	 * </tr>
	 * <tr>
	 * <td>5/32
	 * <td>5/32
	 * <td>5/32
	 * <td>5/32
	 * <td>4/32
	 * <td>4/32
	 * <td>4/32
	 * </tr>
	 * </table>
	 */
	private BonusInfo nextMsPacManBonusInfo() {
		switch (level.number()) {
		//@formatter:off
			case 1: return GameModel.MS_PACMAN_CHERRIES;
			case 2: return GameModel.MS_PACMAN_STRAWBERRY;
			case 3: return GameModel.MS_PACMAN_PEACH_ORANGE;
			case 4: return GameModel.MS_PACMAN_PRETZEL;
			case 5: return GameModel.MS_PACMAN_APPLE;
			case 6: return GameModel.MS_PACMAN_PEAR;
			case 7: return GameModel.MS_PACMAN_BANANA;
			default: {
				int random = Globals.randomInt(0, 320);
				if (random < 50)  return GameModel.MS_PACMAN_CHERRIES;
				if (random < 100) return GameModel.MS_PACMAN_STRAWBERRY;
				if (random < 150) return GameModel.MS_PACMAN_PEACH_ORANGE;
				if (random < 200) return GameModel.MS_PACMAN_PRETZEL;
				if (random < 240) return GameModel.MS_PACMAN_APPLE;
				if (random < 280) return GameModel.MS_PACMAN_PEAR;
				else              return GameModel.MS_PACMAN_BANANA;
			}
		//@formatter:on
		}
	}

	public boolean isFirstBonusReached() {
		switch (level.game().variant()) {
			case MS_PACMAN: return level.world().eatenFoodCount() == 64;
			case PACMAN: return level.world().eatenFoodCount() == 70;
			default: throw new IllegalGameVariantException(level.game().variant());
		}
	}

	public boolean isSecondBonusReached() {
		switch (level.game().variant()) {
			case MS_PACMAN: return level.world().eatenFoodCount() == 176;
			case PACMAN: return level.world().eatenFoodCount() == 170;
			default: throw new IllegalGameVariantException(level.game().variant());
		}
	}

	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	public BonusInfo bonusInfo(int index) {
		return bonusInfo[index];
	}

	public void deactivateBonus() {
		if (bonus != null) {
			bonus.setInactive();
		}
	}

	public void updateBonus() {
		if (bonus != null) {
			bonus.update(level);
		}
	}

	/**
	 * Handles bonus achievment (public access only for level state test).
	 * 
	 * @param bonusIndex achieved bonus index (0 or 1).
	 */
	public void handleBonusReached(int bonusIndex) {
		switch (level.game().variant()) {
			case MS_PACMAN: {
			if (bonusIndex == 1 && bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
				Logger.info("First bonus still active, skip second one");
				return;
			}
			bonus = createMovingBonus(bonusIndex);
			bonus.setEdible(TickTimer.INDEFINITE);
			Logger.info("Moving bonus activated");
			GameEvents.publishGameEvent(GameEventType.BONUS_GETS_ACTIVE, bonus.entity().tile());
			break;
		}
			case PACMAN: {
			bonus = createStaticBonus(bonusIndex);
			int ticks = 10 * GameModel.FPS - RND.nextInt(GameModel.FPS); // between 9 and 10 seconds
			bonus.setEdible(ticks);
			GameEvents.publishGameEvent(GameEventType.BONUS_GETS_ACTIVE, bonus.entity().tile());
			break;
		}
			default: throw new IllegalGameVariantException(level.game().variant());
		}
	}

	private Bonus createStaticBonus(int bonusIndex) {
		var staticBonus = new StaticBonus(bonusInfo[bonusIndex]);
		staticBonus.entity().setPosition(halfTileRightOf(13, 20));
		return staticBonus;
	}

	/**
	 * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the house and
	 * finally leaves the world through a random portal on the opposite side of the world.
	 * <p>
	 * TODO this is not exactly the behavior from the original game, yes I know.
	 **/
	private Bonus createMovingBonus(int bonusIndex) {
		var portals = level.world().portals();
		var leftToRight = RND.nextBoolean();
		var entryPortal = portals.get(RND.nextInt(portals.size()));
		var exitPortal = portals.get(RND.nextInt(portals.size()));
		var startPoint = leftToRight ? np(entryPortal.leftTunnelEnd()) : np(entryPortal.rightTunnelEnd());
		var exitPoint = leftToRight ? np(exitPortal.rightTunnelEnd().plus(1, 0))
				: np(exitPortal.leftTunnelEnd().minus(1, 0));
		var houseEntryTile = World.tileAt(level.world().house().door().entryPosition());
		int houseHeight = level.world().house().size().y();

		var route = new ArrayList<NavigationPoint>();
		route.add(np(houseEntryTile));
		route.add(np(houseEntryTile.plus(0, houseHeight + 1)));
		route.add(np(houseEntryTile));
		route.add(exitPoint);
		route.trimToSize();

		var movingBonus = new MovingBonus(bonusInfo[bonusIndex]);
		movingBonus.setRoute(route);
		movingBonus.entity().placeAtTile(startPoint.tile(), 0, 0);
		movingBonus.entity().setMoveAndWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
		Logger.info("Moving bonus created, route: {} ({})", route, (leftToRight ? "left to right" : "right to left"));

		return movingBonus;
	}
}