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
package de.amr.games.pacman.controller.common;

import static java.util.function.Predicate.not;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;

/**
 * Rule of thumb: here, specify the "what" and "when", not the "how" (which should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	BOOT() {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start();
			game.levelCounter.clear();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(INTRO);
			}
		}
	},

	INTRO() {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start();
			game.reset();
			game.setLevel(1);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}

		@Override
		public void selectGameVariant(GameVariant variant) {
			gc.selectGame(variant);
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.addCredit();
			if (added) {
				gc.sounds().play(GameSound.CREDIT);
			}
			gc.changeState(CREDIT);
		}

		@Override
		public void requestGame(GameModel game) {
			if (game.hasCredit()) {
				game.reset();
				game.setLevel(1);
				gc.changeState(READY);
			}
		}

		@Override
		public void startCutscenesTest(GameModel game) {
			gc.changeState(INTERMISSION_TEST);
		}
	},

	CREDIT {
		@Override
		public void onEnter(GameModel game) {
			game.scores.gameScore.showContent = false;
			game.scores.highScore.showContent = true;
		}

		@Override
		public void onUpdate(GameModel context) {
			// nothing to do here
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.addCredit();
			if (added) {
				gc.sounds().play(GameSound.CREDIT);
			}
		}

		@Override
		public void requestGame(GameModel game) {
			gc.changeState(READY);
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			gc.sounds().stopAll();
			if (game.hasCredit() && !game.playing) {
				// new game
				gc.sounds().play(GameSound.GAME_READY);
				game.reset();
				game.setLevel(1);
				game.levelCounter.clear();
				game.levelCounter.addSymbol(game.level.bonusSymbol);
				game.scores.gameScore.showContent = true;
				game.resetGuys();
				game.guys().forEach(Entity::hide);
			} else {
				// in game or attract mode
				game.resetGuys();
				game.scores.enable(game.hasCredit());
				game.scores.gameScore.showContent = game.hasCredit();
				game.guys().forEach(Entity::show);
				if (!game.hasCredit()) {
					gc.attractModeSteeringMsPacMan.init();
					gc.attractModeSteeringPacMan.init();
					for (var ghost : game.theGhosts) {
						ghost.setPseudoRandomMode(true);
					}
				} else {
					for (var ghost : game.theGhosts) {
						ghost.setPseudoRandomMode(false);
					}
				}
			}
			game.scores.highScore.showContent = true;
		}

		@Override
		public void onUpdate(GameModel game) {
			if (game.hasCredit() && !game.playing) {
				if (timer.atSecond(0.5)) {
					game.guys().forEach(Entity::show);
					game.livesOneLessShown = true;
				} else if (timer.atSecond(4.5)) {
					game.playing = true;
					game.startHuntingPhase(0);
					gc.changeState(GameState.HUNTING);
				}
			} else {
				// game continuing or attract mode
				if (timer.atSecond(1.5)) {
					game.startHuntingPhase(0);
					gc.changeState(GameState.HUNTING);
				}
			}
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.pac.selectAndRunAnimation(AnimKeys.PAC_MUNCHING);
			game.energizerPulse.restart();
			gc.sounds().ensureSirenStarted(game.huntingTimer.phase() / 2);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.happened.flashyThing(); // ich scholze jetzt

			game.whatHappenedWithFood();
			if (game.happened.allFoodEaten) {
				renderSound(game);
				gc.changeState(LEVEL_COMPLETE);
				return;
			}

			game.whatHappenedWithTheGuys();
			if (game.happened.pacMetKiller) {
				renderSound(game);
				gc.changeState(PACMAN_DYING);
				return;

			}

			// TODO remove when attract mode is implemented correctly
			if (!game.hasCredit()) {
				switch (game.variant) {
				case PACMAN -> {
					if (gc.attractModeSteeringPacMan.isComplete()) {
						gc.changeState(INTRO);
						return;
					}
				}
				case MS_PACMAN -> {
					if (gc.attractModeSteeringMsPacMan.isComplete()) {
						gc.changeState(INTRO);
						return;
					}
				}
				}
			}

			if (game.happened.ghostsKilled) {
				renderSound(game);
				gc.changeState(GHOST_DYING);
				return;
			}

			gc.getSteering().steer(game, game.pac);
			game.pac.update(game);
			game.updateGhosts();
			game.updateBonus();
			game.advanceHunting();
			game.energizerPulse.advance();
			game.powerTimer.advance();
			renderSound(game);
		}

		private void renderSound(GameModel game) {
			var snd = gc.sounds();
			if (game.huntingTimer.tick() == 0) {
				snd.ensureSirenStarted(game.huntingTimer.phase() / 2);
			}
			if (game.happened.pacGotPower) {
				snd.stopSirens();
				snd.ensureLoop(GameSound.PACMAN_POWER, GameSoundController.LOOP_FOREVER);
			}
			if (game.happened.pacPowerLost) {
				snd.stop(GameSound.PACMAN_POWER);
				snd.ensureSirenStarted(game.huntingTimer.phase() / 2);
			}
			if (game.happened.foodFound) {
				snd.ensureLoop(GameSound.PACMAN_MUNCH, GameSoundController.LOOP_FOREVER);
			}
			if (game.pac.starvingTime() >= 12) { // ???
				snd.stop(GameSound.PACMAN_MUNCH);
			}
			if (game.ghosts(GhostState.RETURNING_TO_HOUSE).count() > 0) {
				if (!snd.isPlaying(GameSound.GHOST_RETURNING)) {
					snd.loop(GameSound.GHOST_RETURNING, GameSoundController.LOOP_FOREVER);
				}
			} else {
				snd.stop(GameSound.GHOST_RETURNING);
			}
		}

		@Override
		public void addCredit(GameModel game) {
			if (!game.playing) {
				boolean added = game.addCredit();
				if (added) {
					gc.sounds().play(GameSound.CREDIT);
				}
				gc.changeState(CREDIT);
			}
		}

		@Override
		public void cheatEatAllPellets(GameModel game) {
			if (game.playing) {
				game.level.world.tiles().filter(not(game.level.world::isEnergizerTile)).forEach(game.level.world::removeFood);
				GameEvents.publish(GameEventType.PAC_FINDS_FOOD, null);
			}
		}

		@Override
		public void cheatKillAllEatableGhosts(GameModel game) {
			if (game.playing) {
				game.killAllPossibleGhosts();
				gc.changeState(GameState.GHOST_DYING);
			}
		}

		@Override
		public void cheatEnterNextLevel(GameModel game) {
			if (game.playing) {
				game.level.world.tiles().forEach(game.level.world::removeFood);
				gc.changeState(GameState.LEVEL_COMPLETE);
			}
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(4);
			timer.start();
			game.huntingTimer.stop();
			game.bonus().setInactive();
			game.pac.setAbsSpeed(0);
			game.pac.animationSet().ifPresent(EntityAnimationSet::reset);
			game.ghosts().forEach(Ghost::hide);
			game.energizerPulse.reset();
			gc.sounds().stopAll();
			// force UI update to ensure maze flashing animation is up to date
			GameEvents.publish(GameEventType.UI_FORCE_UPDATE, null);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (!game.hasCredit()) {
					gc.changeState(INTRO);
				} else if (game.intermissionNumber(game.level.number) != 0) {
					gc.changeState(INTERMISSION);
				} else {
					gc.changeState(LEVEL_STARTING);
				}
				return;
			}
			var world = (ArcadeWorld) game.world();
			if (timer.atSecond(1)) {
				world.flashingAnimation().ifPresent(mazeFlashing -> {
					mazeFlashing.setRepetions(game.level.numFlashes);
					mazeFlashing.restart();
				});
			}
			world.flashingAnimation().ifPresent(EntityAnimation::advance);
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(1);
			timer.start();
			game.setLevel(game.level.number + 1);
			game.levelCounter.addSymbol(game.level.bonusSymbol);
			game.resetGuys();
			game.guys().forEach(Entity::hide);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(1);
			timer.start();
			game.pac.hide();
			game.ghosts().forEach(ghost -> ghost.setFlashingStopped(true));
			gc.sounds().play(GameSound.GHOST_EATEN);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.fsm.resumePreviousState();
			} else {
				gc.getSteering().steer(game, game.pac);
				game.ghosts(GhostState.RETURNING_TO_HOUSE).forEach(ghost -> ghost.update(game));
				game.energizerPulse.advance();
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.pac.show();
			game.ghosts(GhostState.EATEN).forEach(ghost -> ghost.doReturningToHouse(game));
			game.ghosts().forEach(ghost -> ghost.setFlashingStopped(false));
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(4);
			timer.start();
			game.pac.animation().ifPresent(EntityAnimation::stop);
			game.bonus().setInactive();
			gc.sounds().stopAll();
		}

		@Override
		public void onUpdate(GameModel game) {
			game.energizerPulse.advance();
			game.pac.update(game);
			if (timer.atSecond(0.25)) {
				game.ghosts().forEach(Ghost::hide);
				game.pac.selectAndResetAnimation(AnimKeys.PAC_DYING);
			} else if (timer.atSecond(1.4)) {
				game.pac.animation().ifPresent(EntityAnimation::restart);
				gc.sounds().play(GameSound.PACMAN_DEATH);
			} else if (timer.atSecond(3.0)) {
				game.lives--;
				if (game.lives == 0) {
					game.energizerPulse.stop();
					game.livesOneLessShown = false;
				}
				game.pac.hide();
			} else if (timer.hasExpired()) {
				if (!game.hasCredit()) {
					gc.changeState(INTRO);
				} else {
					gc.changeState(game.lives == 0 ? GAME_OVER : READY);
				}
			}
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(1.5);
			timer.start();
			gc.sounds().stopAll();
			game.consumeCredit();
			game.scores.saveHiscore();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.playing = false;
				gc.changeState(game.hasCredit() ? CREDIT : INTRO);
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.reset();
			game.setLevel(1);
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start(); // UI triggers state timeout
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(!game.hasCredit() || !game.playing ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.resetIndefinitely();
					timer.start();
					// This is a hack to trigger the UI to update its current scene
					GameEvents.publish(new GameStateChangeEvent(game, this, this));
				} else {
					gc.changeState(INTRO);
				}
			}
		}
	};

	// common state attributes and methods:

	GameController gc;
	TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}

	public void selectGameVariant(GameVariant variant) {
		// override if supported for state
	}

	public void requestGame(GameModel game) {
		// override if supported for state
	}

	public void addCredit(GameModel game) {
		// override if supported for state
	}

	public void startCutscenesTest(GameModel game) {
		// override if supported for state
	}

	public void cheatEatAllPellets(GameModel game) {
		// override if supported for state
	}

	public void cheatKillAllEatableGhosts(GameModel game) {
		// override if supported for state
	}

	public void cheatEnterNextLevel(GameModel game) {
		// override if supported for state
	}
}