package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.ComponentRenderingSupport;
import de.extio.game_engine.renderer.model.bo.DrawEffectRenderingBo;
import de.extio.game_engine.renderer.model.DrawEffectRenderingBoEffects;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.RgbaColor;
import de.extio.game_engine.spatial2.WorldUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordD2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordD2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordD2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

public class G2DDrawEffect extends G2DAbstractRenderingBo implements DrawEffectRenderingBo {
	
	private DrawEffectRenderingBoEffects effect;
	
	private List<CoordI2> relativeCoordinates;
	
	private int customInt0;
	
	private double customDouble0;
	
	private String customString0;
	
	public G2DDrawEffect() {
		super(RenderingBoLayer.UI0_2);
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		if (this.effect == null) {
			return;
		}
		
		int x0 = (int) (this.x * scaleFactor);
		int y0 = (int) (this.y * scaleFactor);
		
		List<CoordI2> coordinates = null;
		if (this.relativeCoordinates != null && !this.relativeCoordinates.isEmpty()) {
			coordinates = new ArrayList<>(this.relativeCoordinates.size());
			for (final CoordI2 relativeCoord : this.relativeCoordinates) {
				coordinates.add(ImmutableCoordI2.create((int) (relativeCoord.getX() * scaleFactor), (int) (relativeCoord.getY() * scaleFactor)));
			}
		}
		
		switch (this.effect) {
			case LINE: {
				if (coordinates == null || coordinates.isEmpty()) {
					break;
				}
				final int x1 = x0 + coordinates.get(0).getX();
				final int y1 = y0 + coordinates.get(0).getY();
				
				final Area2 area = WorldUtils2.pointsToArea(ImmutableCoordI2.create(x0, y0), ImmutableCoordI2.create(x1, y1));
				if (this.cull(area)) {
					break;
				}
				
				graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
				graphics.drawLine(x0, y0, x1, y1);
				
				break;
			}
			
			case RECT:
			case RECT_FILLED: {
				if (coordinates == null || coordinates.isEmpty()) {
					break;
				}
				final int x1 = x0 + coordinates.get(0).getX();
				final int y1 = y0 + coordinates.get(0).getY();
				
				final Area2 area = WorldUtils2.pointsToArea(ImmutableCoordI2.create(x0, y0), ImmutableCoordI2.create(x1, y1));
				if (this.cull(area)) {
					break;
				}
				
				graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
				if (this.effect == DrawEffectRenderingBoEffects.RECT) {
					graphics.drawRect(area.getPosition().getX(), area.getPosition().getY(), area.getDimension().getX(), area.getDimension().getY());
				}
				else {
					graphics.fillRect(area.getPosition().getX(), area.getPosition().getY(), area.getDimension().getX(), area.getDimension().getY());
				}
				
				break;
			}
			
			case CIRCLE:
			case CIRCLE_FILLED: {
				final int diameter = Math.max(3, (int) Math.round(this.customInt0 * scaleFactor));
				
				final Area2 area = new Area2(ImmutableCoordI2.create(x0, y0), ImmutableCoordI2.create(diameter, diameter));
				if (this.cull(area)) {
					break;
				}
				
				graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
				if (this.effect == DrawEffectRenderingBoEffects.CIRCLE) {
					graphics.drawOval(x0, y0, diameter, diameter);
				}
				else {
					graphics.fillOval(x0, y0, diameter, diameter);
				}
				
				break;
			}
			
			case CIRCLE_CENTERED:
			case CIRCLE_CENTERED_FILLED: {
				final int radius = Math.max(3, (int) Math.round(this.customInt0 * scaleFactor));
				
				final Area2 area = new Area2(ImmutableCoordI2.create(x0, y0).substract(radius), ImmutableCoordI2.zero().add(radius * 2));
				if (this.cull(area)) {
					break;
				}
				
				graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
				if (this.effect == DrawEffectRenderingBoEffects.CIRCLE_CENTERED) {
					graphics.drawOval(x0 - radius, y0 - radius, radius * 2, radius * 2);
				}
				else {
					graphics.fillOval(x0 - radius, y0 - radius, radius * 2, radius * 2);
				}
				
				break;
			}
			
			case ARROW: {
				if (coordinates == null || coordinates.isEmpty()) {
					break;
				}
				final int x1 = x0 + coordinates.get(0).getX();
				final int y1 = y0 + coordinates.get(0).getY();
				
				final double width = Math.max(1, Math.max(Math.abs(x0 - x1), Math.abs(y0 - y1)) / 5);
				final double height = width;
				final int dx = x1 - x0;
				final int dy = y1 - y0;
				final double D = Math.max(1.0, Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0)));
				double xm = D - width;
				double xn = xm;
				double ym = height;
				double yn = -height;
				double x;
				final double sin = dy / D;
				final double cos = dx / D;
				
				x = xm * cos - ym * sin + x0;
				ym = xm * sin + ym * cos + y0;
				xm = x;
				
				x = xn * cos - yn * sin + x0;
				yn = xn * sin + yn * cos + y0;
				xn = x;
				
				final Area2 area = WorldUtils2.pointsToArea(ImmutableCoordI2.create(x0, y0), ImmutableCoordI2.create(x1, y1), ImmutableCoordI2.create((int) xm, (int) ym), ImmutableCoordI2.create((int) xn, (int) yn));
				if (this.cull(area)) {
					break;
				}
				
				graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
				graphics.drawLine(x0, y0, x1, y1);
				graphics.drawLine(x1, y1, (int) xm, (int) ym);
				graphics.drawLine(x1, y1, (int) xn, (int) yn);
				
				break;
			}
			
