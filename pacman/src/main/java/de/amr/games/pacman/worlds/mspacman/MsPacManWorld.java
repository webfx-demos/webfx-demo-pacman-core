package de.amr.games.pacman.worlds.mspacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;

import java.util.stream.Stream;

import de.amr.games.pacman.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.worlds.AbstractPacManGameWorld;

public class MsPacManWorld extends AbstractPacManGameWorld {

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	public static final byte CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, BREZN = 3, APPLE = 4, PEAR = 5, BANANA = 6;

	/*@formatter:off*/
	// TODO make levels confom to Ms.Pac-Man game
	public static final PacManGameLevel[] LEVELS = {
	/* 1*/ new PacManGameLevel(CHERRIES,   100,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
	/* 2*/ new PacManGameLevel(STRAWBERRY, 200,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
	/* 3*/ new PacManGameLevel(PEACH,      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
	/* 4*/ new PacManGameLevel(BREZN,      700,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
	/* 5*/ new PacManGameLevel(APPLE,     1000, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
	/* 6*/ new PacManGameLevel(PEAR,      2000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
	/* 7*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
//	/* 8*/ new PacManGameLevel(APPLE,     1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
//	/* 9*/ new PacManGameLevel(PEAR,      2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
//	/*10*/ new PacManGameLevel(PEAR,      2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
//	/*11*/ new PacManGameLevel(BANANA,    3000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
//	/*12*/ new PacManGameLevel(BANANA,    3000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
//	/*13*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
//	/*14*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
//	/*15*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
//	/*16*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
//	/*17*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
//	/*18*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
//	/*19*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
//	/*20*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
//	/*21*/ new PacManGameLevel(BANANA,    5000,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0)
	};
	/*@formatter:on*/

	private static final V2i houseEntry = new V2i(13, 14);
	private static final V2i houseCenter = new V2i(13, 17);
	private static final V2i houseLeft = new V2i(11, 17);
	private static final V2i houseRight = new V2i(15, 17);
	private static final V2i bonusTile = new V2i(13, 20);
	private static final V2i pacHome = new V2i(13, 26);

	private static final String[] ghostNames = { "Blinky", "Pinky", "Inky", "Sue" };
	private static final V2i[] ghostHomeTiles = { houseEntry, houseCenter, houseLeft, houseRight };
	private static final V2i[] ghostScatterTiles = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };
	private static final Direction[] ghostStartDirections = { LEFT, UP, DOWN, DOWN };

	private int mapIndex; // 1-6

	public MsPacManWorld() {
		setMapIndex(1);
	}

	public void setMapIndex(int mapIndex) {
		this.mapIndex = mapIndex;
		if (mapIndex < 1 || mapIndex > 6) {
			throw new IllegalArgumentException("Illegal Ms. Pac-Man map index: " + mapIndex);
		}
		// Maps 5 and 6 only differ by color
		int fileIndex = mapIndex == 5 ? 3 : mapIndex == 6 ? 4 : mapIndex;
		map = loadMap("/worlds/mspacman/map" + fileIndex + ".txt");
		findPortals();
		findFoodTiles();
	}

	public int getMapIndex() {
		return mapIndex;
	}

	@Override
	public PacManGameLevel level(int levelNumber) {
		return LEVELS[levelNumber <= 7 ? levelNumber - 1 : 6]; // TODO fixme
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
	public V2i pacHome() {
		return pacHome;
	}

	@Override
	public String ghostName(int ghost) {
		return ghostNames[ghost];
	}

	@Override
	public Direction ghostStartDirection(int ghost) {
		return ghostStartDirections[ghost];
	}

	@Override
	public V2i ghostHome(int ghost) {
		return ghostHomeTiles[ghost];
	}

	@Override
	public V2i ghostScatterTile(int ghost) {
		return ghostScatterTiles[ghost];
	}

	@Override
	public V2i houseEntry() {
		return houseEntry;
	}

	@Override
	public V2i houseCenter() {
		return houseCenter;
	}

	@Override
	public V2i houseLeft() {
		return houseLeft;
	}

	@Override
	public V2i houseRight() {
		return houseRight;
	}

	@Override
	public V2i bonusTile() {
		return bonusTile;
	}

	private boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	@Override
	public boolean isTunnel(int x, int y) {
		return false;
	}

	@Override
	public boolean isUpwardsBlocked(int x, int y) {
//		return isTile(x, y, 12, 13) || isTile(x, y, 15, 13) || isTile(x, y, 12, 25) || isTile(x, y, 15, 25);
		return false; // TODO are there such tiles?
	}

	@Override
	public boolean isIntersection(int x, int y) {
		if (isInsideGhostHouse(x, y) || isGhostHouseDoor(x, y + 1)) {
			return false;
		}
		return Stream.of(Direction.values()).filter(dir -> isAccessible(x + dir.vec.x, y + dir.vec.y)).count() >= 3;
	}
}