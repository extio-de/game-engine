# Exo's Game Engine

## Purpose

A modular game engine framework for building complex 2D games with comprehensive support for rendering, audio, localization, event management, and persistent storage. The engine provides a flexible module system that allows games to be composed of reusable, lifecycle-managed components.

## Overview

Built on Spring Boot 4.0 and Java 25, the engine leverages dependency injection and auto-configuration to provide a robust, professional-grade foundation for game development. Game functionality is organized into modules that can be dynamically loaded, activated, and managed during runtime, enabling complex UI state management and flexible game logic implementations.

### Core Capabilities

- **Module System**: Extensible module architecture with lifecycle management (load, unload, activate, deactivate) and display state control
- **Rendering Engine**: 2D rendering system supporting both cycle-based and stateful rendering approaches, with pooled rendering objects
- **Event System**: Concurrent, type-safe event bus using structured concurrency for decoupled component communication
- **Storage Service**: UUID and path-based persistent storage with ZSTD compression and indexed lookups
- **Resource Management**: Static resource loading from filesystem with deserialization and streaming support
- **Internationalization (i18n)**: YAML-based localization system with integrated GUI editor
- **Input Handling**: Rebindable key binding system with modifier support
- **Audio System**: Multi-strategy audio playback (software mixing and Java mixer) with OGG Vorbis support
- **Steamworks Integration**: Steam platform features including achievements, lobbies, rich presence, and workshop integration
- **Spatial Systems**: 2D spatial calculations with multiple indexing strategies (QuadTree, GridIndex, etc.)
- **Utilities**: Core functionality including YAML serialization, ring buffers, and UUID generation

### Architecture

The engine is structured around the following key subsystems:

- `module/` - Module lifecycle and service management
- `renderer/` - 2D rendering abstractions, working sets, and rendering object pooling
- `audio/` - Audio playback strategies and control
- `event/` - Concurrent event publishing and subscription
- `i18n/` - Localization management and editing
- `storage/` - Data persistence with compression and indexing
- `resource/` - Static resource loading from filesystem
- `keyboard/` - Key binding registry and input handling
- `steamworks/` - Steam platform integration
- `spatial2/` - 2D spatial utilities and indexing structures
- `menu/` - Menu system components
- `util/` - Core utilities for serialization, buffers, and RNG

The engine uses Spring's auto-configuration mechanism to wire components together, making it easy to extend and customize behavior through standard Spring configuration patterns. Each subsystem can be independently enabled/disabled via configuration properties, allowing developers to use only the components they need.

### Demo Module

The repository includes [DemoModule.java](src/test/java/de/extio/game_engine/demo/DemoModule.java), a complete example implementation demonstrating core engine features:

- **Module Lifecycle**: Shows proper implementation of `onLoad()`, `onActivate()`, `onDeactivate()`, `onShow()`, `onHide()`, and `onUnload()` callbacks
- **Window Management**: Creates and manages multiple draggable windows with different configurations
- **Rendering**: Demonstrates rendering objects including labels, images, and buttons using the rendering pool
- **Event Handling**: Registers UI control event handlers and responds to user interactions
- **Audio Integration**: Plays background music and sound effects using the audio controller
- **Localization**: Uses the i18n system for translating UI text
- **Sub-module Management**: Shows how parent modules can manage child module lifecycle and display states

The demo module serves as both a reference implementation and a starting point for building new game modules.

## Background

Exo's Game Engine is derived from [Spacecraft Tactics](https://store.steampowered.com/app/2642080/Spacecraft_Tactics), a 2D turn-based tactical space game released on Steam in 2023. The engine extracts and modernizes the core systems that powered Spacecraft Tactics' UI, campaign, and built-in editors. The engine does not include Spacecraft Tactics' Groovy-based mod system or game-specific logic such as the composite/entity system or tileset rendering. It focuses on the fundamental building blocks: modules, extensible renderer, events, storage, and other core subsystems that can be used to build new games.

