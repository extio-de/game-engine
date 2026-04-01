package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
import de.extio.game_engine.spatial2.model.MutableCoordI2;

public class G2DDrawFont extends G2DAbstractRenderingBo implements DrawFontRenderingBo {
	
	public final static float FONT_SIZE_MIN = 12.0F;
	
	public final static int FONT_SIZE_DEFAULT = 18;
	
	public final static double FONT_LEADING = 1.75;
	
	private static final AtomicReference<Font> cachedFontRef = new AtomicReference<>();
	
	private static final AtomicReference<Font> cachedFallbackFontRef = new AtomicReference<>();
	
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
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
				G2DDrawFont.baseFont = font;
				G2DDrawFont.cachedFontRef.set(null);
				G2DDrawFont.cachedFallbackFontRef.set(null);
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
		final var lineHeight = (int) (getTextDimensions("M", graphics, this.size, 1.0).getY() * FONT_LEADING); //+ (int) (Math.max(FONT_SIZE_MIN, this.size) * 0.5);
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
				
				final var textDimScaled = ImmutableCoordI2.create((int) (lineDims[i].getX() * scaleFactor), (int) (lineDims[i].getY() * scaleFactor));
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
		return cachedFontRef.updateAndGet(existingFont -> {
			if (existingFont == null || existingFont.getSize2D() != sizeScaled) {
				return baseFont.deriveFont(sizeScaled);
			}
			return existingFont;
		});
	}
	
	private static Font getFallbackFont(final double scaleFactor, final int size_) {
		final var size = size_ == 0 ? FONT_SIZE_DEFAULT : size_;
		final var sizeScaled = Math.max(FONT_SIZE_MIN, (float) (size * scaleFactor));
		return cachedFallbackFontRef.updateAndGet(existingFont -> {
			if (existingFont == null || existingFont.getSize2D() != sizeScaled) {
				return new Font(Font.DIALOG, baseFont.getStyle(), Math.max(1, Math.round(sizeScaled)));
			}
			return existingFont;
		});
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
			final var lineHeight = (int) (getTextDimensions("M", graphics, size_, scaleFactor).getY() * FONT_LEADING);
			
			for (int i = 0; i < lines.length; i++) {
				renderText(graphics, null, scaleFactor, x, y + i * lineHeight, size_, lines[i]);
			}
			return;
		}
		renderSingleLine(graphics, textDim_, scaleFactor, x, y, size_, text);
	}
	
	private static void renderSingleLine(final Graphics graphics, final CoordI2 textDim_, final double scaleFactor, final int x, final int y, final int size_, final String text) {
		if (text == null || text.isEmpty()) {
			return;
		}
		
		CoordI2 textDim;
		if (textDim_ == null) {
			textDim = getTextDimensions(text, graphics, size_, scaleFactor);
		}
		else {
			textDim = textDim_;
		}
		final var textLayout = createTextLayout(text, graphics, size_, scaleFactor);
		textLayout.draw((Graphics2D) graphics, (float) (x * scaleFactor), (float) ((y * scaleFactor) + textDim.getY()));
	}
	
	public static CoordI2 getTextDimensions(final String text, final Graphics graphics, final int size_, final double scaleFactor) {
		if (text == null) {
			return ImmutableCoordI2.one();
		}
		
		if (text.isEmpty()) {
			final var fontMetrics = graphics.getFontMetrics(getFont(scaleFactor, size_));
			return MutableCoordI2.create(0, fontMetrics.getAscent());
		}
		
		final var textLayout = createTextLayout(text, graphics, size_, scaleFactor);
		final var pixBounds = textLayout.getPixelBounds(((Graphics2D) graphics).getFontRenderContext(), 0, 0);
		return MutableCoordI2.create(pixBounds.x + pixBounds.width, pixBounds.height - (int) pixBounds.getMaxY());
	}
	
	private static TextLayout createTextLayout(final String text, final Graphics graphics, final int size_, final double scaleFactor) {
		final var attributedString = createAttributedString(text, size_, scaleFactor);
		return new TextLayout(attributedString.getIterator(), ((Graphics2D) graphics).getFontRenderContext());
	}
	
	private static AttributedString createAttributedString(final String text, final int size_, final double scaleFactor) {
		final var primaryFont = getFont(scaleFactor, size_);
		final var fallbackFont = getFallbackFont(scaleFactor, size_);
		final var attributedString = new AttributedString(text);
		var index = 0;
		while (index < text.length()) {
			final var codePoint = text.codePointAt(index);
			final var runFont = primaryFont.canDisplay(codePoint) ? primaryFont : fallbackFont;
			final var runStart = index;
			index += Character.charCount(codePoint);
			while (index < text.length()) {
				final var nextCodePoint = text.codePointAt(index);
				final var nextFont = primaryFont.canDisplay(nextCodePoint) ? primaryFont : fallbackFont;
				if (!runFont.equals(nextFont)) {
					break;
				}
				index += Character.charCount(nextCodePoint);
			}
			attributedString.addAttribute(TextAttribute.FONT, runFont, runStart, index);
		}
		return attributedString;
	}
	
}
