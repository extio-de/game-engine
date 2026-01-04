package de.extio.game_engine.renderer.g2d.control;

import de.extio.game_engine.renderer.g2d.control.impl.G2DBaseControlImpl;

public interface CustomControlConfiguration<T extends G2DBaseControlImpl> {
	
	Class<T> getControlInterface();
	
	T createControl();
	
	void setCustomData(T controlImpl, Object data);
	
}
