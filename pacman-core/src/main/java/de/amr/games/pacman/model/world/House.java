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

package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

import java.util.List;
import java.util.Objects;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public class House {

	Vector2i topLeftTile;
	Vector2i size;
	Door door;
	List<Vector2f> seatPositions;
	Vector2f center;

	public House(Vector2i topLeftTile, Vector2i size, Door door, List<Vector2f> seatPositions, Vector2f center) {
		this.topLeftTile = topLeftTile;
		this.size = size;
		this.door = door;
		this.seatPositions = seatPositions;
		this.center = center;
		checkTileNotNull(topLeftTile);
		checkNotNull(size);
		checkNotNull(door);
		checkNotNull(seatPositions);
		checkNotNull(center);

		if (seatPositions.size() != 3) {
			throw new IllegalArgumentException("There must be exactly 3 seat positions");
		}
		if (seatPositions.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("House seat position must not be null");
		}
	}

	public Vector2i topLeftTile() {
		return topLeftTile;
	}

	public Vector2i size() {
		return size;
	}

	public Door door() {
		return door;
	}

	public List<Vector2f> seatPositions() {
		return seatPositions;
	}

	public Vector2f center() {
		return center;
	}

	public Vector2f seatPosition(int i) {
		return seatPositions.get(i);
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is part of this house
	 */
	public boolean contains(Vector2i tile) {
		Vector2i bottomRightTileOutside = topLeftTile.plus(size());
		return tile.x() >= topLeftTile.x() && tile.x() < bottomRightTileOutside.x() //
				&& tile.y() >= topLeftTile.y() && tile.y() < bottomRightTileOutside.y();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		House house = (House) o;

		if (!Objects.equals(topLeftTile, house.topLeftTile)) return false;
		if (!Objects.equals(size, house.size)) return false;
		if (!Objects.equals(door, house.door)) return false;
		if (!Objects.equals(seatPositions, house.seatPositions))
			return false;
		return Objects.equals(center, house.center);
	}

	@Override
	public int hashCode() {
		int result = topLeftTile != null ? topLeftTile.hashCode() : 0;
		result = 31 * result + (size != null ? size.hashCode() : 0);
		result = 31 * result + (door != null ? door.hashCode() : 0);
		result = 31 * result + (seatPositions != null ? seatPositions.hashCode() : 0);
		result = 31 * result + (center != null ? center.hashCode() : 0);
		return result;
	}
}