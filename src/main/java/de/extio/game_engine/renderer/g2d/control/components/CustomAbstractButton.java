package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public abstract class CustomAbstractButton extends Component {
	
	protected static final int STATE_NORMAL = 0;
	
	protected static final int STATE_TOGGLED = 1;
	
	protected static final int STATE_PRESSED = 2;
	
	protected static final int STATE_HOVERED = 4;
	
	protected String caption;
	
	protected int state = STATE_NORMAL;
	
	protected int fontSize;
	
	protected final boolean toggle;
	
	protected double scaleFactor;
	
	protected boolean dirty = true;
	
	protected String iconResourceName;
	
	protected String loadedIconResourceName;
	
	protected BufferedImage icon;
	
	protected Color backgroundColor;
	
	protected CoordI2 lastMousePosition;
	
	public void setCaption(final String caption) {
		this.caption = caption;
	}
	
	public void setFontSize(final int fontSize) {
		this.fontSize = fontSize;
	}
	
	public void setScaleFactor(final double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
	
	public boolean isToggled() {
		return (this.state & STATE_TOGGLED) != 0;
	}
	
	public void setToggled(final boolean toggled) {
		if (toggled) {
			this.state |= STATE_TOGGLED;
		}
		else {
			this.state &= ~STATE_TOGGLED;
		}
		
		this.dirty = true;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public void setIconResourceName(final String iconResourceName) {
		this.iconResourceName = iconResourceName;
	}
	
	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public CoordI2 getLastMousePosition() {
		return this.lastMousePosition;
	}
	
	@Override
	public void setEnabled(final boolean b) {
		if (!b) {
			this.lastMousePosition = null;
		}
		super.setEnabled(b);
	}
	
	public CustomAbstractButton(final boolean toggle, final ActionListener listener) {
		this.setIgnoreRepaint(true);
		this.setBackground(Color.BLACK);
		this.toggle = toggle;
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				if (!CustomAbstractButton.this.isEnabled()) {
					return;
				}
				
				CustomAbstractButton.this.state |= STATE_PRESSED;
				CustomAbstractButton.this.dirty = true;
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (!CustomAbstractButton.this.isEnabled()) {
					return;
				}
				
				CustomAbstractButton.this.state &= ~STATE_PRESSED;
				if (CustomAbstractButton.this.toggle) {
					CustomAbstractButton.this.state ^= STATE_TOGGLED;
				}
				CustomAbstractButton.this.dirty = true;
				
				listener.actionPerformed(new ActionEvent(this, CustomAbstractButton.this.state & STATE_TOGGLED, null));
			}
			
			@Override
			public void mouseEntered(final MouseEvent e) {
				if (!CustomAbstractButton.this.isEnabled()) {
					return;
				}
				
				CustomAbstractButton.this.state |= STATE_HOVERED;
				CustomAbstractButton.this.dirty = true;
				CustomAbstractButton.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
			}
			
			@Override
			public void mouseExited(final MouseEvent e) {
				CustomAbstractButton.this.lastMousePosition = null;
				CustomAbstractButton.this.state &= ~STATE_HOVERED;
				CustomAbstractButton.this.dirty = true;
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				CustomAbstractButton.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				CustomAbstractButton.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
			}
		});
	}
	
	@Override
	public void update(final Graphics g) {
		//
	}
	
	public void close() {
		this.flushIcon();
	}
	
	protected void loadIcon() {
		if (this.iconResourceName != null) {
			this.loadedIconResourceName = this.iconResourceName;
			this.flushIcon();
			
			try (InputStream stream = new FileInputStream(this.iconResourceName)) {
				this.icon = ImageIO.read(stream);
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
			
			if (this.icon == null) {
				throw new RuntimeException("Resource not found: " + this.iconResourceName);
			}
		}
	}
	
	protected void flushIcon() {
		if (this.icon != null) {
			this.icon.flush();
			this.icon = null;
		}
	}
	
}
