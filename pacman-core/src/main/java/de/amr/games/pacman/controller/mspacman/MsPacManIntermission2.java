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
package de.amr.games.pacman.controller.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.SceneControllerContext;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission2.IntermissionData;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission2.IntermissionState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Clapperboard;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission2 extends Fsm<IntermissionState, IntermissionData> {

	public static class IntermissionData extends SceneControllerContext {
		public final int upperY = t(12);
		public final int middleY = t(18);
		public final int lowerY = t(24);
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPacMan;

		public IntermissionData(GameController gameController) {
			super(gameController);
		}
	}

	public enum IntermissionState implements FsmState<IntermissionData> {

		FLAP {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.resetIndefinitely();
				timer.start();
				ctx.clapperboard = new Clapperboard(2, "THE CHASE");
				ctx.clapperboard.setPosition(t(3), t(10));
				ctx.clapperboard.show();
				ctx.pacMan = new Pac("Pac-Man");
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.selectAndRunAnimation(AnimKeys.PAC_MUNCHING);
				ctx.msPacMan = new Pac("Ms. Pac-Man");
				ctx.msPacMan.setMoveDir(Direction.RIGHT);
				ctx.msPacMan.selectAndRunAnimation(AnimKeys.GHOST_COLOR);
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.atSecond(1)) {
					ctx.gameController().sounds().play(GameSound.INTERMISSION_2);
					ctx.clapperboard.animation().ifPresent(EntityAnimation::restart);
				} else if (timer.atSecond(2)) {
					ctx.clapperboard.hide();
				} else if (timer.atSecond(3)) {
					intermission.changeState(IntermissionState.CHASING);
				}
			}
		},

		CHASING {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.resetIndefinitely();
				timer.start();
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.atSecond(2.5)) {
					ctx.pacMan.setPosition(-t(2), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setAbsSpeed(2.0);
					ctx.pacMan.show();
					ctx.msPacMan.setPosition(-t(8), ctx.upperY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setAbsSpeed(2.0);
					ctx.msPacMan.show();
				} else if (timer.atSecond(7)) {
					ctx.pacMan.setPosition(t(36), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setAbsSpeed(2.0);
					ctx.msPacMan.setPosition(t(30), ctx.lowerY);
					ctx.msPacMan.setMoveDir(Direction.LEFT);
					ctx.msPacMan.setAbsSpeed(2.0);
				} else if (timer.atSecond(11.5)) {
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setAbsSpeed(2.0);
					ctx.msPacMan.setPosition(t(-8), ctx.middleY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setAbsSpeed(2.0);
					ctx.pacMan.setPosition(t(-2), ctx.middleY);
				} else if (timer.atSecond(15.5)) {
					ctx.pacMan.setPosition(t(42), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setAbsSpeed(4.0);
					ctx.msPacMan.setPosition(t(30), ctx.upperY);
					ctx.msPacMan.setMoveDir(Direction.LEFT);
					ctx.msPacMan.setAbsSpeed(4.0);
				} else if (timer.atSecond(16.5)) {
					ctx.pacMan.setPosition(t(-2), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setAbsSpeed(4.0);
					ctx.msPacMan.setPosition(t(-14), ctx.lowerY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setAbsSpeed(4.0);
				} else if (timer.atSecond(21)) {
					ctx.gameController().terminateCurrentState();
					return;
				}
				ctx.pacMan.move();
				ctx.pacMan.updateAnimation();
				ctx.msPacMan.move();
				ctx.msPacMan.updateAnimation();
			}
		};

		MsPacManIntermission2 intermission;
		final TickTimer timer = new TickTimer("Timer-" + name(), GameModel.FPS);

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	private final IntermissionData intermissionData;

	public MsPacManIntermission2(GameController gameController) {
		states = IntermissionState.values();
		for (var state : IntermissionState.values()) {
			state.intermission = this;
		}
		this.intermissionData = new IntermissionData(gameController);
	}

	@Override
	public IntermissionData context() {
		return intermissionData;
	}
}