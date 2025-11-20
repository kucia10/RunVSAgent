# Building, Packaging, and Validating the Plugin (FR-20, FR-21, FR-22, FR-23)

This document describes how to build the `eclipse_plugin_wow` plugin and validate its functionality.

## 1. Building the Plugin (FR-20)

The plugin is built using Maven and the Tycho plugin. To build the plugin, run the following command from the `eclipse_plugin_wow` directory:

```bash
mvn clean install
```

A successful build will generate a `target` directory containing the plugin artifacts.

## 2. Packaging (FR-21)

The build process generates two forms of distributable packages:

1.  **Plugin JAR**: The primary artifact is `target/com.sina.weibo.agent.eclipse.wow-1.0.0-SNAPSHOT.jar`. This JAR can be installed directly into an Eclipse IDE by placing it in the `dropins` directory.
2.  **p2 Repository**: The `target` directory also contains the necessary metadata (`p2artifacts.xml`, `p2content.xml`) to serve as a p2 repository. This allows for installation via the "Install New Software..." dialog in Eclipse.

## 3. Installation and Validation (FR-22)

To validate the plugin, follow these steps:

1.  **Install the Plugin**:
    -   Copy the `com.sina.weibo.agent.eclipse.wow-1.0.0-SNAPSHOT.jar` file to the `dropins` folder of a clean Eclipse installation.
    -   Alternatively, point the "Install New Software..." dialog to the `eclipse_plugin_wow/target` directory.
2.  **Run Eclipse**:
    -   Start Eclipse with the `-consoleLog` option to view the console output.
3.  **Verify Startup**:
    -   On startup, you should see log messages in the console indicating that the "RunVSAgent-wow" plugin has been initialized, confirming that the `Startup` class was successfully loaded.

## 4. Functional Equivalence Validation Report (FR-23)

This section serves as the initial validation report.

| Feature                 | Status      | Notes                                                                                                                                                                                                   |
| ----------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Plugin Skeleton**     | `Completed` | The plugin skeleton has been created, and it builds successfully into a valid Eclipse plugin.                                                                                                             |
| **Startup Logic**       | `Ported`    | The initial startup logic from the IntelliJ plugin has been ported to an `IStartup` implementation. The plugin initializes on startup, as verified by log messages.                                       |
| **UI (Tool Window)**    | `Pending`   | The main tool window has not yet been ported.                                                                                                                                                             |
| **Actions and Menus**   | `Pending`   | The actions and menu contributions have not yet been ported.                                                                                                                                              |
| **Core Services**       | `Pending`   | The core services (e.g., process management, IPC) have been created as placeholders but do not yet contain the full logic.                                                                                |

**Conclusion**: The basic build, packaging, and startup functionality have been successfully implemented and validated. The next phase of development will focus on porting the UI and core application logic.
