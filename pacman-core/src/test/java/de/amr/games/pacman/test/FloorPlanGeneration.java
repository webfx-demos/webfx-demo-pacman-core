package de.amr.games.pacman.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.tinylog.Logger;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.World;

/**
 * Test for floor plan generation.
 * 
 * @author Armin Reichert
 */
public class FloorPlanGeneration {

	public static void main(String[] args) {
		test();
	}

	private static final File DIR = new File(System.getProperty("user.dir"), "tmp");
	private static final String PACMAN_PATTERN = "fp-pacman-map%d-res-%d.txt";
	private static final String MS_PACMAN_PATTERN = "fp-mspacman-map%d-res-%d.txt";

	public static void test() {
		if (!DIR.exists()) {
			DIR.mkdir();
		}
		List.of(8, 4, 2, 1).forEach(res -> {
			createFloorPlan(new World(GameModel.PACMAN_MAP), file(PACMAN_PATTERN, 1, res), res);
			createFloorPlan(new World(GameModel.MS_PACMAN_MAPS[0]), file(MS_PACMAN_PATTERN, 1, res), res);
			createFloorPlan(new World(GameModel.MS_PACMAN_MAPS[1]), file(MS_PACMAN_PATTERN, 2, res), res);
			createFloorPlan(new World(GameModel.MS_PACMAN_MAPS[2]), file(MS_PACMAN_PATTERN, 3, res), res);
			createFloorPlan(new World(GameModel.MS_PACMAN_MAPS[3]), file(MS_PACMAN_PATTERN, 4, res), res);
		});
		List.of(8, 4, 2, 1).forEach(res -> {
			assertTrue(file(PACMAN_PATTERN, 1, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 1, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 2, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 3, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 4, res).exists());
		});
	}

	private static File file(String pattern, int mapNumber, int resolution) {
		return new File(DIR, String.format(pattern, mapNumber, resolution));
	}

	private static void createFloorPlan(World world, File file, int resolution) {
		long time = System.nanoTime();
		var floorPlan = new FloorPlan(world, resolution);
		time = System.nanoTime() - time;
		var timeLog = "%.2f millis".formatted(time / 1e6);
		try (var w = new FileWriter(file, StandardCharsets.UTF_8)) {
			floorPlan.print(w, true);
			Logger.info("Created file {} ({})", file.getAbsolutePath(), timeLog);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}