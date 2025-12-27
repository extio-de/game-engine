package de.extio.game_engine.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.audio.AudioController;
import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawImageRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.resource.StaticResource;

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
	
	private Window mainWindow;
	
	private Window secondaryWindow;
	
	@Override
	public void onLoad() {
		this.mainWindow = this.applicationContext.getBean(Window.class);
		this.mainWindow.setNormalizedPosition(RendererControl.REFERENCE_RESOLUTION.divide(9));
		this.mainWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7));
		this.mainWindow.setDraggable(true);
		
		this.secondaryWindow = this.applicationContext.getBean(Window.class);
		this.secondaryWindow.setNormalizedPosition(RendererControl.REFERENCE_RESOLUTION.divide(7).multiply(3));
		this.secondaryWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(7).multiply(2));
		this.secondaryWindow.setDraggable(true);
		this.secondaryWindow.setCloseButton(true);
		
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}
	
	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.mainWindow.getId());
		this.getModuleService().unloadModule(this.secondaryWindow.getId());
	}
	
	@Override
	public void onActivate() {
		this.setupMainWindow();
		this.getModuleService().changeActiveState(this.mainWindow.getId(), true);
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
	}
	
	@Override
	public void onDeactivate() {
		this.getModuleService().changeActiveState(this.mainWindow.getId(), false);
		this.getModuleService().changeActiveState(this.secondaryWindow.getId(), false);
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
		this.audioController.stopMusic();
	}
	
	private void onUiControlEvent(final UiControlEvent event) {
		switch (event.getId()) {
			case "DemoModule_MainWindow_Label_Welcome" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "alert0.ogg"));
			}
			
			case "DemoModule_MainWindow_Label_Start" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "alert2.ogg"));
				this.setupSecondaryWindow();
				this.getModuleService().changeActiveState(this.secondaryWindow.getId(), true);
				this.getModuleService().changeDisplayState(this.secondaryWindow.getId(), true);
			}

			case "DemoModule_SecondaryWindow_Button_Ok" -> {
				this.audioController.play(new StaticResource(List.of("audio"), "alert0.ogg"));
				this.getModuleService().changeActiveState(this.secondaryWindow.getId(), false);
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
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7).substract(20).getX(), 60)
				.withPositionRelative(10, 100);
		this.mainWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Logo", DrawImageRenderingBo.class)
				.setResource(new StaticResource(List.of("renderer"), "logo.png"))
				.withDimensionAbsolute(256, 256)
				.setLayer(RenderingBoLayer.UI0)
				.withPositionRelative((this.mainWindow.getNormalizedDimension().getX() - 256) / 2, 200);
		this.mainWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Label_Start", ControlRenderingBo.class)
				.setCaption(this.localizationService.translate("test-4"))
				.setFontSize(96)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7).substract(20).getX(), 120)
				.withPositionRelative(10, 600);
		this.mainWindow.putRenderingBo(bo);
	}
	
	private void setupSecondaryWindow() {
		var bo = this.renderingBoPool.acquire("DemoModule_SecondaryWindow_Text", DrawFontRenderingBo.class)
				.setText(this.localizationService.translate("test-5").replace("\\n", "\n"))
				.setSize(32)
				.setAlignment(HorizontalAlignment.CENTER)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(7).multiply(2).substract(20).getX(), 40)
				.withPositionRelative(10, 50);
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
}
