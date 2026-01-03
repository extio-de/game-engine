package de.extio.game_engine.renderer.g2d.control;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DAbstractRenderingBo;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.control.impl.G2DBaseControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DButtonControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DLabelControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DSetFocusControl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DSliderControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DSwitchControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DTableControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DTextfieldControlImpl2;
import de.extio.game_engine.renderer.g2d.control.impl.G2DToggleButtonControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DTooltipControl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DWindowCloseButtonControlImpl;
import de.extio.game_engine.renderer.g2d.control.impl.G2DWindowPanelControlImpl;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

@Conditional(G2DRendererCondition.class)
@Component
public class G2DDrawControl extends G2DAbstractRenderingBo implements ControlRenderingBo {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(G2DDrawControl.class);
	
	public static Map<String, BaseControl> CACHED_CONTROLS = new HashMap<>();
	
	public static Map<String, List<BaseControl>> CONTROL_GROUPS = new HashMap<>();
	
	private Class<? extends BaseControl> clazz;
	
	private String caption;
	
	private String controlGroup;
	
	private int fontSize = G2DDrawFont.FONT_SIZE_DEFAULT;
	
	private Object customData;
	
	private Object customData2;
	
	private Object customData3;
	
	private Object customData4;
	
	private boolean visible;
	
	private boolean enabled = true;
	
	private String tooltip;
	
	private Area2 visibleArea = null;
	
	private Area2 controlArea = null;
	
	private final Area2 tempArea = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	public G2DDrawControl() {
		super(RenderingBoLayer.UI0);
	}
	
	@Override
	public ControlRenderingBo setType(final Class<? extends BaseControl> clazz) {
		this.clazz = clazz;
		return this;
	}
	
	@Override
	public ControlRenderingBo setCaption(final String caption) {
		this.caption = caption;
		return this;
	}
	
	@Override
	public ControlRenderingBo setControlGroup(final String controlGroup) {
		this.controlGroup = controlGroup;
		return this;
	}
	
	@Override
	public ControlRenderingBo setFontSize(final int size) {
		this.fontSize = size;
		return this;
	}
	
	@Override
	public ControlRenderingBo setCustomData(final Object data) {
		this.customData = data;
		return this;
	}
	
	@Override
	public ControlRenderingBo setCustomData2(final Object data) {
		this.customData2 = data;
		return this;
	}
	
	@Override
	public ControlRenderingBo setCustomData3(final Object data) {
		this.customData3 = data;
		return this;
	}
	
	@Override
	public ControlRenderingBo setCustomData4(final Object data) {
		this.customData4 = data;
		return this;
	}
	
	@Override
	public ControlRenderingBo setVisible(final boolean visible) {
		this.visible = visible;
		return this;
	}
	
	@Override
	public ControlRenderingBo setEnabled(final boolean enabled) {
		this.enabled = enabled;
		return this;
	}
	
