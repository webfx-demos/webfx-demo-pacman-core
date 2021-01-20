package de.amr.games.pacman.game.creatures;

import de.amr.games.pacman.lib.Direction;

/**
 * Bonus symbol. In Ms. Pac-Man, the bonus wanders the maze.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Creature {

	/** ID of the bonus symbol. */
	public byte symbol;

	/** Value of this bonus. */
	public int points;

	/** Number of clock ticks the bonus is still available for eating. */
	public long edibleTicks;

	/** Number of clock ticks the consumed bonus is still displayed. */
	public long eatenTicks;

	/* Ms. Pac-Man only: Diretion in which bonus traverses the maze. */
	public Direction targetDirection;
}