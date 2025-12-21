package de.extio.game_engine.renderer.g2d;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import de.extio.game_engine.keyboard.KeyModifiers;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.g2d.control.G2DControlHasExclusiveKeyEvent;
import de.extio.game_engine.renderer.model.event.KeyStrokeEvent;
import de.extio.game_engine.renderer.model.event.MouseClickEvent;
import de.extio.game_engine.renderer.model.event.MouseEnterEvent;
import de.extio.game_engine.renderer.model.event.MouseMoveEvent;
import de.extio.game_engine.renderer.model.options.VideoOptions;
import de.extio.game_engine.renderer.model.options.VideoOptions.VideoOptionsVideoMode;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class G2DMainFrame extends Frame {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(G2DMainFrame.class);
	
	private final RendererData rendererData;
	
	private AWTEventListener aWTEventListener;
	
	private volatile int fullScreenIdxRegistered = -1;
	
	public volatile boolean resizeListenerEnabled;
	
	public G2DMainFrame(final RendererData rendererData) {
		LOGGER.debug("ctor");
		
		this.rendererData = rendererData;
		this.setIgnoreRepaint(true);
		this.setTitle(this.getClass().getCanonicalName());
		this.setLayout(null);
		this.setBackground(Color.BLACK);
		
		try (var in = this.getClass().getClassLoader().getResourceAsStream("icon.png")) {
			if (in != null) {
				final var icon = ImageIO.read(in);
				this.setIconImage(icon);
			}
		}
		catch (final IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(final WindowEvent e) {
				
			}
			
			@Override
			public void windowIconified(final WindowEvent e) {
				
			}
			
			@Override
			public void windowDeiconified(final WindowEvent e) {
				
			}
			
			@Override
			public void windowDeactivated(final WindowEvent e) {
				
			}
			
			@Override
			public void windowClosing(final WindowEvent e) {
				LOGGER.info("Window closing requested");
				SpringApplication.exit(G2DMainFrame.this.rendererData.getApplicationContext(), () -> 0);
			}
			
			@Override
			public void windowClosed(final WindowEvent e) {
				
			}
			
			@Override
			public void windowActivated(final WindowEvent e) {
				
			}
		});
		
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(final ComponentEvent e) {
				
			}
			
			@Override
			public void componentResized(final ComponentEvent e) {
				if (G2DMainFrame.this.resizeListenerEnabled) {
					((G2DRendererControl) G2DMainFrame.this.rendererData.getRendererControl()).updateViewPort(true, true);
				}
			}
			
			@Override
			public void componentMoved(final ComponentEvent e) {
				
			}
			
			@Override
			public void componentHidden(final ComponentEvent e) {
				
			}
		});
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				try {
					G2DMainFrame.this.rendererData.getEventService().fire(new MouseClickEvent(false, G2DMainFrame.getModifiers(e), e.getButton(), ImmutableCoordI2.create(e.getX(), e.getY())));
				}
				catch (final Exception exc) {
					LOGGER.warn(exc.getMessage());
				}
			}
			
			@Override
			public void mousePressed(final MouseEvent e) {
				try {
					G2DMainFrame.this.requestFocus();
					G2DMainFrame.this.rendererData.getEventService().fire(new MouseClickEvent(true, G2DMainFrame.getModifiers(e), e.getButton(), ImmutableCoordI2.create(e.getX(), e.getY())));
				}
				catch (final Exception exc) {
					LOGGER.warn(exc.getMessage());
				}
			}
			
			@Override
			public void mouseExited(final MouseEvent e) {
				try {
					G2DMainFrame.this.rendererData.getEventService().fire(new MouseMoveEvent(false, G2DMainFrame.getModifiers(e), null, 0));
				}
				catch (final Exception exc) {
					LOGGER.warn(exc.getMessage());
				}
			}
			
			@Override
			public void mouseEntered(final MouseEvent e) {
				try {
					G2DMainFrame.this.rendererData.getEventService().fire(new MouseEnterEvent(G2DMainFrame.getModifiers(e), ImmutableCoordI2.create(e.getX(), e.getY())));
				}
				catch (final Exception exc) {
					LOGGER.warn(exc.getMessage());
				}
			}
			
			@Override
			public void mouseClicked(final MouseEvent e) {
				
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(final MouseEvent e) {
				final var button = this.getButton(e);
				
				try {
					G2DMainFrame.this.rendererData.getEventService().fire(new MouseMoveEvent(false, G2DMainFrame.getModifiers(e), ImmutableCoordI2.create(e.getX(), e.getY()), button));
				}
				catch (final Exception exc) {
					if ("Engine not started up yet".equals(exc.getMessage())) {
						return;
					}
					LOGGER.error(exc.getMessage(), exc);
				}
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				final var button = this.getButton(e);
				
				try {
					G2DMainFrame.this.rendererData.getEventService().fire(new MouseMoveEvent(true, G2DMainFrame.getModifiers(e), ImmutableCoordI2.create(e.getX(), e.getY()), button));
				}
				catch (final Exception exc) {
					if ("Engine not started up yet".equals(exc.getMessage())) {
						return;
					}
					LOGGER.error(exc.getMessage(), exc);
				}
			}
			
			private int getButton(final MouseEvent e) {
				var button = MouseEvent.NOBUTTON;
				if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
					button = MouseEvent.BUTTON1;
				}
				else if ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
					button = MouseEvent.BUTTON2;
				}
				else if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
					button = MouseEvent.BUTTON3;
				}
				return button;
			}
		});
		
		this.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				try {
					G2DMainFrame.this.rendererData.getEventService().fire(new MouseClickEvent(true, G2DMainFrame.getModifiers(e), e.getWheelRotation() == -1 ? 4 : 5, ImmutableCoordI2.create(e.getX(), e.getY())));
				}
				catch (final Exception exc) {
					LOGGER.warn(exc.getMessage());
				}
			}
		});
		
		this.aWTEventListener = new AWTEventListener() {
			
			@Override
			public void eventDispatched(final AWTEvent event) {
				if (!(event instanceof final KeyEvent e)) {
					return;
				}
				
				if (e.getComponent() instanceof G2DControlHasExclusiveKeyEvent) {
					return;
				}
				
				if (event.getID() == KeyEvent.KEY_PRESSED) {
					LOGGER.trace("Press {} {} {}", e.getKeyCode(), KeyEvent.getKeyText(e.getKeyCode()), G2DMainFrame.getModifiers(e));
					final var keyPressMessage = new KeyStrokeEvent(false, e.getKeyCode(), KeyEvent.getKeyText(e.getKeyCode()), G2DMainFrame.getModifiers(e));
					G2DMainFrame.this.rendererData.getEventService().fire(keyPressMessage);
				}
				else if (event.getID() == KeyEvent.KEY_RELEASED) {
					LOGGER.trace("Release {} {} {}", e.getKeyCode(), KeyEvent.getKeyText(e.getKeyCode()), G2DMainFrame.getModifiers(e));
					if (G2DMainFrame.this.rendererData.getKeycodeRegistry().check("toggleFullScreen", e.getKeyCode(), G2DMainFrame.getModifiers(e))) {
						final VideoOptions clientVideoOptions = G2DMainFrame.this.rendererData.getVideoOptions();
						if (clientVideoOptions.getVideoMode() == VideoOptionsVideoMode.FULLSCREEN || clientVideoOptions.getVideoMode() == VideoOptionsVideoMode.BORDERLESS) {
							clientVideoOptions.setVideoMode(VideoOptionsVideoMode.WINDOW);
						}
						else {
							if (Objects.requireNonNullElse(System.getProperty("os.name"), "").toLowerCase().contains("windows")) {
								clientVideoOptions.setVideoMode(VideoOptionsVideoMode.BORDERLESS);
							}
							else {
								clientVideoOptions.setVideoMode(VideoOptionsVideoMode.FULLSCREEN);
							}
						}
						G2DMainFrame.this.rendererData.getRendererControl().applyVideoOptions();
					}
					else {
						final var keyPressMessage = new KeyStrokeEvent(true, e.getKeyCode(), KeyEvent.getKeyText(e.getKeyCode()), G2DMainFrame.getModifiers(e));
						G2DMainFrame.this.rendererData.getEventService().fire(keyPressMessage);
					}
				}
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(this.aWTEventListener, AWTEvent.KEY_EVENT_MASK);
	}
	
	@Override
	public void update(final Graphics g) {
		// The standard update() method fills background of sub-components with bgr color - I don't want that!
		
		// Now that all components are cached light weights, I don't need AWT updates at all
		//		if (this.iSSHOWING()) {
		//			THIS.PAINT(G);
		//		}
	}
	
	@Override
	public void dispose() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(this.aWTEventListener);
		
		super.dispose();
	}
	
	public void windowedPre() {
		LOGGER.debug("windowedPre");
		
		this.setSize(795, 596);
		this.setVisible(true);
		Toolkit.getDefaultToolkit().sync();
	}
	
	public void windowed() {
		LOGGER.debug("windowed");
		
		this.unregisterFullScreenWindow();
		this.setUndecorated(false);
		this.setAlwaysOnTop(false);
		
		this.applyReferenceWindowGeometry();
		this.setVisible(true);
		Toolkit.getDefaultToolkit().sync();
		
		EventQueue.invokeLater(() -> {
			this.toFront();
		});
	}
	
	public void fullscreen() {
		LOGGER.debug("windowedFull {}", this.rendererData.getVideoOptions().getFullScreenNumber());
		
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		
		this.unregisterFullScreenWindow();
		this.registerFullScreenWindow(this.rendererData.getVideoOptions().getFullScreenNumber(), this);
		Toolkit.getDefaultToolkit().sync();
	}
	
	private void registerFullScreenWindow(final int screenIdx, final Window window) {
		final var env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device;
		if (screenIdx > -1 && screenIdx < env.getScreenDevices().length) {
			device = env.getScreenDevices()[screenIdx];
			this.fullScreenIdxRegistered = window != null ? screenIdx : -1;
		}
		else {
			device = env.getDefaultScreenDevice();
			for (var i = 0; i < env.getScreenDevices().length; i++) {
				if (device.getIDstring().equals(env.getScreenDevices()[i].getIDstring())) {
					this.rendererData.getVideoOptions().setFullScreenNumber(i);
					this.fullScreenIdxRegistered = window != null ? screenIdx : -1;
					break;
				}
			}
		}
		
		if (window != null) {
			final var x = (int) device.getDefaultConfiguration().getBounds().getWidth();
			final var y = (int) device.getDefaultConfiguration().getBounds().getHeight();
			LOGGER.debug("Full screen size: {}x{}", x, y);
			window.setSize(x, y);
		}
		
		device.setFullScreenWindow(window);
	}
	
	public void unregisterFullScreenWindow() {
		if (this.fullScreenIdxRegistered > -1) {
			LOGGER.debug("Unregister full screen window {}", this.fullScreenIdxRegistered);
			this.registerFullScreenWindow(this.fullScreenIdxRegistered, null);
			this.fullScreenIdxRegistered = -1;
		}
	}
	
	public void borderless() {
		LOGGER.debug("borderless {}", this.rendererData.getVideoOptions().getFullScreenNumber());
		
		this.unregisterFullScreenWindow();
		
		this.setUndecorated(true);
		
		this.setupBorderlessWindow(this.rendererData.getVideoOptions().getFullScreenNumber());
		Toolkit.getDefaultToolkit().sync();
		
		this.setAlwaysOnTop(true);
	}
	
	private void setupBorderlessWindow(final int screenIdx) {
		final var env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device;
		if (screenIdx > -1 && screenIdx < env.getScreenDevices().length) {
			device = env.getScreenDevices()[screenIdx];
		}
		else {
			device = env.getDefaultScreenDevice();
			for (var i = 0; i < env.getScreenDevices().length; i++) {
				if (device.getIDstring().equals(env.getScreenDevices()[i].getIDstring())) {
					this.rendererData.getVideoOptions().setFullScreenNumber(i);
					break;
				}
			}
		}
		
		final var location = device.getDefaultConfiguration().getBounds().getLocation();
		LOGGER.debug("Borderless screen location: {}", location);
		this.setLocation(location);
		final var bounds = device.getDefaultConfiguration().getBounds();
		LOGGER.debug("Borderless screen size: {}", bounds);
		this.setSize(bounds.width, bounds.height);
		
		this.setVisible(true);
		//		this.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
	
	public void createBufferStrategy() {
		this.createBufferStrategy(2);
		//		Toolkit.getDefaultToolkit().sync();
	}
	
	private void applyReferenceWindowGeometry() {
		// final var mode = this.getGraphicsConfiguration().getDevice().getDisplayMode();
		// final var size = ImmutableCoordI2.create(Math.min(mode.getWidth(), RendererControl.REFERENCE_RESOLUTION.getX()), Math.min(mode.getHeight(), RendererControl.REFERENCE_RESOLUTION.getY()));
		// this.setLocation(mode.getWidth() / 2 - size.getX() / 2, mode.getHeight() / 2 - size.getY() / 2);
		final var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final var bounds = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		this.setLocation(bounds.x, bounds.y);
		this.setSize((int) Math.min(bounds.getWidth(), RendererControl.REFERENCE_RESOLUTION.getX()), (int) Math.min(bounds.getHeight(), RendererControl.REFERENCE_RESOLUTION.getY()));
	}
	
	private static int getModifiers(final InputEvent event) {
		var result = 0;
		
		if (event.isShiftDown()) {
			result |= KeyModifiers.MODIFIER_SHIFT;
		}
		if (event.isAltDown()) {
			result |= KeyModifiers.MODIFIER_ALT;
		}
		if (event.isAltGraphDown()) {
			result |= KeyModifiers.MODIFIER_ALTGR;
		}
		if (event.isControlDown()) {
			result |= KeyModifiers.MODIFIER_CTRL;
		}
		
		return result;
	}
}
