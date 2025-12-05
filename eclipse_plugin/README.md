# RunVSAgent Eclipse Plugin

Run VSCode-based Coding Agents in Eclipse IDEs.

## Overview

RunVSAgent is an Eclipse plugin that enables developers to run VSCode-based coding agents and extensions within Eclipse IDEs. This plugin provides a bridge between VSCode extensions and the Eclipse platform, allowing AI-powered coding assistants to work seamlessly in Eclipse.

## Features

- **VSCode Extension Compatibility**: Run VSCode-based coding agents in Eclipse
- **Multiple Extension Support**: Support for Roo Code, Cline, and custom extensions
- **SWT Browser Integration**: Native browser widget for WebView functionality
- **Cross-Platform**: Works on Windows, macOS, and Linux
- **Eclipse 2022-09+**: Supports Eclipse 2022-09 and later versions

## Requirements

- Eclipse IDE 2022-09 or later
- Java 17 or later
- Node.js 18 or later

## Installation

### From Update Site

1. In Eclipse, go to **Help > Install New Software...**
2. Add the RunVSAgent update site URL
3. Select **RunVSAgent** from the list
4. Follow the installation wizard

### Manual Installation

1. Download the plugin JAR file
2. Copy to your Eclipse `dropins` folder
3. Restart Eclipse

## Usage

### Opening the View

1. Go to **Window > Show View > Other...**
2. Select **RunVSAgent > RunVSAgent**
3. The view will appear in your workspace

### Keyboard Shortcuts

- `Ctrl+Alt+R` (Windows/Linux) or `Cmd+Alt+R` (macOS): Open RunVSAgent view
- `Ctrl+Alt+S` (Windows/Linux) or `Cmd+Alt+S` (macOS): Switch extension provider

### Menu Access

- **RunVSAgent** menu in the main menu bar
- Right-click context menu in editors

## Configuration

Go to **Window > Preferences > RunVSAgent** to configure:

- **Extension Provider**: Select the active extension (Roo Code, Cline, or Custom)
- **Node.js Path**: Specify custom Node.js installation path
- **Custom Extension Path**: Path to custom extension directory
- **Debug Mode**: Enable debug logging
- **Auto-start**: Automatically start extension host on Eclipse startup

## Architecture

The plugin consists of the following components:

```
eclipse_plugin/
├── core/                    # Core plugin functionality
│   ├── PluginContext        # Central plugin state
│   ├── ExtensionHostManager # Extension host communication
│   ├── ExtensionProcessManager # Node.js process management
│   ├── ExtensionSocketServer # Socket server for IPC
│   └── RPCManager           # RPC protocol management
├── ipc/                     # Inter-process communication
│   ├── NodeSocket           # Socket implementation
│   ├── ProtocolMessage      # Message format
│   └── proxy/               # RPC proxy classes
├── webview/                 # WebView management
│   └── WebViewManager       # Browser instance management
├── ui/                      # User interface
│   └── views/               # Eclipse views
├── handlers/                # Command handlers
├── preferences/             # Preference pages
└── util/                    # Utility classes
```

## Building

### Prerequisites

- Maven 3.8+
- Java 17+
- Eclipse Tycho

### Build Commands

```bash
# Build the plugin
cd eclipse_plugin
mvn clean package

# Build with update site
mvn clean package -Pupdate-site
```

### Import into Eclipse

1. Import as Maven project
2. Configure target platform
3. Run as Eclipse Application

## Development

### Debug Mode

Set the system property or environment variable to enable debug mode:

```bash
# System property
-Drunvsagent.debug=true

# Environment variable
RUNVSAGENT_DEBUG=true
```

### Extension Host Debugging

To debug the extension host connection:

1. Enable debug mode in preferences
2. Check the Eclipse log for connection details
3. Use the status check action to view current state

## Supported Extensions

- **Roo Code**: AI-powered coding assistant
- **Cline**: AI coding assistant
- **Custom**: Add your own VSCode-compatible extension

## Known Limitations

- SWT Browser may have platform-specific rendering differences
- Some VSCode APIs may not have full Eclipse equivalents
- Terminal integration is limited compared to native Eclipse terminal

## Troubleshooting

### Extension Host Not Starting

1. Check Node.js installation: `node --version`
2. Verify Node.js path in preferences
3. Check Eclipse log for error messages

### Browser Issues

1. Update Eclipse to latest version
2. Check SWT Browser system requirements
3. Try different browser backend (preferences)

### Connection Issues

1. Check firewall settings
2. Verify port is not in use
3. Use status check to diagnose

## Contributing

Contributions are welcome! Please see the main project [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

Apache License 2.0 - See [LICENSE](../LICENSE) for details.

## Support

- GitHub Issues: Report bugs and feature requests
- Documentation: See [docs/](../docs/) for detailed documentation
