# Eclipse Plugin for RunVSAgent

This directory contains the Eclipse plugin implementation for RunVSAgent, which provides VSCode extension integration for Eclipse IDE.

## Project Structure

```
eclipse_plugin/
├── pom.xml                                    # Parent POM with Tycho configuration
├── target-platform/                           # Eclipse target platform definition
│   ├── pom.xml
│   └── runvsagent.target
├── com.sina.weibo.agent.plugin/              # Main plugin bundle
│   ├── pom.xml
│   ├── META-INF/
│   │   └── MANIFEST.MF                       # OSGi bundle manifest
│   ├── plugin.xml                            # Eclipse plugin descriptor
│   ├── build.properties
│   └── src/
│       └── com/sina/weibo/agent/plugin/
│           └── Activator.java                # Plugin activator
├── com.sina.weibo.agent.feature/             # Eclipse feature project
│   ├── pom.xml
│   ├── feature.xml
│   └── build.properties
├── com.sina.weibo.agent.site/                # Update site project
│   ├── pom.xml
│   └── category.xml
└── plugins/                                   # VSCode extension files (roo-code)
```

## Build System

The Eclipse plugin uses **Maven with Tycho** for building:

- **Maven**: Standard Java build tool
- **Tycho**: Maven plugins for building Eclipse plugins and OSGi bundles
- **Target Platform**: Eclipse 2023-12
- **Java Version**: 17

## Building

Use the eclipse_scripts to build the plugin:

```bash
cd /path/to/RunVSAgent
./eclipse_scripts/build.sh
```

Or build directly with Maven:

```bash
cd eclipse_plugin
mvn clean verify
```

## Build Artifacts

After a successful build, artifacts will be located in:

- **Plugin JAR**: `com.sina.weibo.agent.plugin/target/com.sina.weibo.agent.plugin-*.jar`
- **Feature**: `com.sina.weibo.agent.feature/target/com.sina.weibo.agent.feature-*.jar`
- **Update Site**: `com.sina.weibo.agent.site/target/repository/`

## Installation

1. Build the plugin using the build scripts
2. In Eclipse, go to Help → Install New Software
3. Click "Add" → "Local"
4. Browse to `eclipse_plugin/com.sina.weibo.agent.site/target/repository`
5. Select "RunVSAgent" and click "Next" to install

## Development

### Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher
- Eclipse IDE (for development)

### Project Structure

The plugin follows standard Eclipse plugin conventions:

- `META-INF/MANIFEST.MF`: Defines the OSGi bundle
- `plugin.xml`: Defines Eclipse extension points
- `build.properties`: Specifies what to include in the build
- `src/`: Java source code

### Shared Components

The Eclipse plugin shares the following components with the JetBrains plugin:

- `extension_host/`: Base extension runtime (TypeScript/Node.js)
- `deps/roo-code`: VSCode extension files
- VSCode extension integration layer

## Configuration

The plugin will look for configuration in:

- Plugin preferences (Eclipse → Preferences → RunVSAgent)
- `.vscode-agent` file in workspace root (shared with JetBrains plugin)

## Architecture

The Eclipse plugin architecture mirrors the JetBrains plugin:

1. **Plugin Activator**: Initializes the extension host and VSCode extension
2. **Extension Host Manager**: Manages the Node.js extension host process
3. **RPC Manager**: Handles communication between Eclipse and the extension host
4. **Editor Integration**: Bridges Eclipse editor events to VSCode API
5. **Terminal Integration**: Provides terminal support
6. **Webview Manager**: Displays VSCode webviews using Eclipse browser components

## Next Steps

The current implementation provides:

- ✅ Basic plugin structure with Tycho/Maven build
- ✅ OSGi bundle configuration
- ✅ Eclipse feature and update site
- ✅ Build scripts integration

To complete the implementation:

1. Port the core functionality from `jetbrains_plugin/` to Java
2. Implement Eclipse-specific editor, terminal, and webview integrations
3. Set up IPC communication with the extension host
4. Add Eclipse preferences UI
5. Implement extension management
6. Add comprehensive tests

## License

Apache License 2.0 - See LICENSE file for details.
