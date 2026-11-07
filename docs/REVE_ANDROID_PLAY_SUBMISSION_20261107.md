# RE:VE Android / Google Play submission handoff (2026-11-07)

## Verified sources and ownership
- Drive account: sk@terrabridge.us (connected Google Drive and Gmail)
- Source archive: https://drive.google.com/file/d/1tFIv7d6szlJB8up1hhjAKZ8EL94nX3JG/view
- Previous desktop/mobile website QA: https://drive.google.com/file/d/163mJH3i5VyKhhnCFYosDvpMUmlNAevsZ/view
- Website: https://reve.lexid.site/
- GitHub source: https://github.com/skvive/reve
- Development branch: `chatgpt/reve-android-api36-qa-20261107` (do not merge without QA signoff)
- Draft PR: https://github.com/skvive/reve/pull/1

## Android application identity
| Key | Value |
|---|---|
| Name | RE:VE |
| Application ID | `site.lexid.reve` |
| Android source path | `apps/android/` |
| Version | 1.9.1 |
| Version code | 3, **must check for uniqueness in Play Console** |
| Min API | 26 |
| Compile / target API | 36 |
| Website origin | https://reve.lexid.site |
| Environment | Concept product collection and WebAudio-based demonstration; no verified physical speaker pairing |

## Suggested Google Play listing (review before publication)
**Korean title:** RE:VE — 하이엔드 사운드

**Korean short description:** RE:VE 스피커 콘셉트 컬렉션과 사운드 랩을 탐색하세요.

**Korean full description:**
RE:VE는 하이엔드 사운드 디자인과 스피커 콘셉트를 소개하는 공식 브랜드 앱입니다.
12종의 디자인 콘셉트 컬렉션을 탐색하고, 제품 이야기와 오디오 관련 저널 콘텐츠를 확인할 수 있습니다.
사운드 랩은 웹 기반 체험용 시뮬레이션으로, 실제 스피커 하드웨어 제어나 측정 결과를 제공하지 않습니다.
사이트에서 안내하는 시각화·제품 사양·가격은 검증 전 콘셉트 또는 예시일 수 있으며 실제 제조·판매 제품과 다를 수 있습니다.

**English title:** RE:VE — Sound Concepts

**English short description:** Explore RE:VE luxury speaker concepts, stories and a sound lab.

**English full description:**
Explore RE:VE's concept collection of premium loudspeaker designs, creative stories, and audio journal. Sound Lab demonstrates an educational browser audio simulation, not a connection to physical speaker hardware. Product designs, specifications and indicative prices are concepts unless independently verified.

## Release blockers to clear in the actual Play Console
- Sign-in with `sk@terrabridge.us` **in a browser session authorized for Play Console**; connected Drive/Gmail do not grant Play Console access.
- Check if RE:VE already exists in this developer account; never create a duplicate app or reuse a conflicting package ID.
- Verify developer identity and trader/non-trader declaration. Google Play email dated 2026-11-04 notified an account-level deadline of 2027-02-01; current declaration status remains unknown.
- Verify legal entity, app content/rights, age rating, target audience, contact and distribution countries.
- Create an app-specific and production-accurate privacy policy, check SDK/cookie/analytics/lead intake flows, and complete Data Safety accurately. Do not infer the declaration from disabled API routes.
- Verify metadata, 512x512 high-res app icon, feature graphic, screenshots captured from actual installed app, app access instructions if login is required, data retention and account deletion controls if applicable.
- Check versionCode, initialize/verify Google Play App Signing, securely provision upload key (never commit secret), create **signed AAB**. The built unsigned AAB is not suitable for submission.
- Run real emulator and preferably real-device navigation, network errors, share/download behaviors, font/overflow QA. Verify Play pre-launch report and distribution-test track.
- Confirm that site-connected WebView offers sufficient value/functionality under Google Play policies before publication.

## Important limits
- Existing RE:VE site includes concept and demonstration experiences; payment/preorder/physical speaker integration are not established as production-ready.
- A successful APK build does **not** mean Play Console app registration or approval.
- Source and QA outputs are preserved on an isolated GitHub branch; original Drive archive and GitHub main unchanged.

## Verified emulator QA result — 2026-11-07

- Latest source commit: `993cdc5a499e77b6f095a3fc5a3b83a000e5a0fd`
- Passing workflow: https://github.com/skvive/reve/actions/runs/42267303580
- QA artifact ID: `13527761221`; https://github.com/skvive/reve/actions/runs/42267303580/artifacts/13527761221
- Test guest: Android 15 / API 35 `google_apis/x86_64`, headless GitHub Actions Android emulator.
- Installation: `adb install -r .../app-debug.apk` succeeded.
- Launch: `site.lexid.reve/.MainActivity`, `Status: ok`, cold-launch duration 4,069 ms.
- Logcat: **0 `FATAL EXCEPTION` matches** in full captured log.
- Evidence: full `emulator.log`, `app-start.txt`, `activity-dump.txt`, `logcat.txt`, `apk-badging.txt`, and `reve-emulator.png`.
- Build verified as `applicationId=site.lexid.reve`, `versionName=1.9.1`, `targetSdk=36`.
- Android mobile UI review: removed redundant RE:VE native header and moved native Share to 5th bottom nav position, preserving website header and language selector.
- **Scope limitation:** Android 15 single-emulator cold-start smoke QA only; not real-hardware, Android 16 emulator, purchase/auth flow, multi-page interaction, accessibility, or Google Play pre-launch testing.
- **Publication status:** GitHub draft PR is not merged; only unsigned release AAB exists; no Play Console app created or submitted in this chat. Play Console access and account owner actions remain necessary.
