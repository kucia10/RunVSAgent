# IntelliJ Plugin Analysis (FR-5)

This document analyzes the structure, components, and dependencies of the existing IntelliJ plugin (`jetbrains_plugin`).

## 1. Project Structure (FR-1)

The plugin follows a standard Gradle-based Kotlin project structure.

-   `src/main/kotlin`: Contains the Kotlin source code under the package `com.sina.weibo.agent`.
-   `src/main/resources`: Contains plugin configuration (`plugin.xml`), icons, and other resources.
-   `build.gradle.kts`: The main build script.
-   `gradlew`: Gradle wrapper for building the plugin.

The source code is organized into the following main packages:
- `actions`: UI actions.
- `actors`: Seems related to concurrent programming (likely coroutines).
- `commands`: Command handling.
- `core`: Core logic.
- `editor`: Editor-specific functionality, like listeners.
- `events`: Event handling.
- `extensions`: Plugin extension points.
- `ipc`: Inter-process communication, likely with a Node.js agent.
- `model`: Data models.
- `plugin`: Core plugin lifecycle classes.
- `service`: Background services.
- `terminal`: Terminal customization.
- `theme`: UI theme handling.
- `ui`: UI components, including the main tool window.
- `util`: Utility classes.
- `webview`: Components for displaying web content.
- `workspace`: Workspace and project-related logic.

## 2. Extension Points, Actions, and Services (FR-2)

The `plugin.xml` file defines the plugin's integration with the IntelliJ Platform.

### Extension Points:
-   `projectService`: `com.sina.weibo.agent.plugin.WecoderPluginService` - A service available at the project level. This will likely be mapped to a singleton in Eclipse.
-   `postStartupActivity`: `com.sina.weibo.agent.plugin.WecoderPlugin` - Runs code after the project is opened. The Eclipse equivalent is `IStartup`.
-   `editorFactoryListener`: `com.sina.weibo.agent.editor.EditorListener` - Listens to editor creation/destruction events. The Eclipse equivalent is `IPartListener2`.
-   `toolWindow`: A tool window with the ID "RunVSAgent", implemented by `com.sina.weibo.agent.ui.RunVSAgentToolWindowFactory`. This will be a `ViewPart` in Eclipse.
-   `notificationGroup`: For displaying notifications.
-   `localTerminalCustomizer`: `com.sina.weibo.agent.terminal.WeCoderTerminalCustomizer` - Customizes the integrated terminal.

### Actions:
The plugin defines several actions, which are integrated into the main menu, editor popup menu, and a toolbar.
-   `RunVSAgent.extensionSwitcher`: An action to switch between "extension providers".
-   `WecoderToolbarGroup`: A toolbar group that dynamically populates actions.
-   `RunVSAgent.RightClickMenu`: A context menu in the editor.
-   `RunVSAgent.MainMenu`: A menu item in the "Tools" menu.

These will be mapped to Eclipse Commands, Handlers, and Menu Contributions.

## 3. Data Flow (FR-3)

Based on the package structure and `plugin.xml`, the data flow appears to be:
1.  **Initialization**: The `WecoderPlugin` (`postStartupActivity`) and `WecoderPluginService` (`projectService`) initialize the plugin's core components when a project is opened.
2.  **UI**: The `RunVSAgentToolWindowFactory` creates the main UI panel, which likely contains a web view (`webview` package).
3.  **IPC**: The plugin communicates with an external agent (likely a Node.js process, as indicated in the description) via the `ipc` package. This is a critical component to port.
4.  **User Interaction**: Users interact through the tool window, editor context menus (`actions` package), and the main menu.
5.  **Editor Integration**: The `EditorListener` tracks editor events to provide contextual actions.

## 4. IntelliJ API Dependencies (FR-4)

The plugin has dependencies on the following key IntelliJ Platform APIs:

-   `com.intellij.openapi.project.Project`: Represents the current project.
-   `com.intellij.openapi.wm.ToolWindow`: The main UI window.
-   `com.intellij.openapi.startup.StartupActivity`: For initialization.
-   `com.intellij.openapi.components.Service`: For creating services.
-   `com.intellij.openapi.editor.event.EditorFactoryListener`: For editor events.
-   `com.intellij.openapi.actionSystem.AnAction`: For creating actions.
-   `com.intellij.openapi.actionSystem.ActionGroup`: For grouping actions.
-   `com.intellij.ui.jcef.JBCefBrowser`: For the web-based UI.
-   `org.jetbrains.plugins.terminal.LocalTerminalCustomizer`: For terminal integration.

These will need to be mapped to their Eclipse equivalents during the conversion.
