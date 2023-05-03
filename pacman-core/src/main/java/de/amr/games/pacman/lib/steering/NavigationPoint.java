/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.lib.steering;

import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public class NavigationPoint {

	private int x;
	private int y;
	private Direction dir;

	public NavigationPoint(int x, int y, Direction dir) {
		this.x = x;
		this.y = y;
		this.dir = dir;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public Direction dir() {
		return dir;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NavigationPoint that = (NavigationPoint) o;

		if (x != that.x) return false;
		if (y != that.y) return false;
		return dir == that.dir;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		result = 31 * result + (dir != null ? dir.hashCode() : 0);
		return result;
	}

	public static NavigationPoint np(Vector2i tile, Direction dir) {
		return new NavigationPoint(tile.x(), tile.y(), dir);
	}

	public static NavigationPoint np(Vector2i tile) {
		return new NavigationPoint(tile.x(), tile.y(), null);
	}

	public static NavigationPoint np(int x, int y, Direction dir) {
		return new NavigationPoint(x, y, dir);
	}

	public static NavigationPoint np(int x, int y) {
		return new NavigationPoint(x, y, null);
	}

	public Vector2i tile() {
		return new Vector2i(x, y);
	}
}
