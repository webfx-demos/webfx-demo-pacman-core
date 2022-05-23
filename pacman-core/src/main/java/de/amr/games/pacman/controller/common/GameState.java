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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;

import de.amr.games.pacman.controller.common.event.GameEvent;
import de.amr.games.pacman.controller.common.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.common.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.controller.common.event.GameEvent.Info;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;

/**
 * The states of the game. Each state has a timer and a reference to its FSM.
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	INTRO {
		@Override
		public void onEnter(GameModel game) {
			timer.setIndefinite().start();
			game.reset();
			game.requested = false;
			game.running = false;
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.attractMode = true;
				fsm.changeState(READY);
			}
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			timer.setSeconds(game.running || game.attractMode ? 2 : 5).start();
			game.resetGuys();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.ticked() == sec_to_ticks(1.5)) {
				game.showGhosts();
				game.player.show();
			} else if (timer.hasExpired()) {
				if (game.requested) {
					game.running = true;
				}
				// TODO reset hunting timer to INDEFINITE?
				fsm.changeState(GameState.HUNTING);
				return;
			}
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			if (!timer.isStopped()) {
				startHuntingPhase(game, 0);
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				++game.huntingPhase;
				startHuntingPhase(game, game.huntingPhase);
			}
			if (game.world.foodRemaining() == 0) {
				restartHuntingTimer(game, 0); // TODO is this correct?
				fsm.changeState(LEVEL_COMPLETE);
				return;
			}
			if (game.checkKillPlayer(fsm.playerImmune)) {
				restartHuntingTimer(game, 0); // TODO is this correct?
				fsm.changeState(PACMAN_DYING);
				return;
			}
			if (game.checkKillGhosts()) {
				fsm.changeState(GHOST_DYING);
				return;
			}
			updatePlayer(game);
			game.updateGhosts(fsm.gameVariant());
			game.updateBonus();
		}

		private void updatePlayer(GameModel game) {
			fsm.currentPlayerControl().steer(game.player);
			boolean lostPower = game.updatePlayer();
			if (lostPower) {
				timer.start();
				log("%s timer restarted: %s", this, timer);
			}
			checkFood(game);
		}

		private void checkFood(GameModel game) {
			boolean energizerEaten = game.checkFood(game.player.tile());
			if (energizerEaten && game.player.powerTimer.isRunning()) {
				timer.stop();
				log("%s timer stopped: %s", this, timer);
			}
		}

		private void restartHuntingTimer(GameModel game, int phase) {
			long phaseDuration = game.huntingPhaseDurations[phase];
			timer.set(phaseDuration).start();
			log("%s timer set to %d ticks", this, phaseDuration);
		}

		private void startHuntingPhase(GameModel game, int phase) {
			game.huntingPhase = phase;
			restartHuntingTimer(game, phase);
			if (phase > 0) {
				game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(Ghost::forceTurningBack);
			}
			String phaseName = game.inScatteringPhase() ? "Scattering" : "Chasing";
			log("Hunting phase #%d (%s) started, %d of %d ticks remaining", phase, phaseName, timer.ticksRemaining(),
					timer.duration());
			if (game.inScatteringPhase()) {
				game.publishEvent(new ScatterPhaseStartedEvent(game, phase / 2));
			}
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			game.bonus.init();
			game.player.setSpeed(0);
			game.hideGhosts();
			timer.setIndefinite().start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.attractMode) {
					fsm.changeState(INTRO);
				} else if (game.intermissionNumber(game.levelNumber) != 0) {
					fsm.changeState(INTERMISSION);
				} else {
					fsm.changeState(LEVEL_STARTING);
				}
			}
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			game.setLevel(game.levelNumber + 1);
			game.resetGuys();
			timer.setIndefinite().start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(READY);
			}
		}

	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			game.player.setSpeed(0);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			game.bonus.init();
			timer.setIndefinite().start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.player.lives--;
				fsm.changeState(game.attractMode ? INTRO : game.player.lives > 0 ? READY : GAME_OVER);
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			game.player.hide();
			timer.setSeconds(1).start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.resumePreviousState();
				return;
			}
			fsm.currentPlayerControl().steer(game.player);
			game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
					.forEach(ghost -> game.updateGhost(ghost, fsm.gameVariant()));
		}

		@Override
		public void onExit(GameModel game) {
			game.player.show();
			// fire event(s) for dead ghosts not yet returning home (bounty != 0)
			game.ghosts(DEAD).filter(ghost -> ghost.bounty != 0).forEach(ghost -> {
				ghost.bounty = 0;
				game.publishEvent(new GameEvent(game, Info.GHOST_RETURNS_HOME, ghost, null));
			});
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			game.running = false;
			game.ghosts().forEach(ghost -> ghost.setSpeed(0));
			game.player.setSpeed(0);
			new Hiscore(game).save();
			timer.setSeconds(5).start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(INTRO);
			}
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.setIndefinite().start(); // UI triggers state timeout
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(game.attractMode || !game.running ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.setIndefinite().start();
			log("Test intermission scene #%d", game.intermissionTestNumber);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.setIndefinite().start();
					log("Test intermission scene #%d", game.intermissionTestNumber);
					// This is a hack to trigger the UI to update its current scene
					game.publishEvent(new GameStateChangeEvent(game, this, this));
				} else {
					fsm.changeState(INTRO);
				}
			}
		}
	};

	// -----------------------------------------------------------

	protected GameController fsm;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}