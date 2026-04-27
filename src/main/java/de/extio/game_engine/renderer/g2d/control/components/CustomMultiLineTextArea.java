package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
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
import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public class CustomMultiLineTextArea extends Component {
	
	private static final int SCROLLBAR_WIDTH = 12;
	
	private static final int SCROLLBAR_MIN_THUMB_HEIGHT = 20;
	
	private static final int SCROLL_WHEEL_LINES = 3;
	
	private static final int CARET_BLINK_INTERVAL = 500;
	
	private static final int TEXT_PADDING = 2;
	
	private static final int TEXT_AREA_PADDING = 4;
	
	private String text = "";
	
	private int caretPosition = 0;
	
	private int selectionAnchor = 0;
	
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

	private CoordI2 lastMousePosition;
	
	private int scrollbarDragStartY = 0;
	
	private int scrollbarDragStartOffset = 0;
	
	private int cachedLineHeight = 0;
	
	private int cachedFontSize = 0;
	
	private int cachedRawFontHeight = 0;
	
	private int cachedSpaceWidth = 0;
	
	private List<String> cachedWrappedLines = null;
	
	private int cachedTextAreaWidth = -1;
	
	private final ThemeManager themeManager;
	
	private final Consumer<String> onTextChanged;
	
	public void setText(final String text) {
		this.text = text == null ? "" : text;
		this.caretPosition = Math.min(this.caretPosition, this.text.length());
		this.selectionAnchor = this.caretPosition;
		this.cachedWrappedLines = null;
		this.dirty = true;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setFontSize(final int fontSize) {
		if (this.fontSize != fontSize) {
			this.cachedLineHeight = 0;
			this.cachedSpaceWidth = 0;
			this.cachedFontSize = 0;
			this.cachedRawFontHeight = 0;
			this.cachedWrappedLines = null;
		}
		this.fontSize = fontSize;
		this.dirty = true;
	}
	
	public void setScaleFactor(final double scaleFactor) {
		this.scaleFactor = scaleFactor;
		this.cachedWrappedLines = null;
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

	public CoordI2 getLastMousePosition() {
		return this.lastMousePosition;
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
					deleteSelection();
					insertChar(ch);
				}
				else if (ch == '\n' || ch == '\r') {
					deleteSelection();
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
				
				final boolean shiftPressed = e.isShiftDown();
				final boolean ctrlPressed = e.isControlDown();
				
				switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						if (CustomMultiLineTextArea.this.caretPosition > 0) {
							CustomMultiLineTextArea.this.caretPosition--;
							if (!shiftPressed) {
								CustomMultiLineTextArea.this.selectionAnchor = CustomMultiLineTextArea.this.caretPosition;
							}
							ensureCaretVisible();
							CustomMultiLineTextArea.this.dirty = true;
						}
						break;
					case KeyEvent.VK_RIGHT:
						if (CustomMultiLineTextArea.this.caretPosition < CustomMultiLineTextArea.this.text.length()) {
							CustomMultiLineTextArea.this.caretPosition++;
							if (!shiftPressed) {
								CustomMultiLineTextArea.this.selectionAnchor = CustomMultiLineTextArea.this.caretPosition;
							}
							ensureCaretVisible();
							CustomMultiLineTextArea.this.dirty = true;
						}
						break;
					case KeyEvent.VK_UP:
						moveCaretUp();
						if (!shiftPressed) {
							CustomMultiLineTextArea.this.selectionAnchor = CustomMultiLineTextArea.this.caretPosition;
						}
						break;
					case KeyEvent.VK_DOWN:
						moveCaretDown();
						if (!shiftPressed) {
							CustomMultiLineTextArea.this.selectionAnchor = CustomMultiLineTextArea.this.caretPosition;
						}
						break;
					case KeyEvent.VK_HOME:
						moveCaretToLineStart();
						if (!shiftPressed) {
							CustomMultiLineTextArea.this.selectionAnchor = CustomMultiLineTextArea.this.caretPosition;
						}
						break;
					case KeyEvent.VK_END:
						moveCaretToLineEnd();
						if (!shiftPressed) {
							CustomMultiLineTextArea.this.selectionAnchor = CustomMultiLineTextArea.this.caretPosition;
						}
						break;
					case KeyEvent.VK_DELETE:
						if (!CustomMultiLineTextArea.this.readonly) {
							if (hasSelection()) {
								if (e.isShiftDown()) {
									cut();
								}
								else {
									deleteSelection();
								}
							}
							else if (CustomMultiLineTextArea.this.caretPosition < CustomMultiLineTextArea.this.text.length()) {
								CustomMultiLineTextArea.this.text = CustomMultiLineTextArea.this.text.substring(0, CustomMultiLineTextArea.this.caretPosition) +
										CustomMultiLineTextArea.this.text.substring(CustomMultiLineTextArea.this.caretPosition + 1);
								CustomMultiLineTextArea.this.cachedWrappedLines = null;
								notifyTextChanged();
								CustomMultiLineTextArea.this.dirty = true;
							}
						}
						break;
					case KeyEvent.VK_C:
						if (ctrlPressed) {
							copy();
							e.consume();
						}
						break;
					case KeyEvent.VK_X:
						if (ctrlPressed && !CustomMultiLineTextArea.this.readonly) {
							cut();
							e.consume();
						}
						break;
					case KeyEvent.VK_V:
						if (ctrlPressed && !CustomMultiLineTextArea.this.readonly) {
							paste();
							e.consume();
						}
						break;
					case KeyEvent.VK_INSERT:
						if (ctrlPressed) {
							copy();
							e.consume();
						}
						else if (shiftPressed && !CustomMultiLineTextArea.this.readonly) {
							paste();
							e.consume();
						}
						break;
					case KeyEvent.VK_A:
						if (ctrlPressed) {
							selectAll();
							e.consume();
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
			public void mouseEntered(final MouseEvent e) {
				CustomMultiLineTextArea.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				e.consume();
			}
			
			@Override
			public void mousePressed(final MouseEvent e) {
				CustomMultiLineTextArea.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				CustomMultiLineTextArea.this.requestFocus();
				if (!CustomMultiLineTextArea.this.enabled) {
					e.consume();
					return;
				}
				
				if (isInScrollbarArea(e.getX(), e.getY())) {
					handleScrollbarClick(e.getY());
				}
				else {
					updateCaretPosition(e.getX(), e.getY());
					if (!e.isShiftDown()) {
						CustomMultiLineTextArea.this.selectionAnchor = CustomMultiLineTextArea.this.caretPosition;
					}
				}
				e.consume();
			}
			
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (!CustomMultiLineTextArea.this.enabled) {
					e.consume();
					return;
				}
				if (e.getClickCount() == 2 && !isInScrollbarArea(e.getX(), e.getY())) {
					selectWordAtCaret();
					e.consume();
				}
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (!CustomMultiLineTextArea.this.enabled) {
					e.consume();
					return;
				}				
				CustomMultiLineTextArea.this.scrollbarDragging = false;
				e.consume();
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				CustomMultiLineTextArea.this.lastMousePosition = null;
				e.consume();
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				CustomMultiLineTextArea.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				e.consume();
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				CustomMultiLineTextArea.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				if (!CustomMultiLineTextArea.this.enabled) {
					e.consume();
					return;
				}				
				if (CustomMultiLineTextArea.this.scrollbarDragging) {
					handleScrollbarDrag(e.getY());
					e.consume();
				}
				else if (!isInScrollbarArea(e.getX(), e.getY())) {
					updateCaretPosition(e.getX(), e.getY());
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
				final int visibleHeight = getVisibleHeight();
				final int totalContentHeight = getWrappedLines().size() * lineHeight;
				if (totalContentHeight <= visibleHeight) {
					return;
				}
				
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
		this.selectionAnchor = this.caretPosition;
		this.cachedWrappedLines = null;
		ensureCaretVisible();
		notifyTextChanged();
		this.dirty = true;
	}
	
	private void backspace() {
		if (hasSelection()) {
			deleteSelection();
		}
		else if (this.caretPosition > 0) {
			this.text = this.text.substring(0, this.caretPosition - 1) + this.text.substring(this.caretPosition);
			this.caretPosition--;
			this.selectionAnchor = this.caretPosition;
			this.cachedWrappedLines = null;
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
		final List<String> wrappedLines = getWrappedLines();
		final int currentWrappedLine = getWrappedLineIndexAtCaret();
		
		if (currentWrappedLine > 0) {
			final int currentLineStart = getCharOffsetAtWrappedLineStart(currentWrappedLine);
			final int offsetInLine = this.caretPosition - currentLineStart;
			final int prevLineStart = getCharOffsetAtWrappedLineStart(currentWrappedLine - 1);
			final int prevLineLength = wrappedLines.get(currentWrappedLine - 1).length();
			this.caretPosition = prevLineStart + Math.min(offsetInLine, prevLineLength);
			ensureCaretVisible();
			this.dirty = true;
		}
	}
	
	private void moveCaretDown() {
		final List<String> wrappedLines = getWrappedLines();
		final int currentWrappedLine = getWrappedLineIndexAtCaret();
		
		if (currentWrappedLine < wrappedLines.size() - 1) {
			final int currentLineStart = getCharOffsetAtWrappedLineStart(currentWrappedLine);
			final int offsetInLine = this.caretPosition - currentLineStart;
			final int nextLineStart = getCharOffsetAtWrappedLineStart(currentWrappedLine + 1);
			final int nextLineLength = wrappedLines.get(currentWrappedLine + 1).length();
			this.caretPosition = nextLineStart + Math.min(offsetInLine, nextLineLength);
			ensureCaretVisible();
			this.dirty = true;
		}
	}
	
	private void moveCaretToLineStart() {
		final int currentWrappedLine = getWrappedLineIndexAtCaret();
		this.caretPosition = getCharOffsetAtWrappedLineStart(currentWrappedLine);
		ensureCaretVisible();
		this.dirty = true;
	}
	
	private void moveCaretToLineEnd() {
		final List<String> wrappedLines = getWrappedLines();
		final int currentWrappedLine = getWrappedLineIndexAtCaret();
		final int lineStart = getCharOffsetAtWrappedLineStart(currentWrappedLine);
		this.caretPosition = lineStart + wrappedLines.get(currentWrappedLine).length();
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
	
	private int getTextAreaWidth() {
		return (int) ((this.getWidth() / this.scaleFactor) - SCROLLBAR_WIDTH - TEXT_AREA_PADDING);
	}
	
	private List<String> wrapLine(final String line, final int maxWidth) {
		final List<String> wrapped = new ArrayList<>();
		
		if (line.isEmpty()) {
			wrapped.add("");
			return wrapped;
		}
		
		final int lineWidth = G2DDrawFont.getTextDimensions(line, this.getGraphics(), this.fontSize, 1.0).getX();
		if (lineWidth <= maxWidth) {
			wrapped.add(line);
			return wrapped;
		}
		
		final String[] words = line.split(" ");
		final StringBuilder currentLine = new StringBuilder();
		
		for (final String word : words) {
			final String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
			final int testWidth = G2DDrawFont.getTextDimensions(testLine, this.getGraphics(), this.fontSize, 1.0).getX();
			
			if (testWidth <= maxWidth) {
				if (!currentLine.isEmpty()) {
					currentLine.append(" ");
				}
				currentLine.append(word);
			}
			else {
				if (!currentLine.isEmpty()) {
					wrapped.add(currentLine.toString());
					currentLine.setLength(0);
				}
				currentLine.append(word);
			}
		}
		
		if (!currentLine.isEmpty()) {
			wrapped.add(currentLine.toString());
		}
		
		return wrapped;
	}
	
	private List<String> getWrappedLines() {
		if (this.getGraphics() == null) {
			return getLines();
		}
		
		final int textAreaWidth = getTextAreaWidth();
		
		if (this.cachedWrappedLines != null && this.cachedTextAreaWidth == textAreaWidth) {
			return this.cachedWrappedLines;
		}
		
		final List<String> rawLines = getLines();
		final List<String> wrappedLines = new ArrayList<>();
		
		for (final String rawLine : rawLines) {
			wrappedLines.addAll(wrapLine(rawLine, textAreaWidth));
		}
		
		this.cachedWrappedLines = wrappedLines;
		this.cachedTextAreaWidth = textAreaWidth;
		
		return wrappedLines;
	}
	
	private int getWrappedLineIndexAtCaret() {
		final List<String> wrappedLines = getWrappedLines();
		int charCount = 0;
		
		for (int i = 0; i < wrappedLines.size(); i++) {
			final String line = wrappedLines.get(i);
			final int lineLength = line.length();
			
			if (charCount + lineLength >= this.caretPosition || i == wrappedLines.size() - 1) {
				return i;
			}
			
			charCount += lineLength;
			if (i < wrappedLines.size() - 1) {
				if (this.text.length() > charCount && this.text.charAt(charCount) == ' ') {
					charCount++;
				}
				else if (this.text.length() > charCount && this.text.charAt(charCount) == '\n') {
					charCount++;
				}
			}
		}
		
		return wrappedLines.size() - 1;
	}
	
	private int getCharOffsetAtWrappedLineStart(final int wrappedLineIndex) {
		final List<String> wrappedLines = getWrappedLines();
		int charCount = 0;
		
		for (int i = 0; i < wrappedLineIndex && i < wrappedLines.size(); i++) {
			charCount += wrappedLines.get(i).length();
			if (i < wrappedLines.size() - 1) {
				if (this.text.length() > charCount && this.text.charAt(charCount) == ' ') {
					charCount++;
				}
				else if (this.text.length() > charCount && this.text.charAt(charCount) == '\n') {
					charCount++;
				}
			}
		}
		
		return charCount;
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
	
	private int getSpaceWidth() {
		if (this.cachedSpaceWidth > 0 && this.cachedFontSize == this.fontSize) {
			return this.cachedSpaceWidth;
		}
		
		if (this.getGraphics() == null) {
			return 10;
		}
		
		final int width0 = (int) G2DDrawFont.getTextDimensions("MM", this.getGraphics(), this.fontSize, 1.0).getX();
		final int width1 = (int) G2DDrawFont.getTextDimensions("M M", this.getGraphics(), this.fontSize, 1.0).getX();
		this.cachedSpaceWidth = width1 - width0;
		this.cachedFontSize = this.fontSize;
		return this.cachedSpaceWidth;
	}
	
	private void updateCaretPosition(final int mouseX, final int mouseY) {
		final List<String> wrappedLines = getWrappedLines();
		final List<String> rawLines = getLines();
		final int lineHeight = getLineHeight();
		final int textAreaWidth = getTextAreaWidth();
		
		// Convert mouse coordinates from screen pixels to logical coordinates
		final int logicalMouseX = toLogicalCoord(mouseX);
		final int logicalMouseY = toLogicalCoord(mouseY);
		
		final int clickedWrappedLine = Math.max(0, Math.min(wrappedLines.size() - 1, (logicalMouseY + this.scrollOffsetY) / lineHeight));
		
		int wrappedLineCount = 0;
		int charOffset = 0;
		
		for (int i = 0; i < rawLines.size(); i++) {
			final String rawLine = rawLines.get(i);
			final List<String> wrappedForThisLine = wrapLine(rawLine, textAreaWidth);
			final int linesForThisRaw = wrappedForThisLine.size();
			
			if (clickedWrappedLine >= wrappedLineCount && clickedWrappedLine < wrappedLineCount + linesForThisRaw) {
				final String clickedLine = wrappedLines.get(clickedWrappedLine);
				
				int bestPos = 0;
				int bestDist = Integer.MAX_VALUE;
				
				for (int j = 0; j <= clickedLine.length(); j++) {
					final String substr = clickedLine.substring(0, j);
					final int textWidth = G2DDrawFont.getTextDimensions(substr, this.getGraphics(), this.fontSize, 1.0).getX();
					final int dist = Math.abs(textWidth - (logicalMouseX - TEXT_PADDING));
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
		final int textAreaWidth = getTextAreaWidth();
		
		int wrappedLineIndex = 0;
		int charCount = 0;
		final List<String> rawLines = getLines();
		
		for (int i = 0; i < rawLines.size(); i++) {
			final String rawLine = rawLines.get(i);
			final int rawLineLength = rawLine.length() + 1;
			
			if (charCount + rawLineLength > this.caretPosition || i == rawLines.size() - 1) {
				final int posInRaw = this.caretPosition - charCount;
				
				int currentPos = 0;
				
				while (wrappedLineIndex < wrappedLines.size()) {
					final String wrappedLine = wrappedLines.get(wrappedLineIndex);
					final int wrappedLen = wrappedLine.length();
					
					if (currentPos + wrappedLen >= posInRaw || wrappedLineIndex == wrappedLines.size() - 1) {
						break;
					}
					
					currentPos += wrappedLen + 1;
					wrappedLineIndex++;
				}
				break;
			}
			
			final List<String> wrappedForThisLine = wrapLine(rawLine, textAreaWidth);
			wrappedLineIndex += wrappedForThisLine.size();
			charCount += rawLineLength;
		}
		
		final int caretY = wrappedLineIndex * lineHeight;
		final int visibleHeight = getVisibleHeight();
		
		if (caretY < this.scrollOffsetY) {
			this.scrollOffsetY = caretY;
		}
		else if (caretY + lineHeight > this.scrollOffsetY + visibleHeight) {
			this.scrollOffsetY = caretY + lineHeight - visibleHeight;
		}
		
		this.scrollOffsetY = Math.max(0, this.scrollOffsetY);
		this.dirty = true;
	}
	
	private int getVisibleHeight() {
		return (int) (this.getHeight() / this.scaleFactor);
	}
	
	private int toLogicalCoord(final int screenCoord) {
		return (int) (screenCoord / this.scaleFactor);
	}
	
	private int toScreenCoord(final int logicalCoord) {
		return (int) (logicalCoord * this.scaleFactor);
	}
	
	private boolean isInScrollbarArea(final int mouseX, final int mouseY) {
		final int scrollbarX = toScreenCoord(getTextAreaWidth() + TEXT_AREA_PADDING);
		return mouseX >= scrollbarX;
	}
	
	private void handleScrollbarClick(final int mouseY) {
		final int scaledY = toLogicalCoord(mouseY);
		if (scaledY >= this.scrollbarThumbY && scaledY < this.scrollbarThumbY + this.scrollbarThumbHeight) {
			this.scrollbarDragging = true;
			this.scrollbarDragStartY = scaledY;
			this.scrollbarDragStartOffset = this.scrollOffsetY;
		}
		else {
			final int lineHeight = getLineHeight();
			final int visibleHeight = getVisibleHeight();
			final int clickedThumbY = scaledY - this.scrollbarThumbHeight / 2;
			final int totalContentHeight = getWrappedLines().size() * lineHeight;
			final int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
			final int scrollbarHeight = getVisibleHeight();
			final int maxThumbY = scrollbarHeight - this.scrollbarThumbHeight;
			
			if (maxThumbY > 0) {
				this.scrollOffsetY = (int) ((clickedThumbY / (double) maxThumbY) * maxScroll);
				this.scrollOffsetY = Math.max(0, Math.min(maxScroll, this.scrollOffsetY));
				this.dirty = true;
			}
		}
	}
	
	private void handleScrollbarDrag(final int mouseY) {
		final int scaledY = toLogicalCoord(mouseY);
		final int deltaY = scaledY - this.scrollbarDragStartY;
		final int lineHeight = getLineHeight();
		final int visibleHeight = getVisibleHeight();
		final int totalContentHeight = getWrappedLines().size() * lineHeight;
		final int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
		final int scrollbarHeight = getVisibleHeight();
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
		final int visibleHeight = getVisibleHeight();
		final int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
		
		this.scrollOffsetY += amount;
		this.scrollOffsetY = Math.max(0, Math.min(maxScroll, this.scrollOffsetY));
		this.dirty = true;
	}
	
	private boolean hasSelection() {
		return this.caretPosition != this.selectionAnchor;
	}
	
	private int getSelectionStart() {
		return Math.min(this.caretPosition, this.selectionAnchor);
	}
	
	private int getSelectionEnd() {
		return Math.max(this.caretPosition, this.selectionAnchor);
	}
	
	private String getSelectedText() {
		if (!hasSelection()) {
			return "";
		}
		return this.text.substring(getSelectionStart(), getSelectionEnd());
	}
	
	private void deleteSelection() {
		if (!hasSelection()) {
			return;
		}
		
		final int start = getSelectionStart();
		final int end = getSelectionEnd();
		this.text = this.text.substring(0, start) + this.text.substring(end);
		this.caretPosition = start;
		this.selectionAnchor = start;
		this.cachedWrappedLines = null;
		notifyTextChanged();
		this.dirty = true;
	}
	
	private void copy() {
		if (!hasSelection()) {
			return;
		}
		
		final String selectedText = getSelectedText();
		final StringSelection selection = new StringSelection(selectedText);
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, null);
	}
	
	private void cut() {
		if (!hasSelection() || this.readonly) {
			return;
		}
		
		copy();
		deleteSelection();
	}
	
	private void paste() {
		if (this.readonly) {
			return;
		}
		
		try {
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
				final String clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
				if (clipboardText != null && !clipboardText.isEmpty()) {
					deleteSelection();
					this.text = this.text.substring(0, this.caretPosition) + clipboardText + this.text.substring(this.caretPosition);
					this.caretPosition += clipboardText.length();
					this.selectionAnchor = this.caretPosition;
					this.cachedWrappedLines = null;
					ensureCaretVisible();
					notifyTextChanged();
					this.dirty = true;
				}
			}
		}
		catch (final Exception ex) {
		}
	}
	
	private void selectAll() {
		this.selectionAnchor = 0;
		this.caretPosition = this.text.length();
		this.dirty = true;
	}
	
	private void selectWordAtCaret() {
		if (this.text.isEmpty()) {
			return;
		}
		
		int start = this.caretPosition;
		int end = this.caretPosition;
		
		while (start > 0 && isWordCharacter(this.text.charAt(start - 1))) {
			start--;
		}
		
		while (end < this.text.length() && isWordCharacter(this.text.charAt(end))) {
			end++;
		}
		
		if (start < end) {
			this.selectionAnchor = start;
			this.caretPosition = end;
			this.dirty = true;
		}
	}
	
	private boolean isWordCharacter(final char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_';
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
		
		final int textAreaWidth = toScreenCoord(getTextAreaWidth() + TEXT_AREA_PADDING);
		final int visibleHeight = getVisibleHeight();
		
		final List<String> wrappedLines = getWrappedLines();
		final int lineHeight = getLineHeight();
		if (this.cachedRawFontHeight == 0) {
			this.cachedRawFontHeight = G2DDrawFont.getTextDimensions("ÄÖÜABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzgjpqy", g2d, this.fontSize, 1.0).getY();
		}
		final int rawFontHeight = this.cachedRawFontHeight;
		
		final Color fgColor = this.foregroundColor != null ? this.foregroundColor : theme.getTextNormal().toColor();
		
		final var oldClip = g2d.getClip();
		g2d.setClip(0, 0, textAreaWidth, this.getHeight());
		
		final int startLine = Math.max(0, this.scrollOffsetY / lineHeight);
		final int endLine = Math.min(wrappedLines.size(), (this.scrollOffsetY + visibleHeight) / lineHeight + 2);
		
		final boolean hasSelection = hasSelection();
		final int selStart = hasSelection ? getSelectionStart() : 0;
		final int selEnd = hasSelection ? getSelectionEnd() : 0;
		
		for (int i = startLine; i < endLine; i++) {
			final String line = wrappedLines.get(i);
			final int yPos = i * lineHeight - this.scrollOffsetY;
			if (yPos + lineHeight >= 0 && yPos < visibleHeight) {
				final int charOffset = getCharOffsetForWrappedLine(i, wrappedLines);
				
				if (hasSelection && charOffset < selEnd && charOffset + line.length() > selStart) {
					final int lineSelStart = Math.max(0, selStart - charOffset);
					final int lineSelEnd = Math.min(line.length(), selEnd - charOffset);
					
					if (lineSelStart > 0) {
						final String beforeSel = line.substring(0, lineSelStart);
						final var textDim = G2DDrawFont.getTextDimensions(beforeSel, g2d, this.fontSize, 1.0);
						final var textDimScaled = ImmutableCoordI2.create((int) (textDim.getX() * this.scaleFactor), (int) (textDim.getY() * this.scaleFactor));
						g2d.setColor(fgColor);
						G2DDrawFont.renderText(g2d, textDimScaled, this.scaleFactor, TEXT_PADDING, yPos, this.fontSize, beforeSel);
					}
					
					final String selected = line.substring(lineSelStart, lineSelEnd);
					final String beforeSelText = lineSelStart > 0 ? line.substring(0, lineSelStart) : "";
					final var beforeSelTextDim = G2DDrawFont.getTextDimensions(beforeSelText, g2d, this.fontSize, this.scaleFactor);
					final int selX = beforeSelTextDim.getX() + TEXT_PADDING;
					final var selDim = G2DDrawFont.getTextDimensions(selected, g2d, this.fontSize, this.scaleFactor);
					
					g2d.setColor(theme.getSelectionPrimary().toColor());
					g2d.fillRect(selX, (int) (yPos * this.scaleFactor), selDim.getX(), (int) (lineHeight * this.scaleFactor));
					
					g2d.setColor(theme.getBackgroundNormal().toColor());
					G2DDrawFont.renderText(g2d, null, this.scaleFactor, (int) (selX / this.scaleFactor), yPos, this.fontSize, selected);
					
					if (lineSelEnd < line.length()) {
						final String afterSel = line.substring(lineSelEnd);
						g2d.setColor(fgColor);
						G2DDrawFont.renderText(g2d, null, this.scaleFactor, (int) ((selX + selDim.getX()) / this.scaleFactor), yPos, this.fontSize, afterSel);
					}
				}
				else {
					final var textDim = G2DDrawFont.getTextDimensions(line, g2d, this.fontSize, 1.0);
					final var textDimScaled = ImmutableCoordI2.create((int) (textDim.getX() * this.scaleFactor), (int) (textDim.getY() * this.scaleFactor));
					g2d.setColor(fgColor);
					G2DDrawFont.renderText(g2d, textDimScaled, this.scaleFactor, TEXT_PADDING, yPos, this.fontSize, line);
				}
			}
		}
		
		if (this.isFocusOwner() && this.enabled) {
			final long currentTime = System.currentTimeMillis();
			if (currentTime - this.lastBlinkTime > CARET_BLINK_INTERVAL) {
				this.caretVisible = !this.caretVisible;
				this.lastBlinkTime = currentTime;
			}
			
			if (this.caretVisible) {
				final int caretWrappedLineIndex = getWrappedLineIndexAtCaret();
				final int lineStart = getCharOffsetAtWrappedLineStart(caretWrappedLineIndex);
				final int caretPosInWrappedLine = this.caretPosition - lineStart;
				final String caretWrappedLine = wrappedLines.get(caretWrappedLineIndex);
				
				final String beforeCaret = caretPosInWrappedLine > 0 && caretPosInWrappedLine <= caretWrappedLine.length()
						? caretWrappedLine.substring(0, caretPosInWrappedLine)
						: "";
				final var textDim = beforeCaret.isEmpty() ? ImmutableCoordI2.create(0, 0) : G2DDrawFont.getTextDimensions(beforeCaret, g2d, this.fontSize, this.scaleFactor);
				final int spaceWidth = beforeCaret.endsWith(" ") ? getSpaceWidth() : 0;
				final int caretX = textDim.getX() + TEXT_PADDING + (int) (spaceWidth * this.scaleFactor);
				final int caretY = (int) ((caretWrappedLineIndex * lineHeight - this.scrollOffsetY) * this.scaleFactor);
				final int caretHeight = (int) (rawFontHeight * this.scaleFactor);
				
				if (caretY >= 0 && caretY < this.getHeight()) {
					g2d.setColor((currentTime % 1000 < 500) ? theme.getSelectionPrimary().toColor() : theme.getSelectionSecondary().toColor());
					g2d.fillRect(caretX, caretY, 2, caretHeight);
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
		final int scrollbarX = toLogicalCoord(this.getWidth()) - SCROLLBAR_WIDTH;
		final int scrollbarHeight = getVisibleHeight();
		
		g2d.setColor(theme.getBackgroundNormal().toColor().darker());
		g2d.fillRect(toScreenCoord(scrollbarX), 0, toScreenCoord(SCROLLBAR_WIDTH), this.getHeight());
		
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
		g2d.fillRect(toScreenCoord(scrollbarX + 2), toScreenCoord(this.scrollbarThumbY),
				toScreenCoord(SCROLLBAR_WIDTH - 4), toScreenCoord(this.scrollbarThumbHeight));
	}
	
	private int getCharOffsetForWrappedLine(final int wrappedLineIndex, final List<String> wrappedLines) {
		int charOffset = 0;
		for (int i = 0; i < wrappedLineIndex && i < wrappedLines.size(); i++) {
			charOffset += wrappedLines.get(i).length();
			if (i < wrappedLineIndex - 1 || (i < wrappedLines.size() - 1 && wrappedLines.get(i).endsWith(" "))) {
				charOffset++;
			}
		}
		return charOffset;
	}
}
