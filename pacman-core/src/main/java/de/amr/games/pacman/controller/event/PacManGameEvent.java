package de.amr.games.pacman.controller.event;

import java.util.Objects;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;

/**
 * Base class for events fired during game play. This class is a kind of compromise between separate
 * subclasses for each event type and a fat base class.
 * 
 * @author Armin Reichert
 */
public class PacManGameEvent {

	public enum Info {
		BONUS_ACTIVATED, BONUS_EATEN, BONUS_EXPIRED, EXTRA_LIFE, PLAYER_FOUND_FOOD, PLAYER_GAINS_POWER, PLAYER_LOSING_POWER,
		PLAYER_LOST_POWER, GHOST_ENTERS_HOUSE, GHOST_LEAVES_HOUSE, GHOST_RETURNS_HOME, ANY;
	}

	public final GameVariant gameVariant;
	public final GameModel gameModel;
	public final Info info;
	public final Ghost ghost; // only used for ghost-related events
	public V2i tile; // the (optional) tile where this event occurred

	public PacManGameEvent(GameVariant gameVariant, GameModel gameModel) {
		this(gameVariant, gameModel, Info.ANY, null);
	}

	public PacManGameEvent(GameVariant gameVariant, GameModel gameModel, Info info) {
		this(gameVariant, gameModel, info, null);
	}

	public PacManGameEvent(GameVariant gameVariant, GameModel gameModel, Info info, Ghost ghost) {
		this.gameVariant = Objects.requireNonNull(gameVariant);
		this.gameModel = Objects.requireNonNull(gameModel);
		this.info = Objects.requireNonNull(info);
		this.ghost = ghost;
	}
}