			case CONE: {
				if (coordinates == null || coordinates.size() < 2) {
					break;
				}
				final int x1 = x0 + coordinates.get(0).getX();
				final int y1 = y0 + coordinates.get(0).getY();
				final int x2 = x0 + coordinates.get(1).getX();
				final int y2 = y0 + coordinates.get(1).getY();
				
				final Area2 area = WorldUtils2.pointsToArea(ImmutableCoordI2.create(x0, y0), ImmutableCoordI2.create(x1, y1), ImmutableCoordI2.create(x2, y2));
				if (this.cull(area)) {
					break;
				}
				
				graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
				graphics.drawLine(x0, y0, x1, y1);
				graphics.drawLine(x0, y0, x2, y2);
				
				break;
			}
			
			case DECORATIVE_BORDER:
			case DECORATIVE_BORDER_FILLED: {
				if (coordinates == null || coordinates.isEmpty()) {
					break;
				}
				final int x1 = x0 + coordinates.get(0).getX();
				final int y1 = y0 + coordinates.get(0).getY();
				
				final Area2 area = WorldUtils2.pointsToArea(ImmutableCoordI2.create(x0, y0), ImmutableCoordI2.create(x1, y1));
				if (this.cull(area)) {
					break;
				}
				
				final int strength = this.customInt0 <= 0 ? 2 : this.customInt0;
				
				if (this.effect == DrawEffectRenderingBoEffects.DECORATIVE_BORDER) {
					ComponentRenderingSupport.drawDecorativeBorder(graphics, area.getPosition().getX(), area.getPosition().getY(), area.getDimension().getX(), area.getDimension().getY(), strength, this.color == null ? Color.WHITE : this.color.toAwtColor());
				}
				else {
					ComponentRenderingSupport.drawDecorativeBorderFilled(graphics, area.getPosition().getX(), area.getPosition().getY(), area.getDimension().getX(), area.getDimension().getY(), strength, this.color == null ? Color.WHITE : this.color.toAwtColor());
				}
				
				break;
			}
			
			case TEXT: {
				if (this.customString0 == null || this.customString0.isEmpty()) {
					break;
				}
				graphics.setColor(this.color.toAwtColor());
				G2DDrawFont.renderText(graphics, 1.0, x0, y0, (int) (this.customInt0 * scaleFactor), this.customString0);
				break;
			}
			
			default:
				throw new UnsupportedOperationException("Effect not implemented: " + this.effect.toString());
		}
	}
	
	private boolean cull(final Area2 area) {
		final Frame mainFrame = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame();
		return !WorldUtils2.intersects(area, new Area2(ImmutableCoordI2.zero(), MutableCoordI2.create(mainFrame.getWidth(), mainFrame.getHeight())));
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		
		this.relativeCoordinates = null;
		this.effect = null;
		this.customInt0 = 0;
		this.customDouble0 = 0.0;
		this.customString0 = null;
	}
	
	@Override
	public DrawEffectRenderingBo setEffect(final DrawEffectRenderingBoEffects effect) {
		this.effect = effect;
		return this;
	}
	
	@Override
	public DrawEffectRenderingBo setCustomInt0(final int customInt0) {
		this.customInt0 = customInt0;
		return this;
	}
	
	@Override
	public DrawEffectRenderingBo setCustomDouble0(final double customDouble0) {
		this.customDouble0 = customDouble0;
		return this;
	}
	
	@Override
	public DrawEffectRenderingBo setCustomString0(final String customString0) {
		this.customString0 = customString0;
		return this;
	}
	
	@Override
	public List<CoordI2> getRelativeCoordinates() {
		return this.relativeCoordinates;
	}
	
	@Override
	public DrawEffectRenderingBo setRelativeCoordinates(final List<CoordI2> polarCoordinates) {
		this.relativeCoordinates = polarCoordinates;
		return this;
	}
	
}
