package de.extio.game_engine.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.audio.AudioController;
import de.extio.game_engine.audio.AudioOptions;
import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.container.ScrollArea;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonControl;
import de.extio.game_engine.renderer.model.bo.DrawEffectRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawEffectRenderingBoEffects;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawImageRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class DemoModule extends AbstractClientModule {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private AudioController audioController;
	
	@Autowired
	private RenderingBoPool renderingBoPool;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private LocalizationService localizationService;
	
	@Autowired
	private ThemeManager themeManager;
	
	private Window mainWindow;
	
	private Window secondaryWindow;
	
	private Window themeSelectionWindow;
	
	private Window scrollAreaWindow;
	
	private Window languageSelectionWindow;
	
	private ScrollArea scrollArea;
	
	private final Map<String, String> themeSelectionByControlId = new HashMap<>();
	
	private final Map<String, String> languageSelectionByControlId = new HashMap<>();
	
	private boolean audioMuted;
	
	@Override
	public void onLoad() {
		this.mainWindow = this.applicationContext.getBean(Window.class);
		this.mainWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7));
		this.mainWindow.setNormalizedPosition(centeredPosition(this.mainWindow.getNormalizedDimension()));
		this.mainWindow.setDraggable(true);
		
		this.secondaryWindow = this.applicationContext.getBean(Window.class);
		this.secondaryWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(7).multiply(2));
		this.secondaryWindow.setNormalizedPosition(centeredPosition(this.secondaryWindow.getNormalizedDimension()));
		this.secondaryWindow.setDraggable(true);
		this.secondaryWindow.setCloseButton(true);
		this.secondaryWindow.setParent(this.mainWindow);
		
		this.themeSelectionWindow = this.applicationContext.getBean(Window.class);
		this.themeSelectionWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(7).multiply(2));
		this.themeSelectionWindow.setNormalizedPosition(centeredPosition(this.themeSelectionWindow.getNormalizedDimension()));
		this.themeSelectionWindow.setDraggable(true);
		this.themeSelectionWindow.setCloseButton(true);
		this.themeSelectionWindow.setParent(this.mainWindow);
		
		this.scrollAreaWindow = this.applicationContext.getBean(Window.class);
		this.scrollAreaWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(5).multiply(2));
		this.scrollAreaWindow.setNormalizedPosition(centeredPosition(this.scrollAreaWindow.getNormalizedDimension()));
		this.scrollAreaWindow.setDraggable(true);
		this.scrollAreaWindow.setCloseButton(true);
		this.scrollAreaWindow.setParent(this.mainWindow);
		
		this.languageSelectionWindow = this.applicationContext.getBean(Window.class);
		this.languageSelectionWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(7).multiply(2));
		this.languageSelectionWindow.setNormalizedPosition(centeredPosition(this.languageSelectionWindow.getNormalizedDimension()));
		this.languageSelectionWindow.setDraggable(true);
		this.languageSelectionWindow.setCloseButton(true);
		this.languageSelectionWindow.setParent(this.mainWindow);
		
		this.scrollArea = this.applicationContext.getBean(ScrollArea.class);
		this.scrollAreaWindow.addComponent(this.scrollArea);
		
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}
	
	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.mainWindow.getId());
		this.getModuleService().unloadModule(this.secondaryWindow.getId());
		this.getModuleService().unloadModule(this.themeSelectionWindow.getId());
		this.getModuleService().unloadModule(this.scrollAreaWindow.getId());
		this.getModuleService().unloadModule(this.languageSelectionWindow.getId());
	}
	
	@Override
	public void onActivate() {
		final var audioOptions = this.audioController.getAudioOptions();
		this.audioMuted = isAudioMuted(audioOptions);
		
		this.setupMainWindow();
		this.getModuleService().changeActiveState(this.mainWindow.getId(), true);
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
	}
	
	@Override
	public void onDeactivate() {
		this.getModuleService().changeActiveState(this.mainWindow.getId(), false);
		this.getModuleService().changeActiveState(this.secondaryWindow.getId(), false);
		this.getModuleService().changeActiveState(this.themeSelectionWindow.getId(), false);
		this.getModuleService().changeActiveState(this.scrollAreaWindow.getId(), false);
		this.getModuleService().changeActiveState(this.languageSelectionWindow.getId(), false);
	}
	
	@Override
	public void onShow() {
		this.getModuleService().changeDisplayState(this.mainWindow.getId(), true);
		this.audioController.playMusic(null, List.of(new StaticResource(List.of("audio"), "race.ogg")), true);
	}
	
	@Override
	public void onHide() {
		this.getModuleService().changeDisplayState(this.mainWindow.getId(), false);
		this.getModuleService().changeActiveState(this.secondaryWindow.getId(), false);
		this.getModuleService().changeActiveState(this.themeSelectionWindow.getId(), false);
		this.getModuleService().changeActiveState(this.scrollAreaWindow.getId(), false);
		this.getModuleService().changeActiveState(this.languageSelectionWindow.getId(), false);
		this.audioController.stopMusic();
	}
	
	private void onUiControlEvent(final UiControlEvent event) {
		switch (event.getId()) {
			case "DemoModule_MainWindow_Button_Audio" -> {
				this.toggleAudioMuted();
			}
			
			case "DemoModule_MainWindow_Label_Welcome" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "contact0.ogg"));
				this.setupSecondaryWindow();
				this.getModuleService().changeActiveState(this.secondaryWindow.getId(), true);
				this.getModuleService().changeDisplayState(this.secondaryWindow.getId(), true);
			}
			
			case "DemoModule_MainWindow_Label_Start" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "contact0.ogg"));
				this.setupThemeSelectionWindow();
				this.getModuleService().changeActiveState(this.themeSelectionWindow.getId(), true);
				this.getModuleService().changeDisplayState(this.themeSelectionWindow.getId(), true);
			}
			
			case "DemoModule_MainWindow_Button_ScrollDemo" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "contact0.ogg"));
				this.setupScrollAreaWindow();
				this.getModuleService().changeActiveState(this.scrollAreaWindow.getId(), true);
				this.getModuleService().changeDisplayState(this.scrollAreaWindow.getId(), true);
			}
			
			case "DemoModule_MainWindow_Label_Language" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "contact0.ogg"));
				this.setupLanguageSelectionWindow();
				this.getModuleService().changeActiveState(this.languageSelectionWindow.getId(), true);
				this.getModuleService().changeDisplayState(this.languageSelectionWindow.getId(), true);
			}
			
			case "DemoModule_SecondaryWindow_Button_Ok" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "alert0.ogg"));
				this.getModuleService().changeActiveState(this.secondaryWindow.getId(), false);
			}
			
			default -> {
				final var themeName = this.themeSelectionByControlId.get(event.getId());
				if (themeName != null) {
					this.audioController.play(new StaticResource(List.of("audio"), "alert0.ogg"));
					this.themeManager.setCurrentTheme(themeName);
					break;
				}

				final var languageShort = this.languageSelectionByControlId.get(event.getId());
				if (languageShort != null) {
					this.audioController.play(new StaticResource(List.of("audio"), "alert0.ogg"));
					this.localizationService.setLanguage(languageShort);
					this.getModuleService().changeActiveState(this.languageSelectionWindow.getId(), false);
					// refresh UI
					this.getModuleService().changeActiveState(this.id, false);
					this.getModuleService().changeActiveState(this.id, true);
					this.getModuleService().changeDisplayState(this.id, true);
					break;
				}
			}
		}
	}
	
	private void setupMainWindow() {
		var bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Label_Welcome", ControlRenderingBo.class)
				.setCaption(this.localizationService.translate("test-3"))
				.setTooltip(this.localizationService.translate("test-1"))
				.setFontSize(48)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7).substract(Window.MARGIN_LEFT + Window.MARGIN_RIGHT).getX(), 60)
				.withPositionRelative(Window.MARGIN_LEFT, 100);
		this.mainWindow.putRenderingBo(bo);
		
		final var buttonX = Math.max(0, this.mainWindow.getNormalizedDimension().getX() - 64 - Window.MARGIN_RIGHT - 1);
		final var buttonY = Window.MARGIN_TOP;
		bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Button_Audio", ControlRenderingBo.class)
				.setCaption("")
				.setType(ButtonControl.class)
				.setTooltip(this.localizationService.translate("test-7"))
				.setVisible(true)
				.setEnabled(true)
				.setControlData(new ButtonData(null, new StaticResource(List.of("gfx"), "settings.png")))
				.withDimensionAbsolute(64, 64)
				.withPositionRelative(buttonX, buttonY);
		this.mainWindow.putRenderingBo(bo);
		
		this.updateAudioButtonOverlay();
		
		bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Logo", DrawImageRenderingBo.class)
				.setResource(new StaticResource(List.of("renderer"), "logo.png"))
				.withDimensionAbsolute(256, 256)
				.setLayer(RenderingBoLayer.UI0)
				.withPositionRelative((this.mainWindow.getNormalizedDimension().getX() - 256) / 2, 200);
		this.mainWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Label_Start", ControlRenderingBo.class)
				.setCaption(this.localizationService.translate("test-4"))
				.setFontSize(78)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7).substract(Window.MARGIN_LEFT + Window.MARGIN_RIGHT).getX(), 120)
				.withPositionRelative(Window.MARGIN_LEFT, 480);
		this.mainWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Label_Language", ControlRenderingBo.class)
				.setCaption(this.localizationService.translate("test-9"))
				.setFontSize(78)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7).substract(Window.MARGIN_LEFT + Window.MARGIN_RIGHT).getX(), 120)
				.withPositionRelative(Window.MARGIN_LEFT, 580);
		this.mainWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Button_ScrollDemo", ControlRenderingBo.class)
				.setCaption(this.localizationService.translate("test-8"))
				.setFontSize(78)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7).substract(Window.MARGIN_LEFT + Window.MARGIN_RIGHT).getX(), 120)
				.withPositionRelative(Window.MARGIN_LEFT, 690);
		this.mainWindow.putRenderingBo(bo);
	}
	
	private void toggleAudioMuted() {
		final var audioOptions = this.audioController.getAudioOptions();
		final var muted = isAudioMuted(audioOptions);
		
		if (muted) {
			audioOptions.getMusicOptions().setVolume(0.8);
			audioOptions.getSfxOptions().setVolume(1.0);
		}
		else {
			audioOptions.getMusicOptions().setVolume(0.0);
			audioOptions.getSfxOptions().setVolume(0.0);
		}
		
		this.audioController.applyAudioOptions(audioOptions);
		this.audioMuted = !muted;
		this.updateAudioButtonOverlay();
	}
	
	private void updateAudioButtonOverlay() {
		if (!this.audioMuted) {
			this.mainWindow.removeRenderingBo("DemoModule_MainWindow_Button_Audio_Overlay");
			this.mainWindow.draw();
			return;
		}
		
		final var buttonX = Math.max(0, this.mainWindow.getNormalizedDimension().getX() - 64 - Window.MARGIN_RIGHT - 1);
		final var buttonY = Window.MARGIN_TOP;
		final DrawImageRenderingBo overlayBo = this.renderingBoPool.acquire("DemoModule_MainWindow_Button_Audio_Overlay", DrawImageRenderingBo.class);
		overlayBo.setResource(new StaticResource(List.of("gfx"), "decline.png"));
		overlayBo.withDimensionAbsolute(64, 64);
		overlayBo.setLayer(RenderingBoLayer.UI1);
		overlayBo.withPositionRelative(buttonX, buttonY);
		this.mainWindow.putRenderingBo(overlayBo);
		this.mainWindow.draw();
	}
	
	private static boolean isAudioMuted(final AudioOptions audioOptions) {
		final var music = audioOptions.getMusicOptions().getVolume();
		final var sfx = audioOptions.getSfxOptions().getVolume();
		return music <= 0.000_001 && sfx <= 0.000_001;
	}
	
	private void setupSecondaryWindow() {
		var bo = this.renderingBoPool.acquire("DemoModule_SecondaryWindow_Text", DrawFontRenderingBo.class)
				.setText(this.localizationService.translate("test-5").replace("\\n", "\n"))
				.setSize(32)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(7).multiply(2).substract(Window.MARGIN_LEFT + Window.MARGIN_RIGHT).getX(), 40)
				.withPositionRelative(Window.MARGIN_LEFT, 50);
		this.secondaryWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire("DemoModule_SecondaryWindow_Button_Ok", ControlRenderingBo.class)
				.setCaption(this.localizationService.translate("test-6"))
				.setType(ButtonControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(160, 60)
				.withPositionRelative(RendererControl.REFERENCE_RESOLUTION.divide(7).getX() - 80, 200);
		this.secondaryWindow.putRenderingBo(bo);
	}
	
	private void setupThemeSelectionWindow() {
		this.themeSelectionByControlId.clear();
		
		final var themes = this.themeManager.getAvailableThemeNames();
		
		final var paddingX = Window.MARGIN_LEFT;
		final var paddingBottom = Window.MARGIN_BOTTOM;
		final var columns = 2;
		final var columnGap = 16;
		final var buttonWidth = 340;
		final var buttonHeight = 54;
		final var headerHeight = 60;
		final var currentHeight = 40;
		final var startY = 150;
		final var gapY = 14;
		final var rows = (themes.size() + columns - 1) / columns;
		
		final var desiredWidth = (paddingX * 2) + (columns * buttonWidth) + ((columns - 1) * columnGap);
		final var desiredHeight = startY + (rows * buttonHeight) + (Math.max(0, rows - 1) * gapY) + paddingBottom;
		this.themeSelectionWindow.setNormalizedDimension(ImmutableCoordI2.create(desiredWidth, desiredHeight));
		this.themeSelectionWindow.setNormalizedPosition(centeredPosition(ImmutableCoordI2.create(desiredWidth, desiredHeight)));
		
		var bo = this.renderingBoPool.acquire("DemoModule_ThemeSelect_Text_Title", DrawFontRenderingBo.class)
				.setText(this.localizationService.translate("test-4"))
				.setSize(42)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(desiredWidth - (paddingX * 2), headerHeight)
				.withPositionRelative(paddingX, 40);
		this.themeSelectionWindow.putRenderingBo(bo);
		
		final var currentTheme = this.themeManager.getCurrentTheme();
		final var currentThemeName = currentTheme != null ? currentTheme.getName() : "";
		bo = this.renderingBoPool.acquire("DemoModule_ThemeSelect_Text_Current", DrawFontRenderingBo.class)
				.setText(currentThemeName.isBlank() ? "" : this.localizationService.translate("test-10") + currentThemeName)
				.setSize(24)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(desiredWidth - (paddingX * 2), currentHeight)
				.withPositionRelative(paddingX, 95);
		this.themeSelectionWindow.putRenderingBo(bo);
		
		for (var i = 0; i < themes.size(); i++) {
			final var themeName = themes.get(i);
			final var controlId = "DemoModule_ThemeSelect_Button_" + sanitizeThemeId(themeName);
			this.themeSelectionByControlId.put(controlId, themeName);
			final var column = i % columns;
			final var row = i / columns;
			final var x = paddingX + (column * (buttonWidth + columnGap));
			final var y = startY + (row * (buttonHeight + gapY));
			
			bo = this.renderingBoPool.acquire(controlId, ControlRenderingBo.class)
					.setCaption(themeName)
					.setType(ButtonControl.class)
					.setFontSize(28)
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(buttonWidth, buttonHeight)
					.withPositionRelative(x, y);
			this.themeSelectionWindow.putRenderingBo(bo);
		}
	}
	
	private void setupLanguageSelectionWindow() {
		this.languageSelectionByControlId.clear();
		
		final var languages = this.localizationService.getLanguages();
		final var title = "Select Language";
		
		final var paddingX = Window.MARGIN_LEFT;
		final var paddingBottom = Window.MARGIN_BOTTOM;
		final var columns = 2;
		final var columnGap = 24;
		final var buttonWidth = 340;
		final var buttonHeight = 54;
		final var headerHeight = 60;
		final var currentHeight = 40;
		final var startY = 150;
		final var gapY = 14;
		final var rows = (languages.size() + columns - 1) / columns;
		
		final var desiredWidth = (paddingX * 2) + (columns * buttonWidth) + ((columns - 1) * columnGap);
		final var desiredHeight = startY + (rows * buttonHeight) + (Math.max(0, rows - 1) * gapY) + paddingBottom;
		this.languageSelectionWindow.setNormalizedDimension(ImmutableCoordI2.create(desiredWidth, desiredHeight));
		this.languageSelectionWindow.setNormalizedPosition(centeredPosition(ImmutableCoordI2.create(desiredWidth, desiredHeight)));
		
		var bo = this.renderingBoPool.acquire("DemoModule_LanguageSelect_Text_Title", DrawFontRenderingBo.class)
				.setText(title)
				.setSize(42)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(desiredWidth - (paddingX * 2), headerHeight)
				.withPositionRelative(paddingX, 40);
		this.languageSelectionWindow.putRenderingBo(bo);
		
		final var currentLanguage = this.localizationService.getCurrentLanguage();
		final var currentLangObj = languages.stream().filter(l -> l.getShortName().equals(currentLanguage)).findFirst().orElse(null);
		final var currentLangName = currentLangObj != null ? currentLangObj.getName() : currentLanguage;
		bo = this.renderingBoPool.acquire("DemoModule_LanguageSelect_Text_Current", DrawFontRenderingBo.class)
				.setText("Current: " + currentLangName)
				.setSize(24)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(desiredWidth - (paddingX * 2), currentHeight)
				.withPositionRelative(paddingX, 95);
		this.languageSelectionWindow.putRenderingBo(bo);
		
		for (var i = 0; i < languages.size(); i++) {
			final var language = languages.get(i);
			final var controlId = "DemoModule_LanguageSelect_Button_" + sanitizeLanguageId(language.getShortName());
			this.languageSelectionByControlId.put(controlId, language.getShortName());
			final var column = i % columns;
			final var row = i / columns;
			final var x = paddingX + (column * (buttonWidth + columnGap));
			final var y = startY + (row * (buttonHeight + gapY));
			
			bo = this.renderingBoPool.acquire(controlId, ControlRenderingBo.class)
					.setCaption(language.getName())
					.setType(ButtonControl.class)
					.setFontSize(28)
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(buttonWidth, buttonHeight)
					.withPositionRelative(x, y);
			this.languageSelectionWindow.putRenderingBo(bo);
		}
	}
	
	private void setupScrollAreaWindow() {
		var bo = this.renderingBoPool.acquire("DemoModule_ScrollAreaWindow_Title", DrawFontRenderingBo.class)
				.setText(this.localizationService.translate("test-8"))
				.setSize(28)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(this.scrollAreaWindow.getNormalizedDimension().getX() - (Window.MARGIN_LEFT + Window.MARGIN_RIGHT), 50)
				.withPositionRelative(Window.MARGIN_LEFT, Window.MARGIN_TOP);
		this.scrollAreaWindow.putRenderingBo(bo);
		
		this.scrollArea.setRelativePosition(ImmutableCoordI2.create(Window.MARGIN_LEFT, 65));
		this.scrollArea.setDimension(this.scrollAreaWindow.getNormalizedDimension().substract(Window.MARGIN_LEFT + Window.MARGIN_RIGHT, 90));
		
		for (var i = 0; i < 20; i++) {
			final var y = i * 70 + 1;
			
			bo = this.renderingBoPool.acquire("DemoModule_ScrollArea_Label_" + i, ControlRenderingBo.class)
					.setCaption("Item " + (i + 1) + "")
					.setFontSize(28)
					.setType(LabelControl.class)
					.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(100, 60)
					.withPositionRelative(1, y + 2);
			this.scrollArea.putRenderingBo(bo);
			
			bo = this.renderingBoPool.acquire("DemoModule_ScrollAreaWindow_Text_" + i, DrawFontRenderingBo.class)
					.setText(String.valueOf(i))
					.setSize(28)
					.setAlignment(HorizontalAlignment.RIGHT)
					.withDimensionAbsolute(50, 60)
					.setLayer(RenderingBoLayer.UI0)
					.withPositionRelative(100, y + 22);
			this.scrollArea.putRenderingBo(bo);
			
			bo = this.renderingBoPool.acquire("DemoModule_ScrollAreaWindow_Effect_" + i, DrawEffectRenderingBo.class)
					.setRelativeCoordinates(List.of(ImmutableCoordI2.create(64, 64)))
					.setEffect(DrawEffectRenderingBoEffects.DECORATIVE_BORDER_FILLED)
					.setCustomInt0(4)
					.setLayer(RenderingBoLayer.UI0)
					.withPositionRelative(200, y);
			this.scrollArea.putRenderingBo(bo);
			
			bo = this.renderingBoPool.acquire("DemoModule_ScrollArea_Switch_" + i, ControlRenderingBo.class)
					.setCaption("Switch " + (i + 1))
					.setFontSize(24)
					.setType(SwitchControl.class)
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(250, 60)
					.withPositionRelative(300, y + 2);
			this.scrollArea.putRenderingBo(bo);
			
			bo = this.renderingBoPool.acquire("DemoModule_ScrollArea_Button_" + i, ControlRenderingBo.class)
					.setCaption("Button " + (i + 1))
					.setFontSize(24)
					.setType(ToggleButtonControl.class)
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(150, 60)
					.withPositionRelative(550, y + 2);
			this.scrollArea.putRenderingBo(bo);
			
			bo = this.renderingBoPool.acquire("DemoModule_ScrollArea_Image_" + i, DrawImageRenderingBo.class)
					.setResource(new StaticResource(List.of("gfx"), "settings.png"))
					.withDimensionAbsolute(64, 64)
					.setLayer(RenderingBoLayer.UI0)
					.withPositionRelative(750, y);
			this.scrollArea.putRenderingBo(bo);
		}
	}
	
	private static ImmutableCoordI2 centeredPosition(final de.extio.game_engine.spatial2.model.CoordI2 dimension) {
		final var ref = RendererControl.REFERENCE_RESOLUTION;
		final var x = Math.max(0, (ref.getX() - dimension.getX()) / 2);
		final var y = Math.max(0, (ref.getY() - dimension.getY()) / 2);
		return ImmutableCoordI2.create(x, y);
	}
	
	private static String sanitizeThemeId(final String themeName) {
		if (themeName == null || themeName.isBlank()) {
			return "Unknown";
		}
		return themeName.replaceAll("[^a-zA-Z0-9]+", "_");
	}
	
	private static String sanitizeLanguageId(final String shortName) {
		if (shortName == null || shortName.isBlank()) {
			return "Unknown";
		}
		return shortName.replaceAll("[^a-zA-Z0-9]+", "_");
	}
}
