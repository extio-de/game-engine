package de.extio.game_engine.storage.dialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonData;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.storage.StorageItemDescriptor;
import de.extio.game_engine.storage.StorageResource;
import de.extio.game_engine.storage.StorageService;

@Component
public class FileSelectionDialogModule extends AbstractClientModule {
	
	public enum FileSelectionMode {
		LOAD,
		SAVE,
		MANAGE
	}
	
	public record FileSelectionDialogResponse(
			String requestId,
			boolean confirmed,
			FileSelectionMode mode,
			StorageResource resource,
			UUID id,
			String name,
			List<String> path,
			boolean existing) implements Event {
	}
	
	private static final int WINDOW_WIDTH = 1100;
	
	private static final int WINDOW_HEIGHT = 760;
	
	private static final int TITLE_HEIGHT = 46;
	
	private static final int LABEL_HEIGHT = 26;
	
	private static final int FIELD_HEIGHT = 36;
	
	private static final int BUTTON_HEIGHT = 42;
	
	private static final int BUTTON_WIDTH = 150;
	
	private static final int SPACING = 12;
	
	private static final int COLUMN_COUNT = 3;
	
	private static final String FOLDER_PREFIX = "FileSelectionDialog_Folder_";
	
	private static final String ITEM_PREFIX = "FileSelectionDialog_Item_";
	
	private static final String NAME_FIELD_ID = "FileSelectionDialog_NameField";
	
	private static final String NEW_FOLDER_FIELD_ID = "FileSelectionDialog_NewFolderField";
	
	private static final String BUTTON_NEW_FOLDER = "FileSelectionDialog_Button_NewFolder";
	
	private static final String BUTTON_NEW_ITEM = "FileSelectionDialog_Button_NewItem";
	
	private static final String BUTTON_RENAME = "FileSelectionDialog_Button_Rename";
	
	private static final String BUTTON_DELETE = "FileSelectionDialog_Button_Delete";
	
	private static final String BUTTON_OK = "FileSelectionDialog_Button_Ok";
	
	private static final String BUTTON_CANCEL = "FileSelectionDialog_Button_Cancel";
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private LocalizationService localizationService;
	
	@Autowired
	private RenderingBoPool renderingBoPool;
	
	@Autowired
	private StorageService storageService;
	
	private Window dialogWindow;
	
	private ScrollArea itemsScrollArea;
	
	private String requestId;
	
	private FileSelectionMode mode;
	
	private List<String> basePath = List.of();
	
	private List<String> currentPath = List.of();
	
	private String filterPattern;
	
	private boolean recursive;
	
	private boolean modal;
	
	private StorageItemDescriptor selectedDescriptor;
	
	private boolean selectionIsNew;
	
	private String nameFieldValue;
	
	private String newFolderValue;
	
	private final Map<String, List<String>> folderPathByControlId = new HashMap<>();
	
	private final Map<String, StorageItemDescriptor> itemByControlId = new HashMap<>();
	
	private final Set<List<String>> virtualFolders = new HashSet<>();
	
	@Override
	public boolean isProtected() {
		return true;
	}
	
