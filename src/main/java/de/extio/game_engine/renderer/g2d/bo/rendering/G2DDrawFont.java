package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordD2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

@Conditional(G2DRendererCondition.class)
@Component
public class G2DDrawFont extends G2DAbstractRenderingBo implements DrawFontRenderingBo {
	
	private final static float FONT_SIZE_MIN = 12.0F;
	
	private final static int FONT_SIZE_DEFAULT = 14;
	
	private static Font baseFont;
	
	static {
		final Map<TextAttribute, Object> attributes = new HashMap<>();
		attributes.put(TextAttribute.FAMILY, Font.SANS_SERIF);
		attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
		baseFont = Font.getFont(attributes);
	}
	
	public static void updateDefaultFont(final StaticResourceService staticResourceService, final StaticResource resource) {
		staticResourceService.loadStreamByPath(resource).ifPresent(stream -> {
			try (var in = stream) {
				final var font = Font.createFont(Font.TRUETYPE_FONT, in);
				G2DDrawFont.baseFont = font;
			}
			catch (final Exception e) {
				LOGGER.warn(e.getMessage(), e);
			}
		});
	}
	
	private String text;
	
	private int size;
	
	private HorizontalAlignment alignment = HorizontalAlignment.LEFT;
	
	public G2DDrawFont() {
		super(RenderingBoLayer.UI0);
	}
	
	@Override
	public DrawFontRenderingBo setText(final String text) {
		this.text = text;
		return this;
	}
	
	@Override
	public DrawFontRenderingBo setSize(final int size) {
		this.size = size;
		return this;
	}
	
	@Override
	public DrawFontRenderingBo setAlignment(final HorizontalAlignment alignment) {
		this.alignment = alignment;
		return this;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		if (this.text == null || this.text.isEmpty()) {
			return;
		}
		
		graphics.setColor(this.color == null ? this.rendererData.getThemeManager().getCurrentTheme().getTextNormal().toColor() : this.color.toAwtColor());
		
		final var lines = this.text.contains("\n") ? this.text.split("\n", -1) : new String[] { this.text };
		final var lineHeight = getTextDimensions("M", graphics, this.size, 1.0).getY();
		final CoordI2[] lineDims = new CoordI2[lines.length];
		int maxLineWidth = 0;
		for (int i = 0; i < lines.length; i++) {
			lineDims[i] = G2DDrawFont.getTextDimensions(lines[i], graphics, this.size, 1.0);
			if (lineDims[i].getX() > maxLineWidth) {
				maxLineWidth = lineDims[i].getX();
			}
		}
		
		Shape oldClip = null;
		Area2 intersection = null;
		if (visibleAreaX != 0 || visibleAreaY != 0 || visibleAreaWidth != 0 || visibleAreaHeight != 0) {
			final int blockX = (int) (this.x * scaleFactor);
			final int blockY = (int) (this.y * scaleFactor);
			final int blockW = this.width > 0 ? (int) (this.width * scaleFactor) : (int) Math.round(maxLineWidth * scaleFactor);
			final int blockH = (int) Math.round(lineHeight * lines.length * scaleFactor);
			final var controlArea = new Area2(ImmutableCoordI2.create(blockX, blockY), ImmutableCoordI2.create(blockW, blockH));
			final var visibleArea = new Area2(ImmutableCoordI2.create((int) (this.visibleAreaX * scaleFactor), (int) (this.visibleAreaY * scaleFactor)), ImmutableCoordI2.create((int) (this.visibleAreaWidth * scaleFactor), (int) (this.visibleAreaHeight * scaleFactor)));
			intersection = SpatialUtils2.intersectAreas(controlArea, visibleArea);
			if (intersection == null) {
				return;
			}
			oldClip = graphics.getClip();
			graphics.setClip(intersection.getPosition().getX(), intersection.getPosition().getY(), intersection.getDimension().getX(), intersection.getDimension().getY());
		}
		
		try {
			for (int i = 0; i < lines.length; i++) {
				final var line = lines[i];
				final var yOffset = this.y + i * lineHeight;
				
				final var textDimScaled = ImmutableCoordI2.create((int)(lineDims[i].getX() * scaleFactor), (int)(lineDims[i].getY() * scaleFactor));
				switch (this.alignment) {
					case LEFT: {
						renderSingleLine(graphics, textDimScaled, scaleFactor, this.x, yOffset, this.size, line);
						break;
					}
					
					case CENTER: {
						renderSingleLine(graphics, textDimScaled, scaleFactor, (this.width - lineDims[i].getX()) / 2 + this.x, yOffset, this.size, line);
						break;
					}
					
					case RIGHT: {
						renderSingleLine(graphics, textDimScaled, scaleFactor, this.width - lineDims[i].getX() + this.x, yOffset, this.size, line);
						break;
					}
					
					default:
						break;
				}
			}
		}
		finally {
			graphics.setClip(oldClip);
		}
	}
	
