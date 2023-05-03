package de.amr.games.pacman.test;

//import static de.amr.games.pacman.lib.Globals.copyByteArray2D;
import static de.amr.games.pacman.lib.Globals.v2f;
import static de.amr.games.pacman.lib.Globals.v2i;
import static java.util.function.Predicate.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.World;

/**
 * @author Armin Reichert
 */
public class WorldTest {

	@Test(expected = NullPointerException.class)
	public void testNullTileArg() {
		var world = new World(GameModel.PACMAN_MAP);
		world.index(null);
		world.insideBounds(null);
		world.belongsToPortal(null);
		world.isIntersection(null);
		world.isWall(null);
		world.isTunnel(null);
		world.isFoodTile(null);
		world.isEnergizerTile(null);
		world.removeFood(null);
		world.containsFood(null);
		world.containsEatenFood(null);
	}

	@Test
	public void testTileCoordinates() {
		Vector2f pos = v2f(0, 0);
		assertEquals(Vector2i.ZERO, World.tileAt(pos));
		pos = v2f(7.9f, 7.9f);
		assertEquals(v2i(0, 0), World.tileAt(pos));
		pos = v2f(8.0f, 7.9f);
		assertEquals(v2i(1, 0), World.tileAt(pos));
		pos = v2f(8.0f, 0.0f);
		assertEquals(v2i(1, 0), World.tileAt(pos));
		pos = v2f(0.0f, 8.0f);
		assertEquals(v2i(0, 1), World.tileAt(pos));

		var guy = new Ghost(GameModel.RED_GHOST, "Guy");

		guy.setPosition(3.99f, 0);
		assertEquals(v2i(0, 0), guy.tile());
		assertEquals(v2f(3.99f, 0.0f), guy.offset());

		guy.setPosition(4.0f, 0);
		assertEquals(v2i(1, 0), guy.tile());
		assertEquals(v2f(-4.0f, 0.0f), guy.offset());
	}

	@Test
	public void testPacManWorld() {
		var world = new World(GameModel.PACMAN_MAP);
		assertEquals(World.TILES_Y, world.numRows());
		assertEquals(World.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(244, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
	}

	@Test
	public void testMsPacManWorld1() {
		var world = new World(GameModel.MS_PACMAN_MAPS[0]);
		assertEquals(World.TILES_Y, world.numRows());
		assertEquals(World.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(220 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(220, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld2() {
		var world = new World(GameModel.MS_PACMAN_MAPS[1]);
		assertEquals(World.TILES_Y, world.numRows());
		assertEquals(World.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(240 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld3() {
		var world = new World(GameModel.MS_PACMAN_MAPS[2]);
		assertEquals(World.TILES_Y, world.numRows());
		assertEquals(World.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(238 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(238, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
	}

	@Test
	public void testMsPacManWorld4() {
		var world = new World(GameModel.MS_PACMAN_MAPS[3]);
		assertEquals(World.TILES_Y, world.numRows());
		assertEquals(World.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(234 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(234, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	/*@Test
	public void testCopyMapData() {
		byte[][] map = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
		byte[][] copy = copyByteArray2D(map);
		assertEquals(map.length, copy.length);
		for (int i = 0; i < map.length; ++i) {
			assertEquals(map[i].length, copy[i].length);
		}
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				assertEquals(map[i][j], copy[i][j]);
			}
		}
		assertEquals(4, map[1][1]);
		assertEquals(4, copy[1][1]);
		copy[1][1] = (byte) 42;
		assertNotEquals(map[1][1], copy[1][1]);
	}*/
}