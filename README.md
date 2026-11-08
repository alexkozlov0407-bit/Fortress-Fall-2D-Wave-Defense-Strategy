# FortressFall

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

## Play the prototype

Run the desktop game with `./gradlew lwjgl3:run`. The current prototype has a side-view castle defense loop:

- Press `1`, `2`, or `3` to select a mine, arrow tower, or wall, then click one of the five empty plots.
- Mines generate gold automatically. Press `4` or `5` to recruit a knight or archer during preparation or a wave. Units advance, stop at the edge of the battlefield, fight, and can be defeated.
- Press `Space` to start the next enemy wave. Defeat enemies to earn gold and receive a gold bonus for clearing a wave.
- Later waves bring more enemies with increasing health; enemy count is shown while defending.
- Press `Esc` to return to the menu.

This version includes the build, recruitment, wave, and combat milestones. Pause, defeat-state, and retry handling are not included yet.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
