package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

public class PacMan_IntermissionScene2_Controller {

	public enum Phase {

		WALKING, GETTING_STUCK, STUCK;
	}

	public static final int groundTileY = 20;

	public final CountdownTimer timer = new CountdownTimer();
	public final PacManGameController controller;
	public final PacManGameAnimations animations;
	public final SoundManager sounds;

	public Ghost blinky;
	public Pac pac;
	public GameEntity nail;
	public Phase phase;

	public PacMan_IntermissionScene2_Controller(PacManGameController controller, PacManGameAnimations animations,
			SoundManager sounds) {
		this.controller = controller;
		this.animations = animations;
		this.sounds = sounds;
	}

	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.setTilePosition(30, groundTileY);
		pac.visible = true;
		pac.speed = 1;
		animations.playerAnimations().playerMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.setPositionRelativeTo(pac, t(14), 0);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.speed = 1;
		animations.ghostAnimations().ghostKicking(blinky, blinky.dir).restart();

		nail = new GameEntity();
		nail.visible = true;
		nail.setPosition(t(14), t(groundTileY) - 1);

		sounds.play(PacManGameSound.INTERMISSION_2);

		enter(Phase.WALKING);
	}

	public void enter(Phase nextPhase) {
		phase = nextPhase;
		timer.setDuration(Long.MAX_VALUE);
	}

	public int nailDistance() {
		return (int) (nail.position.x - blinky.position.x);
	}

	public void update() {
		switch (phase) {
		case WALKING:
			if (nailDistance() == 0) {
				enter(Phase.GETTING_STUCK);
			}
			break;
		case GETTING_STUCK:
			int stretching = nailDistance() / 4;
			blinky.speed = 0.3f - 0.1f * stretching;
			if (stretching == 3) {
				blinky.speed = 0;
				blinky.dir = Direction.UP;
				enter(Phase.STUCK);
			}
			break;
		case STUCK:
			if (timer.running() == clock.sec(3)) {
				blinky.dir = Direction.RIGHT;
			}
			if (timer.running() == clock.sec(6)) {
				controller.finishCurrentState();
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
		timer.run();
	}
}