	@Override
	public void onLoad() {
		this.dialogWindow = this.applicationContext.getBean(Window.class);
		this.dialogWindow.setNormalizedDimension(ImmutableCoordI2.create(WINDOW_WIDTH, WINDOW_HEIGHT));
		this.dialogWindow.setNormalizedPosition(this.centeredPosition(this.dialogWindow.getNormalizedDimension()));
		this.dialogWindow.setDraggable(true);
		this.dialogWindow.setCloseButton(true);
		this.dialogWindow.setOnCloseAction(this::onCancel);
		
		this.itemsScrollArea = this.applicationContext.getBean(ScrollArea.class);
		this.dialogWindow.addComponent(this.itemsScrollArea);
		
		this.getModuleService().changeActiveState(this.getId(), true);
	}
	
	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.dialogWindow.getId());
	}
	
	@Override
	public void onActivate() {
	}
	
	@Override
	public void onDeactivate() {
		this.eventService.unregisterAll(this.getId());
	}
	
	@Override
	public void onShow() {
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
		this.getModuleService().changeDisplayState(this.dialogWindow.getId(), true);
		if (this.modal) {
			this.getModuleService().hideExcept(this.getId(), this.dialogWindow.getId());
		}
	}
	
	@Override
	public void onHide() {
		this.eventService.unregister(UiControlEvent.class, this.getId());
		this.getModuleService().changeDisplayState(this.dialogWindow.getId(), false);
		if (this.modal) {
			this.getModuleService().restoreVisibility();
		}
		this.resetState();
	}
	
	public void open(final String requestId, final FileSelectionMode mode, final List<String> basePath, final String filterPattern, final boolean recursive, final boolean modal, final String initialName, final Window parentWindow) {
		this.requestId = requestId;
		this.mode = mode != null ? mode : FileSelectionMode.LOAD;
		this.basePath = basePath != null ? List.copyOf(basePath) : List.of();
		this.currentPath = this.basePath;
		this.filterPattern = filterPattern;
		this.recursive = recursive;
		this.modal = modal;
		this.selectedDescriptor = null;
		this.selectionIsNew = this.mode == FileSelectionMode.SAVE;
		this.nameFieldValue = initialName != null && !initialName.isBlank() ? initialName.trim() : "";
		this.newFolderValue = "";
		this.virtualFolders.clear();
		this.dialogWindow.setParent(parentWindow);
		this.applyInitialSelection();
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
				this.selectedDescriptor = null;
				this.selectionIsNew = false;
				this.nameFieldValue = "";
				this.buildDialog();
			}
			return;
		}
		if (event.getId().startsWith(ITEM_PREFIX)) {
			final var descriptor = this.itemByControlId.get(event.getId());
			if (descriptor != null) {
				this.selectedDescriptor = descriptor;
				this.selectionIsNew = false;
				this.nameFieldValue = descriptor.name();
				this.buildDialog();
			}
			return;
		}
		switch (event.getId()) {
			case NAME_FIELD_ID -> {
				if (event.getPayload() instanceof final String text) {
					this.handleNameChanged(text);
				}
			}
			case NEW_FOLDER_FIELD_ID -> {
				if (event.getPayload() instanceof final String text) {
					this.newFolderValue = text;
				}
			}
			case BUTTON_NEW_FOLDER -> this.createFolder();
			case BUTTON_NEW_ITEM -> this.selectNewItem();
			case BUTTON_RENAME -> this.renameSelection();
			case BUTTON_DELETE -> this.deleteSelection();
			case BUTTON_OK -> this.onOk();
			case BUTTON_CANCEL -> this.onCancel();
			default -> {
			}
		}
	}
	
	private void buildDialog() {
		this.dialogWindow.clearRenderingBos();
		this.clearScrollArea();
		this.folderPathByControlId.clear();
		this.itemByControlId.clear();
		
		final var contentWidth = WINDOW_WIDTH - Window.MARGIN_LEFT - Window.MARGIN_RIGHT;
		int yOffset = Window.MARGIN_TOP;
		
		final var title = this.localizationService.translate("ecyoa-185") + " - " + this.getModeLabel();
		final var titleBo = this.renderingBoPool.acquire("FileSelectionDialog_Title", DrawFontRenderingBo.class)
				.setText(title)
				.setSize(28)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(contentWidth, TITLE_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset);
		this.dialogWindow.putRenderingBo(titleBo);
		yOffset += TITLE_HEIGHT + SPACING;
		
		final var pathLabel = this.renderingBoPool.acquire("FileSelectionDialog_Label_Path", ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(this.localizationService.translate("ecyoa-188") + ": " + this.formatPath(this.currentPath))
				.setFontSize(18)
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(pathLabel);
		yOffset += LABEL_HEIGHT + SPACING / 2;
		
		if (this.filterPattern != null && !this.filterPattern.isBlank()) {
			final var filterLabel = this.renderingBoPool.acquire("FileSelectionDialog_Label_Filter", ControlRenderingBo.class)
					.setType(LabelControl.class)
					.setCaption(this.localizationService.translate("ecyoa-189") + ": " + this.filterPattern)
					.setFontSize(16)
					.setVisible(true)
					.setEnabled(false)
					.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
					.withPositionRelative(Window.MARGIN_LEFT, yOffset)
					.setLayer(RenderingBoLayer.UI0);
			this.dialogWindow.putRenderingBo(filterLabel);
			yOffset += LABEL_HEIGHT + SPACING / 2;
		}
		
		final var nameLabel = this.renderingBoPool.acquire("FileSelectionDialog_Label_Name", ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(this.localizationService.translate("ecyoa-48") + ":")
				.setFontSize(18)
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(nameLabel);
		yOffset += LABEL_HEIGHT + SPACING / 2;
		
		final var showNewItemButton = this.mode != FileSelectionMode.LOAD;
		final var nameFieldWidth = showNewItemButton ? contentWidth - BUTTON_WIDTH - SPACING : contentWidth;
		final var nameField = this.renderingBoPool.acquire(NAME_FIELD_ID, ControlRenderingBo.class)
				.setType(TextfieldControl.class)
				.setCaption(this.nameFieldValue != null ? this.nameFieldValue : "")
				.setControlData(new TextfieldData(false, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(nameFieldWidth, FIELD_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(nameField);
		if (showNewItemButton) {
			final var newItemButton = this.renderingBoPool.acquire(BUTTON_NEW_ITEM, ControlRenderingBo.class)
					.setType(ButtonControl.class)
					.setCaption(this.localizationService.translate("ecyoa-191"))
					.setVisible(true)
					.setEnabled(this.canSelectNewItem())
					.withDimensionAbsolute(BUTTON_WIDTH, FIELD_HEIGHT)
					.withPositionRelative(Window.MARGIN_LEFT + nameFieldWidth + SPACING, yOffset)
					.setLayer(RenderingBoLayer.UI1);
			this.dialogWindow.putRenderingBo(newItemButton);
		}
		yOffset += FIELD_HEIGHT + SPACING;
		
		final boolean showFolderField = this.recursive && this.mode != FileSelectionMode.LOAD;
		if (showFolderField) {
			final var folderLabel = this.renderingBoPool.acquire("FileSelectionDialog_Label_NewFolder", ControlRenderingBo.class)
					.setType(LabelControl.class)
					.setCaption(this.localizationService.translate("ecyoa-190") + ":")
					.setFontSize(18)
					.setVisible(true)
					.setEnabled(false)
					.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
					.withPositionRelative(Window.MARGIN_LEFT, yOffset)
					.setLayer(RenderingBoLayer.UI0);
			this.dialogWindow.putRenderingBo(folderLabel);
			yOffset += LABEL_HEIGHT + SPACING / 2;
			
			final var folderField = this.renderingBoPool.acquire(NEW_FOLDER_FIELD_ID, ControlRenderingBo.class)
					.setType(TextfieldControl.class)
					.setCaption(this.newFolderValue != null ? this.newFolderValue : "")
					.setControlData(new TextfieldData(false, null))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(contentWidth, FIELD_HEIGHT)
					.withPositionRelative(Window.MARGIN_LEFT, yOffset)
					.setLayer(RenderingBoLayer.UI1);
			this.dialogWindow.putRenderingBo(folderField);
			yOffset += FIELD_HEIGHT + SPACING;
		}
		
		final var bottomRowY = WINDOW_HEIGHT - Window.MARGIN_BOTTOM - BUTTON_HEIGHT;
		final var scrollHeight = Math.max(120, bottomRowY - yOffset - SPACING);
		this.itemsScrollArea.setRelativeArea(new Area2(ImmutableCoordI2.create(Window.MARGIN_LEFT, yOffset), ImmutableCoordI2.create(contentWidth, scrollHeight)));
		this.buildItemsList(contentWidth);

		var buttonX = Window.MARGIN_LEFT;
		if (this.mode != FileSelectionMode.LOAD && this.recursive) {
			final var newFolderButton = this.renderingBoPool.acquire(BUTTON_NEW_FOLDER, ControlRenderingBo.class)
					.setType(ButtonControl.class)
					.setCaption(this.localizationService.translate("ecyoa-190"))
					.setVisible(true)
					.setEnabled(this.canCreateFolder())
					.withDimensionAbsolute(BUTTON_WIDTH + 20, BUTTON_HEIGHT)
					.withPositionRelative(buttonX, bottomRowY)
					.setLayer(RenderingBoLayer.UI1);
			this.dialogWindow.putRenderingBo(newFolderButton);
			buttonX += BUTTON_WIDTH + 20 + SPACING;
		}
		final var renameButton = this.renderingBoPool.acquire(BUTTON_RENAME, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-57"))
				.setVisible(true)
				.setEnabled(this.canRename())
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(buttonX, bottomRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(renameButton);
		buttonX += BUTTON_WIDTH + SPACING;

		final var deleteButton = this.renderingBoPool.acquire(BUTTON_DELETE, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-68"))
				.setVisible(true)
				.setEnabled(this.selectedDescriptor != null)
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(buttonX, bottomRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(deleteButton);
		buttonX += BUTTON_WIDTH + SPACING;

		final var okButton = this.renderingBoPool.acquire(BUTTON_OK, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-3"))
				.setVisible(true)
				.setEnabled(this.canConfirm())
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(WINDOW_WIDTH - Window.MARGIN_RIGHT - BUTTON_WIDTH * 2 - SPACING, bottomRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(okButton);

		final var cancelButton = this.renderingBoPool.acquire(BUTTON_CANCEL, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-89"))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(WINDOW_WIDTH - Window.MARGIN_RIGHT - BUTTON_WIDTH, bottomRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(cancelButton);
		
		this.itemsScrollArea.draw();
		this.dialogWindow.draw();
	}
	
	private void buildItemsList(final int contentWidth) {
		final var descriptors = this.loadDescriptors();
		final var currentItems = new ArrayList<StorageItemDescriptor>();
		final var folderNames = new HashSet<String>();
		for (final var descriptor : descriptors) {
			if (descriptor == null) {
				continue;
			}
			final var path = this.safePath(descriptor.path());
			if (this.isPathPrefix(this.currentPath, path)) {
				if (path.size() == this.currentPath.size()) {
					currentItems.add(descriptor);
				}
				else if (this.recursive) {
					folderNames.add(path.get(this.currentPath.size()));
				}
			}
		}
		if (this.recursive) {
			for (final var virtualPath : this.virtualFolders) {
				if (this.isPathPrefix(this.currentPath, virtualPath) && virtualPath.size() > this.currentPath.size()) {
					folderNames.add(virtualPath.get(this.currentPath.size()));
				}
			}
		}
		
		final var sortedFolders = folderNames.stream().sorted(String::compareToIgnoreCase).toList();
		currentItems.sort(Comparator.comparing(StorageItemDescriptor::name, String.CASE_INSENSITIVE_ORDER));
		
		final var entries = new ArrayList<DisplayEntry>();
		if (this.recursive && this.currentPath.size() > this.basePath.size()) {
			final var upPath = List.copyOf(this.currentPath.subList(0, this.currentPath.size() - 1));
			entries.add(DisplayEntry.folder("..", upPath));
		}
		for (final var folderName : sortedFolders) {
			entries.add(DisplayEntry.folder(folderName + "/", this.appendPath(this.currentPath, folderName)));
		}
		for (final var item : currentItems) {
			entries.add(DisplayEntry.item(item));
		}
		
		final var leftPad = Window.MARGIN_LEFT;
		final var availableWidth = contentWidth - ScrollArea.SCROLLBAR_WIDTH_WITH_MARGIN - leftPad;
		final var columnSpacing = SPACING;
		final var columnWidth = Math.max(120, (availableWidth - columnSpacing * (COLUMN_COUNT - 1)) / COLUMN_COUNT);
		final var rowSpacing = SPACING / 2;
		
		int index = 0;
		for (final var entry : entries) {
			final var col = index % COLUMN_COUNT;
			final var row = index / COLUMN_COUNT;
			final var x = leftPad + col * (columnWidth + columnSpacing);
			final var y = row * (FIELD_HEIGHT + rowSpacing);
			if (entry.isFolder()) {
				final var controlId = FOLDER_PREFIX + index + "_" + entry.caption().hashCode();
				this.folderPathByControlId.put(controlId, entry.path());
				final var folderButton = this.renderingBoPool.acquire(controlId, ControlRenderingBo.class)
						.setType(ButtonControl.class)
						.setCaption(entry.caption())
						.setTooltip(this.formatPath(entry.path()))
						.setVisible(true)
						.setEnabled(true)
						.withDimensionAbsolute(columnWidth, FIELD_HEIGHT)
						.withPositionRelative(x, y)
						.setLayer(RenderingBoLayer.UI1);
				this.itemsScrollArea.putRenderingBo(folderButton);
			}
			else if (entry.item() != null) {
				final var item = entry.item();
				final var controlId = ITEM_PREFIX + item.id();
				this.itemByControlId.put(controlId, item);
				final var selected = this.selectedDescriptor != null && Objects.equals(this.selectedDescriptor.id(), item.id());
				final var resourcePath = this.formatPath(this.safePath(item.path())) + "/" + item.name();
				final var itemButton = this.renderingBoPool.acquire(controlId, ControlRenderingBo.class)
						.setType(ToggleButtonControl.class)
						.setCaption(item.name())
						.setTooltip(resourcePath)
						.setControlGroup("FileSelectionDialog_Items")
						.setControlData(new ToggleButtonData(selected, true, null))
						.setVisible(true)
						.setEnabled(true)
						.withDimensionAbsolute(columnWidth, FIELD_HEIGHT)
						.withPositionRelative(x, y)
						.setLayer(RenderingBoLayer.UI1);
				this.itemsScrollArea.putRenderingBo(itemButton);
			}
			index++;
		}
		if (entries.isEmpty()) {
			final var emptyLabel = this.renderingBoPool.acquire("FileSelectionDialog_Label_Empty", ControlRenderingBo.class)
					.setType(LabelControl.class)
					.setCaption(this.localizationService.translate("ecyoa-193"))
					.setControlData(new LabelData(null, null, HorizontalAlignment.CENTER))
					.setFontSize(18)
					.setVisible(true)
					.setEnabled(false)
					.withDimensionAbsolute(columnWidth, FIELD_HEIGHT)
					.withPositionRelative(leftPad, 10)
					.setLayer(RenderingBoLayer.UI0);
			this.itemsScrollArea.putRenderingBo(emptyLabel);
		}
	}
	
	private List<StorageItemDescriptor> loadDescriptors() {
		final var queryPath = this.recursive ? this.basePath : this.currentPath;
		if (this.filterPattern != null && !this.filterPattern.isBlank()) {
			return this.storageService.searchByPattern(queryPath, this.filterPattern, this.recursive);
		}
		return this.storageService.listPath(queryPath, this.recursive);
	}
	
	private void createFolder() {
		if (!this.canCreateFolder()) {
			return;
		}
		final var newPath = this.appendPath(this.currentPath, this.newFolderValue.trim());
		this.virtualFolders.add(newPath);
		this.currentPath = newPath;
		this.newFolderValue = "";
		this.selectedDescriptor = null;
		this.selectionIsNew = false;
		this.nameFieldValue = "";
		this.buildDialog();
	}
	
	private void selectNewItem() {
		if (!this.canSelectNewItem()) {
			return;
		}
		this.selectedDescriptor = null;
		this.selectionIsNew = true;
		this.nameFieldValue = this.ensureFreeNameForNewItem(this.nameFieldValue);
		this.buildDialog();
	}
	
	private void renameSelection() {
		if (!this.canRename()) {
			return;
		}
		final var newName = this.nameFieldValue.trim();
		this.storageService.moveById(this.selectedDescriptor.id(), this.selectedDescriptor.path(), newName);
		this.nameFieldValue = newName;
		this.selectionIsNew = false;
		this.selectedDescriptor = null;
		this.buildDialog();
	}
	
	private void deleteSelection() {
		if (this.selectedDescriptor == null) {
			return;
		}
		this.storageService.deleteById(this.selectedDescriptor.id());
		this.selectedDescriptor = null;
		this.selectionIsNew = false;
		this.nameFieldValue = "";
		this.buildDialog();
	}
	
	private void onOk() {
		if (!this.canConfirm()) {
			return;
		}
		final var name = this.resolveSelectionName();
		final var path = this.resolveSelectionPath();
		final var existing = this.selectedDescriptor != null && name != null && name.equals(this.selectedDescriptor.name());
		final var id = existing ? this.selectedDescriptor.id() : null;
		final var resource = name != null ? new StorageResource(path, name) : null;
		this.eventService.fire(new FileSelectionDialogResponse(this.requestId, true, this.mode, resource, id, name, path, existing));
		this.close();
	}
	
	private void onCancel() {
		this.eventService.fire(new FileSelectionDialogResponse(this.requestId, false, this.mode, null, null, null, null, false));
		this.close();
	}
	
	private void close() {
		this.getModuleService().changeDisplayState(this.getId(), false);
	}
	
	private void resetState() {
		this.requestId = null;
		this.mode = FileSelectionMode.LOAD;
		this.basePath = List.of();
		this.currentPath = List.of();
		this.filterPattern = null;
		this.recursive = false;
		this.modal = false;
		this.selectedDescriptor = null;
		this.selectionIsNew = false;
		this.nameFieldValue = "";
		this.newFolderValue = "";
		this.virtualFolders.clear();
		this.folderPathByControlId.clear();
		this.itemByControlId.clear();
		this.clearScrollArea();
	}
	
	private void clearScrollArea() {
		for (final var boId : this.itemsScrollArea.getRenderingBoIds().toArray(new String[0])) {
			this.itemsScrollArea.removeRenderingBo(boId);
		}
	}
	
	private boolean canCreateFolder() {
		if (!this.recursive || this.mode == FileSelectionMode.LOAD) {
			return false;
		}
		if (this.newFolderValue == null || this.newFolderValue.isBlank()) {
			return false;
		}
		final var name = this.newFolderValue.trim();
		final var targetPath = this.appendPath(this.currentPath, name);
		return !this.virtualFolders.contains(targetPath) && !this.folderExistsInStorage(name);
	}
	
	private boolean folderExistsInStorage(final String name) {
		final var descriptors = this.loadDescriptors();
		for (final var descriptor : descriptors) {
			if (descriptor == null) {
				continue;
			}
			final var path = this.safePath(descriptor.path());
			if (this.isPathPrefix(this.currentPath, path) && path.size() > this.currentPath.size()) {
				if (name.equals(path.get(this.currentPath.size()))) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean canSelectNewItem() {
		if (this.mode == FileSelectionMode.LOAD) {
			return false;
		}
		return this.nameFieldValue != null && !this.nameFieldValue.isBlank();
	}
	
	private boolean canRename() {
		if (this.selectedDescriptor == null || this.nameFieldValue == null || this.nameFieldValue.isBlank()) {
			return false;
		}
		final var trimmed = this.nameFieldValue.trim();
		if (trimmed.equalsIgnoreCase(this.selectedDescriptor.name())) {
			return false;
		}
		return this.findDescriptorByName(trimmed) == null;
	}
	
	private boolean canConfirm() {
		return switch (this.mode) {
			case LOAD -> this.selectedDescriptor != null;
			case SAVE -> this.resolveSelectionName() != null;
			case MANAGE -> true;
		};
	}
	
	private String resolveSelectionName() {
		final var name = this.nameFieldValue != null ? this.nameFieldValue.trim() : "";
		if (!name.isBlank()) {
			return name;
		}
		if (this.selectedDescriptor != null) {
			return this.selectedDescriptor.name();
		}
		return null;
	}
	
	private void handleNameChanged(final String text) {
		if (Objects.equals(this.nameFieldValue, text)) {
			return;
		}
		this.nameFieldValue = text;
		final var trimmed = text != null ? text.trim() : "";
		if (this.selectedDescriptor == null && !trimmed.isBlank()) {
			final var match = this.findDescriptorByName(trimmed);
			if (match != null) {
				this.selectedDescriptor = match;
				this.selectionIsNew = false;
				this.buildDialog();
				return;
			}
		}

		if (this.selectedDescriptor == null) {
			this.selectionIsNew = true;
		}
		this.buildDialog();
	}
	
	private void applyInitialSelection() {
		if (this.nameFieldValue == null || this.nameFieldValue.isBlank()) {
			this.selectionIsNew = false;
			this.selectedDescriptor = null;
			return;
		}
		final var match = this.findDescriptorByName(this.nameFieldValue);
		if (this.mode != FileSelectionMode.SAVE && match != null) {
			this.selectedDescriptor = match;
			this.selectionIsNew = false;
			this.nameFieldValue = match.name();
			return;
		}
		if (this.selectionIsNew || this.mode == FileSelectionMode.SAVE) {
			this.nameFieldValue = this.ensureFreeNameForNewItem(this.nameFieldValue);
		}
	}
	
	private String ensureFreeNameForNewItem(final String baseName) {
		if (baseName == null || baseName.isBlank()) {
			return baseName;
		}
		if (this.findDescriptorByName(baseName) == null) {
			return baseName;
		}
		int index = 2;
		String candidate;
		do {
			candidate = baseName + " " + index++;
		} while (this.findDescriptorByName(candidate) != null);
		return candidate;
	}
	
	private StorageItemDescriptor findDescriptorByName(final String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		final var descriptors = this.loadDescriptors();
		for (final var descriptor : descriptors) {
			if (descriptor == null) {
				continue;
			}
			final var path = this.safePath(descriptor.path());
			if (path.size() == this.currentPath.size() && this.isPathPrefix(this.currentPath, path)) {
				if (name.equalsIgnoreCase(descriptor.name())) {
					return descriptor;
				}
			}
		}
		return null;
	}
	
	private record DisplayEntry(String caption, List<String> path, StorageItemDescriptor item) {
		private static DisplayEntry folder(final String caption, final List<String> path) {
			return new DisplayEntry(caption, path, null);
		}
		
		private static DisplayEntry item(final StorageItemDescriptor item) {
			return new DisplayEntry(item.name(), null, item);
		}
		
		private boolean isFolder() {
			return this.path != null;
		}
	}
	
	private List<String> resolveSelectionPath() {
		if (this.selectedDescriptor != null) {
			return this.safePath(this.selectedDescriptor.path());
		}
		return this.currentPath;
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
	
	private String getModeLabel() {
		return switch (this.mode) {
			case LOAD -> this.localizationService.translate("ecyoa-186");
			case SAVE -> this.localizationService.translate("ecyoa-66");
			case MANAGE -> this.localizationService.translate("ecyoa-187");
		};
	}
	
	private ImmutableCoordI2 centeredPosition(final de.extio.game_engine.spatial2.model.CoordI2 dimension) {
		final var referenceResolution = RendererControl.REFERENCE_RESOLUTION;
		final var x = (referenceResolution.getX() - dimension.getX()) / 2;
		final var y = (referenceResolution.getY() - dimension.getY()) / 2;
		return ImmutableCoordI2.create(x, y);
	}
}
