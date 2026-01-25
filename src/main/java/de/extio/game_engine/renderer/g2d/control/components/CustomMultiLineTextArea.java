package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public class CustomMultiLineTextArea extends Component {
	
	private static final int SCROLLBAR_WIDTH = 12;
	
	private static final int SCROLLBAR_MIN_THUMB_HEIGHT = 20;
	
	private static final int SCROLL_WHEEL_LINES = 3;
	
	private static final int CARET_BLINK_INTERVAL = 500;
	
	private String text = "";
	
	private int caretPosition = 0;
	
	private int scrollOffsetY = 0;
	
	private int fontSize;
	
	private double scaleFactor;
	
	private boolean enabled = true;
	
	private Color backgroundColor;
	
	private Color foregroundColor;
	
	private boolean dirty = true;
	
	private boolean readonly = false;
	
	private long lastBlinkTime = 0;
	
	private boolean caretVisible = true;
	
	private int scrollbarThumbY = 0;
	
	private int scrollbarThumbHeight = 0;
	
	private boolean scrollbarDragging = false;
	
	private int scrollbarDragStartY = 0;
	
	private int scrollbarDragStartOffset = 0;
	
	private int cachedLineHeight = 0;
	
	private int cachedFontSize = 0;
	
	private final ThemeManager themeManager;
	
	private final Consumer<String> onTextChanged;
	
	public void setText(final String text) {
		this.text = text == null ? "" : text;
		this.caretPosition = Math.min(this.caretPosition, this.text.length());
		this.dirty = true;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setFontSize(final int fontSize) {
		if (this.fontSize != fontSize) {
			this.cachedLineHeight = 0;
		}
		this.fontSize = fontSize;
		this.dirty = true;
	}
	
	public void setScaleFactor(final double scaleFactor) {
		this.scaleFactor = scaleFactor;
		this.dirty = true;
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
		this.dirty = true;
	}
	
	public void setReadonly(final boolean readonly) {
		this.readonly = readonly;
	}
	
	public void setBackgroundColor(final Color color) {
		this.backgroundColor = color;
		this.dirty = true;
	}
	
	public void setForegroundColor(final Color color) {
		this.foregroundColor = color;
		this.dirty = true;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public CustomMultiLineTextArea(final ThemeManager themeManager, final Consumer<String> onTextChanged) {
		this.themeManager = themeManager;
		this.onTextChanged = onTextChanged;
		this.setIgnoreRepaint(true);
		this.setFocusable(true);
		
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(final KeyEvent e) {
				if (!CustomMultiLineTextArea.this.enabled || CustomMultiLineTextArea.this.readonly) {
					return;
				}
				
				final char ch = e.getKeyChar();
				if (!Character.isISOControl(ch)) {
					insertChar(ch);
				}
				else if (ch == '\n' || ch == '\r') {
					insertChar('\n');
				}
				else if (ch == '\b') {
					backspace();
				}
			}
			
			@Override
			public void keyPressed(final KeyEvent e) {
				if (!CustomMultiLineTextArea.this.enabled) {
					return;
				}
				
				switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						if (CustomMultiLineTextArea.this.caretPosition > 0) {
							CustomMultiLineTextArea.this.caretPosition--;
							ensureCaretVisible();
							CustomMultiLineTextArea.this.dirty = true;
						}
						break;
					case KeyEvent.VK_RIGHT:
						if (CustomMultiLineTextArea.this.caretPosition < CustomMultiLineTextArea.this.text.length()) {
							CustomMultiLineTextArea.this.caretPosition++;
							ensureCaretVisible();
							CustomMultiLineTextArea.this.dirty = true;
						}
						break;
					case KeyEvent.VK_UP:
						moveCaretUp();
						break;
					case KeyEvent.VK_DOWN:
						moveCaretDown();
						break;
					case KeyEvent.VK_HOME:
						moveCaretToLineStart();
						break;
					case KeyEvent.VK_END:
						moveCaretToLineEnd();
						break;
					case KeyEvent.VK_DELETE:
						if (!CustomMultiLineTextArea.this.readonly && CustomMultiLineTextArea.this.caretPosition < CustomMultiLineTextArea.this.text.length()) {
							CustomMultiLineTextArea.this.text = CustomMultiLineTextArea.this.text.substring(0, CustomMultiLineTextArea.this.caretPosition) +
									CustomMultiLineTextArea.this.text.substring(CustomMultiLineTextArea.this.caretPosition + 1);
							notifyTextChanged();
							CustomMultiLineTextArea.this.dirty = true;
						}
						break;
				}
			}
			
			@Override
			public void keyReleased(final KeyEvent e) {
			}
		});
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				CustomMultiLineTextArea.this.requestFocus();
				if (!CustomMultiLineTextArea.this.enabled) {
					return;
				}
				
				if (isInScrollbarArea(e.getX(), e.getY())) {
					handleScrollbarClick(e.getY());
				}
				else {
					updateCaretPosition(e.getX(), e.getY());
				}
				e.consume();
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				CustomMultiLineTextArea.this.scrollbarDragging = false;
				e.consume();
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (CustomMultiLineTextArea.this.scrollbarDragging) {
					handleScrollbarDrag(e.getY());
					e.consume();
				}
			}
		});
		
		this.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				if (!CustomMultiLineTextArea.this.enabled) {
					return;
				}
				final int lineHeight = getLineHeight();
				final int scrollAmount = e.getWheelRotation() * SCROLL_WHEEL_LINES * lineHeight;
				scroll(scrollAmount);
				e.consume();
			}
		});
		
		this.addFocusListener(new FocusListener() {
			
			@Override
			public void focusGained(final FocusEvent e) {
				CustomMultiLineTextArea.this.dirty = true;
			}
			
			@Override
			public void focusLost(final FocusEvent e) {
				CustomMultiLineTextArea.this.dirty = true;
			}
		});
	}
	
	private void insertChar(final char ch) {
		this.text = this.text.substring(0, this.caretPosition) + ch + this.text.substring(this.caretPosition);
		this.caretPosition++;
		ensureCaretVisible();
		notifyTextChanged();
		this.dirty = true;
	}
	
	private void backspace() {
		if (this.caretPosition > 0) {
			this.text = this.text.substring(0, this.caretPosition - 1) + this.text.substring(this.caretPosition);
			this.caretPosition--;
			ensureCaretVisible();
			notifyTextChanged();
			this.dirty = true;
		}
	}
	
	private void notifyTextChanged() {
		if (this.onTextChanged != null) {
			this.onTextChanged.accept(this.text);
		}
	}
	
	private void moveCaretUp() {
		final List<String> lines = getLines();
		final int currentLine = getCaretLine();
		if (currentLine > 0) {
			final int currentLineStart = getLineStartPosition(currentLine);
			final int offsetInLine = this.caretPosition - currentLineStart;
			final int prevLineStart = getLineStartPosition(currentLine - 1);
			final int prevLineLength = lines.get(currentLine - 1).length();
			this.caretPosition = prevLineStart + Math.min(offsetInLine, prevLineLength);
			ensureCaretVisible();
			this.dirty = true;
		}
	}
	
	private void moveCaretDown() {
		final List<String> lines = getLines();
		final int currentLine = getCaretLine();
		if (currentLine < lines.size() - 1) {
			final int currentLineStart = getLineStartPosition(currentLine);
			final int offsetInLine = this.caretPosition - currentLineStart;
			final int nextLineStart = getLineStartPosition(currentLine + 1);
			final int nextLineLength = lines.get(currentLine + 1).length();
			this.caretPosition = nextLineStart + Math.min(offsetInLine, nextLineLength);
			ensureCaretVisible();
			this.dirty = true;
		}
	}
	
	private void moveCaretToLineStart() {
		final int currentLine = getCaretLine();
		this.caretPosition = getLineStartPosition(currentLine);
		ensureCaretVisible();
		this.dirty = true;
	}
	
	private void moveCaretToLineEnd() {
		final int currentLine = getCaretLine();
		final List<String> lines = getLines();
		this.caretPosition = getLineStartPosition(currentLine) + lines.get(currentLine).length();
		ensureCaretVisible();
		this.dirty = true;
	}
	
	private List<String> getLines() {
		if (this.text.isEmpty()) {
			final List<String> result = new ArrayList<>();
			result.add("");
			return result;
		}
		final String[] split = this.text.split("\n", -1);
		final List<String> result = new ArrayList<>();
		for (final String line : split) {
			result.add(line);
		}
		return result;
	}
	
	private List<String> getWrappedLines() {
		if (this.getGraphics() == null) {
			return getLines();
		}
		
		final List<String> rawLines = getLines();
		final List<String> wrappedLines = new ArrayList<>();
		final int textAreaWidth = (int) ((this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH - 4);
		
		for (final String rawLine : rawLines) {
			if (rawLine.isEmpty()) {
				wrappedLines.add("");
				continue;
			}
			
			final int lineWidth = G2DDrawFont.getTextDimensions(rawLine, this.getGraphics(), this.fontSize, 1.0).getX();
			if (lineWidth <= textAreaWidth) {
				wrappedLines.add(rawLine);
			}
			else {
				final String[] words = rawLine.split(" ");
				final StringBuilder currentLine = new StringBuilder();
				
				for (final String word : words) {
					final String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
					final int testWidth = G2DDrawFont.getTextDimensions(testLine, this.getGraphics(), this.fontSize, 1.0).getX();
					
					if (testWidth <= textAreaWidth) {
						if (!currentLine.isEmpty()) {
							currentLine.append(" ");
						}
						currentLine.append(word);
					}
					else {
						if (!currentLine.isEmpty()) {
							wrappedLines.add(currentLine.toString());
							currentLine.setLength(0);
						}
						currentLine.append(word);
					}
				}
				
				if (!currentLine.isEmpty()) {
					wrappedLines.add(currentLine.toString());
				}
			}
		}
		
		return wrappedLines;
	}
	
	private int getCaretLine() {
		final List<String> lines = getLines();
		int pos = 0;
		for (int i = 0; i < lines.size(); i++) {
			final int lineLength = lines.get(i).length() + 1;
			if (pos + lineLength > this.caretPosition || i == lines.size() - 1) {
				return i;
			}
			pos += lineLength;
		}
		return lines.size() - 1;
	}
	
	private int getLineStartPosition(final int lineIndex) {
		final List<String> lines = getLines();
		int pos = 0;
		for (int i = 0; i < lineIndex && i < lines.size(); i++) {
			pos += lines.get(i).length() + 1;
		}
		return pos;
	}
	
	private int getLineHeight() {
		if (this.cachedLineHeight > 0 && this.cachedFontSize == this.fontSize) {
			return this.cachedLineHeight;
		}
		
		if (this.getGraphics() == null) {
			return 20;
		}
		
		final int height = (int) (G2DDrawFont.getTextDimensions("ÄÖÜABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzgjpqy", this.getGraphics(), this.fontSize, 1.0).getY() * G2DDrawFont.FONT_LEADING);
		this.cachedLineHeight = height;
		this.cachedFontSize = this.fontSize;
		return height;
	}
	
	private void updateCaretPosition(final int mouseX, final int mouseY) {
		final List<String> wrappedLines = getWrappedLines();
		final List<String> rawLines = getLines();
		final int lineHeight = getLineHeight();
		final int clickedWrappedLine = Math.max(0, Math.min(wrappedLines.size() - 1, (mouseY + this.scrollOffsetY) / lineHeight));
		
		int rawLineIndex = 0;
		int wrappedLineCount = 0;
		int charOffset = 0;
		
		for (int i = 0; i < rawLines.size(); i++) {
			final String rawLine = rawLines.get(i);
			final int rawLineWidth = G2DDrawFont.getTextDimensions(rawLine, this.getGraphics(), this.fontSize, 1.0).getX();
			final int textAreaWidth = (int) ((this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH - 4);
			
			int linesForThisRaw = 1;
			if (!rawLine.isEmpty() && rawLineWidth > textAreaWidth) {
				final String[] words = rawLine.split(" ");
				final StringBuilder currentLine = new StringBuilder();
				linesForThisRaw = 0;
				
				for (final String word : words) {
					final String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
					final int testWidth = G2DDrawFont.getTextDimensions(testLine, this.getGraphics(), this.fontSize, 1.0).getX();
					
					if (testWidth <= textAreaWidth) {
						if (!currentLine.isEmpty()) {
							currentLine.append(" ");
						}
						currentLine.append(word);
					}
					else {
						if (!currentLine.isEmpty()) {
							linesForThisRaw++;
							currentLine.setLength(0);
						}
						currentLine.append(word);
					}
				}
				if (!currentLine.isEmpty()) {
					linesForThisRaw++;
				}
			}
			
			if (clickedWrappedLine >= wrappedLineCount && clickedWrappedLine < wrappedLineCount + linesForThisRaw) {
				rawLineIndex = i;
				final String clickedLine = wrappedLines.get(clickedWrappedLine);
				
				int bestPos = 0;
				int bestDist = Integer.MAX_VALUE;
				
				for (int j = 0; j <= clickedLine.length(); j++) {
					final String substr = clickedLine.substring(0, j);
					final int textWidth = G2DDrawFont.getTextDimensions(substr, this.getGraphics(), this.fontSize, 1.0).getX();
					final int dist = Math.abs((int) (textWidth * this.scaleFactor) - mouseX);
					if (dist < bestDist) {
						bestDist = dist;
						bestPos = j;
					}
				}
				
				int positionInRaw = 0;
				for (int k = wrappedLineCount; k < clickedWrappedLine; k++) {
					positionInRaw += wrappedLines.get(k).length() + 1;
				}
				positionInRaw += bestPos;
				
				this.caretPosition = charOffset + Math.min(positionInRaw, rawLine.length());
				this.dirty = true;
				return;
			}
			
			wrappedLineCount += linesForThisRaw;
			charOffset += rawLine.length() + 1;
		}
		
		this.caretPosition = Math.min(this.text.length(), charOffset);
		this.dirty = true;
	}
	
	private void ensureCaretVisible() {
		final List<String> wrappedLines = getWrappedLines();
		final int lineHeight = getLineHeight();
		
		int wrappedLineIndex = 0;
		int charCount = 0;
		final List<String> rawLines = getLines();
		
		for (int i = 0; i < rawLines.size(); i++) {
			final String rawLine = rawLines.get(i);
			final int rawLineLength = rawLine.length() + 1;
			
			if (charCount + rawLineLength > this.caretPosition || i == rawLines.size() - 1) {
				final int posInRaw = this.caretPosition - charCount;
				
				int currentPos = 0;
				int wrappedOffset = 0;
				
				while (wrappedLineIndex < wrappedLines.size()) {
					final String wrappedLine = wrappedLines.get(wrappedLineIndex);
					final int wrappedLen = wrappedLine.length();
					
					if (currentPos + wrappedLen >= posInRaw || wrappedLineIndex == wrappedLines.size() - 1) {
						break;
					}
					
					currentPos += wrappedLen + 1;
					wrappedLineIndex++;
					wrappedOffset++;
				}
				break;
			}
			
			final String rawLine2 = rawLines.get(i);
			final int rawLineWidth = G2DDrawFont.getTextDimensions(rawLine2, this.getGraphics(), this.fontSize, 1.0).getX();
			final int textAreaWidth = (int) ((this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH - 4);
			
			if (rawLine2.isEmpty() || rawLineWidth <= textAreaWidth) {
				wrappedLineIndex++;
			}
			else {
				final String[] words = rawLine2.split(" ");
				final StringBuilder currentLine = new StringBuilder();
				
				for (final String word : words) {
					final String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
					final int testWidth = G2DDrawFont.getTextDimensions(testLine, this.getGraphics(), this.fontSize, 1.0).getX();
					
					if (testWidth <= textAreaWidth) {
						if (!currentLine.isEmpty()) {
							currentLine.append(" ");
						}
						currentLine.append(word);
					}
					else {
						if (!currentLine.isEmpty()) {
							wrappedLineIndex++;
							currentLine.setLength(0);
						}
						currentLine.append(word);
					}
				}
				if (!currentLine.isEmpty()) {
					wrappedLineIndex++;
				}
			}
			
			charCount += rawLineLength;
		}
		
		final int caretY = wrappedLineIndex * lineHeight;
		final int visibleHeight = (int) (this.getHeight() / this.scaleFactor);
		
		if (caretY < this.scrollOffsetY) {
			this.scrollOffsetY = caretY;
		}
		else if (caretY + lineHeight > this.scrollOffsetY + visibleHeight) {
			this.scrollOffsetY = caretY + lineHeight - visibleHeight;
		}
		
		this.scrollOffsetY = Math.max(0, this.scrollOffsetY);
		this.dirty = true;
	}
	
	private boolean isInScrollbarArea(final int mouseX, final int mouseY) {
		final int scrollbarX = (int) (this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH;
		return mouseX >= scrollbarX * this.scaleFactor;
	}
	
	private void handleScrollbarClick(final int mouseY) {
		final int scaledY = (int) (mouseY / this.scaleFactor);
		if (scaledY >= this.scrollbarThumbY && scaledY < this.scrollbarThumbY + this.scrollbarThumbHeight) {
			this.scrollbarDragging = true;
			this.scrollbarDragStartY = scaledY;
			this.scrollbarDragStartOffset = this.scrollOffsetY;
		}
		else {
			final int lineHeight = getLineHeight();
			final int visibleHeight = (int) (this.getHeight() / this.scaleFactor);
			final int clickedThumbY = scaledY - this.scrollbarThumbHeight / 2;
			final int totalContentHeight = getWrappedLines().size() * lineHeight;
			final int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
			final int scrollbarHeight = (int) (this.getHeight() / this.scaleFactor);
			final int maxThumbY = scrollbarHeight - this.scrollbarThumbHeight;
			
			if (maxThumbY > 0) {
				this.scrollOffsetY = (int) ((clickedThumbY / (double) maxThumbY) * maxScroll);
				this.scrollOffsetY = Math.max(0, Math.min(maxScroll, this.scrollOffsetY));
				this.dirty = true;
			}
		}
	}
	
	private void handleScrollbarDrag(final int mouseY) {
		final int scaledY = (int) (mouseY / this.scaleFactor);
		final int deltaY = scaledY - this.scrollbarDragStartY;
		final int lineHeight = getLineHeight();
		final int visibleHeight = (int) (this.getHeight() / this.scaleFactor);
		final int totalContentHeight = getWrappedLines().size() * lineHeight;
		final int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
		final int scrollbarHeight = (int) (this.getHeight() / this.scaleFactor);
		final int maxThumbY = scrollbarHeight - this.scrollbarThumbHeight;
		
		if (maxThumbY > 0) {
			final double scrollRatio = deltaY / (double) maxThumbY;
			this.scrollOffsetY = this.scrollbarDragStartOffset + (int) (scrollRatio * maxScroll);
			this.scrollOffsetY = Math.max(0, Math.min(maxScroll, this.scrollOffsetY));
			this.dirty = true;
		}
	}
	
	private void scroll(final int amount) {
		final List<String> wrappedLines = getWrappedLines();
		final int lineHeight = getLineHeight();
		final int totalContentHeight = wrappedLines.size() * lineHeight;
		final int visibleHeight = (int) (this.getHeight() / this.scaleFactor);
		final int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
		
		this.scrollOffsetY += amount;
		this.scrollOffsetY = Math.max(0, Math.min(maxScroll, this.scrollOffsetY));
		this.dirty = true;
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
		
		final Color bgColor = this.backgroundColor != null ? this.backgroundColor : theme.getBackgroundNormal().toColor();
		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		final int textAreaWidth = (int) ((this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH);
		final int visibleHeight = (int) (this.getHeight() / this.scaleFactor);
		
		final List<String> wrappedLines = getWrappedLines();
		final int lineHeight = getLineHeight();
		
		final Color fgColor = this.foregroundColor != null ? this.foregroundColor : theme.getTextNormal().toColor();
		g2d.setColor(fgColor);
		
		final var oldClip = g2d.getClip();
		g2d.setClip(0, 0, (int) (textAreaWidth * this.scaleFactor), this.getHeight());
		
		final int startLine = Math.max(0, this.scrollOffsetY / lineHeight);
		final int endLine = Math.min(wrappedLines.size(), (this.scrollOffsetY + visibleHeight) / lineHeight + 2);
		
		for (int i = startLine; i < endLine; i++) {
			final String line = wrappedLines.get(i);
			final int yPos = i * lineHeight - this.scrollOffsetY;
			if (yPos + lineHeight >= 0 && yPos < visibleHeight) {
				final var textDim = G2DDrawFont.getTextDimensions(line, g2d, this.fontSize, 1.0);
				final var textDimScaled = ImmutableCoordI2.create((int) (textDim.getX() * this.scaleFactor), (int) (textDim.getY() * this.scaleFactor));
				G2DDrawFont.renderText(g2d, textDimScaled, this.scaleFactor, 2, yPos, this.fontSize, line);
			}
		}
		
		if (this.isFocusOwner() && this.enabled) {
			final long currentTime = System.currentTimeMillis();
			if (currentTime - this.lastBlinkTime > CARET_BLINK_INTERVAL) {
				this.caretVisible = !this.caretVisible;
				this.lastBlinkTime = currentTime;
			}
			
			if (this.caretVisible) {
				int wrappedLineIndex = 0;
				int charCount = 0;
				final List<String> rawLines = getLines();
				
				for (int i = 0; i < rawLines.size(); i++) {
					final String rawLine = rawLines.get(i);
					final int rawLineLength = rawLine.length() + 1;
					
					if (charCount + rawLineLength > this.caretPosition || i == rawLines.size() - 1) {
						final int posInRaw = this.caretPosition - charCount;
						
						int currentPos = 0;
						String caretWrappedLine = "";
						int caretPosInWrappedLine = 0;
						
						while (wrappedLineIndex < wrappedLines.size()) {
							final String wrappedLine = wrappedLines.get(wrappedLineIndex);
							final int wrappedLen = wrappedLine.length();
							
							if (currentPos + wrappedLen >= posInRaw) {
								caretWrappedLine = wrappedLine;
								caretPosInWrappedLine = posInRaw - currentPos;
								break;
							}
							
							currentPos += wrappedLen + 1;
							wrappedLineIndex++;
						}
						
						final String beforeCaret = caretPosInWrappedLine > 0 && caretPosInWrappedLine <= caretWrappedLine.length()
								? caretWrappedLine.substring(0, caretPosInWrappedLine)
								: "";
						final int caretX = (int) (G2DDrawFont.getTextDimensions(beforeCaret, g2d, this.fontSize, 1.0).getX() * this.scaleFactor) + 2;
						final int caretY = (int) ((wrappedLineIndex * lineHeight - this.scrollOffsetY) * this.scaleFactor);
						final int textHeight = (int) (G2DDrawFont.getTextDimensions("M", g2d, this.fontSize, 1.0).getY() * this.scaleFactor);
						
						if (caretY >= 0 && caretY < this.getHeight()) {
							g2d.setColor((currentTime % 1000 < 500) ? theme.getSelectionPrimary().toColor() : theme.getSelectionSecondary().toColor());
							g2d.fillRect(caretX, caretY, 2, textHeight);
						}
						break;
					}
					
					final int rawLineWidth = G2DDrawFont.getTextDimensions(rawLine, g2d, this.fontSize, 1.0).getX();
					final int wrapWidth = (int) ((this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH - 4);
					
					if (rawLine.isEmpty() || rawLineWidth <= wrapWidth) {
						wrappedLineIndex++;
					}
					else {
						final String[] words = rawLine.split(" ");
						final StringBuilder currentLine = new StringBuilder();
						
						for (final String word : words) {
							final String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
							final int testWidth = G2DDrawFont.getTextDimensions(testLine, g2d, this.fontSize, 1.0).getX();
							
							if (testWidth <= wrapWidth) {
								if (!currentLine.isEmpty()) {
									currentLine.append(" ");
								}
								currentLine.append(word);
							}
							else {
								if (!currentLine.isEmpty()) {
									wrappedLineIndex++;
									currentLine.setLength(0);
								}
								currentLine.append(word);
							}
						}
						if (!currentLine.isEmpty()) {
							wrappedLineIndex++;
						}
					}
					
					charCount += rawLineLength;
				}
			}
		}
		
		g2d.setClip(oldClip);
		
		final int totalContentHeight = wrappedLines.size() * lineHeight;
		if (totalContentHeight > visibleHeight) {
			drawScrollbar(g2d, theme, visibleHeight, totalContentHeight);
		}
	}
	
	private void drawScrollbar(final Graphics2D g2d, final Theme theme, final int visibleHeight, final int totalContentHeight) {
		final int scrollbarX = (int) (this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH;
		final int scrollbarHeight = (int) (this.getHeight() / this.scaleFactor);
		
		g2d.setColor(theme.getBackgroundNormal().toColor().darker());
		g2d.fillRect((int) (scrollbarX * this.scaleFactor), 0, (int) (SCROLLBAR_WIDTH * this.scaleFactor), this.getHeight());
		
		final double visibleRatio = Math.min(1.0, (double) visibleHeight / totalContentHeight);
		this.scrollbarThumbHeight = Math.max(SCROLLBAR_MIN_THUMB_HEIGHT, (int) (scrollbarHeight * visibleRatio));
		
		final int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
		final int maxThumbY = scrollbarHeight - this.scrollbarThumbHeight;
		if (maxScroll > 0) {
			this.scrollbarThumbY = (int) ((this.scrollOffsetY / (double) maxScroll) * maxThumbY);
		}
		else {
			this.scrollbarThumbY = 0;
		}
		
		g2d.setColor(theme.getSelectionPrimary().toColor());
		g2d.fillRect((int) ((scrollbarX + 2) * this.scaleFactor), (int) (this.scrollbarThumbY * this.scaleFactor),
				(int) ((SCROLLBAR_WIDTH - 4) * this.scaleFactor), (int) (this.scrollbarThumbHeight * this.scaleFactor));
	}
}
