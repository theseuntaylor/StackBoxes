# StackBoxes

A Simon-style memory game for Android, built with Jetpack Compose.

Watch boxes light up in order, then tap them back in the same order. Each box you get right is
pushed onto a stack; when the round ends the stack unwinds, last in, first out.

## How to play

1. Pick a difficulty and press **Start**.
2. Watch the sequence, then repeat it.
3. Each round you complete replays the same sequence with one new box added.
4. A wrong tap costs a life and replays the round. Lose all three lives and the game is over.
5. Every three completed rounds earn a life back, up to three.
6. Your score is the longest sequence you completed. Use every box on the grid to clear the game.

| Difficulty | Grid | Starting sequence |
|------------|------|-------------------|
| Easy       | 3×3  | 4 boxes           |
| Medium     | 4×4  | 6 boxes           |
| Hard       | 5×5  | 8 boxes           |

Boxes never repeat within a sequence. Taps are ignored while a sequence is playing, and the
difficulty is locked until the game ends.

## Build and run

Requirements: JDK 17 and the Android SDK (compile SDK 37). The app runs on Android 7.0
(API 24) and later.

```sh
./gradlew :app:assembleDebug   # build
./gradlew :app:installDebug    # install on a connected device or emulator
```

Or open the project in Android Studio and run the `app` configuration.

## Code layout

- `Game.kt`: game rules (`GameState`, difficulties, rounds, lives, score)
- `MainActivity.kt`: the screen (difficulty picker, status, grid)
- `Lives.kt`: pixel-heart lives indicator
