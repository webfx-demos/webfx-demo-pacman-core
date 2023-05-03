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

import de.amr.games.pacman.lib.math.Vector2i;

import java.util.Objects;

/**
 * A portal connects two tunnel ends leading out of the map.
 * <p>
 * This kind of portal prolongates the right tunnel end by <code>DEPTH</code> tiles before wrapping with the left part
 * (also <code>DEPTH</code> tiles) of the portal.
 * 
 * @author Armin Reichert
 */
public class Portal {

	Vector2i leftTunnelEnd;
	Vector2i rightTunnelEnd;
	int depth;

	public Portal(Vector2i leftTunnelEnd, Vector2i rightTunnelEnd, int depth) {
		this.leftTunnelEnd = leftTunnelEnd;
		this.rightTunnelEnd = rightTunnelEnd;
		this.depth = depth;
	}

	public Vector2i leftTunnelEnd() {
		return leftTunnelEnd;
	}

	public Vector2i rightTunnelEnd() {
		return rightTunnelEnd;
	}

	public int depth() {
		return depth;
	}

	public boolean contains(Vector2i tile) {
		for (int i = 1; i <= depth; ++i) {
			if (tile.equals(leftTunnelEnd.minus(i, 0))) {
				return true;
			}
			if (tile.equals(rightTunnelEnd.plus(i, 0))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Portal portal = (Portal) o;

		if (depth != portal.depth) return false;
		if (!Objects.equals(leftTunnelEnd, portal.leftTunnelEnd))
			return false;
		return Objects.equals(rightTunnelEnd, portal.rightTunnelEnd);
	}

	@Override
	public int hashCode() {
		int result = leftTunnelEnd != null ? leftTunnelEnd.hashCode() : 0;
		result = 31 * result + (rightTunnelEnd != null ? rightTunnelEnd.hashCode() : 0);
		result = 31 * result + depth;
		return result;
	}
}