package de.extio.game_engine.storage.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.ApplicationContext;

import de.extio.game_engine.event.Event;
import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.container.ScrollArea;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldData;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.storage.StorageItemDescriptor;
import de.extio.game_engine.storage.StorageService;

public class FileSelectionMoveDialogModule extends AbstractClientModule {

	public record FileSelectionMoveDialogResponse(
			boolean confirmed,
			List<String> targetPath) implements Event {
	}

	private static final int WINDOW_WIDTH = 700;

	private static final int WINDOW_HEIGHT = 620;

	private static final int TITLE_HEIGHT = 46;

	private static final int LABEL_HEIGHT = 26;

	private static final int FIELD_HEIGHT = 36;

	private static final int BUTTON_HEIGHT = 42;

	private static final int BUTTON_WIDTH = 150;

	private static final int SPACING = 12;

	private static final int LIST_PADDING = 8;

	private static final int ROW_SPACING = 6;

	private static final String LOC_TITLE = "ecyoa-645";

	private static final String FOLDER_PREFIX = "FileSelectionMoveDialog_Folder_";

	private static final String NAME_FIELD_ID = "FileSelectionMoveDialog_NameField";

	private static final String BUTTON_MOVE = "FileSelectionMoveDialog_Button_Move";

	private static final String BUTTON_CANCEL = "FileSelectionMoveDialog_Button_Cancel";

	private final ApplicationContext applicationContext;

	private final EventService eventService;

	private final LocalizationService localizationService;

	private final RenderingBoPool renderingBoPool;

	private final StorageService storageService;

	public FileSelectionMoveDialogModule(final ApplicationContext applicationContext, final EventService eventService, final LocalizationService localizationService, final RenderingBoPool renderingBoPool, final StorageService storageService) {
		this.applicationContext = applicationContext;
		this.eventService = eventService;
		this.localizationService = localizationService;
		this.renderingBoPool = renderingBoPool;
		this.storageService = storageService;
	}

	private Window moveWindow;

	private ScrollArea folderScrollArea;

	private StorageItemDescriptor descriptor;

	private List<String> basePath = List.of();

	private List<String> currentPath = List.of();

	private String nameFieldValue = "";

	private final Map<String, List<String>> folderPathByControlId = new HashMap<>();

	@Override
	public boolean isProtected() {
		return true;
	}

