# StackBoxes

A Simon-style memory game for Android, built with Jetpack Compose.

Watch boxes light up in order, then tap them back in the same order. Each box you get right is
pushed onto a stack; when the round ends the stack unwinds, last in, first out.

## How to play

1. Pick a difficulty and press **Start**.
2. Watch the sequence, then repeat it.
3. Each round you complete replays the same sequence with one new box added, played a little
   faster.
4. A wrong tap costs a life and replays the round. Lose all three lives and the game is over.
5. Every three completed rounds earn a life back, up to three.
6. Your score is the longest sequence you completed. Use every box on the grid to clear the game.
7. Your best score for each difficulty is saved on the device. **Leaderboard** shows the top
   five scores for the selected difficulty, with the date each was set.
8. Turn on **Reverse** to repeat each sequence backwards, last box first. Reverse mode has its
   own best scores and leaderboards.
9. Each box has its own retro tone, rising from C4 on box 1 to C6 on the last box, played
   when it lights up and when you tap it. A wrong tap buzzes. Correct taps give a light
   vibration tick and wrong taps a short buzz.
10. The gear in the top-left corner opens **Settings**, where your choices are remembered:
    - **Appearance**: **System** (the default, following the phone's dark theme), **Light** or
      **Dark**.
    - **Sound** and **Vibration**, on by default.
    - Accessibility: **Slower playback** keeps each box lit 50% longer; **Reduce motion** gives
      a still launch screen (Android 13 and later); **Show box numbers** can hide the numbers
      (TalkBack still reads "Box N").
    - **App icon**: **Arcade** (pixel blocks and a heart, the default) or **Sequence** (a lit
      trail on the grid). The app's colours and lives hearts change to match at once (pixel
      hearts for Arcade, smooth ones for Sequence); the launcher icon changes once you leave
      the app.

| Difficulty | Grid | Starting sequence | Speed (each box lit)                   |
|------------|------|-------------------|----------------------------------------|
| Easy       | 3×3  | 4 boxes           | 450 ms, 30 ms faster every round       |
| Medium     | 4×4  | 6 boxes           | 550 ms, 20 ms faster every round       |
| Hard       | 5×5  | 8 boxes           | 650 ms, 10 ms faster every round       |

Speed never goes below 250 ms per box. Hard plays slowest so its long sequences stay playable.

Boxes never repeat within a sequence. Taps are ignored while a sequence is playing, and the
difficulty is locked until the game ends. Rotating the phone keeps the game exactly where it
was; in landscape the controls sit beside the grid.

## Build and run

Requirements: JDK 17 and the Android SDK (compile SDK 37). The app runs on Android 7.0
(API 24) and later.

```sh
./gradlew :app:assembleDebug   # build
./gradlew :app:installDebug    # install on a connected device or emulator
```

Or open the project in Android Studio and run the `app` configuration.

## Testing

```sh
./gradlew :app:testDebugUnitTest
```

Unit tests run on the JVM, with no device needed, and take a couple of seconds:

- `GameStateTest`: rounds, taps, lives and refills, reverse mode, and playback speed. Uses
  `kotlinx-coroutines-test` virtual time, so whole games run instantly.
- `ScoreStoreTest`: saved scores (sorting, top five, ties, separate difficulties and modes,
  damaged data), using an in-memory `SharedPreferences`.
- `GameFeedbackTest`: when sounds and vibrations are triggered, and that ignored taps
  trigger nothing.
- `ToneTest`: box pitches (distinct, rising, C4 to C6, on the scale).
- `SettingsTest`: sound and vibration switches and their persistence.

Results are written to `app/build/reports/tests/testDebugUnitTest/index.html`.

## Code layout

- `Game.kt`: game rules (`GameState`, difficulties, speed, rounds, lives, score, reverse)
- `MainActivity.kt`: the screen (difficulty picker, status, grid; portrait and landscape)
- `GameViewModel.kt`: holds the game, sound, vibration, settings and scores across rotation
- `Lives.kt`: pixel-heart lives indicator
- `Scores.kt`: saved top scores per difficulty and mode (`ScoreStore`, SharedPreferences)
- `Leaderboard.kt`: leaderboard dialog
- `Tones.kt`: box pitches; `Sound.kt`: square-wave tone player (`SoundPlayer`)
- `Haptics.kt`: vibration; `Settings.kt`: saved settings (sound, vibration, accessibility)
- `SettingsScreen.kt`: the settings screen and its gear button
- `AppIcon.kt`: the launcher icons (one activity-alias each) and switching between them
