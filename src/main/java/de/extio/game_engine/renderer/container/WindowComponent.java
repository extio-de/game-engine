package de.extio.game_engine.renderer.container;

import java.util.Set;

import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.spatial2.model.CoordI2;

public interface WindowComponent {

	void setParent(Window window);

	void onAddedToWindow();

	void onRemovedFromWindow();

	Set<String> getRenderingBoIds();

	CoordI2 getRelativePosition();

	CoordI2 getRenderingBoExtraOffset(RenderingBo renderingBo);

	void draw();

}
