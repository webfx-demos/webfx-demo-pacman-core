package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;

import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.animation.TimedSequence;

public class Bonus2D {

	private PacManBonus bonus;
	private BufferedImage[] symbolSprites;
	private Map<Integer, BufferedImage> numberSprites;
	private TimedSequence<Integer> jumpAnimation;

	public Bonus2D() {
	}

	public TimedSequence<Integer> getJumpAnimation() {
		return jumpAnimation;
	}

	public void setJumpAnimation(TimedSequence<Integer> jumpAnimation) {
		this.jumpAnimation = jumpAnimation;
	}

	public void setBonus(PacManBonus bonus) {
		this.bonus = bonus;
	}

	public void setSymbolSprites(BufferedImage[] symbolSprites) {
		this.symbolSprites = symbolSprites;
	}

	public void setNumberSprites(Map<Integer, BufferedImage> numberSprites) {
		this.numberSprites = numberSprites;
	}

	public void render(Graphics2D g) {
		BufferedImage sprite = currentSprite();
		if (sprite == null || !bonus.visible) {
			return;
		}
		// Ms. Pac.Man bonus is jumping up and down while wandering the maze
		int jump = jumpAnimation != null ? jumpAnimation.animate() : 0;
		int dx = sprite.getWidth() / 2 - HTS, dy = sprite.getHeight() / 2 - HTS;
		Graphics2D gc = (Graphics2D) g.create();
		gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gc.translate(0, jump);
		gc.drawImage(sprite, (int) (bonus.position.x - dx), (int) (bonus.position.y - dy), null);
		gc.translate(0, -jump);
		gc.dispose();
	}

	private BufferedImage currentSprite() {
		if (bonus == null) {
			return null;
		}
		if (bonus.edibleTicksLeft > 0) {
			return symbolSprites[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return numberSprites.get(bonus.points);
		}
		return null;
	}
}