The engine also serves as the foundation for **Exo's CYOA**, an LLM-driven choose-your-own-adventure game that combines the game engine's rendering, module, and UI systems with agentic AI flows to create interactive narrative experiences.

---

## Domain Components

### Module System

#### Purpose and Overview
The Module System provides the core extensibility framework for the game engine. Modules are Spring-managed components with a well-defined lifecycle (load, activate, deactivate, unload) that can subscribe to execution callbacks and interact with the renderer. This system enables games to be built as a composition of loosely-coupled, reusable modules that can be dynamically managed at runtime.

Modules can be either general-purpose (`AbstractModule`) or client-facing with UI capabilities (`AbstractClientModule`). The system supports active/inactive states and display/hidden states for client modules, enabling complex UI management scenarios.

#### Setup / Autoconfiguration
**Configuration Class**: `ModuleServiceAutoConfiguration`

**Property**: `game-engine.module.enabled` (default: `true`)

The module system auto-configures when any `AbstractModule` beans are present in the application context. Modules are automatically discovered and registered during application startup.

#### Exposed Spring Beans
- **`ModuleService`**: Main service for managing module lifecycle, activation states, and display states
- **`ModuleExecutor`**: Executes module callbacks at specific points in the game loop

#### Module Lifecycle and Automatic Cleanup

The Module System implements a comprehensive lifecycle with four states: **load**, **activate**, **deactivate**, and **unload**. When modules transition out of active state or are unloaded, the system automatically performs cleanup operations to prevent resource leaks and stale registrations:

#### Lifecycle Callbacks and State Transitions

Modules implement lifecycle callbacks that are invoked during state transitions:

**Core Lifecycle Methods (AbstractModule):**
- **`onLoad()`**: Called once when the module is registered with the module service. Initialize dependencies, create sub-modules, and perform one-time setup.
- **`onActivate()`**: Called when the module becomes active. Register event handlers, subscribe to execution callbacks, and initialize runtime state.
- **`onDeactivate()`**: Called when the module becomes inactive. Clean up runtime state. Event handlers and renderer working sets are automatically cleared.
- **`onUnload()`**: Called when the module is being removed. Perform final cleanup. The module is automatically deactivated first.

**Additional Client Module Methods (AbstractClientModule):**
- **`onShow()`**: Called when the module becomes visible in the UI. Start animations, play music, or create UI elements.
- **`onHide()`**: Called when the module is hidden from the UI. Stop animations, pause music, or dispose resource-heavy UI elements (UI elements are not displayed automatically and can stay in the renderer working set).

**Typical State Flow:**
```
Load → Activate → Show (client modules)
     ↓           ↑
     Unload ← Deactivate ← Hide (client modules)
```

**Example Module Implementation:**
```java
public class DemoModule extends AbstractClientModule {
    @Autowired
    private EventService eventService;
    
    @Autowired
    private RenderingBoPool renderingBoPool;
    
    @Autowired
    private AudioController audioController;
    
    private Window mainWindow;
    
    @Override
    public void onLoad() {
        // One-time initialization
        this.mainWindow = applicationContext.getBean(Window.class);
        this.mainWindow.setNormalizedPosition(...);
        this.mainWindow.setNormalizedDimension(...);
        
        // Optionally activate and display immediately
        this.getModuleService().changeActiveState(this.getId(), true);
        this.getModuleService().changeDisplayState(this.getId(), true);
    }
    
    @Override
    public void onActivate() {
        // Create rendering objects
        var label = this.renderingBoPool.acquire("MyLabel", ControlRenderingBo.class)
            .setCaption("Welcome!")
            .setFontSize(48);
        this.mainWindow.putRenderingBo(label);
        
        // Register event handlers (automatically cleaned up on deactivate)
        this.eventService.register(UiControlEvent.class, this.getId(), this::onUiEvent);
        
        // Activate sub-modules if needed
        this.getModuleService().changeActiveState(this.mainWindow.getId(), true);
    }
    
    @Override
    public void onDeactivate() {
        // Deactivate sub-modules
        this.getModuleService().changeActiveState(this.mainWindow.getId(), false);
        // Note: Event handlers and renderer working set are automatically cleaned up
    }
    
    @Override
    public void onShow() {
        // Show sub-module UI
        this.getModuleService().changeDisplayState(this.mainWindow.getId(), true);
        // Start music or animations
        this.audioController.playMusic(...);
    }
    
    @Override
    public void onHide() {
        // Hide sub-module UI
        this.getModuleService().changeDisplayState(this.mainWindow.getId(), false);
        // Stop music or animations
        this.audioController.stopMusic();
    }
    
    @Override
    public void onUnload() {
        // Clean up sub-modules
        this.getModuleService().unloadModule(this.mainWindow.getId());
    }
}
```