	@Override
	public void apply(final RenderingBo other) {
		super.apply(other);
		
		if (other instanceof final G2DDrawFont o) {
			this.text = o.text;
			this.size = o.size;
			this.alignment = o.alignment;
		}
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		this.text = null;
		this.size = 0;
		this.alignment = HorizontalAlignment.LEFT;
	}
	
	public static Font getFont(final double scaleFactor, final int size_) {
		final var size = size_ == 0 ? FONT_SIZE_DEFAULT : size_;
		final var sizeScaled = Math.max(FONT_SIZE_MIN, (float) (size * scaleFactor));
		return baseFont.deriveFont(sizeScaled);
	}
	
	public static void renderText(final Graphics graphics, final double scaleFactor, final int x, final int y, final int size_, final String text) {
		renderText(graphics, null, scaleFactor, x, y, size_, text);
	}
	
	public static void renderText(final Graphics graphics, final CoordI2 textDim_, final double scaleFactor, final int x, final int y, final int size_, final String text) {
		if (text == null) {
			return;
		}
		
		if (text.contains("\n")) {
			final var lines = text.split("\n", -1);
			final var lineHeight = getTextDimensions("M", graphics, size_, scaleFactor).getY();
			
			for (int i = 0; i < lines.length; i++) {
				renderText(graphics, null, scaleFactor, x, y + i * lineHeight, size_, lines[i]);
			}
			return;
		}
		renderSingleLine(graphics, textDim_, scaleFactor, x, y, size_, text);
	}
	
	private static void renderSingleLine(final Graphics graphics, final CoordI2 textDim_, final double scaleFactor, final int x, final int y, final int size_, final String text) {
		if (text == null) {
			return;
		}
		
		final var size = size_ == 0 ? FONT_SIZE_DEFAULT : size_;
		final var sizeScaled = Math.max(FONT_SIZE_MIN, (float) (size * scaleFactor));
		final var font = baseFont.deriveFont(sizeScaled);
		CoordI2 textDim;
		if (textDim_ == null) {
			textDim = getTextDimensions(text, graphics, size_, scaleFactor);
		}
		else {
			textDim = textDim_;
		}
		graphics.setFont(font);
		graphics.drawString(text, (int) (x * scaleFactor), (int) (y * scaleFactor) + textDim.getY());
	}
	
	public static CoordI2 getTextDimensions(final String text, final Graphics graphics, final int size_, final double scaleFactor) {
		if (text == null) {
			return ImmutableCoordI2.one();
		}
		
		final var size = size_ == 0 ? FONT_SIZE_DEFAULT : size_;
		final var sizeScaled = Math.max(FONT_SIZE_MIN, (float) (size * scaleFactor));
		final var font = baseFont.deriveFont(sizeScaled);
		final var gv = font.layoutGlyphVector(((Graphics2D) graphics).getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
		final var pixBounds = gv.getPixelBounds(((Graphics2D) graphics).getFontRenderContext(), 0, 0);
		return MutableCoordI2.create(pixBounds.x + pixBounds.width, pixBounds.height - (int) pixBounds.getMaxY());
	}
	
}
