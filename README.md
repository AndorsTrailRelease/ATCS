# Andor's Trail Content Studio

Andor's Trail Content Studio is a Java/Swing editor for creating and maintaining content for the Andor's Trail RPG. It helps contributors work with game data, maps, scripts, sprites, resources, and exportable project workspaces without editing every file by hand.

## Features

- Workspace-based content projects that can reference an existing Andor's Trail game source checkout.
- Editors for common game data such as items, NPCs, quests, dialogues, actor conditions, and other JSON-backed resources.
- TMX map and worldmap support, including map-related objects such as signs, containers, spawn areas, rest areas, scripts, map changes, keys, and replacements.
- Sprite/resource tooling, including spritesheet editing and resource comparison views.
- Project export tools that can write changes to a ZIP archive or directly into a game source folder.
- Utility views for comparing items and NPCs, plus a BeanShell console for advanced inspection or scripting.

## Getting started as a content creator

1. Download and install an [ATCS release](https://github.com/AndorsTrailRelease/ATCS/releases) for your system.
2. Clone a copy of the Andor's Trail game source from the [GitHub repository](https://github.com/AndorsTrailRelease/andors-trail).
3. Run ATCS and choose or create a workspace folder. The workspace stores your ATCS projects and local preferences.
4. Use **File > Create Project...** and point it at the `AndorsTrail` folder in the cloned game repo. For normal content work, choose **Real game data**.
5. Open the project in the left tree and edit or create game data, maps, worldmap segments, sprites, or other resources.
6. Save changed elements as you work. ATCS tracks created and altered resources separately from the original source data.
7. Reach out to the [Andor's Trail community](#community) to discuss adding your content to the game.

## Downloading ATCS
See our [ATCS release](https://github.com/AndorsTrailRelease/ATCS/releases) page for the latest release.

## Build Requirements

- The Gradle wrapper included in this repository.  The wrapper will download the required gradle version if needed.

Gradle is configured to use an Adoptium/Temurin Java 21 toolchain. If a matching JDK is not already available, Gradle can download one through the configured Foojay toolchain resolver.

## Running from the IDE
If you are using the **Android Studio / IntelliJ IDE**, select the **`Run ATCS`** run configuration to build and start the application.  Any required dependencies should be downloaded automatically, but it may be necessary to refresh the download links via the available link if an error occurs. 

If the run configuration is not available, running **File -> Sync Project with Gradle Files** and restart the IDE.

## Running from the command line

Run the project from the root of the cloned repository:

On Linux/macOS:
```sh
./gradlew run
```

On Windows:

```powershell
.\gradlew.bat build
```

Or run the built jar directly:

```sh
java -jar build/libs/ATContentStudio-<version>-all.jar
```

On Windows, add `--show-console` to allocate and attach a console window.

## Build

On Linux/macOS:

```sh
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The main runnable jar is produced at:

```text
build/libs/ATContentStudio-<version>-all.jar
```

See [README-gradle.md](README-gradle.md) for more details and build options, including packaging installers.

## Community

- **Official Forum:** [andorstrail.com](https://andorstrail.com/) - The primary hub for announcements, help, and discussion.
- **Discord Server:** [https://discord.gg/FgwXdy6](https://discord.gg/FgwXdy6) - Join the real-time discussion with players and content creators.
- **GitHub Repository:** [AndorsTrailRelease/andors-trail](https://github.com/AndorsTrailRelease/andors-trail) - The source code for the Andor's Trail game.
