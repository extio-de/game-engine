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
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public abstract class CustomAbstractButton extends Component {
	
	private static final int MAX_ICON_CACHE_SIZE = 10;
	
	private final Map<String, BufferedImage> iconCache = new LinkedHashMap<>(MAX_ICON_CACHE_SIZE, 0.75f, true) {
		
		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, BufferedImage> eldest) {
			final boolean shouldRemove = this.size() > MAX_ICON_CACHE_SIZE;
			if (shouldRemove && eldest.getValue() != null) {
				eldest.getValue().flush();
			}
			return shouldRemove;
		}
	};
	
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
	
	protected StaticResource iconResource;
	
	protected StaticResource loadedIconResource;
	
	protected BufferedImage icon;
	
	protected Color backgroundColor;
	
	protected StaticResourceService staticResourceService;
	
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
	
	public void setIconResource(final StaticResource iconResource) {
		this.iconResource = iconResource;
	}
	
	public void setStaticResourceService(final StaticResourceService staticResourceService) {
		this.staticResourceService = staticResourceService;
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
		this.icon = null;
		for (final var cachedIcon : this.iconCache.values()) {
			if (cachedIcon != null) {
				cachedIcon.flush();
			}
		}
		this.iconCache.clear();
	}
	
	@SuppressWarnings("resource")
	protected void loadIcon() {
		if (this.iconResource != null && this.staticResourceService != null) {
			this.loadedIconResource = this.iconResource;
			this.icon = null;
			
			final String iconKey = this.iconResource.toString();
			
			this.icon = this.iconCache.get(iconKey);
			
			if (this.icon == null) {
				final var in = this.staticResourceService.loadStreamByPath(this.iconResource);
				if (in.isPresent()) {
					try (var stream = in.get()) {
						if (stream != null) {
							this.icon = ImageIO.read(stream);
						}
					}
					catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}
				
				if (this.icon == null) {
					throw new RuntimeException("Resource not found: " + this.iconResource);
				}
				
				this.iconCache.put(iconKey, this.icon);
			}
		}
	}
	
}