	@Override
	public void onLoad() {
		this.moveWindow = this.applicationContext.getBean(Window.class);
		this.moveWindow.setNormalizedDimension(ImmutableCoordI2.create(WINDOW_WIDTH, WINDOW_HEIGHT));
		this.moveWindow.setNormalizedPosition(this.centeredPosition(this.moveWindow.getNormalizedDimension()));
		this.moveWindow.setDraggable(true);
		this.moveWindow.setCloseButton(true);
		this.moveWindow.setOnCloseAction(this::onCancel);

		this.folderScrollArea = this.applicationContext.getBean(ScrollArea.class);
		this.moveWindow.addComponent(this.folderScrollArea);

		this.getModuleService().changeActiveState(this.getId(), true);
	}

	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.moveWindow.getId());
	}

	@Override
	public void onShow() {
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
		this.getModuleService().changeDisplayState(this.moveWindow.getId(), true);
	}

	@Override
	public void onHide() {
		this.eventService.unregister(UiControlEvent.class, this.getId());
		this.getModuleService().changeDisplayState(this.moveWindow.getId(), false);
		this.resetState();
	}

	public void open(final StorageItemDescriptor descriptor, final List<String> basePath, final Window parentWindow) {
		this.descriptor = descriptor;
		this.basePath = basePath != null ? List.copyOf(basePath) : List.of();
		this.currentPath = this.basePath;
		this.nameFieldValue = "";
		this.folderPathByControlId.clear();
		this.moveWindow.setParent(parentWindow);
		this.buildDialog();
		this.getModuleService().changeDisplayState(this.getId(), true);
	}

	private void onUiControlEvent(final UiControlEvent event) {
		if (event.getId() == null) {
			return;
		}
		if (event.getId().startsWith(FOLDER_PREFIX)) {
			final var targetPath = this.folderPathByControlId.get(event.getId());
			if (targetPath != null) {
				this.currentPath = targetPath;
				this.buildDialog();
			}
			return;
		}
		switch (event.getId()) {
			case NAME_FIELD_ID -> {
				if (event.getPayload() instanceof final String text) {
					final var value = text != null ? text : "";
					if (!Objects.equals(this.nameFieldValue, value)) {
						this.nameFieldValue = value;
						this.buildDialog();
					}
				}
			}
			case BUTTON_MOVE -> this.onMove();
			case BUTTON_CANCEL -> this.onCancel();
			default -> {
			}
		}
	}

	private void buildDialog() {
		this.moveWindow.clearRenderingBos();
		this.clearScrollArea();
		this.folderPathByControlId.clear();

		final var contentWidth = WINDOW_WIDTH - Window.MARGIN_LEFT - Window.MARGIN_RIGHT;
		int yOffset = Window.MARGIN_TOP;

		final var titleBo = this.renderingBoPool.acquire("FileSelectionMoveDialog_Title", DrawFontRenderingBo.class)
				.setText(this.localizationService.translate(LOC_TITLE))
				.setSize(28)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(contentWidth, TITLE_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset);
		this.moveWindow.putRenderingBo(titleBo);
		yOffset += TITLE_HEIGHT + SPACING;

		final var pathLabel = this.renderingBoPool.acquire("FileSelectionMoveDialog_Label_Path", ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(this.localizationService.translate("ecyoa-188") + ": " + this.formatPath(this.currentPath))
				.setFontSize(18)
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset)
				.setLayer(RenderingBoLayer.UI0);
		this.moveWindow.putRenderingBo(pathLabel);
		yOffset += LABEL_HEIGHT + SPACING / 2;

		final var buttonRowY = WINDOW_HEIGHT - Window.MARGIN_BOTTOM - BUTTON_HEIGHT;
		final var nameFieldY = buttonRowY - SPACING - FIELD_HEIGHT;
		final var nameLabelY = nameFieldY - SPACING / 2 - LABEL_HEIGHT;
		final var scrollHeight = Math.max(120, nameLabelY - SPACING - yOffset);
		this.folderScrollArea.setRelativeArea(new Area2(ImmutableCoordI2.create(Window.MARGIN_LEFT, yOffset), ImmutableCoordI2.create(contentWidth, scrollHeight)));
		this.buildFolderList(contentWidth);

		final var nameLabel = this.renderingBoPool.acquire("FileSelectionMoveDialog_Label_Name", ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(this.localizationService.translate("ecyoa-646") + ":")
				.setFontSize(18)
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, nameLabelY)
				.setLayer(RenderingBoLayer.UI0);
		this.moveWindow.putRenderingBo(nameLabel);

		final var nameField = this.renderingBoPool.acquire(NAME_FIELD_ID, ControlRenderingBo.class)
				.setType(TextfieldControl.class)
				.setCaption(this.nameFieldValue != null ? this.nameFieldValue : "")
				.setControlData(new TextfieldData(false, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(contentWidth, FIELD_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, nameFieldY)
				.setLayer(RenderingBoLayer.UI1);
		this.moveWindow.putRenderingBo(nameField);

		final var moveButton = this.renderingBoPool.acquire(BUTTON_MOVE, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-644"))
				.setVisible(true)
				.setEnabled(this.canMove())
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, buttonRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.moveWindow.putRenderingBo(moveButton);

		final var cancelButton = this.renderingBoPool.acquire(BUTTON_CANCEL, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-89"))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(WINDOW_WIDTH - Window.MARGIN_RIGHT - BUTTON_WIDTH, buttonRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.moveWindow.putRenderingBo(cancelButton);

		this.folderScrollArea.draw();
		this.moveWindow.draw();
	}

	private void buildFolderList(final int contentWidth) {
		final var descriptors = this.storageService.listPath(this.basePath, true);
		final var folderNames = new HashSet<String>();
		for (final var item : descriptors) {
			if (item == null) {
				continue;
			}
			final var path = this.safePath(item.path());
			if (this.isPathPrefix(this.currentPath, path) && path.size() > this.currentPath.size()) {
				folderNames.add(path.get(this.currentPath.size()));
			}
		}

		final var leftPad = LIST_PADDING;
		final var availableWidth = contentWidth - ScrollArea.SCROLLBAR_WIDTH_WITH_MARGIN - leftPad * 2;

		int yOffset = 0;
		if (this.currentPath.size() > this.basePath.size()) {
			final var upPath = List.copyOf(this.currentPath.subList(0, this.currentPath.size() - 1));
			final var controlId = FOLDER_PREFIX + "up_" + upPath.hashCode();
			this.folderPathByControlId.put(controlId, upPath);
			final var upButton = this.renderingBoPool.acquire(controlId, ControlRenderingBo.class)
					.setType(ButtonControl.class)
					.setCaption("..")
					.setTooltip(this.formatPath(upPath))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(availableWidth, FIELD_HEIGHT)
					.withPositionRelative(leftPad, yOffset)
					.setLayer(RenderingBoLayer.UI1);
			this.folderScrollArea.putRenderingBo(upButton);
			yOffset += FIELD_HEIGHT + ROW_SPACING;
		}
		final var sortedFolders = folderNames.stream().sorted(String::compareToIgnoreCase).toList();
		for (int index = 0; index < sortedFolders.size(); index++) {
			final var folderName = sortedFolders.get(index);
			final var targetPath = this.appendPath(this.currentPath, folderName);
			final var controlId = FOLDER_PREFIX + index + "_" + targetPath.hashCode();
			this.folderPathByControlId.put(controlId, targetPath);
			final var folderButton = this.renderingBoPool.acquire(controlId, ControlRenderingBo.class)
					.setType(ButtonControl.class)
					.setCaption(folderName + "/")
					.setTooltip(this.formatPath(targetPath))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(availableWidth, FIELD_HEIGHT)
					.withPositionRelative(leftPad, yOffset)
					.setLayer(RenderingBoLayer.UI1);
			this.folderScrollArea.putRenderingBo(folderButton);
			yOffset += FIELD_HEIGHT + ROW_SPACING;
		}
		if (sortedFolders.isEmpty() && this.currentPath.size() <= this.basePath.size()) {
			final var emptyLabel = this.renderingBoPool.acquire("FileSelectionMoveDialog_Label_Empty", ControlRenderingBo.class)
					.setType(LabelControl.class)
					.setCaption(this.localizationService.translate("ecyoa-193"))
					.setControlData(new LabelData(null, null, HorizontalAlignment.CENTER))
					.setFontSize(18)
					.setVisible(true)
					.setEnabled(false)
					.withDimensionAbsolute(availableWidth, FIELD_HEIGHT)
					.withPositionRelative(leftPad, yOffset + 10)
					.setLayer(RenderingBoLayer.UI0);
			this.folderScrollArea.putRenderingBo(emptyLabel);
		}
	}

	private void onMove() {
		if (this.descriptor == null || !this.canMove()) {
			return;
		}
		this.eventService.fire(new FileSelectionMoveDialogResponse(true, this.resolveTargetPath()));
		this.onClose();
	}

	private void onCancel() {
		if (this.descriptor != null) {
			this.eventService.fire(new FileSelectionMoveDialogResponse(false, null));
			this.onClose();
		}
	}

	private void onClose() {
		this.getModuleService().changeDisplayState(this.getId(), false);
	}

	private void resetState() {
		this.descriptor = null;
		this.basePath = List.of();
		this.currentPath = List.of();
		this.nameFieldValue = "";
		this.folderPathByControlId.clear();
		this.clearScrollArea();
	}

	private void clearScrollArea() {
		for (final var boId : this.folderScrollArea.getRenderingBoIds().toArray(new String[0])) {
			this.folderScrollArea.removeRenderingBo(boId);
		}
	}

	private boolean canMove() {
		if (this.descriptor == null) {
			return false;
		}
		final var targetPath = this.resolveTargetPath();
		if (targetPath.equals(this.safePath(this.descriptor.path()))) {
			return false;
		}
		return !this.targetContainsSameName(targetPath);
	}

	private boolean targetContainsSameName(final List<String> targetPath) {
		final var name = this.descriptor != null ? this.descriptor.name() : null;
		if (name == null || name.isBlank()) {
			return false;
		}
		final var descriptors = this.storageService.listPath(this.basePath, true);
		for (final var item : descriptors) {
			if (item == null) {
				continue;
			}
			if (this.safePath(item.path()).equals(targetPath) && name.equalsIgnoreCase(item.name())) {
				return true;
			}
		}
		return false;
	}

	private List<String> resolveTargetPath() {
		final var name = this.nameFieldValue != null ? this.nameFieldValue.trim() : "";
		if (name.isEmpty()) {
			return this.currentPath;
		}
		return this.appendPath(this.currentPath, name);
	}

	private String formatPath(final List<String> path) {
		if (path == null || path.isEmpty()) {
			return "/";
		}
		return "/" + String.join("/", path);
	}

	private List<String> safePath(final List<String> path) {
		return path != null ? List.copyOf(path) : List.of();
	}

	private boolean isPathPrefix(final List<String> prefix, final List<String> candidate) {
		if (prefix.size() > candidate.size()) {
			return false;
		}
		for (int i = 0; i < prefix.size(); i++) {
			if (!Objects.equals(prefix.get(i), candidate.get(i))) {
				return false;
			}
		}
		return true;
	}

	private List<String> appendPath(final List<String> base, final String segment) {
		final var result = new ArrayList<String>(base.size() + 1);
		result.addAll(base);
		result.add(segment);
		return List.copyOf(result);
	}

	private ImmutableCoordI2 centeredPosition(final CoordI2 dimension) {
		final var referenceResolution = RendererControl.REFERENCE_RESOLUTION;
		final var x = (referenceResolution.getX() - dimension.getX()) / 2;
		final var y = (referenceResolution.getY() - dimension.getY()) / 2;
		return ImmutableCoordI2.create(x, y);
	}

}
