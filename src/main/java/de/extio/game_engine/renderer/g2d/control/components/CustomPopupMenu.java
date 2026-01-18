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
import java.util.List;
import java.util.Objects;

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.PopupMenuItem;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public class CustomPopupMenu extends Component {
	
	private static final int DEFAULT_PADDING = 6;
	
	private List<PopupMenuItem> items = List.of();
	
	private int fontSize;
	
	private double scaleFactor;
	
	private boolean enabled;
	
	private Color backgroundColor;
	
	private Color foregroundColor;
	
	private Color selectionColor;
	
	private int rowHeight;
	
	private int padding = DEFAULT_PADDING;
	
	private boolean dirty = true;
	
	private int hoveredIndex = -1;
	
	private CoordI2 lastMousePosition;
	
	private String lastSelectedItemId;
	
	private final ActionListener actionListener;
	
	private final ThemeManager themeManager;
	
	public CustomPopupMenu(final ActionListener listener, final ThemeManager themeManager) {
		this.actionListener = listener;
		this.themeManager = themeManager;
		this.setIgnoreRepaint(true);
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				CustomPopupMenu.this.dirty = true;
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (!CustomPopupMenu.this.enabled) {
					return;
				}
				final var index = CustomPopupMenu.this.getHoveredIndex();
				if (index < 0 || index >= CustomPopupMenu.this.items.size()) {
					return;
				}
				final var item = CustomPopupMenu.this.items.get(index);
				if (item == null || !item.enabled()) {
					return;
				}
				CustomPopupMenu.this.lastSelectedItemId = item.id();
				if (CustomPopupMenu.this.actionListener != null) {
					CustomPopupMenu.this.actionListener.actionPerformed(new ActionEvent(this, 0, item.id()));
				}
				CustomPopupMenu.this.dirty = true;
			}
			
			@Override
			public void mouseEntered(final MouseEvent e) {
				CustomPopupMenu.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				CustomPopupMenu.this.updateHover(e.getY());
			}
			
			@Override
			public void mouseExited(final MouseEvent e) {
				CustomPopupMenu.this.lastMousePosition = null;
				CustomPopupMenu.this.setHoveredIndex(-1);
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				CustomPopupMenu.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				CustomPopupMenu.this.updateHover(e.getY());
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				CustomPopupMenu.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				CustomPopupMenu.this.updateHover(e.getY());
			}
		});
	}
	
	public void setItems(final List<PopupMenuItem> items) {
		if (Objects.equals(this.items, items)) {
			return;
		}
		this.items = items == null ? List.of() : List.copyOf(items);
		this.setHoveredIndex(-1);
		this.dirty = true;
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
			this.setHoveredIndex(-1);
		}
		this.enabled = enabled;
	}
	
	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public void setForegroundColor(final Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
	
	public void setSelectionColor(final Color selectionColor) {
		this.selectionColor = selectionColor;
	}
	
	public void setRowHeight(final int rowHeight) {
		this.rowHeight = rowHeight;
	}
	
	public void setPadding(final int padding) {
		this.padding = Math.max(0, padding);
	}
	
	public String getLastSelectedItemId() {
		return this.lastSelectedItemId;
	}
	
	public CoordI2 getLastMousePosition() {
		return this.lastMousePosition;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	@Override
	public void update(final Graphics g) {
	}
	
	@Override
	public void paint(final Graphics g) {
		if (this.themeManager == null) {
			return;
		}

		final var g2d = (Graphics2D) g;
		final Theme theme = this.themeManager.getCurrentTheme();
		final var patternRenderer = ((G2DThemeManager) this.themeManager).getPatternRenderer(theme.getPatternRendererName());
		
		final var background = this.backgroundColor != null ? this.backgroundColor : theme.getWindowBackground().toColor();
		final var borderColor = theme.getBorderOuter().toColor();
		
		if (patternRenderer != null) {
			patternRenderer.drawDecorativeBorderFilled(g2d, 0, 0, this.getWidth(), this.getHeight(), 2, borderColor, background);
		}
		else {
			g2d.setColor(background);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		
		final var selection = this.selectionColor != null ? this.selectionColor : theme.getSelectionPrimary().toColor();
		final var normalText = this.foregroundColor != null ? this.foregroundColor : theme.getTextNormal().toColor();
		final var disabledText = theme.getTextDisabled().toColor();
		
		final var rowHeight = this.getEffectiveRowHeight(g2d);
		final int paddedWidth = Math.max(1, this.getWidth() - (this.padding * 2));
		
		for (int i = 0; i < this.items.size(); i++) {
			final var item = this.items.get(i);
			if (item == null) {
				continue;
			}
			final var rowY = this.padding + i * rowHeight;
			if (rowY > this.getHeight()) {
				break;
			}
			
			if (i == this.hoveredIndex) {
				g2d.setColor(selection);
				g2d.fillRect(this.padding, rowY, paddedWidth, rowHeight);
			}
			
			if (!item.enabled() || !this.enabled) {
				g2d.setColor(disabledText);
			}
			else if (i == this.hoveredIndex) {
				g2d.setColor(theme.getTextNormal().adjustBrightness(theme.getHoverBrightnessAdjustment()).toColor());
			}
			else {
				g2d.setColor(normalText);
			}
			
			final var text = item.label();
			final var textDim = G2DDrawFont.getTextDimensions(text, g2d, this.fontSize, this.scaleFactor);
			final int textX = this.padding;
			final int textY = rowY + (rowHeight - textDim.getY()) / 2;
			G2DDrawFont.renderText(g2d,
					textDim,
					this.scaleFactor,
					(int) (textX / this.scaleFactor),
					(int) (textY / this.scaleFactor),
					this.fontSize,
					text);
		}
		
		this.dirty = false;
	}
	
	private void updateHover(final int mouseY) {
		if (!this.enabled) {
			this.setHoveredIndex(-1);
			return;
		}
		final var rowHeight = this.getEffectiveRowHeight(null);
		if (rowHeight <= 0) {
			this.setHoveredIndex(-1);
			return;
		}
		final int index = (mouseY - this.padding) / rowHeight;
		if (mouseY < this.padding || index < 0 || index >= this.items.size()) {
			this.setHoveredIndex(-1);
		}
		else {
			this.setHoveredIndex(index);
		}
	}
	
	private int getHoveredIndex() {
		return this.hoveredIndex;
	}
	
	private void setHoveredIndex(final int index) {
		if (this.hoveredIndex != index) {
			this.hoveredIndex = index;
			this.dirty = true;
		}
	}
	
	private int getEffectiveRowHeight(final Graphics2D g2d) {
		if (this.rowHeight > 0) {
			return this.rowHeight;
		}
		if (g2d == null) {
			return (int) (Math.max(G2DDrawFont.FONT_SIZE_MIN, this.fontSize) * G2DDrawFont.FONT_LEADING);
		}
		final var textDim = G2DDrawFont.getTextDimensions("M", g2d, this.fontSize, 1.0);
		return (int) (textDim.getY() * G2DDrawFont.FONT_LEADING);
	}
}
