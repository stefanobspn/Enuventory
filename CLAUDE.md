# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project overview

Enuventory is a native Android app (Kotlin + Jetpack Compose) for managing an inventory of
borrowable assets/equipment. Two roles share the same app: `Admin` (manages assets, approves/rejects
borrow requests) and `RegularUser` (browses assets, requests to borrow, returns items with photo
proof). Backend is Firebase (Auth + Firestore) for data/auth, plus Supabase Storage for file
uploads (asset photos) — no custom server for either.

Code identifiers (classes, functions, variables) are in English; comments and in-app user-facing
strings/UI labels are written in Bahasa Indonesia (e.g. `Pengembalian` = return, `Riwayat` =
history,
`Persetujuan`/`Approval`, `Tambah` = add). Keep this convention when touching existing files.

## Build & run

This is a standard Gradle/Android Studio project — use Android Studio, or the CLI:

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build + install on connected device/emulator
./gradlew test                   # run JVM unit tests (app/src/test)
./gradlew connectedAndroidTest   # run instrumented tests (app/src/androidTest, needs a device/emulator)
./gradlew lint                   # Android lint
```

JVM unit tests (`app/src/test`) cover the ViewModels for both main flows (borrow + return) plus
the asset-ID generator, using hand-written fakes in `fake/` (no mocking library, no Robolectric —
`testOptions.unitTests.isReturnDefaultValues = true` handles the odd `android.util.Log` call).
Instrumented tests (`app/src/androidTest`) are still just the default template placeholder.

Firebase requires `app/google-services.json` (already present in this repo) and a matching
Firebase project (Auth email/password + Firestore enabled). Supabase Storage requires
`SUPABASE_URL` and `SUPABASE_ANON_KEY` in `local.properties` (gitignored, not committed) — see
`di/SupabaseModule.kt`. Firebase Storage is intentionally **not** used: as of the Google policy
change in Oct 2024, new Storage buckets require the paid Blaze plan, so file uploads (asset
photos) go through Supabase Storage's free tier instead (`data/repository/StorageRepositoryImpl.kt`,
bucket `Enuventory`, public read).

## Architecture

Standard Clean Architecture, three layers, strict dependency direction
`ui → domain → data` (domain never imports data or ui):

- **`domain/`** — framework-free business layer.
    - `model/` — plain data classes (`Asset`, `BorrowRecord`, `User`, `Category`, `AppThemeMode`,
      enums like `AssetStatus`, `BorrowStatus`, `UserRole`). No Firestore/Android types leak in
      here. `Category` (its own `categories` Firestore collection, managed via the "Kelola
      Kategori" admin screen) is the source of truth for both the Tambah/Edit Asset category
      picker and the Home page category filter badges — `Asset.category` itself stays a plain
      string, kept in sync by cascading renames through `KelolaKategoriViewModel`.
    - `repository/` — repository *interfaces* only.
    - `usecase/` — one class per action (e.g. `RequestBorrowUseCase`, `ApproveRequestUseCase`,
      `GetAssetsUseCase`), each with a single `operator fun invoke(...)`. ViewModels depend on
      use cases, not repositories directly.
- **`data/repository/`** — repository implementations talking directly to Firebase
  (`FirebaseFirestore`, `FirebaseAuth`). Firestore reads are exposed as `Flow` via
  `callbackFlow` + `addSnapshotListener` (real-time) for lists/streams, and `suspend fun` +
  `.await()` for one-shot writes/mutations. Firestore documents are hand-mapped to domain models
  with private `DocumentSnapshot.toX()` extension functions inside each repo — there is no
  DTO/mapper layer or Room database.
    - Query results are sorted **in memory** (`.sortedByDescending { ... }`) rather than via
      Firestore `.orderBy()`, specifically to avoid requiring composite indexes (see git history —
      this caused a real infinite-loading bug).
    - Avoid blocking one-shot `.get()` calls inside write flows (e.g. `requestBorrow`) when the
      needed data is already held in memory/state — pass it in as parameters instead (see git
      history for a prior bug this pattern fixes).
- **`di/`** — Hilt modules. `RepositoryModule` binds each `XxxRepository` interface to its
  `XxxRepositoryImpl` with `@Binds @Singleton`. `FirebaseModule` provides `FirebaseAuth`/
  `FirebaseFirestore` instances, plus a **second**, `@SecondaryAuth`-qualified `FirebaseAuth`
  bound to its own secondary `FirebaseApp` — used solely by `UserRepositoryImpl.createUser()` so
  Admin can create a real Firebase Auth login for a new RegularUser account without getting
  signed out of their own Admin session (`createUserWithEmailAndPassword` on the *primary*
  instance would otherwise sign in as the new user). There is no Firebase Admin SDK/Cloud
  Functions in this project, so this client-only trick is also why account **deletion** is a
  soft "disabled" flag on the user's Firestore doc (checked in `AuthRepositoryImpl.signIn()`) —
  actually revoking another user's Auth credential isn't possible from a pure client app.
  `SupabaseModule` provides the `SupabaseClient` (Storage plugin
  installed) used for file uploads. `DataStoreModule` provides the Preferences DataStore used for
  theme persistence.
- **`ui/`**
    - `screen/<feature>/` — one `@HiltViewModel` per screen (e.g.
      `screen/asset/DetailAssetUserViewModel.kt`).
      ViewModels expose `StateFlow<UiState<T>>` built via `combine`/`flatMapLatest` over use-case
      flows, collected with `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`.
    - `common/UiState.kt` — the single generic state wrapper (`Loading`, `Empty`, `Success<T>`,
      `Error`) used across *every* screen instead of one bespoke state enum per page. Reuse this
      rather than inventing a new per-screen state type.
    - `pages/` — stateless Composable screens (e.g. `HomeUserPage`, `DetailAssetAdminPage`) that
      take
      state + lambdas as parameters; they don't reach into ViewModels or repositories directly.
    - `components/` — shared, reusable Compose widgets prefixed `Enu*` (`EnuButton`, `EnuTextField`,
      `EnuBorrowDialog`, `EnuTopBar`, `EnuBottomBar`, badges, etc).
    - `navigation/EnuNavGraph.kt` + `EnuRoute.kt` — single `NavHost` wiring all screens; routes are
      type-safe `@Serializable` sealed interface members (
      `navController.navigate(EnuRoute.DetailAsset(id))`),
      not raw string routes. Admin vs. user destinations for the same route (e.g. `Home`,
      `Settings`)
      are branched inside the composable based on `currentUser?.role`, not via separate routes.
    - `theme/` — Compose theme (`EnuTheme`), custom color tokens, typography.
    - `util/DomainToUiMapper.kt` — maps domain models to page-specific UI models/strings.

### Auth & role routing

`AuthRepositoryImpl` streams the current `User` (with role) via a Firebase `AuthStateListener`
combined with a Firestore `users/{uid}` doc lookup (role defaults to `RegularUser` if the doc read
fails or `role` isn't `"admin"`). `EnuNavGraph` observes this as `currentUser` at the top level and
derives `isAdmin`/`startDestination` from it — there is no separate admin app variant or role
parameter threaded through call sites. Firebase email/password auth is the only sign-in method;
there is no mock/demo login path (previously existed, was intentionally removed).

## Module/version notes

- Single Gradle module: `app`. Dependency versions are centralized in `gradle/libs.versions.toml`
  (version catalog) — add new dependencies there, not as inline coordinates in `build.gradle.kts`.
- Kotlin serialization is used for type-safe nav args, not for network DTOs (there's no REST API).
- `compileSdk`/`targetSdk` 37, `minSdk` 24, Java 11 source/target compatibility. Core library
  desugaring is enabled (`isCoreLibraryDesugaringEnabled`) specifically because supabase-kt
  requires `minSdk` 26.