	@Override
	public ControlRenderingBo setTooltip(final String tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	@Override
	public RenderingBo withVisibleArea(final int x, final int y, final int width, final int height) {
		if (this.visibleArea == null ||
				this.visibleArea.getPosition().getX() != x ||
				this.visibleArea.getPosition().getY() != y ||
				this.visibleArea.getDimension().getX() != width ||
				this.visibleArea.getDimension().getY() != height) {
			
			this.visibleArea = new Area2(ImmutableCoordI2.create(x, y), ImmutableCoordI2.create(width, height));
			super.withVisibleArea(x, y, width, height);
		}
		
		return this;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		if (visibleAreaX != 0 || visibleAreaY != 0 || visibleAreaWidth != 0 || visibleAreaHeight != 0) {
			if (this.controlArea == null ||
					this.controlArea.getPosition().getX() != this.x ||
					this.controlArea.getPosition().getY() != this.y ||
					this.controlArea.getDimension().getX() != this.width ||
					this.controlArea.getDimension().getY() != this.height) {
				
				this.controlArea = new Area2(ImmutableCoordI2.create(this.x, this.y), ImmutableCoordI2.create(this.width, this.height));
			}
			if (!SpatialUtils2.intersectAreasMutable(this.controlArea, this.visibleArea, this.tempArea)) {
				return;
			}
		}
		
		var control = (G2DBaseControlImpl) CACHED_CONTROLS.get(this.id);
		if (control == null) {
			if (this.clazz == ButtonControl.class) {
				control = new G2DButtonControlImpl();
			}
			else if (this.clazz == LabelControl.class) {
				control = new G2DLabelControlImpl();
			}
			else if (this.clazz == TextfieldControl.class) {
				//				control = new G2DTextfieldControlImpl();
				control = new G2DTextfieldControlImpl2();
			}
			else if (this.clazz == ToggleButtonControl.class) {
				control = new G2DToggleButtonControlImpl();
			}
			else if (this.clazz == SwitchControl.class) {
				control = new G2DSwitchControlImpl();
			}
			else if (this.clazz == WindowCloseButtonControl.class) {
				control = new G2DWindowCloseButtonControlImpl();
			}
			else if (this.clazz == WindowPanelControl.class) {
				control = new G2DWindowPanelControlImpl();
			}
			else if (this.clazz == SliderControl.class) {
				control = new G2DSliderControlImpl();
			}
			else if (this.clazz == TableControl.class) {
				control = new G2DTableControlImpl();
			}
			else if (this.clazz == SetFocusControl.class) {
				control = new G2DSetFocusControl();
			}
			else if (this.clazz == TooltipControl.class) {
				control = new G2DTooltipControl();
			}
			else if (this.clazz == null) {
				LOGGER.warn("G2DDrawControl: control type not specified for control id {}", this.id);
				return;
			}
			else {
				throw new UnsupportedOperationException("Not implemented " + this.clazz.getName());
			}
			
			control.setScaleFactor(scaleFactor)
					.setControlGroup(this.controlGroup)
					.setRendererData(this.rendererData)
					.setControlId(this.id);
			if (this.clazz == TextfieldControl.class && this.customData instanceof Boolean) {
				((TextfieldControl) control).setMultiLine(((Boolean) this.customData).booleanValue());
			}
			this.setControlProps(control, scaleFactor);
			
			control.build();
			
			CACHED_CONTROLS.put(this.id, control);
			if (this.controlGroup != null && !this.controlGroup.isEmpty()) {
				var controls = CONTROL_GROUPS.get(this.controlGroup);
				if (controls == null) {
					controls = new ArrayList<>();
					CONTROL_GROUPS.put(this.controlGroup, controls);
				}
				
				controls.add(control);
			}
			
			LOGGER.debug("Added control " + control.toString());
		}
		
		this.setControlProps(control, scaleFactor);
		control.setMainFrameGraphics(graphics);
		control.setInUse(true);
		control.render();
	}
	
	@SuppressWarnings("unchecked")
	private void setControlProps(final G2DBaseControlImpl control, final double scaleFactor) {
		control.setScaleFactor(scaleFactor);
		control.setX((int) (this.x * scaleFactor));
		control.setY((int) (this.y * scaleFactor));
		control.setFontSize(this.fontSize);
		control.setWidth((int) (this.width * scaleFactor));
		control.setHeight((int) (this.height * scaleFactor));
		control.setVisible(this.visible);
		control.setEnabled(this.enabled);
		control.setCaption(this.caption);
		control.setTooltip(this.tooltip);
		control.setZIndex(this.zIndex);
		control.setLayer(this.layer);
		control.setVisibleArea(
				(int) (this.visibleAreaX * scaleFactor),
				(int) (this.visibleAreaY * scaleFactor),
				(int) (this.visibleAreaWidth * scaleFactor),
				(int) (this.visibleAreaHeight * scaleFactor));
		
		if (this.clazz == LabelControl.class) {
			((LabelControl) control).setForegroundColor(this.color);
			if (this.customData instanceof RgbaColor) {
				((LabelControl) control).setBackgroundColor((RgbaColor) this.customData);
			}
			if (this.customData2 instanceof HorizontalAlignment) {
				((LabelControl) control).setTextAlignment((HorizontalAlignment) this.customData2);
			}
		}
		else if (this.clazz == ToggleButtonControl.class) {
			((ButtonControl) control).setBackgroundColor(this.color);
			if (this.customData instanceof Boolean) {
				((ToggleButtonControl) control).setToggled(((Boolean) this.customData).booleanValue());
			}
			if (this.customData2 instanceof StaticResource) {
				((ButtonControl) control).setIconResource((StaticResource) this.customData2);
			}
		}
		else if (this.clazz == SwitchControl.class) {
			((ButtonControl) control).setBackgroundColor(this.color);
			if (this.customData instanceof Boolean) {
				((SwitchControl) control).setToggled(((Boolean) this.customData).booleanValue());
			}
			if (this.customData2 instanceof StaticResource) {
				((ButtonControl) control).setIconResource((StaticResource) this.customData2);
			}
			if (this.customData4 instanceof Boolean) {
				((SwitchControl) control).setDrawBorder(((Boolean) this.customData4).booleanValue());
			}
		}
		else if (this.clazz == ButtonControl.class) {
			((ButtonControl) control).setBackgroundColor(this.color);
			if (this.customData2 instanceof StaticResource) {
				((ButtonControl) control).setIconResource((StaticResource) this.customData2);
			}
		}
		else if (this.clazz == WindowCloseButtonControl.class) {
			((ButtonControl) control).setBackgroundColor(this.color);
			if (this.customData instanceof Boolean) {
				((WindowCloseButtonControl) control).setThickBorder(((Boolean) this.customData).booleanValue());
			}
		}
		else if (this.clazz == WindowPanelControl.class) {
			((WindowPanelControl) control).setColor(this.color);
			if (this.customData instanceof Boolean) {
				((WindowPanelControl) control).setThickBorder(((Boolean) this.customData).booleanValue());
			}
		}
		else if (this.clazz == SliderControl.class) {
			((SliderControl) control).setColor(this.color);
			if (this.customData instanceof Boolean) {
				((SliderControl) control).setHorizontal(((Boolean) this.customData).booleanValue());
			}
			if (this.customData2 instanceof Double) {
				((SliderControl) control).setValue(((Double) this.customData2).doubleValue());
			}
			if (this.customData3 instanceof Double) {
				((SliderControl) control).setValue2(((Double) this.customData3).doubleValue());
			}
		}
		else if (this.clazz == TableControl.class) {
			if (this.customData instanceof List) {
				((TableControl) control).setData((List<Object>) this.customData);
			}
			if (this.customData2 instanceof Integer) {
				((TableControl) control).setRows((Integer) this.customData2);
			}
			if (this.customData3 instanceof Long) {
				((TableControl) control).setVersion((Long) this.customData3);
			}
			if (this.customData4 instanceof Boolean) { // This parameter could be changed to a more generic way to set column sizing 
				((TableControl) control).setFirstColDoubleSize((Boolean) this.customData4);
			}
		}
		else if (this.clazz == SetFocusControl.class) {
			if (this.customData instanceof String) {
				((SetFocusControl) control).setFocusId((String) this.customData);
			}
		}
		else if (this.clazz == TextfieldControl.class) {
			((TextfieldControl) control).setBackgroundColor(this.color);
			if (this.customData2 instanceof Boolean) {
				((TextfieldControl) control).setReadonly((Boolean) this.customData2);
			}
		}
	}
	
	@Override
	public void apply(final RenderingBo other) {
		super.apply(other);
		
		if (other instanceof final G2DDrawControl o) {
			this.id = o.id;
			this.clazz = o.clazz;
			this.caption = o.caption;
			this.controlGroup = o.controlGroup;
			this.fontSize = o.fontSize;
			this.customData = o.customData;
			this.customData2 = o.customData2;
			this.customData3 = o.customData3;
			this.customData4 = o.customData4;
			this.visible = o.visible;
			this.enabled = o.enabled;
			this.tooltip = o.tooltip;
			this.visibleArea = o.visibleArea;
			this.controlArea = o.controlArea;
		}
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		
		this.id = null;
		this.clazz = null;
		this.caption = null;
		this.controlGroup = null;
		this.fontSize = G2DDrawFont.FONT_SIZE_DEFAULT;
		this.customData = null;
		this.customData2 = null;
		this.customData3 = null;
		this.customData4 = null;
		this.visible = false;
		this.enabled = true;
		this.tooltip = null;
		this.visibleArea = null;
		this.controlArea = null;
	}
	
	@Override
	public void staticCleanupAfterFrame() {
		final var it = CACHED_CONTROLS.values().iterator();
		while (it.hasNext()) {
			final var control = it.next();
			if (control.getInUse()) {
				control.setInUse(false);
			}
			else {
				if (control.getControlGroup() != null && !control.getControlGroup().isEmpty()) {
					final var controls = CONTROL_GROUPS.get(control.getControlGroup());
					if (controls != null) {
						controls.remove(control);
					}
				}
				
				control.close();
				it.remove();
				LOGGER.debug("Removed control " + control.toString());
			}
		}
	}
	
	public static void reset() {
		CACHED_CONTROLS.values().forEach(control -> control.close());
		CACHED_CONTROLS.clear();
	}
	
}
