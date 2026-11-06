# RE:VE Android v1.9.1 (2026-11-07)

Reuse of the original Google Drive RE:VE v1.9 Android Java WebView app, unchanged site origin https://reve.lexid.site. Package: site.lexid.reve. Min SDK 26, compile/target SDK 36. AGP 8.13.2 + Gradle 8.13 + JDK 17.

Build with Android SDK 36: `gradle -p apps/android :app:assembleDebug :app:bundleRelease`.
The debug APK is installable for tests; the release AAB is **unsigned and must not be submitted to Google Play**. Store publication requires secure signing, privacy declarations and a Play Console account.

CI workflow: .github/workflows/reve-android-ci.yml compiles and attempts installation / launch on an Android emulator. CI smoke tests do not replace manual device tests, network validation or Play pre-launch checks.

The app contains a native navigation bar and share sheet, and renders the owned website. Audio processing is a website simulation only: no hardware speaker connection, paid purchases or production identity verification. Google Play may reject apps that provide insufficient native functionality.
