package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
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
		
		graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
		
		if (this.text.contains("\n")) {
			final var lines = this.text.split("\n", -1);
			final var lineHeight = getTextDimensions("M", graphics, this.size, 1.0).getY();
			
			for (int i = 0; i < lines.length; i++) {
				final var line = lines[i];
				final var yOffset = this.y + i * lineHeight;
				
				switch (this.alignment) {
					case LEFT:
						renderSingleLine(graphics, null, scaleFactor, this.x, yOffset, this.size, line);
						break;
					
					case CENTER: {
						final var textDim = G2DDrawFont.getTextDimensions(line, graphics, this.size, 1.0);
						renderSingleLine(graphics, textDim, scaleFactor, (this.width - textDim.getX()) / 2 + this.x, yOffset, this.size, line);
						break;
					}
					
					case RIGHT: {
						final var textDim = G2DDrawFont.getTextDimensions(line, graphics, this.size, 1.0);
						renderSingleLine(graphics, textDim, scaleFactor, this.width - textDim.getX() + this.x, yOffset, this.size, line);
						break;
					}
					
					default:
						break;
				}
			}
		}
		else {
			switch (this.alignment) {
				case LEFT:
					renderSingleLine(graphics, null, scaleFactor, this.x, this.y, this.size, this.text);
					break;
				
				case CENTER: {
					final var textDim = G2DDrawFont.getTextDimensions(this.text, graphics, this.size, 1.0);
					renderSingleLine(graphics, textDim, scaleFactor, (this.width - textDim.getX()) / 2 + this.x, this.y, this.size, this.text);
					break;
				}
				
				case RIGHT: {
					final var textDim = G2DDrawFont.getTextDimensions(this.text, graphics, this.size, 1.0);
					renderSingleLine(graphics, textDim, scaleFactor, this.width - textDim.getX() + this.x, this.y, this.size, this.text);
					break;
				}
				
				default:
					break;
			}
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
