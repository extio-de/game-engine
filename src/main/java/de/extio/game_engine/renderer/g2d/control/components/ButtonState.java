package de.extio.game_engine.renderer.g2d.control.components;

public class ButtonState {
	
	private volatile boolean toggled;
	
	private volatile boolean pressed;
	
	private volatile boolean hovered;
	
	public boolean isToggled() {
		return this.toggled;
	}
	
	public void setToggled(final boolean toggled) {
		this.toggled = toggled;
	}
	
	public boolean isPressed() {
		return this.pressed;
	}
	
	public void setPressed(final boolean pressed) {
		this.pressed = pressed;
	}
	
	public boolean isHovered() {
		return this.hovered;
	}
	
	public void setHovered(final boolean hovered) {
		this.hovered = hovered;
	}
	
	public void toggleToggled() {
		this.toggled = !this.toggled;
	}
	
}
