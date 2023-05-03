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
import static de.amr.games.pacman.lib.Globals.isOdd;
import static de.amr.games.pacman.lib.Globals.v2i;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.UP;

import java.util.BitSet;
import java.util.List;

import dev.webfx.platform.util.collection.Collections;
import org.tinylog.Logger;

import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntro extends Fsm<MsPacManIntro.State, MsPacManIntro.Context> {

	public static class Context {
		//@formatter:off
		public GameController gameController;
		public float          speed                = 1.1f;
		public int            stopY                = TS * 11 + 1;
		public int            stopX                = TS * 6 - 4; 
		public int            stopMsPacX           = TS * 15 + 2;
		public int            ticksUntilLifting    = 0;
		public Vector2i       titlePosition        = v2i(TS * 10, TS * 8);
		public TickTimer      marqueeTimer         = new TickTimer("marquee-timer");
		public int            numBulbs             = 96;
		public int            bulbOnDistance       = 16;
		public Pac            msPacMan             = new Pac("Ms. Pac-Man");
		public List<Ghost>    ghosts                ;{
    Ghost[] elements = new Ghost[]{new Ghost(GameModel.RED_GHOST,    "Blinky"),new Ghost(GameModel.PINK_GHOST,   "Pinky"),new Ghost(GameModel.CYAN_GHOST,   "Inky"),new Ghost(GameModel.ORANGE_GHOST, "Sue")};
        ghosts=Collections.listOf(elements);}
		public int ghostIndex = 0;
		//@formatter:on

		public Context(GameController gameController) {
			this.gameController = gameController;
		}

		/**
		 * In the Arcade game, 6 of the 96 bulbs are switched-on every frame, shifting every tick. The bulbs in the leftmost
		 * column however are switched-off every second frame. Maybe a bug?
		 * 
		 * @return bitset indicating which marquee bulbs are on
		 */
		public BitSet marqueeState() {
			var state = new BitSet(numBulbs);
			long t = marqueeTimer.tick();
			for (int b = 0; b < 6; ++b) {
				state.set((int) (b * bulbOnDistance + t) % numBulbs);
			}
			for (int i = 81; i < numBulbs; ++i) {
				if (isOdd(i)) {
					state.clear(i);
				}
			}
			return state;
		}
	}

	public enum State implements FsmState<MsPacManIntro.Context> {

		START {
			@Override
			public void onEnter(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.restartIndefinitely();
				ctx.msPacMan.setPosition(TS * 31, TS * 20);
				ctx.msPacMan.setMoveDir(LEFT);
				ctx.msPacMan.setPixelSpeed(ctx.speed);
				ctx.msPacMan.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.ghosts.forEach(ghost -> {
					ghost.setPosition(TS * 33.5f, TS * 20);
					ghost.setMoveAndWishDir(LEFT);
					ghost.setPixelSpeed(ctx.speed);
					ghost.enterStateHuntingPac();
				});
				ctx.ghostIndex = 0;
			}

			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				if (timer.atSecond(1)) {
					intro.changeState(State.GHOSTS);
				}
			}
		},

		GHOSTS {
			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				var ghost = ctx.ghosts.get(ctx.ghostIndex);
				ghost.show();

				if (ghost.moveDir() == LEFT) {
					if (ghost.position().x() <= ctx.stopX) {
						ghost.setPosition(ctx.stopX, ghost.position().y());
						ghost.setMoveAndWishDir(UP);
						ctx.ticksUntilLifting = 2;
					} else {
						ghost.moveAndAnimate();
					}
					return;
				}

				if (ghost.moveDir() == UP) {
					if (ctx.ticksUntilLifting > 0) {
						ctx.ticksUntilLifting -= 1;
						Logger.trace("Ticks until lifting {}: {}", ghost.name(), ctx.ticksUntilLifting);
						return;
					}
					if (ghost.position().y() <= ctx.stopY + ghost.id() * 16) {
						ghost.setPixelSpeed(0);
						ghost.animation().ifPresent(Animated::reset);
						if (ctx.ghostIndex == 3) {
							intro.changeState(State.MSPACMAN);
						} else {
							++ctx.ghostIndex;
						}
					} else {
						ghost.moveAndAnimate();
					}
				}
			}
		},

		MSPACMAN {
			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				ctx.msPacMan.show();
				ctx.msPacMan.moveAndAnimate();
				if (ctx.msPacMan.position().x() <= ctx.stopMsPacX) {
					ctx.msPacMan.setPixelSpeed(0);
					ctx.msPacMan.animation().ifPresent(Animated::reset);
					intro.changeState(State.READY_TO_PLAY);
				}
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				if (timer.atSecond(2.0) && !ctx.gameController.game().hasCredit()) {
					ctx.gameController.changeState(GameState.READY);
					// go into demo mode
				} else if (timer.atSecond(5)) {
					ctx.gameController.changeState(GameState.CREDIT);
				}
			}
		};

		MsPacManIntro intro;
		final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	private final Context introData;

	public MsPacManIntro(GameController gameController) {
		super(State.values());
		for (var state : states) {
			state.intro = this;
		}
		introData = new Context(gameController);
	}

	@Override
	public Context context() {
		return introData;
	}
}