**Automatic Cleanup on Deactivation:**
- All event handlers registered by the module are automatically unregistered from the event system
- For client modules (`AbstractClientModule`), the renderer working set is automatically cleared
- Module's execution callback subscriptions are removed from the executor
- The module's `onDeactivate()` lifecycle method is invoked

**Automatic Cleanup on Unload:**
- The module is first deactivated (if active), triggering all deactivation cleanup
- All event handlers registered by the module are unregistered from the event system
- For client modules, the renderer working set is cleared
- The module's `onUnload()` lifecycle method is invoked
- The module is removed from the module registry

This automatic cleanup ensures that modules can be safely loaded, activated, deactivated, and unloaded during runtime without leaving behind orphaned event handlers or rendering objects. Modules don't need to manually unregister their event handlers or clear their rendering objects in their lifecycle callbacks - the system handles this automatically.

---

### Event System

#### Purpose and Overview
A lightweight, type-safe event bus that enables decoupled communication between game components. Components can register event handlers for specific event types and fire events that will be dispatched to all registered consumers. This pattern eliminates tight coupling between modules and simplifies complex interactions.

Events are processed asynchronously on a dedicated event processor thread using a blocking queue. When multiple handlers are registered for the same event type, they are executed concurrently using structured concurrency (virtual threads via `StructuredTaskScope`), providing excellent scalability for event-heavy workloads. Single handlers execute directly without concurrency overhead. Each consumer is identified by a unique string ID to enable selective unregistration.

#### Setup / Autoconfiguration
**Configuration Class**: `EventsAutoConfiguration`

**Property**: `game-engine.events.enabled` (default: `true`)

Auto-configures the event system with no additional setup required.

#### Exposed Spring Beans
- **`EventService`**: Main interface for registering consumers and firing events
- **`EventHandlerRegistry`**: Internal registry managing event handler mappings
- **`EventExecutor`**: Handles event dispatch to registered consumers

#### Integration with Module System

