package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.ImmutableRgbaColor;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.util.rng.ThreadLocalXorShift128Random;

public class SpacecraftPatternRenderer implements PatternRenderer {
	
	private final static List<Star> STARS = new ArrayList<>();
	
	private final static List<Star> REFERENCE_STARS = new ArrayList<>();
	
	private static CoordI2 STARS_LAST_VIEWPORT = ImmutableCoordI2.create();

	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var strength2 = strength * 2;
		final var strength4 = strength * 4;
		
		g2d.setColor(color);
		
		// Vertical lines
		g2d.fillRect(x, y + strength2, strength, height - strength4);
		g2d.fillRect(x + width - strength, y + strength2, strength, height - strength4);
		
		// Horizontal lines
		g2d.fillRect(x + strength2, y, width - strength4, strength);
		g2d.fillRect(x + strength2, y + height - strength, width - strength4, strength);
		
		// Corner squares
		g2d.fillRect(x + strength, y + strength, strength2, strength2);
		g2d.fillRect(x + width - strength - strength2, y + strength, strength2, strength2);
		g2d.fillRect(x + strength, y + height - strength - strength2, strength2, strength2);
		g2d.fillRect(x + width - strength - strength2, y + height - strength - strength2, strength2, strength2);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		// Fill background
		g2d.setColor(backgroundColor);
		g2d.fillRect(x + strength, y + strength, width - strength, height - strength);
		
		// Draw border on top
		this.drawDecorativeBorder(g2d, x, y, width, height, strength, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var strength = Math.max((thickBorder ? 4 : 2), (int) ((thickBorder ? 6 : 3) * scaleFactor));
		final var strength2 = strength * 2;
		final var backgroundTransparency = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 220);
		
		drawDecorativeBorderFilled(g2d, x + strength, y + strength, width - strength2, height - strength2, strength, innerBorderColor, backgroundTransparency);
		drawDecorativeBorder(g2d, x, y, width, height, strength, outerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var highlight = (hovered || toggled);
		
		Color bgColor = null;
		if (pressed) {
			bgColor = theme.getSelectionPrimary().toColor();
		}
		else if (highlight) {
			bgColor = theme.getBackgroundNormal().toColor();
		}
		else if (backgroundColor == null) {
			bgColor = theme.getBorderInner().toColor();
		}
		else {
			bgColor = backgroundColor;
		}
		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, width, height);
		
		if (enabled) {
			if (pressed) {
				g2d.setColor(RgbaColor.YELLOW.toAwtColor());
			}
			else if (highlight) {
				g2d.setColor(theme.getSelectionSecondary().toColor());
			}
			else {
				g2d.setColor(Color.BLACK);
			}
			final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 16, scaleFactor);
			G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), (int) (16 * scaleFactor), "X");
		}
	}

	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		this.generateStars(viewPort);
		
		for (final Star element : STARS) {
			g2d.setColor(element.color.toAwtColor());
			g2d.fillOval(
					Math.floorMod(element.position.getX() - offset.getX(), viewPort.getX()),
					Math.floorMod(element.position.getY() - offset.getY(), viewPort.getY()),
					element.radius.getX(),
					element.radius.getY());
		}
	}

	private void generateStars(final CoordI2 viewPort) {
		if (REFERENCE_STARS.isEmpty()) {
			final var rand = ThreadLocalXorShift128Random.current();

			for (var i = 0; i < 350; i++) {
				final var position = ImmutableCoordI2.create(rand.nextInt(RendererControl.REFERENCE_RESOLUTION.getX()), rand.nextInt(RendererControl.REFERENCE_RESOLUTION.getY()));
				final var radius = ImmutableCoordI2.create(rand.nextInt(3) + 2, rand.nextInt(3) + 2);
				final var color = new ImmutableRgbaColor(rand.nextInt(30) + 100, rand.nextInt(30) + 100, rand.nextInt(30) + 130);
				
				REFERENCE_STARS.add(new Star(position, radius, color));
				STARS.add(new Star(position, radius, color));
			}
		}
		
		if (!viewPort.equals(STARS_LAST_VIEWPORT)) {
			final double scaleX = (double) viewPort.getX() / RendererControl.REFERENCE_RESOLUTION.getX();
			final double scaleY = (double) viewPort.getY() / RendererControl.REFERENCE_RESOLUTION.getY();

			for (int i = 0; i < REFERENCE_STARS.size(); i++) {
				final var refStar = REFERENCE_STARS.get(i);
				final var star = STARS.get(i);
				
				star.position = ImmutableCoordI2.create(
						(int) (refStar.position.getX() * scaleX),
						(int) (refStar.position.getY() * scaleY));
			}
			STARS_LAST_VIEWPORT = viewPort.toImmutableCoordI2();
		}
	}

	private static class Star {
		
		CoordI2 position;
		
		CoordI2 radius;
		
		RgbaColor color;
		
		Star(final CoordI2 position, final CoordI2 radius, final RgbaColor color) {
			super();
			this.position = position;
			this.radius = radius;
			this.color = color;
		}
		
	}

	@Override
	public void drawButton(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		float h, s, b;
		
		if (backgroundColor == null) {
			final var baseColor = !toggled ? theme.getBackgroundNormal() : theme.getBackgroundSelected();
			h = baseColor.getHue();
			s = baseColor.getSaturation();
			b = baseColor.getBrightness();
			
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}
		}
		else {
			final var hsb = new float[3];
			Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), hsb);
			h = hsb[0];
			s = hsb[1];
			
			if (!toggled) {
				b = 0.30F;
			}
			else {
				b = 0.50F;
			}
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}
		}
		
		b = Math.max(0.0f, Math.min(1.0f, b));
		final var bgColor = Color.getHSBColor(h, s, b);
		g2d.setColor(bgColor);
		g2d.fillRect(x, y, width, height);
		
		final var borderStrength = Math.max(2, (int) (3 * scaleFactor));
		final var borderColor = hovered ? theme.getSelectionPrimary().toColor() : 
				enabled ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, borderColor);
	}
}
