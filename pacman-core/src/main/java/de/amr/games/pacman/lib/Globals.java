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

package de.amr.games.pacman.lib;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.IllegalGhostIDException;
import de.amr.games.pacman.model.IllegalLevelNumberException;

/**
 * @author Armin Reichert
 */
public class Globals {

	/** Tile size (8px). */
	public static final int TS = 8;

	/** Half tile size (4px). */
	public static final int HTS = 4;

	public static final Random RND = new Random();

	private static final String MSG_GAME_NULL = "Game model must not be null";
	private static final String MSG_LEVEL_NULL = "Game level must not be null";
	private static final String MSG_TILE_NULL = "Tile must not be null";
	private static final String MSG_DIR_NULL = "Direction must not be null";

	public static Vector2i v2i(int x, int y) {
		return new Vector2i(x, y);
	}

	public static Vector2f v2f(double x, double y) {
		return new Vector2f((float) x, (float) y);
	}

	public static void checkNotNull(Object value) {
		Objects.requireNonNull(value, "");
	}

	public static void checkNotNull(Object value, String message) {
		Objects.requireNonNull(value, message);
	}

	public static void checkGameNotNull(GameModel game) {
		checkNotNull(game, MSG_GAME_NULL);
	}

	public static void checkGhostID(byte id) {
		if (id < 0 || id > 3) {
			throw new IllegalGhostIDException(id);
		}
	}

	public static void checkGameVariant(GameVariant variant) {
		if (variant == null) {
			throw new IllegalGameVariantException(variant);
		}
		switch (variant) {
		case MS_PACMAN, PACMAN -> { // ok
		}
		default -> throw new IllegalGameVariantException(variant);
		}
	}

	public static void checkLevelNumber(int number) {
		if (number < 1) {
			throw new IllegalLevelNumberException(number);
		}
	}

	public static void checkTileNotNull(Vector2i tile) {
		checkNotNull(tile, MSG_TILE_NULL);
	}

	public static void checkLevelNotNull(GameLevel level) {
		checkNotNull(level, MSG_LEVEL_NULL);
	}

	public static void checkDirectionNotNull(Direction dir) {
		checkNotNull(dir, MSG_DIR_NULL);
	}

	public static double requirePositive(double value, String messageFormat) {
		if (value < 0) {
			throw new IllegalArgumentException(messageFormat.formatted(value));
		}
		return value;
	}

	public static double requirePositive(double value) {
		return requirePositive(value, "%f must be positive");
	}

	/**
	 * @param a left interval bound
	 * @param b right interval bound
	 * @return Random integer number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged to
	 *         guarantee <code>a<=b</code>
	 */
	public static int randomInt(int a, int b) {
		if (a > b) {
			var tmp = a;
			a = b;
			b = tmp;
		}
		return a + RND.nextInt(b - a);
	}

	/**
	 * @param a left interval bound
	 * @param b right interval bound
	 * @return Random floating-point number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged
	 *         to guarantee <code>a<=b</code>
	 */
	public static float randomFloat(float a, float b) {
		if (a > b) {
			var tmp = a;
			a = b;
			b = tmp;
		}
		return a + RND.nextFloat(b - a);
	}

	/**
	 * @param a left interval bound
	 * @param b right interval bound
	 * @return Random double-precision floating-point number from right-open interval <code>[a; b[</code>. Interval bounds
	 *         are rearranged to guarantee <code>a<=b</code>
	 */
	public static double randomDouble(double a, double b) {
		if (a > b) {
			var tmp = a;
			a = b;
			b = tmp;
		}
		return a + RND.nextDouble(b - a);
	}

	public static boolean inPercentOfCases(int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent value must be in range [0, 100] but is %d".formatted(percent));
		}
		if (percent == 0) {
			return false;
		}
		if (percent == 100) {
			return true;
		}
		return randomInt(0, 100) < percent;
	}

	public static boolean isEven(int n) {
		return n % 2 == 0;
	}

	public static boolean isOdd(int n) {
		return n % 2 != 0;
	}

	public static final float percent(int value) {
		return value / 100f;
	}

	/**
	 * @param value1 value1
	 * @param value2 value2
	 * @param t      "time" between 0 and 1
	 * @return linear interpolation between {@code value1} and {@code value2} values
	 */
	public static double lerp(double value1, double value2, double t) {
		return (1 - t) * value1 + t * value2;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	/**
	 * @param delta  maximum allowed deviation (non-negative number)
	 * @param value  value
	 * @param target target value
	 * @return {@code true} if the given values differ at most by the given difference
	 */
	public static boolean differsAtMost(double delta, double value, double target) {
		if (delta < 0) {
			throw new IllegalArgumentException("Difference must not be negative but is %f".formatted(delta));
		}
		return value >= (target - delta) && value <= (target + delta);
	}

	public static byte[][] copyByteArray2D(byte[][] array) {
		return Arrays.stream(array).map(byte[]::clone).toArray(byte[][]::new);
	}

	@SafeVarargs
	public static <T> boolean oneOf(T value, T... alternatives) {
		return switch (alternatives.length) {
		case 0 -> false;
		case 1 -> value.equals(alternatives[0]);
		default -> Stream.of(alternatives).anyMatch(value::equals);
		};
	}
}