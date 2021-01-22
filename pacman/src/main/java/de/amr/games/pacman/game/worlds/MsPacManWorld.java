package de.amr.games.pacman.game.worlds;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Logging.log;

import java.util.Random;

import de.amr.games.pacman.game.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;

/**
 * Ms. Pac-Man game world. Has 6 maze variants.
 * 
 * TODO: lots of details still missing
 * 
 * @author Armin Reichert
 */
public class MsPacManWorld extends AbstractPacManGameWorld {

	public static final byte CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, PRETZEL = 3, APPLE = 4, PEAR = 5, BANANA = 6;

	public static final short[] BONUS_POINTS = { 100, 200, 500, 700, 1000, 2000, 5000 };

	public static final String[] BONUS_NAMES = { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR",
			"BANANA" };

	// TODO how exactly are the levels of the Ms.Pac-Man game?
	/*@formatter:off*/
	public static final int[][] LEVELS = {
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PRETZEL,     90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {PEAR,       100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {BANANA,     100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {BANANA,      90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private static final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Sue" };

	private final Random rnd = new Random();

	@Override
	public PacManGameLevel createLevel(int levelNumber) {
		int row = levelNumber <= 21 ? levelNumber - 1 : 20;
		PacManGameLevel level = new PacManGameLevel(LEVELS[row]);
		if (levelNumber > 7) {
			level.bonusSymbol = (byte) rnd.nextInt(7);
		}
		if (levelNumber <= 2) {
			level.mazeNumber = 1; // pink maze, white dots
		} else if (levelNumber <= 5) {
			level.mazeNumber = 2; // light blue maze, yellow dots
		} else if (levelNumber <= 9) {
			level.mazeNumber = 3; // orange maze, red dots
		} else if (levelNumber <= 13) {
			level.mazeNumber = 4; // dark blue maze, white dots
		} else if ((levelNumber - 14) % 8 < 4) {
			// from level 14 on, maze switches between 5 and 6 every 4 levels
			level.mazeNumber = 5; // pink maze, cyan dots (same map as maze 3)
		} else {
			level.mazeNumber = 6; // orange maze, white dots (same map as maze 4)
		}
		// Maze #5 has the same map as #3 but a different color, same for #6 vs. #4
		int mapIndex = level.mazeNumber == 5 ? 3 : level.mazeNumber == 6 ? 4 : level.mazeNumber;
		loadMap("/worlds/mspacman/map" + mapIndex + ".txt");
		log("Use maze #%d at game level %d", level.mazeNumber, levelNumber);
		log("Use bonus %s at level %d", BONUS_NAMES[level.bonusSymbol], levelNumber);
		return level;
	}

	@Override
	public String pacName() {
		return "Ms. Pac-Man";
	}

	@Override
	public Direction pacStartDirection() {
		return LEFT;
	}

	@Override
	public String ghostName(int ghost) {
		return GHOST_NAMES[ghost];
	}

	@Override
	public boolean isUpwardsBlocked(int x, int y) {
		return false; // ghosts can travel all paths
	}
}