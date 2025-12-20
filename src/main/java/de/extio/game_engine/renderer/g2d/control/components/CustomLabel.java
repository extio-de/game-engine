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

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBoTextAlignment;
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
	
	private DrawFontRenderingBoTextAlignment textAlignment;
	
	protected boolean dirty = true;
	
	private CoordI2 lastMousePosition;
	
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
	
	public DrawFontRenderingBoTextAlignment getTextAlignment() {
		return this.textAlignment;
	}
	
	public void setTextAlignment(final DrawFontRenderingBoTextAlignment textAlignment) {
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
	
	public CustomLabel(final ActionListener listener) {
		this.setIgnoreRepaint(true);
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				CustomLabel.this.state |= STATE_PRESSED;
				CustomLabel.this.dirty = true;
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				CustomLabel.this.state &= ~STATE_PRESSED;
				listener.actionPerformed(new ActionEvent(this, 0, null));
				CustomLabel.this.dirty = true;
			}
			
			@Override
			public void mouseEntered(final MouseEvent e) {
				CustomLabel.this.state |= STATE_HOVERED;
				CustomLabel.this.dirty = true;
				CustomLabel.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
			}
			
			@Override
			public void mouseExited(final MouseEvent e) {
				CustomLabel.this.state &= ~STATE_HOVERED;
				CustomLabel.this.dirty = true;
				CustomLabel.this.lastMousePosition = null;
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				CustomLabel.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				CustomLabel.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
			}
		});
	}
	
	@Override
	public void update(final Graphics g) {
		//
	}
	
	@Override
	public void paint(final Graphics g) {
		final var g2d = (Graphics2D) g;
		
		if (this.backgroundColor != null) {
			g2d.setColor(this.backgroundColor);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		
		float h, s, b;
		if (this.foregroundColor == null) {
			h = 0.0F;
			s = 0.0F;
			b = 0.80F;
		}
		else {
			final var hsb = Color.RGBtoHSB(this.foregroundColor.getRed(), this.foregroundColor.getGreen(), this.foregroundColor.getBlue(), null);
			h = hsb[0];
			s = hsb[1];
			b = hsb[2];
		}
		
		if (!this.enabled) {
			b -= 0.30F;
		}
		else if ((this.state & STATE_PRESSED) != 0) {
			h = ComponentRenderingSupport.HSB_COMPONENT_SELECTED_0.b();
			s = ComponentRenderingSupport.HSB_COMPONENT_SELECTED_0.s();
			b = ComponentRenderingSupport.HSB_COMPONENT_SELECTED_0.b() + 0.4f;
		}
		else if ((this.state & STATE_HOVERED) != 0) {
			b += 0.15F;
		}
		
		h = Math.max(0.0f, Math.min(1.0f, h));
		s = Math.max(0.0f, Math.min(1.0f, s));
		b = Math.max(0.0f, Math.min(1.0f, b));
		g2d.setColor(Color.getHSBColor(h, s, b));
		
		final var alignment = this.textAlignment == null ? DrawFontRenderingBoTextAlignment.CENTER : this.textAlignment;
		final var textDim = G2DDrawFont.getTextDimensions(this.caption, g2d, this.fontSize, this.scaleFactor);
		switch (alignment) {
			case LEFT: {
				G2DDrawFont.renderText(g2d,
						this.scaleFactor,
						1,
						(int) ((this.getHeight() - (textDim.getY())) / 2 / this.scaleFactor),
						this.fontSize,
						this.caption);
				break;
			}
			
			case CENTER: {
				G2DDrawFont.renderText(g2d,
						textDim,
						this.scaleFactor,
						(int) ((this.getWidth() - (textDim.getX())) / 2 / this.scaleFactor),
						(int) ((this.getHeight() - (textDim.getY())) / 2 / this.scaleFactor),
						this.fontSize,
						this.caption);
				break;
			}
			
			case RIGHT: {
				G2DDrawFont.renderText(g2d,
						textDim,
						this.scaleFactor,
						(int) ((this.getWidth() - textDim.getX()) / this.scaleFactor),
						(int) ((this.getHeight() - (textDim.getY())) / 2 / this.scaleFactor),
						this.fontSize,
						this.caption);
				break;
			}
			
			default:
				break;
		}
	}
	
}
