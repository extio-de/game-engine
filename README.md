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

## Background

Exo's Game Engine is derived from [Spacecraft Tactics](https://store.steampowered.com/app/2642080/Spacecraft_Tactics), a 2D turn-based tactical space game released on Steam in 2023. The engine extracts the core systems that powered Spacecraft Tactics' campaign, multiplayer modes, and built-in editors. Note that the engine does not include the Groovy-based mod system from Spacecraft Tactics. It focuses on the fundamental building blocks: modules, rendering, events, storage, and other core subsystems that can be used to build new games.

The engine also serves as the foundation for **Exo's CYOA**, an LLM-driven choose-your-own-adventure game that combines the game engine's rendering, module, and UI systems with agentic AI flows to create interactive narrative experiences.

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

---

### Renderer System

#### Purpose and Overview
The rendering system provides an abstraction layer for 2D graphics rendering. It manages the game loop, viewport scaling, frame rate control, and the rendering of business objects (`RenderingBo`). The system supports multiple rendering backends and provides a working set model for managing renderable objects efficiently.

Key features include automatic viewport scaling, configurable frame rates, reference resolution (1920x1080), and screenshot capabilities. The renderer integrates tightly with the module system to render module-specific UI elements.

**Rendering Workflow Example:**
```java
// Acquire a rendering business object from the pool
RectangleBo rectangle = renderingBoPool.acquire("myRect", RectangleBo.class);
rectangle.setPosition(100, 200);
rectangle.setSize(50, 30);
rectangle.setColor(Color.BLUE);

// Add to the working set for your module
rendererWorkingSet.add(MyModule.class, rectangle);

// Commit the working set when ready to render
rendererWorkingSet.commit(MyModule.class, true);
```

The `RendererWorkingSet` supports two rendering paradigms:
- **Cycle-based rendering**: Modules rebuild their rendering objects each frame, providing maximum flexibility for highly dynamic content
- **Stateful/event-driven rendering**: Modules maintain their working set between frames and only update when state changes occur, optimizing performance for static or infrequently-changing UI elements

Both approaches can be mixed within the same application, allowing each module to choose the most appropriate strategy for its needs.

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

#### Exposed Spring Beans
- **`Renderer`**: Main rendering interface (provided by G2DRenderer or custom implementation)
- **`RendererControl`**: Controls renderer settings like title, scale factor, and video options (provided by G2DRendererControl or custom implementation)
- **`RendererData`**: Central data object containing all renderer dependencies
- **`RendererWorkingSet`**: Manages the current set of objects to be rendered
- **`RenderingBoPool`**: Factory for creating rendering business objects
- **`RendererLauncher`**: Handles renderer initialization and launch

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
