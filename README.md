# Tutorial Mod

A small, beginner-friendly starting point for a Minecraft NeoForge mod.

## What you need

- A Java 21 JDK
- IntelliJ IDEA (recommended) or another Java IDE
- An internet connection for the first Gradle setup

The project uses Minecraft 1.21.1 and NeoForge 21.1.249. Gradle can download a
Java 21 toolchain automatically if your installed Java version is different.

## Open the project

1. Open this folder in IntelliJ IDEA.
2. Choose **Import Gradle Project** if IntelliJ asks how to import it.
3. Wait for the Gradle sync and dependency downloads to finish.

## Useful commands

Use `gradlew.bat` instead of `./gradlew` on Windows.

```bash
# Start Minecraft with the mod installed
./gradlew runClient

# Compile and package the mod
./gradlew build
```

The built mod JAR appears in `build/libs/`.

## Project layout

```text
src/main/java/        Java source code
src/main/templates/   Mod information used by NeoForge
gradle.properties     Minecraft, NeoForge, and mod settings
build.gradle          Build and development-run setup
```

The starting mod only writes `Tutorial Mod is loading!` to the game log. This
keeps the first project focused on setup; blocks, items, and other features can
be added one concept at a time in later tutorials.

## Renaming the mod

For a real mod, update these values together:

1. Change `mod_id`, `mod_name`, and `mod_group_id` in `gradle.properties`.
2. Rename the Java package folder and the `package` line in `TutorialMod.java`.
3. Change `MOD_ID` in `TutorialMod.java` so it matches `mod_id` exactly.

Also choose an appropriate license before publishing the mod.
