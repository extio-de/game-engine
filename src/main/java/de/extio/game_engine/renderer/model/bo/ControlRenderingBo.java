package de.extio.game_engine.renderer.model.bo;

import java.util.List;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.RenderingBoHasDimension;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.resource.StaticResource;

/**
 * This bo renders ui controls (buttons, ...)
 */
public interface ControlRenderingBo extends RenderingBoHasDimension {
	
	record LabelData(RgbaColor backgroundColor, RgbaColor foregroundColor, HorizontalAlignment textAlignment) {}
	
	record ButtonData(RgbaColor backgroundColor, StaticResource iconResource) {}
	
	record ToggleButtonData(boolean toggled, StaticResource iconResource) {}
	
	record SwitchData(boolean toggled, StaticResource iconResource, boolean drawBorder) {}
	
	record WindowCloseButtonData(boolean thickBorder) {}
	
	record WindowPanelData(boolean thickBorder, RgbaColor color) {}
	
	record TextfieldData(boolean multiLine, boolean readonly, RgbaColor backgroundColor) {}
	
	record SliderData(boolean horizontal, double value, double value2, RgbaColor color) {}
	
	record TableData(List<Object> data, int rows, long version, boolean firstColDoubleSize) {}
	
	record SetFocusData(String focusId) {}
	
	/**
	 * Sets the type of the control. See class tree of BaseControl
	 */
	ControlRenderingBo setType(Class<? extends BaseControl> clazz);
	
	ControlRenderingBo setCaption(String caption);
	
	/**
	 * Optional, some controls (e.g. toggle buttons) can build groups
	 */
	ControlRenderingBo setControlGroup(String controlGroup);
	
	ControlRenderingBo setFontSize(int size);
	
	ControlRenderingBo setControlData(Object data);
	
	ControlRenderingBo setVisible(boolean visible);
	
	ControlRenderingBo setEnabled(boolean enabled);
	
	ControlRenderingBo setTooltip(String tooltip);
	
	public static interface BaseControl {
		
		BaseControl setRendererData(RendererData RendererData);
		
		BaseControl setInUse(boolean inUse);
		
		boolean getInUse();
		
		BaseControl setControlId(String id);
		
		BaseControl setCaption(String caption);
		
		BaseControl setX(int x);
		
		BaseControl setY(int y);
		
		BaseControl setWidth(int width);
		
		BaseControl setHeight(int height);
		
		BaseControl setFontSize(int size);
		
		BaseControl setControlGroup(String controlGroup);
		
		String getControlGroup();
		
		BaseControl setVisible(boolean visible);
		
		BaseControl setEnabled(boolean enabled);
		
		BaseControl setTooltip(String tooltip);
		
		void build();
		
		void render();
		
		void performAction();
		
		void close();
		
	}
	
	/**
	 * Label
	 */
	public static interface LabelControl extends BaseControl {
		
		void setCustomData(LabelData data);
		
	}
	
	/**
	 * Button
	 */
	public static interface ButtonControl extends BaseControl {
		
		void setCustomData(ButtonData data);
		
	}
	
	/**
	 * Toogle button
	 */
	public static interface ToggleButtonControl extends ButtonControl {
		
		void setCustomData(ToggleButtonData data);
		
	}
	
	/**
	 * Switch (Modern version of a toggle button)
	 */
	public static interface SwitchControl extends ButtonControl {
		
		void setCustomData(SwitchData data);
		
	}
	
	/**
	 * Window closing button
	 */
	public static interface WindowCloseButtonControl extends ButtonControl {
		
		void setCustomData(WindowCloseButtonData data);
		
	}
	
	/**
	 * Window panel (decorative container)
	 */
	public static interface WindowPanelControl extends BaseControl {
		
		void setCustomData(WindowPanelData data);
		
	}
	
	/**
	 * Text field
	 */
	public static interface TextfieldControl extends BaseControl {
		
		void setCustomData(TextfieldData data);
		
	}
	
	/**
	 * Slider
	 */
	public static interface SliderControl extends BaseControl {
		
		void setCustomData(SliderData data);
		
	}
	
	/*
	 * Table control
	 */
	public static interface TableControl extends BaseControl {
		
		void setCustomData(TableData data);
		
	}
	
	/**
	 * Sets focus on a certain control
	 */
	public static interface SetFocusControl extends BaseControl {
		
		void setCustomData(SetFocusData data);
		
	}
	
	/**
	 * Shows a tooltip
	 */
	public static interface TooltipControl extends BaseControl {
		
	}
	
}
