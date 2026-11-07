# Nim TV

The Nim launcher, for Google TV. Text only: a clock, your favorites
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
Google's launcher. `HomeRedirectService` covers it two ways:

- **Home key:** consumed before the system sees it, then Nim TV opens. Reacting
  after the system handled Home doesn't work on Android 11: activity starts
  from other apps are held back for ~5 s after a Home press. Side effect: a
  long press on Home does nothing.
- **Stock launcher in front** (Back out of an app, boot): Nim TV opens over it,
  with a brief flash.

It only receives window-change and key events, never screen content.

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

On macOS, if `adb connect` says *No route to host* while the TV answers ping,
the terminal app lacks System Settings › Privacy & Security › Local Network.

### Turning on the Home button (TCL, Android 11)

1. TCL blocks system-started services of apps without its auto-start
   permission (`TclAppBoot` in logcat). Allow it for Nim TV:

   ```sh
   adb shell appops set com.gadfaria.nimtv APP_AUTO_START allow
   ```

2. Turn on Settings › System › Accessibility › Services › Nim TV. This TCL
   has no standard `ACCESSIBILITY_SETTINGS` screen, only
   `android.settings.ACCESSIBILITY_TV_OEM_LINK` — the in-app shortcut tries both.

3. Check it's running (`Bound services` must list Nim TV):

   ```sh
   adb shell dumpsys accessibility | grep "Bound services"
   ```

   If it's enabled but not bound, re-trigger the binding:

   ```sh
   adb shell settings put secure enabled_accessibility_services '""'
   adb shell settings put secure enabled_accessibility_services com.gadfaria.nimtv/com.gadfaria.nimtv.HomeRedirectService
   ```

A Home pressed with `adb shell input keyevent HOME` bypasses the accessibility
key filter, so it opens Nim TV only after the ~5 s delay. Test with the remote.

## Tests

```sh
./gradlew :app:testDebugUnitTest
```

Search ranking (`data/AppSearch.kt`, copied from Nim) and favorite reordering
(`data/Favorites.kt`).
