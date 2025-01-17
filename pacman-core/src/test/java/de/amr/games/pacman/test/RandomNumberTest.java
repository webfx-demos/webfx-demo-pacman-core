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

package de.amr.games.pacman.test;

import static de.amr.games.pacman.lib.Globals.randomFloat;
import static de.amr.games.pacman.lib.Globals.randomInt;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Armin Reichert
 */
public class RandomNumberTest {

	static final int N = 1_000_000;

	@Test
	public void testRandomInt() {
		for (int i = 0; i < N; ++i) {
			var number = randomInt(10, 100);
			Assert.assertTrue(10 <= number && number < 100);
		}
	}

	@Test
	public void testRandomFloat() {
		for (int i = 0; i < N; ++i) {
			var number = randomFloat(10.0f, 100.0f);
			Assert.assertTrue(10.0f <= number && number < 100.0f);
		}
	}
}