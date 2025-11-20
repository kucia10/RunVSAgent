# API Mapping and Conversion Strategy (FR-10 to FR-14)

This document outlines the strategy for converting the IntelliJ plugin's codebase to the Eclipse Platform, focusing on API mapping and design patterns.

## 1. Core Component Mapping (FR-10)

The following table defines the mapping between key IntelliJ Platform APIs and their Eclipse PDE/RCP equivalents.

| IntelliJ Platform API                | Eclipse Platform API                               | Description                                                                                             |
| ------------------------------------ | -------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `postStartupActivity`                | `org.eclipse.ui.IStartup`                          | For running code on IDE startup.                                                                        |
| `projectService`                     | Singleton Pattern via `Activator` or OSGi Service  | Eclipse does not have a direct equivalent; a custom singleton or OSGi service will manage project-level state. |
| `editorFactoryListener`              | `org.eclipse.ui.IPartListener2`                    | Listens for part (editor, view) lifecycle events.                                                       |
| `ToolWindow`                         | `org.eclipse.ui.part.ViewPart`                     | The base class for creating custom views (the equivalent of tool windows).                             |
| `AnAction` / `ActionGroup`           | `org.eclipse.core.commands.AbstractHandler`        | Handlers execute commands, which are defined declaratively in `plugin.xml`.                              |
| `Notifications`                      | `org.eclipse.core.runtime.Status` / `StatusManager` | For displaying notifications to the user.                                                               |
| `com.intellij.openapi.project.Project` | `org.eclipse.core.resources.IProject`              | Represents a project in the workspace.                                                                  |

## 2. UI Conversion Strategy (FR-11)

The IntelliJ plugin uses `JBCefBrowser` (a Swing component) to display a web-based UI. The Eclipse equivalent will use the SWT `Browser` widget.

-   **Swing/AWT to SWT/JFace:** All UI code will be rewritten using SWT.
-   **JCEF to SWT Browser:** The core `JBCefBrowser` component will be replaced with `org.eclipse.swt.browser.Browser`.
-   **Event Handling:** Swing event listeners (`ActionListener`, etc.) will be replaced with SWT listeners (`SelectionListener`, etc.).
-   **Layouts:** IntelliJ's Swing layouts will be replaced with SWT layouts (e.g., `GridLayout`, `FillLayout`).

## 3. Project and File API Mapping (FR-12)

Accessing and manipulating project files will be migrated from IntelliJ's Virtual File System (VFS) to the Eclipse Workspace API.

| IntelliJ API               | Eclipse API                                        | Description                                     |
| -------------------------- | -------------------------------------------------- | ----------------------------------------------- |
| `VirtualFile`              | `org.eclipse.core.resources.IFile` / `IResource`   | Represents a file or resource in the workspace. |
| `PsiFile`                  | `org.eclipse.jdt.core.ICompilationUnit` (for Java) | Represents a source file in the Java model.     |
| `FileEditorManager`        | `IWorkbenchPage.openEditor()`                      | For opening files in an editor.                 |

## 4. Action and Menu Conversion Strategy (FR-13)

IntelliJ's `<actions>` in `plugin.xml` will be converted to the Eclipse Command Framework. This involves three extension points in the Eclipse `plugin.xml`:

1.  **`org.eclipse.ui.commands`**: Define the command's ID, name, and description.
2.  **`org.eclipse.ui.handlers`**: Link a command ID to a Kotlin class that implements `org.eclipse.core.commands.AbstractHandler`.
3.  **`org.eclipse.ui.menus`**: Contribute the command to menus and toolbars (e.g., the main menu, editor context menu).

This declarative approach separates the action's definition from its implementation, which is a core concept in the Eclipse Platform. Dynamic and context-sensitive menus will be handled using `visibleWhen` expressions in the `plugin.xml`.

This document will serve as the primary technical guide for the code conversion phase (FR-15 to FR-19).
