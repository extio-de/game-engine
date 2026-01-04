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
	
	private LabelData labelData;
	
	private ButtonData buttonData;
	
	private ToggleButtonData toggleButtonData;
	
	private SwitchData switchData;
	
	private WindowCloseButtonData windowCloseButtonData;
	
	private WindowPanelData windowPanelData;
	
	private TextfieldData textfieldData;
	
	private SliderData sliderData;
	
	private TableData tableData;
	
	private SetFocusData setFocusData;

	private Object customData;
	
	private boolean visible;
	
	private boolean enabled = true;
	
	private String tooltip;
	
	private Area2 visibleArea = null;
	
	private Area2 controlArea = null;
	
	private final Area2 tempArea = new Area2(MutableCoordI2.create(), MutableCoordI2.create());

	private List<CustomControlConfiguration<? extends G2DBaseControlImpl>> customControlConfigurations;
	
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
	public ControlRenderingBo setControlData(final Object data) {
		if (data instanceof final LabelData labelData) {
			this.labelData = labelData;
		}
		else if (data instanceof final ButtonData buttonData) {
			this.buttonData = buttonData;
		}
		else if (data instanceof final ToggleButtonData toggleButtonData) {
			this.toggleButtonData = toggleButtonData;
		}
		else if (data instanceof final SwitchData switchData) {
			this.switchData = switchData;
		}
		else if (data instanceof final WindowCloseButtonData windowCloseButtonData) {
			this.windowCloseButtonData = windowCloseButtonData;
		}
		else if (data instanceof final WindowPanelData windowPanelData) {
			this.windowPanelData = windowPanelData;
		}
		else if (data instanceof final TextfieldData textfieldData) {
			this.textfieldData = textfieldData;
		}
		else if (data instanceof final SliderData sliderData) {
			this.sliderData = sliderData;
		}
		else if (data instanceof final TableData tableData) {
			this.tableData = tableData;
		}
		else if (data instanceof final SetFocusData setFocusData) {
			this.setFocusData = setFocusData;
		}
		else {
			this.customData = data;
		}
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

		if (this.customControlConfigurations == null) {
			this.customControlConfigurations = List.copyOf( this.rendererData.getApplicationContext().getBeansOfType(CustomControlConfiguration.class).values());
		}
		
		var control = (G2DBaseControlImpl) CACHED_CONTROLS.get(this.id);
		if (control == null) {
			if (this.clazz == null) {
				LOGGER.warn("G2DDrawControl: control type not specified for control id {}", this.id);
				return;
			}			
			else if (this.clazz == ButtonControl.class) {
				control = new G2DButtonControlImpl();
			}
			else if (this.clazz == LabelControl.class) {
				control = new G2DLabelControlImpl();
			}
			else if (this.clazz == TextfieldControl.class) {
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
			else {
				for (final var customControlConfiguration : this.customControlConfigurations) {
					if (customControlConfiguration.getControlClass().equals(this.clazz)) {
						control = customControlConfiguration.createControl();
						break;
					}
				}
				
				if (control == null) {
					throw new IllegalArgumentException("Control type not registered: " + this.clazz.getName());
				}
			}
			
			control.setScaleFactor(scaleFactor)
					.setControlGroup(this.controlGroup)
					.setRendererData(this.rendererData)
					.setControlId(this.id);
			if (this.clazz == TextfieldControl.class && this.textfieldData != null) {
				((TextfieldControl) control).setCustomData(this.textfieldData);
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
		
		if (this.clazz == LabelControl.class && this.labelData != null) {
			((LabelControl) control).setCustomData(this.labelData);
		}
		else if (this.clazz == ToggleButtonControl.class && this.toggleButtonData != null) {
			((ToggleButtonControl) control).setCustomData(this.toggleButtonData);
		}
		else if (this.clazz == SwitchControl.class && this.switchData != null) {
			((SwitchControl) control).setCustomData(this.switchData);
		}
		else if (this.clazz == ButtonControl.class && this.buttonData != null) {
			((ButtonControl) control).setCustomData(this.buttonData);
		}
		else if (this.clazz == WindowCloseButtonControl.class && this.windowCloseButtonData != null) {
			((WindowCloseButtonControl) control).setCustomData(this.windowCloseButtonData);
		}
		else if (this.clazz == WindowPanelControl.class && this.windowPanelData != null) {
			((WindowPanelControl) control).setCustomData(this.windowPanelData);
		}
		else if (this.clazz == SliderControl.class && this.sliderData != null) {
			((SliderControl) control).setCustomData(this.sliderData);
		}
		else if (this.clazz == TableControl.class && this.tableData != null) {
			((TableControl) control).setCustomData(this.tableData);
		}
		else if (this.clazz == SetFocusControl.class && this.setFocusData != null) {
			((SetFocusControl) control).setCustomData(this.setFocusData);
		}
		else if (this.clazz == TextfieldControl.class && this.textfieldData != null) {
			((TextfieldControl) control).setCustomData(this.textfieldData);
		}
		else {
			for (final var customControlConfiguration : this.customControlConfigurations) {
				if (customControlConfiguration.getControlClass().equals(this.clazz)) {
					((CustomControlConfiguration) customControlConfiguration).setCustomData(control, this.customData);
					break;
				}
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
			this.labelData = o.labelData;
			this.buttonData = o.buttonData;
			this.toggleButtonData = o.toggleButtonData;
			this.switchData = o.switchData;
			this.windowCloseButtonData = o.windowCloseButtonData;
			this.windowPanelData = o.windowPanelData;
			this.textfieldData = o.textfieldData;
			this.sliderData = o.sliderData;
			this.tableData = o.tableData;
			this.setFocusData = o.setFocusData;
			this.customData = o.customData;
			this.visible = o.visible;
			this.enabled = o.enabled;
			this.tooltip = o.tooltip;
			this.visibleArea = o.visibleArea;
			this.controlArea = o.controlArea;
			this.customControlConfigurations = o.customControlConfigurations;
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
		this.labelData = null;
		this.buttonData = null;
		this.toggleButtonData = null;
		this.switchData = null;
		this.windowCloseButtonData = null;
		this.windowPanelData = null;
		this.textfieldData = null;
		this.sliderData = null;
		this.tableData = null;
		this.setFocusData = null;
		this.customData = null;
		this.visible = false;
		this.enabled = true;
		this.tooltip = null;
		this.visibleArea = null;
		this.controlArea = null;
		this.customControlConfigurations = null;
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
