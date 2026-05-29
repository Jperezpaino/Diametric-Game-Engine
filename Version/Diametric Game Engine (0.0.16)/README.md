# Diametric Game Engine

A lightweight 2D game engine written in pure Java using **Graphics2D** and a **diametric 2:1 projection** (tile size 64 × 32 px). No external frameworks.

## Status

🔄 **Phase 1 – Foundations** (current). See [`doc/ROADMAP.md`](doc/ROADMAP.md).

## Requirements

- Temurin JDK **21**
- Maven (TCN-IDE bundled distribution)

## Build

```powershell
$env:JAVA_HOME = "C:\Users\x922180\TCN-IDE\eclipse-jee-2022-12-R-win32-x86_64\jdk\Temurin-21.0.9+10"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
& "C:\Users\x922180\TCN-IDE\eclipse-jee-2022-12-R-win32-x86_64\Maven\bin\mvn.cmd" clean package "-f" "C:\Users\x922180\Copilot\Diametric Game Engine\pom.xml"
```

Output JAR: `target\Diametric Game Engine-0.0.0.jar`.

## Run

```powershell
& "C:\Users\x922180\TCN-IDE\eclipse-jee-2022-12-R-win32-x86_64\jdk\Temurin-21.0.9+10\bin\java.exe" -jar "C:\Users\x922180\Copilot\Diametric Game Engine\target\Diametric Game Engine-0.0.0.jar"
```

A 800×600 window will appear showing a 7×7 diametric demo map (grass interior, wall border, water tile at column 3 / row 3) with a red diamond marking the player and an FPS/UPS HUD.

## Documentation

- [`doc/ARCHITECTURE.md`](doc/ARCHITECTURE.md) – modules and responsibilities
- [`doc/PROJECTION.md`](doc/PROJECTION.md) – 2:1 projection math
- [`doc/ROADMAP.md`](doc/ROADMAP.md) – planned phases
- [`doc/phases/PHASE_01.md`](doc/phases/PHASE_01.md) – Phase 1 details
