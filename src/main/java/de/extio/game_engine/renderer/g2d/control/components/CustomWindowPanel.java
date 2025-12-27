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
import de.extio.game_engine.renderer.model.color.ImmutableRgbaColor;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class CustomWindowPanel extends Component {
	
	private static final RgbaColor COLOR_WINDOW = new ImmutableRgbaColor(ComponentRenderingSupport.COLOR_COMPONENT_BORDER1.getRed(), ComponentRenderingSupport.COLOR_COMPONENT_BORDER1.getGreen(), ComponentRenderingSupport.COLOR_COMPONENT_BORDER1.getBlue());
	
	protected boolean thickBorder;
	
	protected Color color;
	
	protected double scaleFactor;
	
	protected boolean dirty = true;
	
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
	
	public CustomWindowPanel() {
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
		final var g2d = (Graphics2D) g;
		
		final var strength = Math.max(2, (int) (3 * this.scaleFactor));
		final var effectiveColor = this.color != null ? this.color : COLOR_WINDOW.toAwtColor();
		
		final var cMain = effectiveColor;
		final var rgbaDarker = new Color(effectiveColor.getRed() / 2, effectiveColor.getGreen() / 2, effectiveColor.getBlue() / 2);
		
		if (this.thickBorder) {
			final var borderStrength = strength * 2;
			g2d.setColor(cMain);
			g2d.fillRect(strength * 2, strength * 2, this.getWidth() - strength * 4, borderStrength);
			g2d.setColor(rgbaDarker);
			g2d.fillRect(strength * 2, strength * 2 + borderStrength, this.getWidth() - strength * 4, strength);
		}
		
		ComponentRenderingSupport.drawDecorativeBorder(g2d, 0, 0, this.getWidth(), this.getHeight(), strength, rgbaDarker);
		ComponentRenderingSupport.drawDecorativeBorderFilled(g2d, strength, strength, this.getWidth() - strength * 2, this.getHeight() - strength * 2, strength, cMain);
	}
	
}
