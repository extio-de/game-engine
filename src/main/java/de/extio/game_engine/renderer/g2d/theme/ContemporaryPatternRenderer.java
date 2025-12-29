package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.spatial2.model.CoordI2;

@Conditional(G2DRendererCondition.class)
@Component
public class ContemporaryPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final int s = Math.max(1, strength);
		final int arc = Math.max(10, s * 6);
		
		final Object aaOld = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		final Object scOld = g2d.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
		final Stroke strokeOld = g2d.getStroke();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g2d.setColor(color);
			g2d.setStroke(new BasicStroke(s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			final double inset = s / 2.0;
			final double rw = Math.max(0.0, (width - 1) - inset * 2);
			final double rh = Math.max(0.0, (height - 1) - inset * 2);
			final var rr = new RoundRectangle2D.Double(x + inset, y + inset, rw, rh, arc, arc);
			g2d.draw(rr);
		}
		finally {
			g2d.setStroke(strokeOld);
			if (aaOld != null) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld);
			}
			if (scOld != null) {
				g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, scOld);
			}
		}
		
		final int tick = Math.max(2, s);
		final int tickLen = Math.max(8, s * 6);
		if (width > tickLen + s * 2 && height > tickLen + s * 2) {
			final int cx = x + width / 2;
			final int cy = y + height / 2;
			g2d.fillRect(cx - tickLen / 2, y + s, tickLen, tick);
			g2d.fillRect(cx - tickLen / 2, y + height - s - tick, tickLen, tick);
			g2d.fillRect(x + s, cy - tickLen / 2, tick, tickLen);
			g2d.fillRect(x + width - s - tick, cy - tickLen / 2, tick, tickLen);
		}
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		final int s = Math.max(1, strength);
		final int arc = Math.max(10, s * 6);
		
		final Object aaOld = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(backgroundColor);
			g2d.fillRoundRect(x + s, y + s, Math.max(0, width - s * 2), Math.max(0, height - s * 2), arc, arc);
		}
		finally {
			if (aaOld != null) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld);
			}
		}
		this.drawDecorativeBorder(g2d, x, y, width, height, s, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final int s = Math.max((thickBorder ? 4 : 2), (int) ((thickBorder ? 7 : 4) * scaleFactor));
		final int s2 = s * 2;
		final int arc = Math.max(10, (int) (s * 6 * scaleFactor));
		
		final Object aaOld = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 185);
			g2d.setColor(bg);
			g2d.fillRoundRect(x, y, width, height, arc, arc);
			
			final var oldPaint = g2d.getPaint();
			final var topGlow = new Color(255, 255, 255, 25);
			final var bottomShade = new Color(0, 0, 0, 55);
			g2d.setPaint(new GradientPaint(x, y, topGlow, x, y + height, bottomShade));
			g2d.fillRoundRect(x + 1, y + 1, Math.max(0, width - 2), Math.max(0, height - 2), arc, arc);
			g2d.setPaint(oldPaint);
		}
		finally {
			if (aaOld != null) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld);
			}
		}
		
		// this.drawDecorativeBorder(g2d, x + s, y + s, width - s2, height - s2, s, innerBorderColor);
		this.drawDecorativeBorder(g2d, x, y, width, height, s, outerBorderColor);
		
		final int headerH = Math.max(2, (int) (3 * scaleFactor));
		g2d.setColor(new Color(outerBorderColor.getRed(), outerBorderColor.getGreen(), outerBorderColor.getBlue(), 120));
		g2d.fillRoundRect(x + s, y + s, Math.max(0, width - s2), headerH, arc, arc);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final int STATE_HOVERED = 4;
		final int STATE_TOGGLED = 1;
		final int STATE_PRESSED = 2;
		
		final boolean hovered = (state & STATE_HOVERED) != 0;
		final boolean pressed = (state & STATE_PRESSED) != 0;
		final boolean toggled = (state & STATE_TOGGLED) != 0;
		
		Color base = backgroundColor;
		if (base == null) {
			base = theme.getWindowBackground().toColor();
		}
		if (hovered || toggled) {
			base = theme.getSelectionSecondary().toColor();
		}
		if (pressed) {
			base = theme.getSelectionPrimary().toColor();
		}
		
		final var hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
		final float topB = pressed ? Math.max(0.0f, hsb[2] - 0.10f) : Math.min(1.0f, hsb[2] + 0.10f);
		final float bottomB = pressed ? Math.min(1.0f, hsb[2] + 0.04f) : Math.max(0.0f, hsb[2] - 0.12f);
		final var top = new Color(Color.getHSBColor(hsb[0], hsb[1], topB).getRed(), Color.getHSBColor(hsb[0], hsb[1], topB).getGreen(), Color.getHSBColor(hsb[0], hsb[1], topB).getBlue(), 220);
		final var bottom = new Color(Color.getHSBColor(hsb[0], hsb[1], bottomB).getRed(), Color.getHSBColor(hsb[0], hsb[1], bottomB).getGreen(), Color.getHSBColor(hsb[0], hsb[1], bottomB).getBlue(), 220);
		
		final int arc = Math.max(10, (int) (12 * scaleFactor));
		final Object aaOld = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final var oldPaint = g2d.getPaint();
			g2d.setPaint(new GradientPaint(0, 0, top, 0, height, bottom));
			g2d.fillRoundRect(0, 0, width, height, arc, arc);
			g2d.setPaint(oldPaint);
		}
		finally {
			if (aaOld != null) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld);
			}
		}
		
		final int s = Math.max(1, (int) (2 * scaleFactor));
		final var border = hovered ? theme.getSelectionPrimary().toColor() : theme.getBorderOuter().toColor();
		this.drawDecorativeBorder(g2d, 0, 0, width, height, s, border);
		
		if (!enabled) {
			return;
		}
		
		final Color fg;
		if (pressed) {
			fg = theme.getTextNormal().toColor();
		}
		else if (hovered) {
			fg = Color.BLACK;
		}
		else {
			fg = theme.getTextNormal().toColor();
		}
		g2d.setColor(fg);
		
		final int fontSize = (int) (15 * scaleFactor);
		final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 15, scaleFactor);
		G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), fontSize, "X");
	}

	private BufferedImage patternCache;
	
	private BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int cell = 80;
			this.patternCache = new BufferedImage(cell, cell, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, cell, cell);
			
			g2d.setColor(new Color(255, 255, 255, 18));
			for (int x = 10; x < cell; x += 20) {
				g2d.drawLine(x, 0, x, cell);
			}
			for (int y = 10; y < cell; y += 20) {
				g2d.drawLine(0, y, cell, y);
			}
			
			g2d.setColor(new Color(255, 255, 255, 30));
			g2d.fillRoundRect(14, 14, 6, 6, 6, 6);
			g2d.fillRoundRect(54, 44, 5, 5, 5, 5);
			g2d.fillRoundRect(28, 58, 4, 4, 4, 4);
			
			g2d.dispose();
		}
		return this.patternCache;
	}
	
	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		final var cache = this.getPatternCache();
		final int cell = cache.getWidth();
		
		final int offX = Math.floorMod(offset.getX(), cell);
		final int offY = Math.floorMod(offset.getY(), cell);
		
		for (int x = -cell; x < viewPort.getX() + cell; x += cell) {
			for (int y = -cell; y < viewPort.getY() + cell; y += cell) {
				g2d.drawImage(cache, x + offX, y + offY, null);
			}
		}
	}
	
	@Override
	public void drawButton(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final int STATE_TOGGLED = 1;
		final int STATE_PRESSED = 2;
		final int STATE_HOVERED = 4;
		
		final boolean toggled = (state & STATE_TOGGLED) != 0;
		final boolean pressed = (state & STATE_PRESSED) != 0;
		final boolean hovered = (state & STATE_HOVERED) != 0;
		
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
			b = toggled ? 0.55F : 0.35F;
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}
		}
		
		b = Math.max(0.0f, Math.min(1.0f, b));
		Color base = Color.getHSBColor(h, s, b);
		if (!enabled) {
			final var hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
			base = Color.getHSBColor(hsb[0], Math.min(1.0f, hsb[1] * 0.18f), Math.min(1.0f, hsb[2] * 0.60f));
		}
		
		final var baseHsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
		final float topB = pressed ? Math.max(0.0f, baseHsb[2] - 0.10f) : Math.min(1.0f, baseHsb[2] + 0.10f);
		final float bottomB = pressed ? Math.min(1.0f, baseHsb[2] + 0.04f) : Math.max(0.0f, baseHsb[2] - 0.14f);
		final var top = Color.getHSBColor(baseHsb[0], Math.min(1.0f, baseHsb[1] * 0.95f), topB);
		final var bottom = Color.getHSBColor(baseHsb[0], baseHsb[1], bottomB);
		
		final int arc = Math.max(10, (int) (12 * scaleFactor));
		final Object aaOld = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final var oldPaint = g2d.getPaint();
			g2d.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
			g2d.fillRoundRect(x, y, width, height, arc, arc);
			g2d.setPaint(oldPaint);
		}
		finally {
			if (aaOld != null) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld);
			}
		}
		
		int shimmerAlpha = hovered ? 34 : 18;
		if (pressed) {
			shimmerAlpha = 12;
		}
		if (!enabled) {
			shimmerAlpha = 8;
		}
		if (shimmerAlpha > 0 && width > 8 && height > 8) {
			final Object aaOld2 = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			try {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(new Color(255, 255, 255, shimmerAlpha));
				g2d.fillRoundRect(x + 2, y + 2, width - 4, Math.max(3, height / 3), arc, arc);
			}
			finally {
				if (aaOld2 != null) {
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld2);
				}
			}
		}
		
		final int accentW = Math.max(2, (int) (4 * scaleFactor));
		if (hovered || toggled) {
			final var accentBase = pressed ? theme.getSelectionSecondary().toColor() : theme.getSelectionPrimary().toColor();
			final var accent = new Color(accentBase.getRed(), accentBase.getGreen(), accentBase.getBlue(), enabled ? (pressed ? 130 : 170) : 80);
			final Object aaOld2 = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			try {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(accent);
				g2d.fillRoundRect(x + 2, y + 2, accentW, Math.max(0, height - 4), arc, arc);
			}
			finally {
				if (aaOld2 != null) {
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld2);
				}
			}
			
			final int dot = Math.max(3, (int) (5 * scaleFactor));
			if (width > dot + 12 && height > dot + 10) {
				g2d.fillOval(x + width - dot - 6, y + 6, dot, dot);
			}
		}
		
		final int borderStrength = Math.max(1, (int) (2 * scaleFactor));
		final var outerBorder = hovered ? theme.getSelectionPrimary().toColor() : theme.getBorderOuter().toColor();
		final var innerBorder = enabled ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, outerBorder);
		final int inset = Math.max(1, borderStrength);
		if (width > inset * 4 && height > inset * 4) {
			this.drawDecorativeBorder(g2d, x + inset, y + inset, width - inset * 2, height - inset * 2, Math.max(1, borderStrength / 2), innerBorder);
		}
	}
}
