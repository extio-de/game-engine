package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public class CustomLabel extends Component {
	
	protected static final int STATE_NORMAL = 0;
	
	protected static final int STATE_PRESSED = 2;
	
	protected static final int STATE_HOVERED = 4;
	
	protected String caption;
	
	protected int state = STATE_NORMAL;
	
	protected int fontSize;
	
	protected double scaleFactor;
	
	protected boolean enabled;
	
	protected Color backgroundColor;
	
	protected Color foregroundColor;
	
	private HorizontalAlignment textAlignment;
	
	protected boolean dirty = true;
	
	private CoordI2 lastMousePosition;
	
	private final ThemeManager themeManager;
	
	public void setCaption(final String caption) {
		this.caption = caption;
	}
	
	public void setFontSize(final int fontSize) {
		this.fontSize = fontSize;
	}
	
	public void setScaleFactor(final double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		if (!enabled) {
			this.lastMousePosition = null;
		}
		this.enabled = enabled;
	}
	
	public void setBackgroundColor(final Color color) {
		this.backgroundColor = color;
	}
	
	public void setForegroundColor(final Color color) {
		this.foregroundColor = color;
	}
	
	public HorizontalAlignment getTextAlignment() {
		return this.textAlignment;
	}
	
	public void setTextAlignment(final HorizontalAlignment textAlignment) {
		this.textAlignment = textAlignment;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public CoordI2 getLastMousePosition() {
		return this.lastMousePosition;
	}
	
	public CustomLabel(final ActionListener listener, final ThemeManager themeManager) {
		this.themeManager = themeManager;
		this.setIgnoreRepaint(true);
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				CustomLabel.this.state |= STATE_PRESSED;
				CustomLabel.this.dirty = true;
				e.consume();
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				CustomLabel.this.state &= ~STATE_PRESSED;
				listener.actionPerformed(new ActionEvent(this, 0, null));
				CustomLabel.this.dirty = true;
				e.consume();
			}
			
			@Override
			public void mouseEntered(final MouseEvent e) {
				CustomLabel.this.state |= STATE_HOVERED;
				CustomLabel.this.dirty = true;
				CustomLabel.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				e.consume();
			}
			
			@Override
			public void mouseExited(final MouseEvent e) {
				CustomLabel.this.state &= ~STATE_HOVERED;
				CustomLabel.this.dirty = true;
				CustomLabel.this.lastMousePosition = null;
				e.consume();
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				CustomLabel.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				e.consume();
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				CustomLabel.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				e.consume();
			}
		});
	}
	
	@Override
	public void update(final Graphics g) {
		//
	}
	
	@Override
	public void paint(final Graphics g) {
		if (this.themeManager == null) {
			return;
		}
		
		final var g2d = (Graphics2D) g;
		final Theme theme = this.themeManager.getCurrentTheme();
		
		if (this.backgroundColor != null) {
			g2d.setColor(this.backgroundColor);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		
		float h, s, b;
		if (this.foregroundColor == null) {
			h = theme.getTextNormal().getHue();
			s = theme.getTextNormal().getSaturation();
			b = theme.getTextNormal().getBrightness();
		}
		else {
			final var hsb = Color.RGBtoHSB(this.foregroundColor.getRed(), this.foregroundColor.getGreen(), this.foregroundColor.getBlue(), null);
			h = hsb[0];
			s = hsb[1];
			b = hsb[2];
		}
		
		if (!this.enabled) {
			// h = theme.getTextDisabled().getHue();
			// s = theme.getTextDisabled().getSaturation();
			// b = theme.getTextDisabled().getBrightness();
		}
		else if ((this.state & STATE_PRESSED) != 0) {
			h = theme.getSelectionPrimary().getHue();
			s = theme.getSelectionPrimary().getSaturation();
			b = theme.getSelectionPrimary().getBrightness() + theme.getPressedBrightnessAdjustment();
		}
		else if ((this.state & STATE_HOVERED) != 0) {
			if (theme.getTextNormal().getBrightness() > 0.85f) {
				b -= theme.getHoverBrightnessAdjustment();
			}
			else {
				b += theme.getHoverBrightnessAdjustment();
				if (theme.getTextNormal().getBrightness() < 0.3f) {
					b += theme.getHoverBrightnessAdjustment();
				}
			}
		}
		
		h = Math.max(0.0f, Math.min(1.0f, h));
		s = Math.max(0.0f, Math.min(1.0f, s));
		b = Math.max(0.0f, Math.min(1.0f, b));
		g2d.setColor(Color.getHSBColor(h, s, b));
		
		final var alignment = this.textAlignment == null ? HorizontalAlignment.CENTER : this.textAlignment;
		
		final var rawLines = this.caption != null && this.caption.contains("\n") ? this.caption.split("\n", -1) : new String[] { this.caption == null ? "" : this.caption };
		final var availableWidth = (int) (this.getWidth() / this.scaleFactor);
		final List<String> wrappedLines = new ArrayList<>();
		
		for (final var rawLine : rawLines) {
			if (rawLine.isEmpty()) {
				wrappedLines.add("");
				continue;
			}
			
			final var lineWidth = G2DDrawFont.getTextDimensions(rawLine, g2d, this.fontSize, 1.0).getX();
			if (lineWidth <= availableWidth) {
				wrappedLines.add(rawLine);
			}
			else {
				final var words = rawLine.split(" ");
				final var currentLine = new StringBuilder();
				
				for (final var word : words) {
					final var testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
					final var testWidth = G2DDrawFont.getTextDimensions(testLine, g2d, this.fontSize, 1.0).getX();
					
					if (testWidth <= availableWidth) {
						if (!currentLine.isEmpty()) {
							currentLine.append(" ");
						}
						currentLine.append(word);
					}
					else {
						if (!currentLine.isEmpty()) {
							wrappedLines.add(currentLine.toString());
							currentLine.setLength(0);
						}
						
						final var wordWidth = G2DDrawFont.getTextDimensions(word, g2d, this.fontSize, 1.0).getX();
						if (wordWidth <= availableWidth) {
							currentLine.append(word);
						}
						else {
							wrappedLines.add(word);
						}
					}
				}
				
				if (!currentLine.isEmpty()) {
					wrappedLines.add(currentLine.toString());
				}
			}
		}
		
		final var lines = wrappedLines.toArray(new String[0]);
		final var lineHeight = G2DDrawFont.getTextDimensions("M", g2d, this.fontSize, 1.0).getY() + (int) (Math.max(G2DDrawFont.FONT_SIZE_MIN, this.fontSize) * 0.5);
		final CoordI2[] lineDims = new CoordI2[lines.length];
		int maxLineWidth = 0;
		for (int i = 0; i < lines.length; i++) {
			lineDims[i] = G2DDrawFont.getTextDimensions(lines[i], g2d, this.fontSize, 1.0);
			if (lineDims[i].getX() > maxLineWidth) {
				maxLineWidth = lineDims[i].getX();
			}
		}
		
		final int totalTextHeight = lineHeight * lines.length;
		final int startY = ((int) (this.getHeight() / this.scaleFactor) - totalTextHeight) / 2;
		
		for (int i = 0; i < lines.length; i++) {
			final var line = lines[i];
			final var yOffset = startY + i * lineHeight;
			final var textDimScaled = ImmutableCoordI2.create((int) (lineDims[i].getX() * this.scaleFactor), (int) (lineDims[i].getY() * this.scaleFactor));
			
			switch (alignment) {
				case LEFT: {
					G2DDrawFont.renderText(g2d,
							textDimScaled,
							this.scaleFactor,
							1,
							yOffset,
							this.fontSize,
							line);
					break;
				}
				
				case CENTER: {
					G2DDrawFont.renderText(g2d,
							textDimScaled,
							this.scaleFactor,
							((int) (this.getWidth() / this.scaleFactor) - lineDims[i].getX()) / 2,
							yOffset,
							this.fontSize,
							line);
					break;
				}
				
				case RIGHT: {
					G2DDrawFont.renderText(g2d,
							textDimScaled,
							this.scaleFactor,
							(int) (this.getWidth() / this.scaleFactor) - lineDims[i].getX(),
							yOffset,
							this.fontSize,
							line);
					break;
				}
				
				default:
					break;
			}
		}
	}
	
}
