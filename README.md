# Fast Hoppers (Fabric, Minecraft 26.3)

Build: `./gradlew build` (JDK 25+) -> `build/libs/fast-hoppers-1.0.0.jar`

If there's no Gradle wrapper, generate a fresh project at https://fabricmc.net/develop/template/
and copy `src/` plus the versions from `gradle.properties` into it.

| Block         | Pushes out / 8 ticks | Pulls in / 8 ticks |
|---------------|----------------------|--------------------|
| Vanilla       | 1                    | 1                  |
| Better Hopper | 4                    | 16                 |
| Super Hopper  | 16                   | 32                 |
| Mega Hopper   | 64                   | 64                 |
| Insta Hopper  | everything (5 slots) | everything         |
|-----------------------------------------------------------|
