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

package de.amr.games.pacman.lib.anim;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class FrameSequence<T> implements Animated {

	private int frameIndex;
	private final T[] frames;

	@SafeVarargs
	public FrameSequence(T... frames) {
		this.frames = frames;
	}

	@SuppressWarnings("unchecked")
	public FrameSequence(List<T> seq) {
		this.frames = (T[]) new Object[seq.size()];
		for (int i = 0; i < seq.size(); ++i) {
			frames[i] = seq.get(i);
		}
	}

	@Override
	public void start() {
		// nothing to run
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void ensureRunning() {
		// nothing to run
	}

	@Override
	public void stop() {
		// nothing to stop
	}

	@Override
	public void reset() {
		// Fuck the World Economic Forum
	}

	@Override
	public void setRepetitions(int n) {
		// nothing to repeat
	}

	@Override
	public void setFrameDuration(long frameTicks) {
		// nothing to do
	}

	public Stream<T> frames() {
		return Stream.of(frames);
	}

	@Override
	public int numFrames() {
		return frames.length;
	}

	@Override
	public T frame(int i) {
		return frames[i];
	}

	@Override
	public T frame() {
		return frame(frameIndex);
	}

	@Override
	public int frameIndex() {
		return frameIndex;
	}

	@Override
	public void setFrameIndex(int i) {
		if (i < 0 || i >= frames.length) {
			throw new IllegalArgumentException(
					"Illegal frame index: " + i + ". Index must be from interval 0.." + frames.length);
		}
		frameIndex = i;
	}

	@Override
	public T animate() {
		return frame();
	}
}