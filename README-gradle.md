# Andor's Trail Content Studio Gradle Build Options 

This Gradle build replaces the old IDE-based build with packaging shell script, and covers compile, test, packaging, and runtime-image generation.

## Requirements

- GitHub checkout with the Gradle wrapper
- A JVM capable of running Gradle. The Gradle wrapper downloads the configured Gradle version.
- The build uses a Gradle-managed Java 25 toolchain through Foojay, so a JDK 25 install does not need to be configured manually in normal environments.

## Common commands

### Build the project
```sh
./gradlew build
```
This runs the default build and produces two outputs in `build/libs`:
* `ATContentStudio-<version>.jar` - the conventional ("thin") jar without support libraries.
* `ATContentStudio-<version>-all.jar` - the "fat" jar with support libraries, suitable for running with `java -jar`

### Run the app
```sh
./gradlew run
```

### Run tests
```sh
./gradlew test
```
(Only very minimal testing has been implemented so far)

## Packaging
Release packaging is done through the `release.yml` GitHub Action workflow, but can be done manually for testing.
Installers must be built on the target platform, and some native packaging tools must be installed locally.

Packaged installer outputs are archived under `build/distributions/`.

### Windows

Windows installers are built using the [Wix3 Toolset](https://github.com/wixtoolset/wix3/releases), which must be installed on the build system.

To build a Windows EXE installer:
```powershell
.\gradlew.bat packageWindowsExe
```

To build a Windows MSI installer:
```powershell
.\gradlew.bat packageWindowsMsi
```

The installer output is written to `build/distributions/`.

### Linux
To build a Linux RPM installer (Fedora/Redhat/Bazzite/etc) with desktop/menu shortcuts:

```sh
./gradlew packageLinuxRpm
```

The RPM output is written to `build/distributions/`. RPM packaging requires the `rpm` tooling to be installed. The release workflow also installs `git`, `findutils`, `tar`, and `unzip` in its container.

To build a Linux DEB (Debian/Ubuntu/Mint/etc) installer:

```sh
./gradlew packageLinuxDeb
```

The DEB output is written to `build/distributions/`. DEB packaging requires `fakeroot`.

### macOS

Build the DMG with:

```sh
./gradlew packageMacosDmg
```

The DMG output is written to `build/distributions/`.  Note that due to a platform limitation requiring that the major
release version may not be '0', the MacOS release version is bumped by 10, so 10.7.1 is equivalent to 0.7.1.  Note that this
applies to the installer _only_; the application retains the correct version number.

### Zip Distribution
To build a ZIP distribution using the fat jar with icon and start script (requires a compatible JVM installed on target system):

```sh
./gradlew packageZip
```
The zip file will be written to `build/distributions/ATCS-<version>.zip`

### JAR-only .EXE Installer (NSIS)
The legacy jar-with-start-script installer is packaged directly in the GitHub release workflow, and is not performed via Gradle.

### Source Distribution

```
./gradlew sourcesJar
```
A source distribution is written to `build/libs/ATContentStudio-<version>-sources.jar`.

## Notes

- Local jars are loaded from `lib/`.
- Source roots include `src`, `hacked-libtiled`, `minify`, and `siphash-zackehh/src/main/java`.
- `hacked-libtiled/tiled/io/resources/map.dtd` is copied into the runtime resources so TMX loading works.
- The build currently uses a Gradle-managed JDK 25 toolchain so `jlink` can run without a hard-coded path.
- Windows installers include Start Menu and desktop shortcuts.
- Windows packaging uses `packaging/Windows/ATCS.ico` for the installer and launcher icon.
- Linux packaging uses `packaging/common/ATCS.png` for the installer and launcher icon.
