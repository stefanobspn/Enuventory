# Enuventory

[![CI](https://github.com/stefanobspn/Enuventory/actions/workflows/ci.yml/badge.svg)](https://github.com/stefanobspn/Enuventory/actions/workflows/ci.yml)

Enuventory is a native Android app (Kotlin + Jetpack Compose) for managing an inventory of
borrowable assets/equipment. Two roles share the same app:

- **Admin** — manages assets and categories, approves/rejects borrow requests, manages user
  accounts.
- **RegularUser** — browses assets, requests to borrow, returns items with photo proof, scans
  QR codes.

Backend is Firebase (Auth + Firestore) for data/auth, plus Supabase Storage for file uploads
(asset photos) — there is no custom server for either.

## Tech stack

- **UI**: Jetpack Compose, Material 3
- **Architecture**: Clean Architecture (`ui → domain → data`), MVVM, one `@HiltViewModel` per
  screen
- **DI**: Hilt
- **Async**: Kotlin Coroutines + Flow
- **Navigation**: Navigation Compose with type-safe `@Serializable` routes
- **Backend**: Firebase Auth + Firestore
- **File storage**: Supabase Storage (Firebase Storage is intentionally not used — see
  [CLAUDE.md](CLAUDE.md))
- **Image loading**: Coil
- **Camera / QR**: CameraX, ML Kit Barcode Scanning, ZXing
- **Serialization**: kotlinx.serialization

See [CLAUDE.md](CLAUDE.md) for a full architecture breakdown (layer responsibilities, DI wiring,
auth/role routing, module conventions).

## Requirements

- Android Studio (latest stable) or the Gradle CLI
- JDK 17+
- Android SDK: `compileSdk`/`targetSdk` 37, `minSdk` 24
- A Firebase project with Email/Password Auth and Firestore enabled
- A Supabase project with a public-read Storage bucket named `Enuventory`

## Setup

1. **Firebase** — place your `google-services.json` in `app/`. (A project file is already
   checked in for this repo; substitute your own if pointing at a different Firebase project.)
2. **Supabase** — create `local.properties` in the repo root (gitignored, never commit this) and
   add:

   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```

   These are read at build time via `buildConfigField` (see `app/build.gradle.kts` and
   `di/SupabaseModule.kt`). The anon key is safe to embed client-side by Supabase's design, but
   the file itself stays out of version control regardless.

## Build & run

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build + install on a connected device/emulator
./gradlew test                   # run JVM unit tests (app/src/test)
./gradlew connectedAndroidTest   # run instrumented tests (app/src/androidTest, needs a device/emulator)
./gradlew lint                   # Android lint
```

## Releases

Publishing a GitHub Release (`.github/workflows/release.yml`, triggered on `release: published`)
builds the `release` build variant (`./gradlew assembleRelease`) and attaches the resulting APK to
that release automatically. The `release` build type currently reuses the debug signing config
(see `app/build.gradle.kts`), so no signing secrets are required.

## Testing

JVM unit tests (`app/src/test`) cover the ViewModels for both main flows (borrow + return) plus
the asset-ID generator, using hand-written fakes in `fake/` (no mocking library, no Robolectric).
Instrumented tests (`app/src/androidTest`) are still just the default template placeholder.

## Project structure

```
app/src/main/java/dev/stefano/enuventory/
├── data/repository/   # Repository implementations (Firebase, Supabase)
├── di/                 # Hilt modules
├── domain/
│   ├── model/          # Plain domain models & enums
│   ├── repository/     # Repository interfaces
│   └── usecase/        # One class per action
└── ui/
    ├── screen/<feature>/  # ViewModels
    ├── common/            # Shared UiState wrapper
    ├── pages/             # Stateless Composable screens
    ├── components/        # Shared Enu* Compose widgets
    ├── navigation/        # NavHost + type-safe routes
    ├── theme/             # Compose theme
    └── util/              # UI mappers
```