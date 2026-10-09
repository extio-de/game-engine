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
import java.util.function.Consumer;

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

@SuppressWarnings("serial")
public class CustomSingleLineTextField extends Component {

	private static final int CARET_BLINK_INTERVAL = 500;

	private static final int TEXT_PADDING = 4;

	private String text = "";

	private int caretPosition = 0;

	private int selectionAnchor = 0;

	private int scrollOffsetX = 0;

	private int fontSize;

	private double scaleFactor;

	private boolean enabled = true;

	private Color backgroundColor;

	private Color foregroundColor;

	private boolean dirty = true;

	private boolean readonly = false;

	private long lastBlinkTime = 0;

	private boolean caretVisible = true;

	private CoordI2 lastMousePosition;

	private int cachedRawFontHeight = 0;

	private final ThemeManager themeManager;

	private final Consumer<String> onTextChanged;

	private final Runnable onSubmit;

	public void setText(final String text) {
		this.text = text == null ? "" : text;
		this.caretPosition = Math.min(this.caretPosition, this.text.length());
		this.selectionAnchor = this.caretPosition;
		this.dirty = true;
	}

	public String getText() {
		return this.text;
	}

	public void setFontSize(final int fontSize) {
		if (this.fontSize != fontSize) {
			this.cachedRawFontHeight = 0;
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

	public CoordI2 getLastMousePosition() {
		return this.lastMousePosition;
	}

	public CustomSingleLineTextField(final ThemeManager themeManager, final Consumer<String> onTextChanged, final Runnable onSubmit) {
		this.themeManager = themeManager;
		this.onTextChanged = onTextChanged;
		this.onSubmit = onSubmit;
		this.setIgnoreRepaint(true);
		this.setFocusable(true);

		this.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(final KeyEvent e) {
				if (!CustomSingleLineTextField.this.enabled || CustomSingleLineTextField.this.readonly) {
					return;
				}

				final char ch = e.getKeyChar();
				if (!Character.isISOControl(ch)) {
					deleteSelection();
					insertChar(ch);
				}
			else if (ch == '\b' && !e.isControlDown()) {
				backspace();
			}
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				if (!CustomSingleLineTextField.this.enabled) {
					return;
				}

				final boolean shiftPressed = e.isShiftDown();
				final boolean ctrlPressed = e.isControlDown();
				final boolean altPressed = e.isAltDown();

				switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						if (!CustomSingleLineTextField.this.readonly) {
							if (CustomSingleLineTextField.this.onSubmit != null) {
								CustomSingleLineTextField.this.onSubmit.run();
							}
						}
						break;
				case KeyEvent.VK_LEFT:
					if (ctrlPressed || altPressed) {
						if (CustomSingleLineTextField.this.caretPosition > 0) {
							moveCaretToPreviousWordStart();
							if (!shiftPressed) {
								CustomSingleLineTextField.this.selectionAnchor = CustomSingleLineTextField.this.caretPosition;
							}
						}
					}
					else if (CustomSingleLineTextField.this.caretPosition > 0) {
						CustomSingleLineTextField.this.caretPosition--;
						if (!shiftPressed) {
							CustomSingleLineTextField.this.selectionAnchor = CustomSingleLineTextField.this.caretPosition;
						}
						ensureCaretVisible();
						CustomSingleLineTextField.this.dirty = true;
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (ctrlPressed || altPressed) {
						if (CustomSingleLineTextField.this.caretPosition < CustomSingleLineTextField.this.text.length()) {
							moveCaretToNextWordEnd();
							if (!shiftPressed) {
								CustomSingleLineTextField.this.selectionAnchor = CustomSingleLineTextField.this.caretPosition;
							}
						}
					}
					else if (CustomSingleLineTextField.this.caretPosition < CustomSingleLineTextField.this.text.length()) {
						CustomSingleLineTextField.this.caretPosition++;
						if (!shiftPressed) {
							CustomSingleLineTextField.this.selectionAnchor = CustomSingleLineTextField.this.caretPosition;
						}
						ensureCaretVisible();
						CustomSingleLineTextField.this.dirty = true;
					}
					break;
					case KeyEvent.VK_HOME:
						CustomSingleLineTextField.this.caretPosition = 0;
						if (!shiftPressed) {
							CustomSingleLineTextField.this.selectionAnchor = CustomSingleLineTextField.this.caretPosition;
						}
						ensureCaretVisible();
						CustomSingleLineTextField.this.dirty = true;
						break;
					case KeyEvent.VK_END:
						CustomSingleLineTextField.this.caretPosition = CustomSingleLineTextField.this.text.length();
						if (!shiftPressed) {
							CustomSingleLineTextField.this.selectionAnchor = CustomSingleLineTextField.this.caretPosition;
						}
						ensureCaretVisible();
						CustomSingleLineTextField.this.dirty = true;
						break;
				case KeyEvent.VK_DELETE:
					if (ctrlPressed && !CustomSingleLineTextField.this.readonly) {
						deleteWordForward();
						e.consume();
					}
					else if (!CustomSingleLineTextField.this.readonly) {
						if (hasSelection()) {
							if (e.isShiftDown()) {
								cut();
							}
							else {
								deleteSelection();
							}
						}
						else if (CustomSingleLineTextField.this.caretPosition < CustomSingleLineTextField.this.text.length()) {
							CustomSingleLineTextField.this.text = CustomSingleLineTextField.this.text.substring(0, CustomSingleLineTextField.this.caretPosition) +
									CustomSingleLineTextField.this.text.substring(CustomSingleLineTextField.this.caretPosition + 1);
							notifyTextChanged();
							CustomSingleLineTextField.this.dirty = true;
						}
					}
					break;
				case KeyEvent.VK_BACK_SPACE:
					if (ctrlPressed && !CustomSingleLineTextField.this.readonly) {
						deleteWordBackward();
						e.consume();
					}
					break;
					case KeyEvent.VK_C:
						if (ctrlPressed) {
							copy();
							e.consume();
						}
						break;
					case KeyEvent.VK_X:
						if (ctrlPressed && !CustomSingleLineTextField.this.readonly) {
							cut();
							e.consume();
						}
						break;
					case KeyEvent.VK_V:
						if (ctrlPressed && !CustomSingleLineTextField.this.readonly) {
							paste();
							e.consume();
						}
						break;
					case KeyEvent.VK_INSERT:
						if (ctrlPressed) {
							copy();
							e.consume();
						}
						else if (shiftPressed && !CustomSingleLineTextField.this.readonly) {
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
				CustomSingleLineTextField.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				e.consume();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				CustomSingleLineTextField.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				CustomSingleLineTextField.this.requestFocus();
				if (!CustomSingleLineTextField.this.enabled) {
					e.consume();
					return;
				}
				updateCaretPosition(e.getX());
				if (!e.isShiftDown()) {
					CustomSingleLineTextField.this.selectionAnchor = CustomSingleLineTextField.this.caretPosition;
				}
				e.consume();
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (!CustomSingleLineTextField.this.enabled) {
					e.consume();
					return;
				}
				if (e.getClickCount() == 2) {
					selectWordAtCaret();
					e.consume();
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				e.consume();
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				CustomSingleLineTextField.this.lastMousePosition = null;
				e.consume();
			}
		});

		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(final MouseEvent e) {
				CustomSingleLineTextField.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				e.consume();
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				CustomSingleLineTextField.this.lastMousePosition = ImmutableCoordI2.create(e.getX(), e.getY());
				if (!CustomSingleLineTextField.this.enabled) {
					e.consume();
					return;
				}
				updateCaretPosition(e.getX());
				e.consume();
			}
		});

		this.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
				CustomSingleLineTextField.this.dirty = true;
			}

			@Override
			public void focusLost(final FocusEvent e) {
				CustomSingleLineTextField.this.dirty = true;
			}
		});
	}

	private void insertChar(final char ch) {
		this.text = this.text.substring(0, this.caretPosition) + ch + this.text.substring(this.caretPosition);
		this.caretPosition++;
		this.selectionAnchor = this.caretPosition;
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
			ensureCaretVisible();
			notifyTextChanged();
			this.dirty = true;
		}
	}

	private void deleteWordBackward() {
		if (hasSelection()) {
			deleteSelection();
			return;
		}

		if (this.caretPosition == 0) {
			return;
		}

		int position = this.caretPosition;

		if (isWordCharacter(this.text.charAt(position - 1))) {
			while (position > 0 && isWordCharacter(this.text.charAt(position - 1))) {
				position--;
			}
		}
		else {
			while (position > 0 && !isWordCharacter(this.text.charAt(position - 1))) {
				position--;
			}
		}

		this.text = this.text.substring(0, position) + this.text.substring(this.caretPosition);
		this.caretPosition = position;
		this.selectionAnchor = position;
		ensureCaretVisible();
		notifyTextChanged();
		this.dirty = true;
	}

	private void deleteWordForward() {
		if (hasSelection()) {
			deleteSelection();
			return;
		}

		final int length = this.text.length();
		if (this.caretPosition >= length) {
			return;
		}

		int position = this.caretPosition;

		if (isWordCharacter(this.text.charAt(position))) {
			while (position < length && isWordCharacter(this.text.charAt(position))) {
				position++;
			}
		}
		else {
			while (position < length && !isWordCharacter(this.text.charAt(position))) {
				position++;
			}
		}

		this.text = this.text.substring(0, this.caretPosition) + this.text.substring(position);
		ensureCaretVisible();
		notifyTextChanged();
		this.dirty = true;
	}

	private void notifyTextChanged() {
		if (this.onTextChanged != null) {
			this.onTextChanged.accept(this.text);
		}
	}

	private void updateCaretPosition(final int mouseX) {
		if (this.getGraphics() == null) {
			return;
		}
		final int clickX = mouseX - (int) (TEXT_PADDING * this.scaleFactor) + (int) (this.scrollOffsetX * this.scaleFactor);

		int bestPos = 0;
		int bestDist = Integer.MAX_VALUE;

		for (int j = 0; j <= this.text.length(); j++) {
			final String substr = this.text.substring(0, j);
			final int textWidth = G2DDrawFont.getTextAdvance(substr, this.getGraphics(), this.fontSize, this.scaleFactor);
			final int dist = Math.abs(textWidth - clickX);
			if (dist < bestDist) {
				bestDist = dist;
				bestPos = j;
			}
		}

		this.caretPosition = bestPos;
		this.dirty = true;
	}

	private void ensureCaretVisible() {
		if (this.getGraphics() == null) {
			return;
		}

		final String beforeCaret = this.text.substring(0, this.caretPosition);
		final int marginScreen = (int) (TEXT_PADDING * this.scaleFactor);
		final int caretContentScreenX = marginScreen + G2DDrawFont.getTextAdvance(beforeCaret, this.getGraphics(), this.fontSize, this.scaleFactor);
		final int scrollScreen = (int) (this.scrollOffsetX * this.scaleFactor);
		final int visibleScreenWidth = (int) (getVisibleWidth() * this.scaleFactor);

		if (caretContentScreenX < scrollScreen + marginScreen) {
			this.scrollOffsetX = Math.max(0, (int) ((caretContentScreenX - marginScreen) / this.scaleFactor));
		}
		else if (caretContentScreenX > scrollScreen + visibleScreenWidth - marginScreen) {
			this.scrollOffsetX = (int) ((caretContentScreenX - visibleScreenWidth + marginScreen) / this.scaleFactor);
		}

		this.scrollOffsetX = Math.max(0, this.scrollOffsetX);
		this.dirty = true;
	}

	private int getVisibleWidth() {
		return (int) (this.getWidth() / this.scaleFactor);
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
				String clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
				if (clipboardText != null && !clipboardText.isEmpty()) {
					clipboardText = clipboardText.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ');
					deleteSelection();
					this.text = this.text.substring(0, this.caretPosition) + clipboardText + this.text.substring(this.caretPosition);
					this.caretPosition += clipboardText.length();
					this.selectionAnchor = this.caretPosition;
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

	private void moveCaretToPreviousWordStart() {
		int position = this.caretPosition;

		while (position > 0 && !isWordCharacter(this.text.charAt(position - 1))) {
			position--;
		}

		while (position > 0 && isWordCharacter(this.text.charAt(position - 1))) {
			position--;
		}

		this.caretPosition = position;
		ensureCaretVisible();
		this.dirty = true;
	}

	private void moveCaretToNextWordEnd() {
		int position = this.caretPosition;
		final int length = this.text.length();

		if (position < length && isWordCharacter(this.text.charAt(position))) {
			while (position < length && isWordCharacter(this.text.charAt(position))) {
				position++;
			}
		}
		else {
			while (position < length && !isWordCharacter(this.text.charAt(position))) {
				position++;
			}
		}

		this.caretPosition = position;
		ensureCaretVisible();
		this.dirty = true;
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

		g2d.setColor(theme.getBorderOuter().toColor());
		g2d.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		g2d.setColor(theme.getBorderInner().toColor());
		g2d.drawRect(1, 1, this.getWidth() - 2, this.getHeight() - 2);

		if (this.cachedRawFontHeight == 0) {
			this.cachedRawFontHeight = G2DDrawFont.getTextDimensions("ÄÖÜABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzgjpqy", g2d, this.fontSize, 1.0).getY();
		}
		final int rawFontHeight = this.cachedRawFontHeight > 0 ? this.cachedRawFontHeight : this.fontSize;
		final int textY = (int) ((this.getHeight() / this.scaleFactor - rawFontHeight) / 2.0);
		final Color fgColor = this.foregroundColor != null ? this.foregroundColor : theme.getTextNormal().toColor();

		final var oldClip = g2d.getClip();
		g2d.setClip(0, 0, this.getWidth(), this.getHeight());

		final boolean hasSelection = hasSelection();
		final int selStart = hasSelection ? getSelectionStart() : 0;
		final int selEnd = hasSelection ? getSelectionEnd() : 0;

		if (hasSelection && !this.text.isEmpty()) {
			final String beforeSel = this.text.substring(0, selStart);
			final String selected = this.text.substring(selStart, selEnd);

			final int originX = TEXT_PADDING - this.scrollOffsetX;
			final int selScreenX = (int) (originX * this.scaleFactor) + G2DDrawFont.getTextAdvance(beforeSel, g2d, this.fontSize, this.scaleFactor);
			final int selScreenY = (int) (textY * this.scaleFactor);
			final int selScreenWidth = G2DDrawFont.getTextAdvance(selected, g2d, this.fontSize, this.scaleFactor);
			final int selScreenHeight = (int) (rawFontHeight * this.scaleFactor);

			g2d.setColor(fgColor);
			G2DDrawFont.renderText(g2d, null, this.scaleFactor, originX, textY, this.fontSize, this.text);

			if (selScreenWidth > 0) {
				g2d.setColor(theme.getSelectionPrimary().toColor());
				g2d.fillRect(selScreenX, selScreenY, selScreenWidth, selScreenHeight);

				final var previousClip = g2d.getClip();
				g2d.clipRect(selScreenX, selScreenY, selScreenWidth, selScreenHeight);
				g2d.setColor(theme.getBackgroundNormal().toColor());
				G2DDrawFont.renderText(g2d, null, this.scaleFactor, originX, textY, this.fontSize, this.text);
				g2d.setClip(previousClip);
			}
		}
		else {
			final var textDim = G2DDrawFont.getTextDimensions(this.text, g2d, this.fontSize, 1.0);
			final var textDimScaled = ImmutableCoordI2.create((int) (textDim.getX() * this.scaleFactor), (int) (textDim.getY() * this.scaleFactor));
			g2d.setColor(fgColor);
			G2DDrawFont.renderText(g2d, textDimScaled, this.scaleFactor, TEXT_PADDING - this.scrollOffsetX, textY, this.fontSize, this.text);
		}

		if (this.isFocusOwner() && this.enabled) {
			final long currentTime = System.currentTimeMillis();
			if (currentTime - this.lastBlinkTime > CARET_BLINK_INTERVAL) {
				this.caretVisible = !this.caretVisible;
				this.lastBlinkTime = currentTime;
			}

			if (this.caretVisible) {
				final String beforeCaret = this.text.substring(0, this.caretPosition);
				final int caretX = (int) ((TEXT_PADDING - this.scrollOffsetX) * this.scaleFactor)
						+ G2DDrawFont.getTextAdvance(beforeCaret, g2d, this.fontSize, this.scaleFactor);
				final int caretY = (int) (textY * this.scaleFactor);
				final int caretHeight = (int) (rawFontHeight * this.scaleFactor);

				g2d.setColor((currentTime % 1000 < 500) ? theme.getSelectionPrimary().toColor() : theme.getSelectionSecondary().toColor());
				g2d.fillRect(caretX, caretY, 2, caretHeight);
			}
		}

		g2d.setClip(oldClip);
	}
}