The Event System integrates seamlessly with the Module System to provide automatic cleanup of event registrations. When a module is deactivated or unloaded, all event handlers registered by that module (identified by the module's ID) are automatically unregistered from the event system. This ensures that inactive or unloaded modules don't continue receiving events and prevents memory leaks from orphaned event handlers.

Modules don't need to manually unregister their event handlers in their `onDeactivate()` or `onUnload()` lifecycle methods - the Module Service handles this cleanup automatically.

---

### Renderer System

#### Purpose and Overview
The rendering system provides an abstraction layer for 2D graphics rendering. It manages the game loop, viewport scaling, frame rate control, and the rendering of business objects (`RenderingBo`). The system supports multiple rendering backends and provides a working set model for managing renderable objects efficiently.

Key features include automatic viewport scaling, configurable frame rates, reference resolution (1920x1080), and screenshot capabilities. The renderer integrates tightly with the module system to render module-specific UI elements.

#### Setup / Autoconfiguration
**Configuration Class**: `RendererAutoConfiguration`

**Property**: `game-engine.renderer.enabled` (default: `true`)

The core rendering infrastructure is auto-configured by `RendererAutoConfiguration`, which wires together all rendering components and creates a central `RendererData` object containing all necessary dependencies.

**G2D Renderer Implementation**

**Configuration Class**: `G2DAutoConfiguration`

**Properties**:
- `game-engine.renderer.strategy` (default: `g2d`) - Must be set to "g2d" to enable
- `game-engine.renderer.title` - Optional window title

The `G2DAutoConfiguration` provides the default Java2D-based renderer implementation, which is enabled when the renderer strategy is set to "g2d" (the default).

- `game-engine.renderer.default-theme` (default: `urbanTheme`) - Default theme, see the ThemeManager section below.

#### Exposed Spring Beans
- **`Renderer`**: Main rendering interface (provided by G2DRenderer or custom implementation)
- **`RendererControl`**: Controls renderer settings like title, scale factor, and video options (provided by G2DRendererControl or custom implementation)
- **`RendererData`**: Central data object containing all renderer dependencies
- **`RendererWorkingSet`**: Manages the current set of objects to be rendered
- **`RenderingBoPool`**: Factory for creating rendering business objects
- **`RendererLauncher`**: Handles renderer initialization and launch

**Rendering Workflow Example:**
```java
// Create a module instance
var myModule = new MyModule();

// Acquire a rendering business object from the pool
var rectangle = renderingBoPool.acquire("myRect", RectangleBo.class);
rectangle.setPosition(100, 200);
rectangle.setSize(50, 30);
rectangle.setColor(Color.BLUE);

// Add to the working set for your module (using module ID)
rendererWorkingSet.put(myModule.getId(), rectangle);

// Commit the working set when ready to render
rendererWorkingSet.commit(myModule.getId(), true);

// Later, for example when handling an event, retrieve and modify an existing rendering object
var existingRect = rendererWorkingSet.get(myModule.getId(), "myRect");
existingRect.setColor(Color.RED);
existingRect.setPosition(150, 250);

// Commit again to apply the changes
rendererWorkingSet.commit(myModule.getId(), true);
```

The `RendererWorkingSet` supports two rendering paradigms:
- **Cycle-based rendering**: Modules rebuild their rendering objects each frame, providing maximum flexibility for highly dynamic content
- **Stateful/event-driven rendering**: Modules maintain their working set between frames and only update when state changes occur, optimizing performance for static or infrequently-changing UI elements

Both approaches can be mixed within the same application, allowing each module to choose the most appropriate strategy for its needs.

#### UI Controls

The renderer provides a comprehensive set of interactive UI controls that can be used to build game interfaces. Controls are created as `ControlRenderingBo` objects and support common UI elements with automatic event handling.

**Available Control Types:**
- **`ButtonControl`**: Standard clickable button
- **`ToggleButtonControl`**: Button with on/off state
- **`SwitchControl`**: Toggle switch with visual state
- **`LabelControl`**: Non-interactive text display with optional background
- **`TextfieldControl`**: Single-line or multi-line text input
- **`SliderControl`**: Value slider with horizontal/vertical orientation
- **`TableControl`**: Data table with scrolling support
- **`WindowCloseButtonControl`**: Specialized close button for windows
- **`TooltipControl`**: Hover tooltip display

**Creating and Using Controls:**

```java
// Create a button control
var button = renderingBoPool.acquire("myButton", ControlRenderingBo.class)
    .setType(ButtonControl.class)
    .setCaption("Click Me")
    .setFontSize(16)
    .setVisible(true)
    .setEnabled(true)
    .withPositionAbsoluteAnchorTopLeft(100, 50)
    .withDimensionAbsolute(120, 40);

// Add to working set
rendererWorkingSet.put(moduleId, button);

// Create a toggle button with custom state
var toggle = renderingBoPool.acquire("myToggle", ControlRenderingBo.class)
    .setType(ToggleButtonControl.class)
    .setCaption("Toggle")
    .setCustomData(Boolean.TRUE) // Initial toggled state
    .withPositionAbsoluteAnchorTopLeft(250, 50)
    .withDimensionAbsolute(100, 40);

// Create a label with custom colors
var label = renderingBoPool.acquire("myLabel", ControlRenderingBo.class)
    .setType(LabelControl.class)
    .setCaption("Status: Ready")
    .setColor(RgbaColor.WHITE) // Foreground color
    .setCustomData(RgbaColor.DARK_GRAY) // Background color
    .setCustomData2(HorizontalAlignment.CENTER) // Text alignment
    .withPositionAbsoluteAnchorTopLeft(100, 100)
    .withDimensionAbsolute(200, 30);

// Create a text field (multi-line)
var textField = renderingBoPool.acquire("myTextField", ControlRenderingBo.class)
    .setType(TextfieldControl.class)
    .setCaption("Enter text here...")
    .setCustomData(Boolean.TRUE) // Multi-line mode
    .setCustomData2(Boolean.FALSE) // Not read-only
    .withPositionAbsoluteAnchorTopLeft(100, 150)
    .withDimensionAbsolute(300, 100);
```

**Handling Control Events:**

Controls fire `UiControlEvent` instances when interacted with. Register event handlers to respond to user actions:

```java
// In your module's onActivate() method
eventService.registerConsumer(
    moduleId,
    UiControlEvent.class,
    event -> {
        var controlId = event.getControlId();
        var eventData = event.getData();
        
        switch (controlId) {
            case "myButton" -> handleButtonClick();
            case "myToggle" -> handleToggleChange((Boolean) eventData);
            case "myTextField" -> handleTextChange((String) eventData);
        }
    }
);
```

**Control Groups:**

Controls can be organized into groups for coordinated behavior (e.g., radio button groups where only one can be selected):

```java
var option1 = renderingBoPool.acquire("option1", ControlRenderingBo.class)
    .setType(ToggleButtonControl.class)
    .setControlGroup("radioGroup")
    .setCaption("Option 1");

var option2 = renderingBoPool.acquire("option2", ControlRenderingBo.class)
    .setType(ToggleButtonControl.class)
    .setControlGroup("radioGroup")
    .setCaption("Option 2");
```

When toggle buttons share a control group, only one can be active at a time - selecting one automatically deselects the others.

**Custom Data Fields:**

Controls support up to four custom data fields (`setCustomData()` through `setCustomData4()`) for control-specific configuration:
- Toggle buttons: `customData` = initial state (Boolean)
- Labels: `customData` = background color, `customData2` = text alignment
- Text fields: `customData` = multi-line mode (Boolean), `customData2` = read-only flag
- Sliders: `customData` = horizontal orientation, `customData2` = current value, `customData3` = secondary value
- Tables: `customData` = data list, `customData2` = row count, `customData3` = version number

#### Integration with Module System

The Renderer System integrates with the Module System to provide automatic cleanup of rendering objects. When a client module (`AbstractClientModule`) is deactivated or unloaded, its entire renderer working set is automatically cleared by the Module Service. This ensures that rendering objects associated with inactive or unloaded modules are not rendered and don't consume memory.

Modules don't need to manually clear their rendering objects in their `onDeactivate()` or `onUnload()` lifecycle methods - the Module Service handles this cleanup automatically using `rendererWorkingSet.clear(moduleId)`.

#### Rendering Layers and Z-Index

The rendering system uses a two-level ordering system to control the draw order of rendering objects and UI components:

**Rendering Layers**

All `RenderingBo` objects have a `layer` property that determines their primary rendering order. Objects are rendered from lowest layer value to highest layer value, with higher layers painted on top. The `RenderingBoLayer` class defines standard layer constants:

- **Background Layers**: `BACKGROUND0` (100), `BACKGROUND1` (110), `BACKGROUND2` (120)
- **Foreground Layers**: `FOREGROUND0` (1000) through `FOREGROUND6` (1600)
- **UI Layers**: `UI_BGR` (2000), `UI0` (2100), `UI1` (2200), `UI2` (2300), `UI_TOP` (2499)
- **Top Layer**: `TOP` (32000) - always rendered last

**Z-Index for Fine-Grained Ordering**

Within the UI layers (`UI_BGR` through `UI_TOP`), rendering objects can additionally specify a `zIndex` property for fine-grained ordering. The z-index allows multiple objects on the same layer to be ordered relative to each other:

```java
// Both labels are on UI0 layer, but label2 renders on top due to higher zIndex
var label1 = pool.acquire("label1", LabelBo.class)
    .setLayer(RenderingBoLayer.UI0)
    .setZIndex((short) 0)
    .setText("Behind");

var label2 = pool.acquire("label2", LabelBo.class)
    .setLayer(RenderingBoLayer.UI0)
    .setZIndex((short) 10)
    .setText("In Front");
```

**Effective Layer Calculation**

For objects in UI layers, the effective rendering order is calculated as:
```
effectiveLayer = layer + (zIndex * ZINDEX_STEPS)
where ZINDEX_STEPS = UI_TOP - UI_BGR + 1 = 500
```

This formula ensures that:
- Objects with the same layer and zIndex render in the order they were added
- Higher zIndex values always render on top of lower values within the same layer
- **The zIndex must be less than 48** to prevent overlapping with different layer ranges

**AWT Component Z-Order**

For UI controls implemented as AWT/Swing components (buttons, labels, text fields, etc.), the z-index is synchronized with AWT's component z-order. When a control's zIndex or layer changes, the `G2DMainFrame.recalculateAllComponentZOrder()` method:

1. Collects all components and their effective layers
2. Sorts components by effective layer (ascending order)
3. Applies the sorted z-order to all components using `Container.setComponentZOrder()`

This ensures that AWT components respect the same layering rules as rendered objects, maintaining consistent visual stacking throughout the UI.

**Best Practices**

- Use layers for major UI groupings (backgrounds, game content, UI overlays)
- Use zIndex for ordering within a specific layer (window stacking, overlapping controls)
- Reserve `TOP` layer for critical always-on-top elements (tooltips, modal dialogs)
- **Keep zIndex values below 48** - higher values will cause incorrect rendering order
- For most use cases, zIndex values in the 0-20 range are sufficient

#### Theme System

The G2D renderer supports UI theming via `ThemeManager` (implemented by `G2DThemeManager`). Themes affect how controls and UI decorations are rendered.

**Levels of theme customization**

In practice, games typically use one of these approaches (from simplest to most flexible):

1. **Use a predefined theme**: pick one of the built-in theme beans (see below) and set `game-engine.renderer.default-theme` (or switch at runtime via `setCurrentTheme(String)`).
2. **Provide your own `Theme` instance**: construct a `Theme` object (for example via the `Theme.builder()` API) and activate it via `themeManager.setCurrentTheme(theme)`.
3. **Implement your own `PatternRenderer`**: for deeper visual changes beyond theme parameters, provide a custom `PatternRenderer` implementation as a Spring bean; `G2DThemeManager` discovers all `PatternRenderer` beans and makes them available to the renderer.

**Built-in themes**

`G2DAutoConfiguration` registers a set of built-in themes as Spring beans. The renderer starts with a default theme chosen by bean name.

Built-in theme bean names:

- `bevelDarkTheme`
- `bevelLightTheme`
- `blueprintTheme`
- `contemporaryTheme`
- `dreamTheme`
- `fantasyTheme`
- `modernTheme`
- `neonTheme`
- `noirTheme`
- `spacecraftTheme`
- `urbanTheme`
- `vintageTheme`

**Configure the default theme**

- Property: `game-engine.renderer.default-theme` (default: `urbanTheme`)
- Value: the Spring bean name of the theme

Example (`application.properties`):

```properties
game-engine.renderer.default-theme=neonTheme
```

**Switch the current theme**

You can switch themes either by a known bean name (fast path) or by the theme's `Theme.name` (case-insensitive match):

```java
// By bean name (e.g. "urbanTheme")
themeManager.setCurrentTheme("urbanTheme");

// Or by Theme.name (if it differs)
themeManager.setCurrentTheme("Urban");

// Or directly
themeManager.setCurrentTheme(myTheme);
```

When the theme changes, the G2D renderer is reset so the new theme takes effect immediately.

**Last-used theme persistence**

`G2DThemeManager` automatically persists the last selected theme (as a full `Theme` object) via `StorageService` and restores it on startup.

- Storage path: `['gameEngine', 'themes']`
- Storage name: `lastTheme`

If persistence fails or no saved theme exists, the manager falls back to the configured default theme.

**Loading and saving themes**

`ThemeManager` exposes explicit theme I/O helpers:

- `saveThemeToStorage(Theme theme)` stores a theme using `theme.getName()` as the storage name (under the same `['gameEngine','themes']` path).
- `loadThemeFromStorage(String themeName)` loads a previously saved theme by that name.
- `loadThemeFromStaticResource(StaticResource resource)` loads a theme from the static `data/` directory via `StaticResourceService`.

Example (load from static resources and activate):

```java
var resource = new StaticResource(List.of("themes"), "myTheme.yaml");
themeManager.loadThemeFromStaticResource(resource)
    .ifPresent(themeManager::setCurrentTheme);
```

Themes loaded from storage or static resources are automatically registered into the theme manager's in-memory list, so they show up in `getAvailableThemeNames()`.

---

### Audio System

#### Purpose and Overview
A comprehensive audio system supporting both sound effects and music playback. Features multiple audio strategies (software mixing and Java mixer), configurable audio options (volume, muting), and support for OGG Vorbis audio format. The system handles audio loading asynchronously and manages playback queues for music.

Audio options are persisted to storage and automatically loaded on startup, preserving user preferences across sessions.

#### Setup / Autoconfiguration
**Configuration Class**: `AudioAutoConfiguration`

**Property**: `game-engine.audio.enabled` (default: `true`)

Requires `StaticResourceService` and `StorageService` to be available.

#### Exposed Spring Beans
- **`AudioController`**: Main audio control interface implementing `AudioControl`

---

### Internationalization (i18n)

#### Purpose and Overview
A YAML-based localization system supporting multiple languages. Localizations are loaded from a structured YAML file containing translation entries organized by language code. The system supports both integer and string-based translation keys and provides fallback to default text when translations are missing.

Includes an integrated `LocalizationEditor` - a Swing-based GUI tool for managing translations, adding new languages, and editing localization entries during development.

#### Setup / Autoconfiguration
**Configuration Class**: `LocalizationAutoConfiguration`

**Properties**:
- `game-engine.i18n.enabled` (default: `true`)
- `game-engine.i18n.load-on-start` (default: `true`) - Auto-load localizations on startup
- `game-engine.i18n.resource` (default: `i18n/i18n.yaml`) - Path to localization resource

#### Exposed Spring Beans
- **`LocalizationService`**: Main interface for translation, language management, and localization editing

---

### Storage System

#### Purpose and Overview
A persistent storage system using ZSTD compression for efficient data serialization. Supports both UUID-based and path-based storage/retrieval, enabling flexible data organization. The system maintains an index for fast lookups and supports structured paths for logical data organization (e.g., `["gameEngine", "audioOptions"]`).

Storage location defaults to a `storage/` folder adjacent to the application JAR (or parent of `target/` in development). The location can be overridden via the `storagelocation` system property.

**Filesystem Structure:**
```
storage/
├── index          # Serialized index mapping paths/names to UUIDs
└── data/
    ├── <uuid-1>   # Individual stored objects, compressed
    ├── <uuid-2>
    └── ...
```

Each stored object is saved as a UUID-named file under `storage/data/`, while the `index` file maintains the mapping between logical paths and physical UUIDs.

#### Setup / Autoconfiguration
**Configuration Class**: `StorageServiceAutoConfiguration`

**Property**: `game-engine.storage.enabled` (default: `true`)

Storage directory is created automatically at startup in the `storage/` folder relative to the application.

#### Exposed Spring Beans
- **`StorageService`**: Main interface for storing and loading data by ID or path

---

### Resource Management

#### Purpose and Overview
Provides access to static resources packaged with the application (textures, sounds, data files, etc.). Resources are loaded from a `data/` folder adjacent to the application JAR (or parent of `target/` in development) and can be accessed by path segments. Supports both deserialized object loading and raw stream access for maximum flexibility.

The resource system is read-only and optimized for accessing bundled game assets that don't change during runtime. The data location can be overridden via the `datalocation` system property.

#### Setup / Autoconfiguration
**Configuration Class**: `StaticResourceAutoConfiguration`

**Property**: `game-engine.resource.enabled` (default: `true`)

No additional configuration required.

#### Exposed Spring Beans
- **`StaticResourceService`**: Main interface for loading resources by path

---

### Keyboard/Input System

#### Purpose and Overview
A key binding system that allows modules to register named key actions (qualifiers) and map them to actual keyboard codes. Users can configure their preferred keys for each action, and the system handles the mapping. Supports key modifiers (Ctrl, Alt, Shift) for complex key combinations.

This abstraction enables rebindable controls and consistent key handling across modules.

#### Setup / Autoconfiguration
**Configuration Class**: `KeycodeAutoConfiguration`

**Property**: `game-engine.keycode-registry.enabled` (default: `true`)

No additional configuration required.

#### Exposed Spring Beans
- **`KeycodeRegistry`**: Main interface for registering and querying key bindings

---

### Steamworks Integration

#### Purpose and Overview
Provides integration with the Steam platform API through a connector abstraction. Supports Steam features including user identity, Steam overlay, lobbies, achievements, and rich presence. The connector is designed as a singleton and can be optionally initialized when running on Steam.

**Note**: This is an optional component that requires the Steamworks4J library and proper Steam initialization.

#### Setup / Autoconfiguration
No auto-configuration provided. Must be manually initialized by setting `SteamworksConnector.setInstance()` with a concrete implementation (e.g., `SteamworksConnectorImpl`).

#### Exposed Spring Beans
None (managed as a singleton via static accessor)

---

### Spatial Utilities

#### Purpose and Overview
Provides 2D spatial mathematics utilities including coordinate systems, vectors, and geometric calculations. Used throughout the renderer and game logic for positioning, collision detection, and spatial queries.

Includes multiple spatial indexing implementations for efficient spatial queries:
- **`QuadTree`**: Hierarchical tree structure for fast spatial partitioning
- **`GridIndex2D`**: Grid-based spatial index for uniform distributions
- **`LinearSearchIndex2D`**: Simple linear search for small datasets
- **`Matrix2`**: 2D matrix structure for grid-based data
- **`SpatialIndex2D`**: Common interface for spatial index implementations

#### Setup / Autoconfiguration
No auto-configuration required. Utility classes and data structures used directly.

#### Exposed Spring Beans
None (utility classes)

---

### Menu System

#### Purpose and Overview
Framework for building in-game menus and UI screens as modules. Provides base components for menu rendering and interaction handling.

#### Setup / Autoconfiguration
Integrated with the module system. Menu components are implemented as `AbstractClientModule` instances.

#### Exposed Spring Beans
None (menu implementations are modules)

---

### Utilities

#### Purpose and Overview
Core utility classes providing common functionality used across the engine:

- **`ObjectSerialization`**: YAML-based serialization/deserialization with optional ZSTD compression, Base64 encoding, and digest calculation. Powers both storage and resource systems.
- **`RingBuffer`**: Fixed-size circular buffer implementation with automatic overwrite of oldest elements when full. Useful for maintaining bounded histories or event logs.
- **`rng/`**: Random number generation utilities including `FastRandomUUID` for high-performance UUID generation.

#### Setup / Autoconfiguration
No auto-configuration required. Utility classes used directly throughout the engine.

#### Exposed Spring Beans
None (utility classes)
