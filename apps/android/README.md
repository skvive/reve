# RE:VE Android v1.9.3 — verification and release gates

The Android wrapper is reused from the original RE:VE project and renders https://reve.lexid.site using an HTTPS-only WebView. Application ID: `site.lexid.reve`; min SDK 26, compile/target SDK 36, versionCode 5. Existing official branding and source are retained.

## 1. Android device installation
- GitHub Actions `reve-android-ci.yml` builds a **debug-signed** APK, verifies the signature and installs/launches it on an Android 35 emulator.
- Physical phone installation is **not** established by an emulator result. Connect a physical device with Developer options/USB debugging and run `adb install -r app-debug.apk`; open RE:VE, navigate Home / Collection / Sound Lab / Journal, then capture `adb logcat -d` and screen recordings. Verify offline/retry, sharing and external links.
- Debug certificate is for testing only; it cannot be treated as the official Play signing certificate.

## 2. Foldable and large-screen
- `reve-android-ci.yml` runs `apps/android/tests/adaptive-qa.sh` (320 px, 390 px, 360 px outer, 840 px inner, landscape, 200% text scenarios). These sizes are **simulated viewports**, not a genuine hinge test.
- `reve-android-foldable.yml` starts a foldable emulator and calls `adb emu fold` / `adb emu unfold`, checking viewport changes, activity survival, screenshot evidence and logcat. A failure to change physical display dimensions is **BLOCKED**, never PASS.
- A real Samsung Galaxy Z Fold or equivalent must still be checked opening and closing during active navigation, split screen, resuming from the background, changing orientation and holding a deep link. Real hardware is a separate gate.

## 3. Accessibility
- Native tab actions have full accessible names in Korean, English, simplified Chinese and Japanese; pressed/focused state is highlighted. The 200% text-mode tabs use compact visible labels but preserve full screen-reader descriptions.
- Native action touch targets are at least 48dp in the adaptive-qa matrix. Its UIAutomator test is **strict**: if no accessibility tree is returned, it reports BLOCKED rather than pretending to pass.
- Manual TalkBack, Switch Access, screen magnifier, dynamic font sizes, high contrast, keyboard-only focus order, contrast and **website WebView content** require hands-on evaluation. The automated native-button test does not certify complete WCAG compliance.

## 4. Play App Signing (signed AAB)
- `gradle -p apps/android :app:bundleRelease` without signing variables produces an **unsigned** AAB; its ZIP structure alone does not make it Play-ready.
- Secure release signing is opt-in and must use the existing Play Console upload certificate, never a newly invented production identity.
- Add **encrypted GitHub Actions secrets** (not in source or chat): `REVE_UPLOAD_JKS_BASE64`, `REVE_UPLOAD_STORE_PASSWORD`, `REVE_UPLOAD_KEY_ALIAS`, `REVE_UPLOAD_KEY_PASSWORD`, `REVE_UPLOAD_CERT_SHA256`. The final value is the SHA-256 of the actual Play Console **upload** certificate.
- After code is merged to the default branch, manually run workflow `reve-android-release.yml`. It securely decodes the keystore on the runner, builds `bundleRelease`, checks the JAR signature, ZIP integrity and exact expected certificate SHA-256 using `tests/verify-play-aab.sh`, and publishes a short-lived signed AAB artifact.
- Verify versionCode vs any existing Play listing, app ownership, data safety, privacy policy, Play pre-launch report, app access instructions, content rating and internal closed test before any production release.
- Do not upload/release with a debug key, without the authorized upload key, or before these gates pass.

## Scope boundaries
The app is an existing WebView-based site container. Website audio demonstrations do **not** imply connection to physical speaker hardware. Live payment or other hardware-related integrations require separate verification.
