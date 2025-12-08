# Eclipse Plugin Build Scripts

This directory contains build and maintenance scripts for the RunVSAgent Eclipse plugin project.

## Scripts Overview

| Script | Purpose |
|--------|---------|
| [`run.sh`](run.sh) | Main entry point - unified interface to all operations |
| [`setup.sh`](setup.sh) | Initialize development environment |
| [`build.sh`](build.sh) | Build project components |
| [`clean.sh`](clean.sh) | Clean build artifacts and temporary files |
| [`test.sh`](test.sh) | Run tests and validations |

## Library Files

| File | Purpose |
|------|---------|
| [`lib/common.sh`](lib/common.sh) | Shared utility functions (copied from scripts/lib) |
| [`lib/build.sh`](lib/build.sh) | Eclipse-specific build functions |
| [`lib/json_parser.sh`](lib/json_parser.sh) | JSON parsing utilities (shared) |
| [`lib/extensions.json`](lib/extensions.json) | Extensions configuration (shared) |

## Quick Start

```bash
# First time setup
./eclipse_scripts/run.sh setup

# Build the project
./eclipse_scripts/run.sh build

# Clean build artifacts
./eclipse_scripts/run.sh clean

# Run tests
./eclipse_scripts/run.sh test
```

## Detailed Usage

### Setup Script

Initialize the development environment:

```bash
./eclipse_scripts/setup.sh [OPTIONS]

Options:
  -f, --force           Force reinstall of dependencies
  -s, --skip-submodules Skip git submodule initialization
  -d, --skip-deps       Skip dependency installation
  -p, --no-patches      Skip applying patches
  -v, --verbose         Enable verbose output
  -h, --help            Show help message
```

### Build Script

Build Eclipse plugin and related components:

```bash
./eclipse_scripts/build.sh [OPTIONS] [TARGET]

Targets:
  all         Build all components (default)
  vscode      Build only VSCode extension
  base        Build only base extension
  eclipse     Build only Eclipse plugin

Options:
  -m, --mode MODE       Build mode: release (default) or debug
  -c, --clean           Clean before building
  -o, --output DIR      Output directory for build artifacts
  -t, --skip-tests      Skip running tests
  --vsix FILE           Use existing VSIX file
  -v, --verbose         Enable verbose output
  -h, --help            Show help message
```

### Clean Script

Clean build artifacts:

```bash
./eclipse_scripts/clean.sh [OPTIONS] [TARGET]

Targets:
  build       Clean build artifacts only (default)
  deps        Clean dependencies
  cache       Clean cache files
  logs        Clean log files
  temp        Clean temporary files
  all         Clean everything

Options:
  -f, --force           Force clean without confirmation
  -k, --keep-logs       Keep log files when cleaning
  -v, --verbose         Enable verbose output
  -h, --help            Show help message
```

### Test Script

Run tests and validations:

```bash
./eclipse_scripts/test.sh [OPTIONS] [TEST_TYPE]

Test Types:
  all           Run all tests (default)
  unit          Run unit tests only
  integration   Run integration tests only
  lint          Run linting checks
  build         Run build validation
  env           Run environment validation

Options:
  -f, --fail-fast       Stop on first test failure
  -c, --coverage        Generate coverage reports
  -v, --verbose         Enable verbose output
  -h, --help            Show help message
```

## Build Process

The Eclipse plugin build process consists of these steps:

1. **Setup Phase**
   - Validate system requirements (JDK 17+, Maven, Node.js, Git LFS)
   - Initialize git submodules
   - Install Node.js dependencies
   - Apply patches to VSCode source

2. **Build Phase**
   - Build VSCode extension → VSIX file
   - Build extension_host → JavaScript runtime
   - Build Eclipse plugin → JAR file using Maven/Tycho

3. **Package Phase**
   - Copy VSCode extension files to `eclipse_plugin/plugins/`
   - Generate Eclipse update site
   - Create distribution artifacts

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `BUILD_MODE` | Build mode (release/debug) | release |
| `VERBOSE` | Enable verbose output | false |
| `DRY_RUN` | Show what would be done | false |
| `SKIP_VALIDATION` | Skip environment validation | false |
| `VSIX_FILE` | Path to existing VSIX file | - |

## Directory Structure

```
eclipse_scripts/
├── run.sh              # Main entry point
├── setup.sh            # Environment setup
├── build.sh            # Build script
├── clean.sh            # Clean script
├── test.sh             # Test runner
├── README.md           # This file
└── lib/
    ├── common.sh       # Shared utilities
    ├── build.sh        # Build functions
    ├── json_parser.sh  # JSON parsing
    └── extensions.json # Extensions config
```

## Comparison with scripts/ folder

The `eclipse_scripts/` folder mirrors the structure and functionality of the `scripts/` folder but targets Eclipse plugin development instead of IntelliJ:

| Feature | scripts/ (IntelliJ) | eclipse_scripts/ (Eclipse) |
|---------|---------------------|---------------------------|
| Build System | Gradle | Maven/Tycho |
| Plugin Directory | `jetbrains_plugin/` | `eclipse_plugin/` |
| Build Tool | `./gradlew` | `mvn` |
| Output Format | .zip | .jar + p2 repository |

## Requirements

- **Java**: JDK 17 or higher
- **Maven**: 3.6 or higher
- **Node.js**: 16.0.0 or higher
- **Git**: With Git LFS installed
- **npm**: For Node.js dependencies

## Troubleshooting

### Maven not found

Install Maven:
```bash
# macOS
brew install maven

# Linux (Ubuntu/Debian)
sudo apt-get install maven

# Or download from https://maven.apache.org/
```

### JDK version too old

The Eclipse plugin requires JDK 17 or higher:
```bash
# Check your Java version
java -version

# Install JDK 17 or higher
# macOS: brew install openjdk@17
# Linux: sudo apt-get install openjdk-17-jdk
```

### Git LFS files not pulled

Ensure Git LFS is installed and initialized:
```bash
git lfs install
git lfs pull
```

## See Also

- [Eclipse Plugin README](../eclipse_plugin/README.md)
- [Main Project Scripts](../scripts/README.md)
- [Build Documentation](../BUILD.md)

## License

Apache License 2.0 - See LICENSE file for details.
