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
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghosts
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacManIntro extends Fsm<PacManIntro.State, PacManIntro.Context> {

	public static class GhostInfo {
		public Ghost ghost;
		public String character;
		public boolean pictureVisible;
		public boolean nicknameVisible;
		public boolean characterVisible;

		public GhostInfo(byte id, String nickname, String character) {
			ghost = new Ghost(id, nickname);
			this.character = character;
		}
	}

	public static class Context {
		public GameController gameController;
		public float chaseSpeed = 1.1f;
		public int leftTileX = 4;
		public Pulse blinking = new Pulse(10, true);
		public Pac pacMan = new Pac("Pac-Man");
		public GhostInfo[] ghostInfo = new GhostInfo[4];
		public boolean creditVisible = false;
		public boolean titleVisible = false;
		public int ghostIndex;
		public long ghostKilledTime;

		public Context(GameController gameController) {
			this.gameController = gameController;
			ghostInfo[0] = new GhostInfo(GameModel.RED_GHOST, "BLINKY", "SHADOW");
			ghostInfo[1] = new GhostInfo(GameModel.PINK_GHOST, "PINKY", "SPEEDY");
			ghostInfo[2] = new GhostInfo(GameModel.CYAN_GHOST, "INKY", "BASHFUL");
			ghostInfo[3] = new GhostInfo(GameModel.ORANGE_GHOST, "CLYDE", "POKEY");
		}

		public Stream<Ghost> ghosts() {
			return Stream.of(ghostInfo).map(info -> info.ghost);
		}
	}

	public enum State implements FsmState<Context> {

		START {
			@Override
			public void onUpdate(Context ctx) {
				if (timer.tick() == 2) {
					ctx.creditVisible = true;
				} else if (timer.tick() == 3) {
					ctx.titleVisible = true;
				} else if (timer.atSecond(1)) {
					controller.changeState(State.PRESENTING_GHOSTS);
				}
			}
		},

		PRESENTING_GHOSTS {
			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(0)) {
					ctx.ghostInfo[ctx.ghostIndex].pictureVisible = true;
				} else if (timer.atSecond(1.0)) {
					ctx.ghostInfo[ctx.ghostIndex].characterVisible = true;
				} else if (timer.atSecond(1.5)) {
					ctx.ghostInfo[ctx.ghostIndex].nicknameVisible = true;
				} else if (timer.atSecond(2.0)) {
					if (++ctx.ghostIndex < 4) {
						timer.resetIndefinitely();
					}
				} else if (timer.atSecond(2.5)) {
					controller.changeState(State.SHOWING_POINTS);
				}
			}
		},

		SHOWING_POINTS {
			@Override
			public void onEnter(Context ctx) {
				ctx.blinking.stop();
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					controller.changeState(State.CHASING_PAC);
				}
			}
		},

		CHASING_PAC {
			@Override
			public void onEnter(Context ctx) {
				timer.restartIndefinitely();
				ctx.pacMan.setPosition(TS * (36), TS * (20));
				ctx.pacMan.setMoveDir(Direction.LEFT);
				ctx.pacMan.setPixelSpeed(ctx.chaseSpeed);
				ctx.pacMan.show();
				ctx.pacMan.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.ghosts().forEach(ghost -> {
					ghost.enterStateHuntingPac();
					ghost.setPosition(ctx.pacMan.position().plus(16 * (ghost.id() + 1), 0));
					ghost.setMoveAndWishDir(Direction.LEFT);
					ghost.setPixelSpeed(ctx.chaseSpeed);
					ghost.show();
					ghost.selectAndRunAnimation(GameModel.AK_GHOST_COLOR);
				});
			}

			@Override
			public void onUpdate(Context ctx) {
				// Pac-Man reaches the energizer
				if (ctx.pacMan.position().x() <= TS * (ctx.leftTileX)) {
					controller.changeState(State.CHASING_GHOSTS);
				}
				// ghosts already reverse direction before Pac-man eats the energizer and turns right!
				else if (ctx.pacMan.position().x() <= TS * (ctx.leftTileX) + 4) {
					ctx.ghosts().forEach(ghost -> {
						ghost.enterStateFrightened();
						ghost.selectAndRunAnimation(GameModel.AK_GHOST_BLUE);
						ghost.setMoveAndWishDir(Direction.RIGHT);
						ghost.setPixelSpeed(0.6f);
						ghost.moveAndAnimate();
					});
					ctx.pacMan.moveAndAnimate();
				}
				// keep moving
				else {
					// wait 1 sec before blinking
					if (timer.atSecond(1)) {
						ctx.blinking.start();
					}
					ctx.blinking.animate();
					ctx.pacMan.moveAndAnimate();
					ctx.ghosts().forEach(Ghost::moveAndAnimate);
				}
			}
		},

		CHASING_GHOSTS {

			@Override
			public void onEnter(Context ctx) {
				timer.restartIndefinitely();
				ctx.ghostKilledTime = timer.tick();
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPixelSpeed(ctx.chaseSpeed);
			}

			@Override
			public void onUpdate(Context ctx) {
				if (ctx.ghosts().allMatch(ghost -> ghost.is(GhostState.EATEN))) {
					ctx.pacMan.hide();
					controller.changeState(READY_TO_PLAY);
					return;
				}
				var nextVictim = ctx.ghosts()//
						.filter(ctx.pacMan::sameTile)//
						.filter(ghost -> ghost.is(GhostState.FRIGHTENED))//
						.findFirst();
				nextVictim.ifPresent(victim -> {
					victim.setKilledIndex(victim.id());
					ctx.ghostKilledTime = timer.tick();
					victim.enterStateEaten();
					ctx.pacMan.hide();
					ctx.pacMan.setPixelSpeed(0);
					ctx.ghosts().forEach(ghost -> {
						ghost.setPixelSpeed(0);
						ghost.animation(GameModel.AK_GHOST_BLUE).ifPresent(Animated::stop);
					});
				});

				// After ??? sec, Pac-Man and the surviving ghosts get visible again and move on
				if (timer.tick() - ctx.ghostKilledTime == timer.secToTicks(0.9)) {
					ctx.pacMan.show();
					ctx.pacMan.setPixelSpeed(ctx.chaseSpeed);
					ctx.ghosts().forEach(ghost -> {
						if (!ghost.is(GhostState.EATEN)) {
							ghost.show();
							ghost.setPixelSpeed(0.6f);
							ghost.animation(GameModel.AK_GHOST_BLUE).ifPresent(Animated::start);
						} else {
							ghost.hide();
						}
					});
				}
				ctx.pacMan.moveAndAnimate();
				ctx.ghosts().forEach(Ghost::moveAndAnimate);
				ctx.blinking.animate();
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(0.75)) {
					ctx.ghostInfo[3].ghost.hide();
					if (!ctx.gameController.game().hasCredit()) {
						ctx.gameController.changeState(GameState.READY);
						return;
					}
				}
				if (timer.atSecond(5)) {
					ctx.gameController.changeState(GameState.CREDIT);
				}
			}
		};

		PacManIntro controller;
		final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	private final Context introData;

	public PacManIntro(GameController gameController) {
		super(State.values());
		for (var state : states) {
			state.controller = this;
		}
		introData = new Context(gameController);
	}

	@Override
	public Context context() {
		return introData;
	}

}