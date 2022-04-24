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
package de.amr.games.pacman.model.mspacman.world;

import java.util.List;

import de.amr.games.pacman.model.common.world.Portal;
import de.amr.games.pacman.model.common.world.SimpleWorld;

/**
 * @author Armin Reichert
 *
 */
public class MsPacManWorld4 extends SimpleWorld {

	private static final String[] MAP = {
			//@formatter:off
			"############################",
			"############################",
			"############################",
			"############################",
			"#..........................#",
			"#.##.####.########.####.##.#",
			"#*##.####.########.####.##*#",
			"#.##.####.##....##.####.##.#",
			"#.##......##.##.##......##.#",
			"#.####.##.##.##.##.##.####.#",
			"#.####.##.##.##.##.##.####.#",
			"#......##....##....##......#",
			"###.######## ## ########.###",
			"  #.######## ## ########.#  ",
			"  #....##          ##....#  ",
			"### ##.## ###LR### ##.## ###",
			"TTT ##.## #      # ##.## TTT",
			"######.   #      #   .######",
			"######.## #      # ##.######",
			"TTT ##.## ######## ##.## TTT",
			"### ##.##          ##.## ###",
			"  #....##### ## #####....#  ",
			"  #.##.##### ## #####.##.#  ",
			"  #.##....   ##   ....##.#  ",
			"  #.#####.## ## ##.#####.#  ",
			"###.#####.## ## ##.#####.###",
			"#.........##    ##.........#",
			"#.####.##.########.##.####.#",
			"#.####.##.########.##.####.#",
			"#.##...##..........##...##.#",
			"#*##.#######.##.#######.##*#",
			"#.##.#######.##.#######.##.#",
			"#............##............#",
			"############################",
			"############################",
			"############################",
			//@formatter:on
	};

	public MsPacManWorld4() {
		super(MAP);
		portals = List.of(//
				new Portal(v(-1, 16), v(28, 16)), //
				new Portal(v(-1, 19), v(28, 19)) //
		);
		pelletsToEatForBonus[0] = 70;
		pelletsToEatForBonus[1] = 170;
	}
}