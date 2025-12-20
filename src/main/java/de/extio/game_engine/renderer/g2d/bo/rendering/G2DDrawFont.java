package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.DrawFontRenderingBoTextAlignment;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

public class G2DDrawFont extends G2DAbstractRenderingBo implements DrawFontRenderingBo {
	
	private final static float FONT_SIZE_MIN = 12.0F;
	
	private final static int FONT_SIZE_DEFAULT = 14;
	
	private final static Font baseFont;
	
	static {
		Font f;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemClassLoader().getResourceAsStream("Roboto-Regular.ttf"));
		}
		catch (final Exception e) {
			final Map<TextAttribute, Object> attributes = new HashMap<>();
			attributes.put(TextAttribute.FAMILY, Font.SANS_SERIF);
			attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
			f = Font.getFont(attributes);
		}
		baseFont = f;
	}
	
	private String text;
	
	private int size;
	
	private DrawFontRenderingBoTextAlignment alignment = DrawFontRenderingBoTextAlignment.LEFT;
	
	public G2DDrawFont() {
		super(RenderingBoLayer.UI0_0);
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
	public DrawFontRenderingBo setAlignment(final DrawFontRenderingBoTextAlignment alignment) {
		this.alignment = alignment;
		return this;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		if (this.text == null || this.text.isEmpty()) {
			return;
		}
		
		graphics.setColor(this.color == null ? Color.WHITE : this.color.toAwtColor());
		
		switch (this.alignment) {
			case LEFT:
				renderText(graphics, scaleFactor, this.x, this.y, this.size, this.text);
				break;
			
			case CENTER: {
				final CoordI2 textDim = G2DDrawFont.getTextDimensions(this.text, graphics, this.size, 1.0);
				renderText(graphics, textDim, scaleFactor, (this.width - textDim.getX()) / 2 + this.x, this.y, this.size, this.text);
				break;
			}
			
			case RIGHT: {
				final CoordI2 textDim = G2DDrawFont.getTextDimensions(this.text, graphics, this.size, 1.0);
				renderText(graphics, textDim, scaleFactor, this.width - textDim.getX() + this.x, this.y, this.size, this.text);
				break;
			}
			
			default:
				break;
		}
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		this.text = null;
		this.size = 0;
		this.alignment = DrawFontRenderingBoTextAlignment.LEFT;
	}
	
	public static Font getFont(final double scaleFactor, final int size_) {
		final int size = size_ == 0 ? FONT_SIZE_DEFAULT : size_;
		final float sizeScaled = Math.max(FONT_SIZE_MIN, (float) (size * scaleFactor));
		return baseFont.deriveFont(sizeScaled);
	}
	
	public static void renderText(final Graphics graphics, final double scaleFactor, final int x, final int y, final int size_, final String text) {
		renderText(graphics, null, scaleFactor, x, y, size_, text);
	}
	
	public static void renderText(final Graphics graphics, final CoordI2 textDim_, final double scaleFactor, final int x, final int y, final int size_, final String text) {
		if (text == null) {
			return;
		}
		
		final int size = size_ == 0 ? FONT_SIZE_DEFAULT : size_;
		final float sizeScaled = Math.max(FONT_SIZE_MIN, (float) (size * scaleFactor));
		final Font font = baseFont.deriveFont(sizeScaled);
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
		
		final int size = size_ == 0 ? FONT_SIZE_DEFAULT : size_;
		final float sizeScaled = Math.max(FONT_SIZE_MIN, (float) (size * scaleFactor));
		final Font font = baseFont.deriveFont(sizeScaled);
		final GlyphVector gv = font.layoutGlyphVector(((Graphics2D) graphics).getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
		final Rectangle pixBounds = gv.getPixelBounds(((Graphics2D) graphics).getFontRenderContext(), 0, 0);
		return MutableCoordI2.create(pixBounds.x + pixBounds.width, pixBounds.height - (int) pixBounds.getMaxY());
	}
	
}
