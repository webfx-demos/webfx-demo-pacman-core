/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.lib.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Armin Reichert
 *
 * @param <K>     key type of map e.g. the direction of an actor
 * @param <THING> thing type (Image, Rectangle, ...)
 */
public class SpriteAnimationMap<K, THING> implements SpriteAnimation<THING> {

	private Map<K, SingleSpriteAnimation<THING>> animationMap;

	public SpriteAnimationMap(Map<K, SingleSpriteAnimation<THING>> animationMap) {
		this.animationMap = animationMap;
	}

	public SpriteAnimationMap(int capacity) {
		animationMap = new HashMap<>(capacity);
	}

	public void put(K key, SingleSpriteAnimation<THING> animation) {
		animationMap.put(key, animation);
	}

	public SingleSpriteAnimation<THING> get(K key) {
		return animationMap.get(key);
	}

	public Collection<SingleSpriteAnimation<THING>> all() {
		return animationMap.values();
	}

	@Override
	public void reset() {
		all().forEach(SingleSpriteAnimation::reset);
	}

	@Override
	public void restart() {
		all().forEach(SingleSpriteAnimation::restart);
	}

	@Override
	public void stop() {
		all().forEach(SingleSpriteAnimation::stop);
	}

	@Override
	public void run() {
		all().forEach(SingleSpriteAnimation::run);
	}

	@Override
	public void ensureRunning() {
		all().forEach(SingleSpriteAnimation::ensureRunning);
	}
}