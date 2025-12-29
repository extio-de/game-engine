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

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public class CustomSlider extends Component {
	
	protected static final int STATE_NORMAL = 0;
	
	protected static final int STATE_HOVERED = 4;
	
	protected int state = STATE_NORMAL;
	
	protected double scaleFactor;
	
	protected boolean enabled;
	
	protected Color color;
	
	protected boolean horizontal;
	
	protected double value;
	
	private double value2;
	
	protected boolean dirty = true;
	
	private CoordI2 lastMousePosition;
	
	private final ActionListener actionListener;
	
	private ActionEvent lastActionEvent;
	
	private long lastActionEventReleased;
	
	private final ThemeManager themeManager;
	
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
	
	public void setColor(final Color color) {
		this.color = color;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public void setHorizontal(final boolean horizontal) {
		this.horizontal = horizontal;
	}
	
	public void setValue(final double value) {
		this.value = value;
	}
	
	public double getValue() {
		return this.value;
	}
	
	public double getValue2() {
		return this.value2;
	}
	
	public void setValue2(final double value2) {
		this.value2 = value2;
	}
	
	public CoordI2 getLastMousePosition() {
		return this.lastMousePosition;
	}
	
	public CustomSlider(final ActionListener listener, final ThemeManager themeManager) {
		this.setIgnoreRepaint(true);
		this.actionListener = listener;
		this.themeManager = themeManager;
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				CustomSlider.this.dirty = true;
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				CustomSlider.this.dirty = true;
				CustomSlider.this.action(e);
			}
			
			@Override
			public void mouseEntered(final MouseEvent e) {
				CustomSlider.this.state |= STATE_HOVERED;
				CustomSlider.this.dirty = true;
				CustomSlider.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
			}
			
			@Override
			public void mouseExited(final MouseEvent e) {
				CustomSlider.this.state &= ~STATE_HOVERED;
				CustomSlider.this.dirty = true;
				CustomSlider.this.lastMousePosition = null;
			}
			
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				CustomSlider.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				CustomSlider.this.dirty = true;
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				CustomSlider.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				CustomSlider.this.dirty = true;
				CustomSlider.this.action(e);
			}
		});
	}
	
	private void action(final MouseEvent e) {
		var value = CustomSlider.this.horizontal ? ((double) e.getX() / (double) CustomSlider.this.getWidth()) : (1.0 - ((double) e.getY() / (double) CustomSlider.this.getHeight()));
		value = Math.min(1.0, Math.max(0.0, value));
		CustomSlider.this.setValue(value);
		synchronized (this) {
			this.lastActionEvent = new ActionEvent(this, 0, Double.toString(value));
		}
	}
	
	public void releaseEvents() {
		synchronized (this) {
			if (this.lastActionEvent != null && Math.abs(System.currentTimeMillis() - this.lastActionEventReleased) >= 33) {
				this.lastActionEventReleased = System.currentTimeMillis();
				this.actionListener.actionPerformed(this.lastActionEvent);
				this.lastActionEvent = null;
			}
		}
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
		
		float h, s, b;
		if (this.color == null) {
			if (!this.enabled) {
				h = theme.getTextDisabled().getHue();
				s = theme.getTextDisabled().getSaturation();
				b = theme.getTextDisabled().getBrightness();
			}
			else {
				h = theme.getTextNormal().getHue();
				s = theme.getTextNormal().getSaturation();
				b = theme.getTextNormal().getBrightness();
			}
		}
		else {
			final var hsb = Color.RGBtoHSB(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), null);
			h = hsb[0];
			s = hsb[1];
			b = hsb[2];
			if (!this.enabled) {
				b -= 0.30F;
			}
		}
		
		// Main line
		if (this.horizontal) {
			this.drawLine(g2d, h, s, b, 0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2, this.horizontal);
		}
		else {
			this.drawLine(g2d, h, s, b, this.getWidth() / 2, 0, this.getWidth() / 2, this.getHeight(), this.horizontal);
		}
		
		// Small lines
		b -= 0.20F;
		final int length = this.horizontal ? this.getWidth() : this.getHeight();
		for (int i = 1; i < 10; i++) {
			final int pos = (int) ((double) length / 10.0 * i);
			if (this.horizontal) {
				this.drawLine(g2d, h, s, b, pos, this.getHeight() / 4, pos, this.getHeight() / 4 * 3, !this.horizontal);
			}
			else {
				this.drawLine(g2d, h, s, b, this.getWidth() / 4, pos, this.getWidth() / 4 * 3, pos, !this.horizontal);
			}
		}
		b += 0.20F;
		
		// Pointer
		if ((this.state & STATE_HOVERED) != 0 && this.lastMousePosition != null) {
			try {
				if (this.horizontal) {
					this.drawLine(g2d, h, s, b, this.lastMousePosition.getX(), 0, this.lastMousePosition.getX(), this.getHeight(), !this.horizontal);
				}
				else {
					this.drawLine(g2d, h, s, b, 0, this.lastMousePosition.getY(), this.getWidth(), this.lastMousePosition.getY(), !this.horizontal);
				}
			}
			catch (final NullPointerException exc) {
				// AWT MouseAdapter can set this.lastMousePosition null at any time 
			}
		}
		
		// Value2
		if (this.value2 != this.value) {
			if (this.horizontal) {
				this.drawLine(g2d, h, s, b, (int) (this.getWidth() * this.value2), 0, (int) (this.getWidth() * this.value2), this.getHeight(), !this.horizontal);
			}
			else {
				this.drawLine(g2d, h, s, b, 0, (int) (this.getHeight() * (1.0 - this.value2)), this.getWidth(), (int) (this.getHeight() * (1.0 - this.value2)), !this.horizontal);
			}
		}
		
		// Value
		b += 0.15F;
		if (this.horizontal) {
			this.drawLine(g2d, h, s, b, (int) (this.getWidth() * this.value), 0, (int) (this.getWidth() * this.value), this.getHeight(), !this.horizontal);
		}
		else {
			this.drawLine(g2d, h, s, b, 0, (int) (this.getHeight() * (1.0 - this.value)), this.getWidth(), (int) (this.getHeight() * (1.0 - this.value)), !this.horizontal);
		}
	}
	
	private void applyColor(final Graphics2D g2d, float h, float s, float b) {
		h = Math.max(0.0f, Math.min(1.0f, h));
		s = Math.max(0.0f, Math.min(1.0f, s));
		b = Math.max(0.0f, Math.min(1.0f, b));
		g2d.setColor(Color.getHSBColor(h, s, b));
	}
	
	private void drawLine(final Graphics2D g2d, final float h, float s, float b, int x, int y, int x2, int y2, final boolean horizontal) {
		x = Math.max(1, x);
		x = Math.min(this.getWidth() - 2, x);
		y = Math.max(1, y);
		y = Math.min(this.getHeight() - 2, y);
		x2 = Math.max(1, x2);
		x2 = Math.min(this.getWidth() - 2, x2);
		y2 = Math.max(1, y2);
		y2 = Math.min(this.getHeight() - 2, y2);
		
		this.applyColor(g2d, h, s, b);
		g2d.drawLine(x, y, x2, y2);
		
		s -= 0.2F;
		b -= 0.2F;
		this.applyColor(g2d, h, s, b);
		if (horizontal) {
			g2d.drawLine(x, y - 1, x2, y2 - 1);
			g2d.drawLine(x, y + 1, x2, y2 + 1);
		}
		else {
			g2d.drawLine(x - 1, y, x2 - 1, y2);
			g2d.drawLine(x + 1, y, x2 + 1, y2);
		}
	}
	
}
