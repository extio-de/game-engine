package de.extio.game_engine.keyboard;

import java.util.Map;
import java.util.Map.Entry;

public final class KeyModifiers {
	
	public final static int MODIFIER_NONE = 0;
	
	public final static int MODIFIER_SHIFT = 1;
	
	public final static int MODIFIER_CTRL = 2;
	
	public final static int MODIFIER_ALT = 4;
	
	public final static int MODIFIER_ALTGR = 8;
	
	private final static Map<Integer, String> MODIFIER_LABELS = Map.of(
			MODIFIER_SHIFT, "SHIFT ",
			MODIFIER_CTRL, "CTRL ",
			MODIFIER_ALT, "ALT ",
			MODIFIER_ALTGR, "ALTGR ");
	
	public static String getModifierLabel(final int modifier) {
		final StringBuilder label = new StringBuilder(12);
		for (final Entry<Integer, String> entry : MODIFIER_LABELS.entrySet()) {
			if ((modifier & entry.getKey().intValue()) != 0) {
				label.append(entry.getValue());
			}
		}
		return label.toString();
	}
}
