/*
MIT License

Copyright (c) 2021 Armin Reichert

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
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.File;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.world.MapBasedWorld;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends GameModel {

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	public PacManGame() {
		levels = new Object[][] {
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

		// all levels use the same world
		world = new MapBasedWorld("/pacman/maps/map1.txt");
		
		player = new Pac("Pac-Man");
		ghosts = createGhosts("Blinky", "Pinky", "Inky", "Clyde");
		bonus = new Bonus();
		hiscorePath = new File(System.getProperty("user.home"), "highscore-pacman.xml");
	}

	@Override
	public void setLevel(int levelNumber) {
		this.levelNumber = levelNumber;
		mazeNumber = 1;
		mapNumber = 1;
		setLevelData(levelNumber, world);
		huntingPhaseTicks = DEFAULT_HUNTING_PHASE_TICKS[levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2];
		levelCounter.add(bonusSymbol);
		player.world = world;
		player.starvingTimeLimit = sec_to_ticks(levelNumber < 5 ? 4 : 3);
		resetGhostBounty();
		resetGhosts(world);
		bonus.world = world;
		bonus.init();
		bonus.placeAt(world.bonusTile(), HTS, 0);
		log("Pac-Man game entered level #%d", levelNumber);
	}

	@Override
	public int bonusValue(int symbolID) {
		switch (symbolID) {
		case CHERRIES:
			return 100;
		case STRAWBERRY:
			return 300;
		case PEACH:
			return 500;
		case APPLE:
			return 700;
		case GRAPES:
			return 1000;
		case GALAXIAN:
			return 2000;
		case BELL:
			return 3000;
		case KEY:
			return 5000;
		default:
			throw new IllegalArgumentException("Unknown symbol ID: " + symbolID);
		}
	}
}