package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.spatial2.model.CoordI2;

public class AsciiPatternRenderer implements PatternRenderer {

	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		g2d.setColor(color);

		// Use simple ASCII characters for borders
		final int fontSize = Math.max(8, strength * 4); // Scale font size with strength

		// Get character dimensions
		final var horizDim = G2DDrawFont.getTextDimensions("-", g2d, fontSize, 1.0);
		final var vertDim = G2DDrawFont.getTextDimensions("|", g2d, fontSize, 1.0);
		final var cornerDim = G2DDrawFont.getTextDimensions("+", g2d, fontSize, 1.0);

		final int charWidth = Math.max(1, (int)horizDim.getX());
		final int charHeight = Math.max(1, (int)cornerDim.getY());

		final int horizChars = Math.max(0, (width - 2 * charWidth) / charWidth);
		final int vertChars = Math.max(0, (height - 2 * charHeight) / charHeight);

		final int totalWidth = (horizChars + 2) * charWidth;
		final int totalHeight = (vertChars + 2) * charHeight;
		final int offsetX = x + Math.max(0, (width - totalWidth) / 2);
		final int offsetY = y + Math.max(0, (height - totalHeight) / 2);

		// Top border
		G2DDrawFont.renderText(g2d, cornerDim, 1.0, offsetX, offsetY, fontSize, "+");
		for (int i = 0; i < horizChars; i++) {
			G2DDrawFont.renderText(g2d, horizDim, 1.0, offsetX + charWidth + i * charWidth, offsetY, fontSize, "-");
		}
		G2DDrawFont.renderText(g2d, cornerDim, 1.0, offsetX + charWidth + horizChars * charWidth, offsetY, fontSize, "+");

		// Bottom border
		G2DDrawFont.renderText(g2d, cornerDim, 1.0, offsetX, offsetY + charHeight + vertChars * charHeight, fontSize, "+");
		for (int i = 0; i < horizChars; i++) {
			G2DDrawFont.renderText(g2d, horizDim, 1.0, offsetX + charWidth + i * charWidth, offsetY + (int)(charHeight * 1.5) + vertChars * charHeight, fontSize, "-");
		}
		G2DDrawFont.renderText(g2d, cornerDim, 1.0, offsetX + charWidth + horizChars * charWidth, offsetY + charHeight + vertChars * charHeight, fontSize, "+");

		// Left and right borders
		for (int i = 0; i < vertChars; i++) {
			G2DDrawFont.renderText(g2d, vertDim, 1.0, offsetX, offsetY + charHeight + i * charHeight, fontSize, "|");
			G2DDrawFont.renderText(g2d, vertDim, 1.0, offsetX + charWidth + horizChars * charWidth, offsetY + charHeight + i * charHeight, fontSize, "|");
		}
	}

	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		final var s = Math.max(1, strength);
		g2d.setColor(backgroundColor);
		g2d.fillRect(x + s, y + s, Math.max(0, width - s * 2), Math.max(0, height - s * 2));
		this.drawDecorativeBorder(g2d, x, y, width, height, s, borderColor);
	}

	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var s = Math.max((thickBorder ? 2 : 1), (int) ((thickBorder ? 3 : 2) * scaleFactor));
		final var s2 = s * 2;

		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 220);
		this.drawDecorativeBorderFilled(g2d, x + s, y + s, width - s2, height - s2, s, innerBorderColor, bg);
		this.drawDecorativeBorder(g2d, x, y, width, height, s, outerBorderColor);
	}

	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var highlight = (hovered || toggled);

		Color bgColor = backgroundColor;
		if (bgColor == null) {
			bgColor = theme.getBackgroundNormal().toColor();
		}
		if (highlight) {
			bgColor = theme.getBorderInner().toColor();
		}
		if (pressed) {
			bgColor = theme.getSelectionPrimary().toColor();
		}

		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, width, height);

		final var s = Math.max(1, (int) (1 * scaleFactor));
		this.drawDecorativeBorder(g2d, 0, 0, width, height, s, theme.getBorderOuter().toColor());

		if (!enabled) {
			return;
		}

		final Color textColor;
		if (pressed) {
			textColor = theme.getTextNormal().toColor();
		}
		else if (highlight) {
			textColor = theme.getTextDisabled().toColor();
		}
		else {
			textColor = theme.getSelectionSecondary().toColor();
		}
		g2d.setColor(textColor);

		final var fontSize = (int) (12 * scaleFactor);
		final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 12, scaleFactor);
		G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), fontSize, "X");
	}

	private java.awt.image.BufferedImage patternCache;

	private java.awt.image.BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int spacing = 40;
			this.patternCache = new BufferedImage(spacing, spacing, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();

			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, spacing, spacing);

			// Simple ASCII-like pattern - just dots
			g2d.setColor(new Color(0, 255, 0, 30));
			g2d.fillRect(spacing / 2 - 1, spacing / 2 - 1, 2, 2);

			g2d.dispose();
		}
		return this.patternCache;
	}

	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		final var cache = this.getPatternCache();
		final int spacing = cache.getWidth();

		final int offX = Math.floorMod(offset.getX(), spacing);
		final int offY = Math.floorMod(offset.getY(), spacing);

		for (int x = -spacing; x < viewPort.getX() + spacing; x += spacing) {
			for (int y = -spacing; y < viewPort.getY() + spacing; y += spacing) {
				g2d.drawImage(cache, x + offX, y + offY, null);
			}
		}
	}

	@Override
	public void drawButton(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		Color bgColor;
		if (backgroundColor == null) {
			final var baseColor = !toggled ? theme.getBackgroundNormal() : theme.getBackgroundSelected();
			float b = baseColor.getBrightness();

			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}

			b = Math.max(0.0f, Math.min(1.0f, b));
			bgColor = Color.getHSBColor(baseColor.getHue(), baseColor.getSaturation(), b);
		}
		else {
			final var hsb = new float[3];
			Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), hsb);

			float b = !toggled ? 0.25F : 0.45F;
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}

			b = Math.max(0.0f, Math.min(1.0f, b));
			bgColor = Color.getHSBColor(hsb[0], hsb[1], b);
		}

		if (!enabled) {
			final var hsb = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);
			bgColor = Color.getHSBColor(hsb[0], Math.min(1.0f, hsb[1] * 0.1f), Math.min(1.0f, hsb[2] * 0.5f));
		}

		// Simple flat fill, no gradient for ASCII style
		g2d.setColor(bgColor);
		g2d.fillRect(x, y, width, height);

		// Simple border
		final var borderStrength = Math.max(1, (int) (1 * scaleFactor));
		final var borderColor = hovered ? theme.getSelectionPrimary().toColor() : theme.getBorderOuter().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, borderColor);
	}
}