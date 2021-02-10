package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.common.scene.PlayScene;
import de.amr.games.pacman.ui.fx.mspacman.scene.MsPacManGameIntroScene;
import de.amr.games.pacman.ui.fx.pacman.scene.PacManGameIntroScene;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameFXUI implements PacManGameUI {

	private final Stage stage;
	private final double scaling;
	private final double sizeX, sizeY;

	private PacManGameModel game;

	private SoundManager soundManager;

	private PacManGameScene currentScene;

	// Pac-Man scenes
	private PacManGameScene pacManIntroScene;
	private PacManGameScene pacManPlayScene;
	private PacManGameScene pacManIntermissionScene1;
	private PacManGameScene pacManIntermissionScene2;
	private PacManGameScene pacManIntermissionScene3;

	// Ms. Pac_an scenes
	private PacManGameScene msPacManIntroScene;
	private PacManGameScene msPacManPlayScene;
	private PacManGameScene msPacManIntermissionScene1;
	private PacManGameScene msPacManIntermissionScene2;
	private PacManGameScene msPacManIntermissionScene3;

	public PacManGameFXUI(Stage stage, PacManGameModel game, double scaling) {
		sizeX = 28 * TS * scaling;
		sizeY = 36 * TS * scaling;
		this.scaling = scaling;
		this.stage = stage;
		stage.setTitle("JavaFX: Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});

		setGame(game);
		currentScene = selectScene();
		currentScene.start();
		stage.setScene(currentScene.getFXScene());
		stage.sizeToScene();
		log("Initial scene is %s", currentScene);
	}

	@Override
	public void setGame(PacManGameModel game) {
		this.game = game;
		if (game instanceof MsPacManGame) {
			soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getMsPacManSoundURL);
			msPacManIntroScene = new MsPacManGameIntroScene(game, sizeX, sizeY, scaling);
			msPacManPlayScene = new PlayScene(game, sizeX, sizeY, scaling, true);
			msPacManIntermissionScene1 = msPacManIntroScene; // TODO
			msPacManIntermissionScene2 = msPacManIntroScene; // TODO
			msPacManIntermissionScene3 = msPacManIntroScene; // TODO
		} else if (game instanceof PacManGame) {
			soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getPacManSoundURL);
			pacManIntroScene = new PacManGameIntroScene(game, sizeX, sizeY, scaling);
			pacManPlayScene = new PlayScene(game, sizeX, sizeY, scaling, false);
			pacManIntermissionScene1 = pacManIntroScene; // TODO
			pacManIntermissionScene2 = pacManIntroScene; // TODO
			pacManIntermissionScene3 = pacManIntroScene; // TODO
		} else {
			log("%s: Cannot create scenes for invalid game: %s", this, game);
		}
	}

	@Override
	public void updateScene() {
		if (game == null) {
			log("%s: No game?", this);
			return;
		}
		PacManGameScene scene = selectScene();
		if (scene == null) {
			log("%s: No scene matches current game state %s", this, game.state);
			return;
		}
		if (currentScene != scene) {
			if (currentScene != null) {
				currentScene.end();
				log("%s: Scene changed from %s to %s", this, currentScene.getClass().getSimpleName(),
						scene.getClass().getSimpleName());
			} else {
				log("%s: Scene changed to %s", this, scene.getClass().getSimpleName());
			}
			currentScene = scene;
			currentScene.start();
			currentScene.update();
		}
		if (currentScene != null) {
			Platform.runLater(() -> {
				stage.setScene(currentScene.getFXScene());
			});
		}
	}

	private PacManGameScene selectScene() {
		if (game instanceof MsPacManGame) {
			if (game.state == PacManGameState.INTRO) {
				return msPacManIntroScene;
			}
			if (game.state == PacManGameState.INTERMISSION) {
				if (game.intermissionNumber == 1) {
					return msPacManIntermissionScene1;
				}
				if (game.intermissionNumber == 2) {
					return msPacManIntermissionScene2;
				}
				if (game.intermissionNumber == 3) {
					return msPacManIntermissionScene3;
				}
			}
			return msPacManPlayScene;
		}
		if (game instanceof PacManGame) {
			if (game.state == PacManGameState.INTRO) {
				return pacManIntroScene;
			}
			if (game.state == PacManGameState.INTERMISSION) {
				if (game.intermissionNumber == 1) {
					return pacManIntermissionScene1;
				}
				if (game.intermissionNumber == 2) {
					return pacManIntermissionScene2;
				}
				if (game.intermissionNumber == 3) {
					return pacManIntermissionScene3;
				}
			}
			return pacManPlayScene;
		}
		throw new IllegalStateException("No scene found");
	}

	@Override
	public void setCloseHandler(Runnable handler) {
	}

	@Override
	public void show() {
		stage.show();
	}

	@Override
	public void render() {
		if (currentScene != null) {
			Platform.runLater(currentScene::render);
		}
	}

	@Override
	public void showFlashMessage(String message) {
	}

	@Override
	public boolean keyPressed(String keySpec) {
		if (currentScene != null) {
			boolean pressed = currentScene.keyboard().keyPressed(keySpec);
			currentScene.keyboard().clearKey(keySpec); // TODO
			return pressed;
		} else {
			return false;
		}
	}

	@Override
	public Optional<SoundManager> sounds() {
		return Optional.ofNullable(soundManager);
	}

	@Override
	public void mute(boolean muted) {
		// TODO
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return currentScene.animations();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}