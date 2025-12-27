package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import de.extio.game_engine.renderer.g2d.G2DMainFrame;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class CustomWindowPanel extends Component {
	
	protected boolean thickBorder;
	
	protected Color color;
	
	protected double scaleFactor;
	
	protected boolean dirty = true;
	
	private final G2DThemeManager themeManager;
	
	public void setThickBorder(final boolean thickBorder) {
		this.thickBorder = thickBorder;
	}
	
	public void setColor(final Color color) {
		this.color = color;
	}
	
	public void setScaleFactor(final double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public CustomWindowPanel(final G2DThemeManager themeManager) {
		this.themeManager = themeManager;
		this.setIgnoreRepaint(true);
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (CustomWindowPanel.this.getParent() instanceof final G2DMainFrame mainFrame) {
					mainFrame.handleMouseReleased(e, ImmutableCoordI2.create(CustomWindowPanel.this.getX(), CustomWindowPanel.this.getY()));
				}
			}
			
			@Override
			public void mousePressed(final MouseEvent e) {
				if (CustomWindowPanel.this.getParent() instanceof final G2DMainFrame mainFrame) {
					mainFrame.handleMousePressed(e, ImmutableCoordI2.create(CustomWindowPanel.this.getX(), CustomWindowPanel.this.getY()));
				}
			}
			
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (CustomWindowPanel.this.getParent() instanceof final G2DMainFrame mainFrame) {
					mainFrame.handleMouseDragged(e, ImmutableCoordI2.create(CustomWindowPanel.this.getX(), CustomWindowPanel.this.getY()));
				}
			}
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				if (CustomWindowPanel.this.getParent() instanceof final G2DMainFrame mainFrame) {
					mainFrame.handleMouseMoved(e, ImmutableCoordI2.create(CustomWindowPanel.this.getX(), CustomWindowPanel.this.getY()));
				}
			}
		});
		
		this.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				if (CustomWindowPanel.this.getParent() instanceof final G2DMainFrame mainFrame) {
					mainFrame.handleWheelMoved(e, ImmutableCoordI2.create(CustomWindowPanel.this.getX(), CustomWindowPanel.this.getY()));
				}
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
		final var patternRenderer = this.themeManager.getPatternRenderer(theme.getPatternRendererName());
		
		if (patternRenderer != null) {
			patternRenderer.drawWindowPanel(g2d,
					0,
					0,
					this.getWidth(),
					this.getHeight(),
					this.thickBorder,
					theme.getBorderInner().toColor(),
					theme.getBorderOuter().toColor(),
					this.color != null ? this.color : theme.getWindowBackground().toColor(),
					this.scaleFactor);
		}
	}
	
}
