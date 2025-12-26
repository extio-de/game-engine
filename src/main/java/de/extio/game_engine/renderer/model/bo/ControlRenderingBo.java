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
	
	/**
	 * Custom data a specific to the type of the control. Best is to search for code examples in shipped groovy mods to see what can be set
	 */
	ControlRenderingBo setCustomData(Object data);
	
	/**
	 * Custom data a specific to the type of the control. Best is to search for code examples in shipped groovy mods to see what can be set
	 */
	ControlRenderingBo setCustomData2(Object data);
	
	/**
	 * Custom data a specific to the type of the control. Best is to search for code examples in shipped groovy mods to see what can be set
	 */
	ControlRenderingBo setCustomData3(Object data);
	
	/**
	 * Custom data a specific to the type of the control. Best is to search for code examples in shipped groovy mods to see what can be set
	 */
	ControlRenderingBo setCustomData4(Object data);
	
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
		
		RgbaColor getBackgroundColor();
		
		void setBackgroundColor(RgbaColor color);
		
		RgbaColor getForegroundColor();
		
		void setForegroundColor(RgbaColor color);
		
		HorizontalAlignment getTextAlignment();
		
		void setTextAlignment(HorizontalAlignment alignment);
		
	}
	
	/**
	 * Button
	 */
	public static interface ButtonControl extends BaseControl {
		
		RgbaColor getBackgroundColor();
		
		void setBackgroundColor(RgbaColor backgroundColor);
		
		void setIconResource(StaticResource iconResource);
		
		StaticResource getIconResource();
		
	}
	
	/**
	 * Toogle button
	 */
	public static interface ToggleButtonControl extends ButtonControl {
		
		boolean isToggled();
		
		void setToggled(boolean toggled);
		
	}
	
	/**
	 * Switch (Modern version of a toggle button)
	 */
	public static interface SwitchControl extends ButtonControl {
		
		boolean isToggled();
		
		void setToggled(boolean toggled);
		
		boolean isDrawBorder();
		
		void setDrawBorder(boolean draw);
		
	}
	
	/**
	 * Window closing button
	 */
	public static interface WindowCloseButtonControl extends ButtonControl {
		
		boolean isThickBorder();
		
		void setThickBorder(boolean thickBorder);
		
	}
	
	/**
	 * Text field
	 */
	public static interface TextfieldControl extends BaseControl {
		
		RgbaColor getBackgroundColor();
		
		void setBackgroundColor(RgbaColor color);
		
		boolean isMultiLine();
		
		void setMultiLine(boolean multiLine);
		
		boolean isReadonly();
		
		void setReadonly(boolean readonly);
		
	}
	
	/**
	 * Slider
	 */
	public static interface SliderControl extends BaseControl {
		
		RgbaColor getColor();
		
		void setColor(RgbaColor color);
		
		double getValue();
		
		void setValue(double value);
		
		double getValue2();
		
		void setValue2(double value2);
		
		boolean isHorizontal();
		
		void setHorizontal(boolean horizontal);
		
	}
	
	/*
	 * Table control
	 */
	public static interface TableControl extends BaseControl {
		
		List<Object> getData();
		
		void setData(List<Object> data);
		
		int getRows();
		
		void setRows(int rows);
		
		long getVersion();
		
		void setVersion(long version);
		
		void setFirstColDoubleSize(boolean bool);
		
		boolean isFirstColDoubleSize();
		
	}
	
	/**
	 * Sets focus on a certain control
	 */
	public static interface SetFocusControl extends BaseControl {
		
		String getFocusId();
		
		void setFocusId(String id);
		
	}
	
	/**
	 * Shows a tooltip
	 */
	public static interface TooltipControl extends BaseControl {
		
	}
	
}
