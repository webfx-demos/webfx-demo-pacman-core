package de.amr.games.pacman.model;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.world.WorldMap;

/**
 * Game model of the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends PacManGameModel {

	/*@formatter:off*/
	public static final int[][] PACMAN_LEVELS = {
	/* 1*/ {0,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {1,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {2,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {2,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {3, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {3, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {4, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {4, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {5, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {5, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {6, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {6, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {7, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {7, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {7, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {7, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {7, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {7, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {7, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {7, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {7,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private final MapBasedPacManGameWorld world;

	public PacManGame() {
		world = new MapBasedPacManGameWorld();
		world.setMap(new WorldMap("/pacman/maps/map1.txt"));
		world.setUpwardsBlocked(new V2i(12, 13), new V2i(15, 13), new V2i(12, 25), new V2i(15, 25));

		bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "APPLE", "GRAPES", "GALAXIAN", "BELL", "KEY" };
		bonusValues = new int[] { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
		bonus = new Bonus();
		bonus.setPosition(13 * TS + HTS, 20 * TS);

		pac = new Pac("Pac-Man", RIGHT);

		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost(BLINKY, "Blinky", LEFT);
		ghosts[PINKY] = new Ghost(PINKY, "Pinky", UP);
		ghosts[INKY] = new Ghost(INKY, "Inky", DOWN);
		ghosts[CLYDE] = new Ghost(CLYDE, "Clyde", DOWN);

		bonus.world = world;
		pac.world = world;
		for (Ghost ghost : ghosts) {
			ghost.world = world;
		}
		highscoreFileName = "hiscore-pacman.xml";
	}

	@Override
	protected void buildLevel(int levelNumber) {
		level = new GameLevel(PACMAN_LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		level.setWorld(world);
		log("Pac-Man classic level %d created", levelNumber);
	}

	@Override
	public long bonusActivationTicks(int levelNumber) {
		return clock.sec(9 + random.nextFloat());
	}

	@Override
	public int mapNumber(int mazeNumber) {
		return 1;
	}

	@Override
	public int mazeNumber(int levelNumber) {
		return 1;
	}
}