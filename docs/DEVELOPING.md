# Developing BigWatts

For building the app yourself and publishing releases. Riders only need the [README](../README.md).

## Build

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew -q assembleDebug
adb install -r app/build.nosync/outputs/apk/debug/app-debug.apk
```

Debug builds show only real data, like release builds: to test without riding, send values from
the Mac with FakeWatts. Only install while no ride is recording.

Debug builds also have a preview of the field, for screenshots without a ride page:

```bash
adb shell am start -S -n com.daviddokakis.bigwatts/.PreviewActivity --es look KAROO --es number GAUGE --es gauge ARC --ei watts 315 --ei h 288
```

## Releases and update notices

The app tells riders on the Karoo when a newer release is out: `UpdateCheck.kt` reads
`releases/latest/download/manifest.json` every time the Karoo starts and once a day, never during a
ride, and puts an "Update available" card in Control Center › Notifications. Tapping it opens the
Karoo's Settings › Apps page for the app, whose UPDATE button installs the new APK. So every
release uploads `manifest.json` next to `bigwatts.apk`, and is a normal release (not a pre-release). While the repo is private, GitHub hides its
release files from the Karoo, so the check finds nothing (and waits a day) until it is public:

```bash
./gradlew :app:assembleRelease
./gradlew -q :app:releaseManifest -PreleaseNotes="What changed"
cp app/build.nosync/outputs/apk/release/app-release.apk bigwatts.apk
gh release create vX.Y.Z bigwatts.apk app/build.nosync/manifest.json --title vX.Y.Z --notes "What changed"
```

## Code map

| File | What it does |
| --- | --- |
| `BigWattsExtension.kt` | Entry point the Karoo binds to |
| `BigWattsSession.kt` | Live data: four power streams, heart rate, ride average, power zones from the rider profile |
| `BigWattsState.kt` | What the field shows; the style, top line and smoothing choices |
| `BigWattsDataType.kt` | The field: redraws when something visible changes (at most ~once a second) |
| `WattsRenderer.kt` | Draws the field as one bitmap: both looks, gauge arc / bar, line under the watts (IBM Plex Sans and Sans Condensed, the Karoo's own fonts, OFL) |
| `SettingsActivity.kt`, `Settings.kt` | Settings page and stored choices |
| `src/debug/` | The preview screen (fixed example values) and the field gallery |

Design: [`docs/design/`](design/README.md). The Karoo SDK (karoo-ext 1.1.9, Apache 2.0) is
included as source in `karoo-ext/`.
