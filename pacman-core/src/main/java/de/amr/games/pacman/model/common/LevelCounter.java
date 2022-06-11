/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.common.world.World.t;

import java.util.ArrayList;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.actors.Entity;

/**
 * @author Armin Reichert
 */
public class LevelCounter extends Entity {

	private final int maxSize;
	public final ArrayList<Integer> symbols;

	public LevelCounter(int maxSize) {
		this.maxSize = maxSize;
		symbols = new ArrayList<>(maxSize);
		// position x = right edge!
		position = new V2d(t(24), t(34));
	}

	public void clear() {
		symbols.clear();
	}

	public void addSymbol(int symbol) {
		if (symbols.size() == maxSize) {
			symbols.remove(0);
		}
		symbols.add(symbol);
	}

	public int symbol(int i) {
		return symbols.get(i);
	}

	public int size() {
		return symbols.size();
	}

	public Stream<Integer> symbols() {
		return symbols.stream();
	}
}