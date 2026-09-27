package de.extio.game_engine.storage.dialog;

import java.util.Objects;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import de.extio.game_engine.event.Event;
import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldData;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class FileSelectionNewFolderDialogModule extends AbstractClientModule {

	public record FileSelectionNewFolderDialogResponse(
			boolean confirmed,
			String name) implements Event {
	}

	private static final int WINDOW_WIDTH = 500;

	private static final int WINDOW_HEIGHT = 230;

	private static final int TITLE_HEIGHT = 46;

	private static final int LABEL_HEIGHT = 26;

	private static final int FIELD_HEIGHT = 36;

	private static final int BUTTON_HEIGHT = 42;

	private static final int BUTTON_WIDTH = 150;

	private static final int SPACING = 12;

	private static final String NAME_FIELD_ID = "FileSelectionNewFolderDialog_NameField";

	private static final String BUTTON_OK = "FileSelectionNewFolderDialog_Button_Ok";

	private static final String BUTTON_CANCEL = "FileSelectionNewFolderDialog_Button_Cancel";

	private final ApplicationContext applicationContext;

	private final EventService eventService;

	private final LocalizationService localizationService;

	private final RenderingBoPool renderingBoPool;

	public FileSelectionNewFolderDialogModule(final ApplicationContext applicationContext, final EventService eventService, final LocalizationService localizationService, final RenderingBoPool renderingBoPool) {
		this.applicationContext = applicationContext;
		this.eventService = eventService;
		this.localizationService = localizationService;
		this.renderingBoPool = renderingBoPool;
	}

	private Window dialogWindow;

	private boolean opened;

	private String nameFieldValue = "";

	private Set<String> existingFolderNames = Set.of();

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

		this.getModuleService().changeActiveState(this.getId(), true);
	}

	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.dialogWindow.getId());
	}

	@Override
	public void onShow() {
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
		this.getModuleService().changeDisplayState(this.dialogWindow.getId(), true);
	}

	@Override
	public void onHide() {
		this.eventService.unregister(UiControlEvent.class, this.getId());
		this.getModuleService().changeDisplayState(this.dialogWindow.getId(), false);
		this.resetState();
	}

	public void open(final Set<String> existingFolderNames, final Window parentWindow) {
		this.existingFolderNames = existingFolderNames != null ? Set.copyOf(existingFolderNames) : Set.of();
		this.nameFieldValue = "";
		this.opened = true;
		this.dialogWindow.setParent(parentWindow);
		this.buildDialog();
		this.getModuleService().changeDisplayState(this.getId(), true);
	}

	private void onUiControlEvent(final UiControlEvent event) {
		if (event.getId() == null) {
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
			case BUTTON_OK -> this.onOk();
			case BUTTON_CANCEL -> this.onCancel();
			default -> {
			}
		}
	}

	private void buildDialog() {
		this.dialogWindow.clearRenderingBos();

		final var contentWidth = WINDOW_WIDTH - Window.MARGIN_LEFT - Window.MARGIN_RIGHT;
		final var buttonRowY = WINDOW_HEIGHT - Window.MARGIN_BOTTOM - BUTTON_HEIGHT;
		final var nameFieldY = buttonRowY - SPACING - FIELD_HEIGHT;
		final var nameLabelY = nameFieldY - SPACING / 2 - LABEL_HEIGHT;

		final var titleBo = this.renderingBoPool.acquire("FileSelectionNewFolderDialog_Title", DrawFontRenderingBo.class)
				.setText(this.localizationService.translate("ecyoa-190"))
				.setSize(28)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(contentWidth, TITLE_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, Window.MARGIN_TOP);
		this.dialogWindow.putRenderingBo(titleBo);

		final var nameLabel = this.renderingBoPool.acquire("FileSelectionNewFolderDialog_Label_Name", ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(this.localizationService.translate("ecyoa-646") + ":")
				.setFontSize(18)
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, nameLabelY)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(nameLabel);

		final var nameField = this.renderingBoPool.acquire(NAME_FIELD_ID, ControlRenderingBo.class)
				.setType(TextfieldControl.class)
				.setCaption(this.nameFieldValue != null ? this.nameFieldValue : "")
				.setControlData(new TextfieldData(false, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(contentWidth, FIELD_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, nameFieldY)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(nameField);

		final var okButton = this.renderingBoPool.acquire(BUTTON_OK, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-3"))
				.setVisible(true)
				.setEnabled(this.canConfirm())
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, buttonRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(okButton);

		final var cancelButton = this.renderingBoPool.acquire(BUTTON_CANCEL, ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption(this.localizationService.translate("ecyoa-89"))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(WINDOW_WIDTH - Window.MARGIN_RIGHT - BUTTON_WIDTH, buttonRowY)
				.setLayer(RenderingBoLayer.UI1);
		this.dialogWindow.putRenderingBo(cancelButton);

		this.dialogWindow.draw();
	}

	private void onOk() {
		if (!this.canConfirm()) {
			return;
		}
		this.eventService.fire(new FileSelectionNewFolderDialogResponse(true, this.nameFieldValue.trim()));
		this.onClose();
	}

	private void onCancel() {
		if (this.opened) {
			this.eventService.fire(new FileSelectionNewFolderDialogResponse(false, null));
			this.onClose();
		}
	}

	private void onClose() {
		this.getModuleService().changeDisplayState(this.getId(), false);
	}

	private void resetState() {
		this.opened = false;
		this.nameFieldValue = "";
		this.existingFolderNames = Set.of();
	}

	private boolean canConfirm() {
		final var name = this.nameFieldValue != null ? this.nameFieldValue.trim() : "";
		if (name.isEmpty()) {
			return false;
		}
		for (final var existing : this.existingFolderNames) {
			if (existing.equalsIgnoreCase(name)) {
				return false;
			}
		}
		return true;
	}

	private ImmutableCoordI2 centeredPosition(final CoordI2 dimension) {
		final var referenceResolution = RendererControl.REFERENCE_RESOLUTION;
		final var x = (referenceResolution.getX() - dimension.getX()) / 2;
		final var y = (referenceResolution.getY() - dimension.getY()) / 2;
		return ImmutableCoordI2.create(x, y);
	}

}
