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
package de.amr.games.pacman.model.world;

import java.util.List;

import de.amr.games.pacman.lib.V2i;

/**
 * Ghost house interface.
 * 
 * @author Armin Reichert
 */
public interface GhostHouse {

	V2i topLeftTile();

	default boolean contains(V2i tile) {
		V2i bottomRightTile = topLeftTile().plus(numTilesX(), numTilesY());
		return tile.x >= topLeftTile().x && tile.x <= bottomRightTile.x //
				&& tile.y >= topLeftTile().y && tile.y <= bottomRightTile.y;
	}

	int numTilesX();

	int numTilesY();

	V2i seat(int index);

	V2i entryTile();

	List<V2i> doorTiles();
}