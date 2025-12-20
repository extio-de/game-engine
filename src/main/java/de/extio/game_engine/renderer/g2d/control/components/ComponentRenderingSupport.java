package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Graphics2D;

public class ComponentRenderingSupport {
	
	public final static record HSB(float h, float s, float b) {}
	
	public final static HSB HSB_COMPONENT_BORDER0 = new HSB(0.71f, 0.12f, 0.50f);
	
	public final static Color COLOR_COMPONENT_BORDER0 = Color.getHSBColor(HSB_COMPONENT_BORDER0.h(), HSB_COMPONENT_BORDER0.s(), HSB_COMPONENT_BORDER0.b());
	
	public final static HSB HSB_COMPONENT_BORDER1 = new HSB(0.71f, 0.12f, 0.80f);
	
	public final static Color COLOR_COMPONENT_BORDER1 = Color.getHSBColor(HSB_COMPONENT_BORDER1.h(), HSB_COMPONENT_BORDER1.s(), HSB_COMPONENT_BORDER1.b());
	
	public final static HSB HSB_COMPONENT_BORDER1_DIS = new HSB(0.71f, 0.15f, 0.20f);
	
	public final static Color COLOR_COMPONENT_BORDER1_DIS = Color.getHSBColor(HSB_COMPONENT_BORDER1_DIS.h(), HSB_COMPONENT_BORDER1_DIS.s(), HSB_COMPONENT_BORDER1_DIS.b());
	
	public final static HSB HSB_COMPONENT_BGR = new HSB(0.71f, 0.92f, 0.30f);
	
	public final static Color COLOR_COMPONENT_BGR = Color.getHSBColor(HSB_COMPONENT_BGR.h(), HSB_COMPONENT_BGR.s(), HSB_COMPONENT_BGR.b());
	
	public final static HSB HSB_COMPONENT_SELECTED_0 = new HSB(0.199f, 0.83f, 0.50f);
	
	public final static Color COLOR_COMPONENT_SELECTED_0 = Color.getHSBColor(HSB_COMPONENT_SELECTED_0.h(), HSB_COMPONENT_SELECTED_0.s(), HSB_COMPONENT_SELECTED_0.b());
	
	public final static HSB HSB_COMPONENT_SELECTED_1 = new HSB(0.199f, 0.83f, 0.90f);
	
	public final static Color COLOR_COMPONENT_SELECTED_1 = Color.getHSBColor(HSB_COMPONENT_SELECTED_1.h(), HSB_COMPONENT_SELECTED_1.s(), HSB_COMPONENT_SELECTED_1.b());
	
	public final static Color COLOR_WINDOW_BGR = new Color(0, 0, 0, 180);
	
	public static void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int w, final int h, final int strength, final Color color) {
		final int strength2 = strength * 2;
		final int strength4 = strength * 4;
		
		g2d.setColor(color);
		
		g2d.fillRect(x, y + strength2, strength, h - strength4);
		g2d.fillRect(x + w - strength, y + strength2, strength, h - strength4);
		g2d.fillRect(x + strength2, y, w - strength4, strength);
		g2d.fillRect(x + strength2, y + h - strength, w - strength4, strength);
		
		g2d.fillRect(x + strength, y + strength, strength2, strength2);
		g2d.fillRect(x + w - strength - strength2, y + strength, strength2, strength2);
		g2d.fillRect(x + strength, y + h - strength - strength2, strength2, strength2);
		g2d.fillRect(x + w - strength - strength2, y + h - strength - strength2, strength2, strength2);
	}
	
	public static void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int w, final int h, final int strength, final Color color) {
		g2d.setColor(ComponentRenderingSupport.COLOR_WINDOW_BGR);
		g2d.fillRect(x + strength * 2, y + strength * 2, w - strength * 4, h - strength * 4);
		
		drawDecorativeBorder(g2d, x, y, w, h, strength, color);
	}
	
}