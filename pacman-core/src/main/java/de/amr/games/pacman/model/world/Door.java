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

package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2f;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

import java.util.Objects;

/**
 * @author Armin Reichert
 */
public class Door {

	Vector2i leftWing;
	Vector2i rightWing;

	public Door(Vector2i leftWing, Vector2i rightWing) {
		this.leftWing = leftWing;
		this.rightWing = rightWing;
		checkNotNull(leftWing);
		checkNotNull(rightWing);
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is occupied by this door
	 */
	public boolean occupies(Vector2i tile) {
		return leftWing.equals(tile) || rightWing.equals(tile);
	}

	/**
	 * @return position where ghost can enter the door
	 */
	public Vector2f entryPosition() {
		return v2f(TS * rightWing.x() - HTS, TS * (rightWing.y() - 1));
	}

	public Vector2i leftWing() {
		return leftWing;
	}

	public Vector2i rightWing() {
		return rightWing;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Door door = (Door) o;

		if (!Objects.equals(leftWing, door.leftWing)) return false;
		return Objects.equals(rightWing, door.rightWing);
	}

	@Override
	public int hashCode() {
		int result = leftWing != null ? leftWing.hashCode() : 0;
		result = 31 * result + (rightWing != null ? rightWing.hashCode() : 0);
		return result;
	}
}