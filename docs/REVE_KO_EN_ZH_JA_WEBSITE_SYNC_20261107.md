# RE:VE four-language website sync (2026-11-07)
This branch preserves the successfully deployed site edits in source control without overwriting any originals on main.

Changes:
- Korean default; English, Simplified Chinese and Japanese selection.
- Expanded /locales/native.json to 506 keys.
- Homepage hero, multi-language catalogue, and full-page translation behavior.
- Updated PWA service-worker cache version and mobile UI fixes.
- Versioned Android v1.9.2 QA APK resides at https://reve.lexid.site/downloads/REVE_Android_v1.9.2_debug.apk; this branch does not change the APK.
- Confirm merge impact on connected Vercel project; don't overwrite production with older sources.

Acceptance tests:
- Check mobile widths 320, 390, 480, 768, 1280.
- Confirm navigation and content in all four languages across all pages, not just menu labels.
- Validate frontend JS and anchors; no empty images and all buttons functional.
- Confirm app shell Android v1.9.3 newer PR #1 independently.
