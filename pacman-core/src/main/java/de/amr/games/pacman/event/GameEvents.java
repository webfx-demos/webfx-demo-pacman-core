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
package de.amr.games.pacman.event;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public class GameEvents {

	private static GameController gameController;
	private static Collection<GameEventListener> subscribers = new ArrayList<>();// new ConcurrentLinkedQueue<>();
	private static boolean soundEventsEnabled = true;

	private GameEvents() {
	}

	public static void setGameController(GameController gameController) {
		checkNotNull(gameController);
		GameEvents.gameController = gameController;
	}

	public static void setSoundEventsEnabled(boolean enabled) {
		GameEvents.soundEventsEnabled = enabled;
		Logger.info("Sound events {}", enabled ? "enabled" : "disabled");
	}

	public static void addListener(GameEventListener subscriber) {
		checkNotNull(subscriber);
		GameEvents.subscribers.add(subscriber);
	}

	public static void removeListener(GameEventListener subscriber) {
		checkNotNull(subscriber);
		GameEvents.subscribers.remove(subscriber);
	}

	public static void publishGameEvent(GameEvent event) {
		checkNotNull(event);
		Logger.trace("Publish game event: {}", event);
		GameEvents.subscribers.forEach(subscriber -> subscriber.onGameEvent(event));
	}

	public static void publishGameEvent(GameEventType type, Vector2i tile) {
		checkNotNull(type);
		checkNotNull(tile);
		publishGameEvent(new GameEvent(gameController.game(), type, tile));
	}

	public static void publishGameEventOfType(GameEventType type) {
		publishGameEvent(new GameEvent(gameController.game(), type, null));
	}

	public static void publishSoundEvent(String soundCommand) {
		checkNotNull(soundCommand);
		if (GameEvents.soundEventsEnabled) {
			publishGameEvent(new SoundEvent(gameController.game(), soundCommand));
		}
	}
}