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
package de.amr.games.pacman.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.actors.Ghost;

/**
 * @author Armin Reichert
 */
public class Memory {
	public Optional<Vector2i> foodFoundTile;
	public boolean energizerFound;
	public int bonusReachedIndex; // 0=first, 1=second, -1=no bonus
	public boolean pacKilled;
	public boolean pacPowerActive;
	public boolean pacPowerStarts;
	public boolean pacPowerLost;
	public boolean pacPowerFading;
	public List<Ghost> pacPrey;
	public final List<Ghost> killedGhosts = new ArrayList<>(4);

	public Memory() {
		forgetEverything();
	}

	public void forgetEverything() {
		foodFoundTile = Optional.empty();
		energizerFound = false;
		bonusReachedIndex = -1;
		pacKilled = false;
		pacPowerActive = false;
		pacPowerStarts = false;
		pacPowerLost = false;
		pacPowerFading = false;
		pacPrey = Collections.emptyList();
		killedGhosts.clear();
	}

	/*@Override
	public String toString() {

		var foodText = "";
		if (foodFoundTile.isPresent()) {
			foodText = "%s at %s".formatted(energizerFound ? "Energizer" : "Pellet", foodFoundTile.get());
		}

		var bonusText = "";
		if (bonusReachedIndex != -1) {
			bonusText = "Bonus %d reached".formatted(bonusReachedIndex);
		}

		var powerText = "";
		if (pacPowerStarts) {
			powerText += " starts";
		}
		if (pacPowerActive) {
			powerText += " active";
		}
		if (pacPowerFading) {
			powerText += " fading";
		}
		if (pacPowerLost) {
			powerText += " lost";
		}
		if (!powerText.isEmpty()) {
			powerText = "Pac power: " + powerText;
		}

		var pacKilledText = pacKilled ? "Pac killed" : "";

		var preyText = "";
		if (!pacPrey.isEmpty()) {
			preyText = "Prey: %s".formatted(pacPrey);
		}

		var killedGhostsText = killedGhosts.isEmpty() ? "" : killedGhosts.toString();

		return "%s%s%s%s%s%s".formatted(foodText, bonusText, powerText, pacKilledText, preyText, killedGhostsText);
	}*/

	public boolean edibleGhostsExist() {
		return !pacPrey.isEmpty();
	}
}