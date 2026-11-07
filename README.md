# Nim TV

The [Nim](../nim) launcher, for Google TV. Text only: a clock, your favorites
and an A–Z grid of every app, driven by the remote.

Built with Kotlin + Jetpack Compose, for personal use (TCL Google TV).

## Features

- **Home** with clock, date and favorites. `todos os apps` opens the grid.
- **A–Z grid** in four columns, with search one press up (same ranking as the
  phone Nim: `yt` finds YouTube, `cam` finds Câmera).
- **Hold OK** (or the remote's Menu key) on an app: favorite, move, app info
  or uninstall.
- **Mover**: up/down reorder the favorite, OK confirms, Back cancels.
- **Home button** opens Nim TV, through an accessibility service.

## Why an accessibility service

Google TV ignores third-party HOME activities: the Home button always opens
Google's launcher. `HomeRedirectService` watches which app comes to the front
and, when it's the stock launcher, opens Nim TV over it. It only receives
window-change events, never screen content. The cost is a brief flash of the
stock launcher.

The alternative, disabling the stock launcher with `pm disable-user`, breaks
input switching and settings shortcuts on some TCL models, so it isn't used.

## Differences from the phone Nim

- Apps come from `PackageManager` with `LEANBACK_LAUNCHER`: `LauncherApps`
  only lists `LAUNCHER` activities and misses most TV apps.
- No work profile, pinned shortcuts or wallpaper: TVs don't have them.
- No auto-launch on a single search result: the TV keyboard covers the list.

## Build and install on the TV

On the TV: Settings › System › About › tap *Android TV OS build* 7 times, then
Settings › System › Developer options › enable *USB debugging* (and *Wireless
debugging* / network debugging, if listed).

```sh
./gradlew :app:assembleDebug
adb connect <tv-ip>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then turn on the Home button: Settings › System › Accessibility › Nim TV, or:

```sh
adb shell settings put secure enabled_accessibility_services com.gadfaria.nimtv/com.gadfaria.nimtv.HomeRedirectService
adb shell settings put secure accessibility_enabled 1
```

## Tests

```sh
./gradlew :app:testDebugUnitTest
```

Search ranking (`data/AppSearch.kt`, copied from Nim) and favorite reordering
(`data/Favorites.kt`).
