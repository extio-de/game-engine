package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Graphics2D;

import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.DrawTestRenderingBo;

public class G2DDrawTest extends G2DAbstractRenderingBo implements DrawTestRenderingBo {
	
	//	private static BufferedImage TILE;
	//	
	//	private static BufferedImage TILE2;
	
	static {
		/*ResourceFileManager.load(ResourceFileManager.RESOURCE_FILE_RESOURCES, (name, stream) -> {
			try {
				TILE = ImageIO.read(stream);
				
				// Right
				//TILE = tileImg;
				
				// Left
		//				AffineTransform tx = AffineTransform.getRotateInstance(Math.toRadians(180));
		//				tx.translate(-TILE.getWidth(), -TILE.getHeight());
		//				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		//				TILE2 = op.filter(TILE, null);				
		
				// Bottom
		//				AffineTransform tx = AffineTransform.getRotateInstance(Math.toRadians(90));
		//				tx.translate(0, -TILE.getHeight());
		//				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		//				TILE2 = op.filter(TILE, null);
		
				
				// Top
		//				AffineTransform tx = AffineTransform.getRotateInstance(Math.toRadians(270));
		//				tx.translate(-TILE.getWidth(), 0);
		//				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		//				TILE2 = op.filter(TILE, null);
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}, "gfx/tilesets/0.png");*/
	}
	
	public G2DDrawTest() {
		super(RenderingBoLayer.UI1);
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		//		graphics.drawImage(TILE,
		//				600,
		//				200,
		//				null);
		//		graphics.drawImage(TILE2,
		//				600,
		//				200,
		//				null);		
	}
	
}
