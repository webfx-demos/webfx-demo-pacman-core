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
package de.amr.games.pacman.model.pacman;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.io.File;
import java.util.List;
import java.util.Random;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.GhostHouse;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends GameModel {

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	private final Object[][] data = {
	/*@formatter:off*/
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1 */
	/* 3*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2 */
	/* 6*/ {APPLE,      100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {GRAPES,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {GRAPES,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {GALAXIAN,   100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*10*/ {GALAXIAN,   100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {BELL,       100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BELL,       100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {KEY,        100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*14*/ {KEY,        100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {KEY,        100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3 */
	/*18*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {KEY,        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {KEY,        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {KEY,         90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*@formatter:on*/
	};

	private static final byte[][] MAP = {
	//@formatter:off
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,4,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{2,2,2,2,2,2,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,2,2,2,2,2,2,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		//@formatter:on
	};

	private final ArcadeWorld theWorld;
	private final StaticBonus bonus;

	public static ArcadeWorld createWorld() {
		ArcadeWorld world = new ArcadeWorld(MAP);
		world.upwardsBlockedTiles = List.of(v(12, 13), v(15, 13), v(12, 25), v(15, 25));
		world.bonusTile = v(13, 20);
		return world;
	}

	public PacManGame() {
		// all levels use the same world
		theWorld = createWorld();
		player = new Pac("Pac-Man");
		ghosts = new Ghost[] { //
				new Ghost(RED_GHOST, "Blinky", GameModel::chaseLikeShadow), //
				new Ghost(PINK_GHOST, "Pinky", GameModel::chaseLikeSpeedy), //
				new Ghost(CYAN_GHOST, "Inky", GameModel::chaseLikeBashful), //
				new Ghost(ORANGE_GHOST, "Clyde", GameModel::chaseLikePokey) //
		};
		bonus = new StaticBonus(new V2d(theWorld.bonusTile().scaled(TS)).plus(HTS, 0));
		hiscoreFile = new File(System.getProperty("user.home"), "highscore-pacman.xml");
		setLevel(1);
	}

	@Override
	public void setLevel(int n) {
		level = new GameLevel(n, n <= data.length ? data[n - 1] : data[data.length - 1]);
		initLevel(level);
		levelCounter.add(level.bonusSymbol);
		player.starvingTimeLimit = (int) sec_to_ticks(n < 5 ? 4 : 3);
		initGhosts(level);
		bonus.init();
		ghostBounty = GameModel.FIRST_GHOST_VALUE;
	}

	private void initLevel(GameLevel level) {
		level.world = theWorld;
		level.world.resetFood();
		level.mapNumber = level.mazeNumber = 1;
		level.globalDotLimits = new int[] { Integer.MAX_VALUE, 7, 17, Integer.MAX_VALUE };
		level.privateDotLimits = switch (level.number) {
		case 1 -> new int[] { 0, 0, 30, 60 };
		case 2 -> new int[] { 0, 0, 0, 50 };
		default -> new int[] { 0, 0, 0, 0 };
		};
	}

	private void initGhosts(GameLevel level) {
		GhostHouse house = theWorld.ghostHouse();
		ghosts[RED_GHOST].homeTile = house.entry();
		ghosts[RED_GHOST].revivalTile = house.seatMiddle();
		ghosts[RED_GHOST].scatterTarget = theWorld.rightUpperTarget;

		ghosts[PINK_GHOST].homeTile = ghosts[PINK_GHOST].revivalTile = house.seatMiddle();
		ghosts[PINK_GHOST].scatterTarget = theWorld.leftUpperTarget;

		ghosts[CYAN_GHOST].homeTile = ghosts[CYAN_GHOST].revivalTile = house.seatLeft();
		ghosts[CYAN_GHOST].scatterTarget = theWorld.rightLowerTarget;

		ghosts[ORANGE_GHOST].homeTile = ghosts[ORANGE_GHOST].revivalTile = house.seatRight();
		ghosts[ORANGE_GHOST].scatterTarget = theWorld.leftLowerTarget;

		for (var ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
	}

	@Override
	public StaticBonus bonus() {
		return bonus;
	}

	@Override
	public boolean checkBonusAwarded() {
		if (level.world.eatenFoodCount() == 70 || level.world.eatenFoodCount() == 170) {
			bonus.activate(level.world, level.bonusSymbol, bonusValue(level.bonusSymbol),
					sec_to_ticks(9.0 + new Random().nextDouble()));
			return true;
		}
		return false;
	}

	@Override
	public void updateBonus() {
		switch (bonus.state()) {
		case INACTIVE -> {
		}
		case EDIBLE -> {
			if (player.tile().equals(bonus.tile())) {
				log("%s found bonus: %s", player.name, bonus);
				score(bonus.value());
				bonus.eat(sec_to_ticks(2));
				eventSupport.publish(GameEventType.BONUS_GETS_EATEN, bonus.tile());
			} else {
				boolean expired = bonus.tick();
				if (expired) {
					log("Bonus expired: %s", bonus);
					bonus.init();
					eventSupport.publish(GameEventType.BONUS_EXPIRES, bonus.tile());
				}
			}
		}
		case EATEN -> {
			boolean expired = bonus.tick();
			if (expired) {
				log("Bonus expired: %s", bonus);
				bonus.init();
				eventSupport.publish(GameEventType.BONUS_EXPIRES, bonus.tile());
			}
		}
		}
	}

	@Override
	public int bonusValue(int symbol) {
		return switch (symbol) {
		case CHERRIES -> 100;
		case STRAWBERRY -> 300;
		case PEACH -> 500;
		case APPLE -> 700;
		case GRAPES -> 1000;
		case GALAXIAN -> 2000;
		case BELL -> 3000;
		case KEY -> 5000;
		default -> throw new IllegalArgumentException("Unknown symbol ID: " + symbol);
		};
	}
}