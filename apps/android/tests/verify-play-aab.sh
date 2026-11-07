#!/usr/bin/env bash
# Verify AAB contents and the exact Play Console upload certificate SHA256.
set -euo pipefail
AAB="${1:?Usage: verify-play-aab.sh path/to/app-release.aab}"
test -s "$AAB"
unzip -t "$AAB" > /dev/null
PROOF="$(mktemp)"
trap 'rm -f "$PROOF"' EXIT
if ! jarsigner -verify -verbose -certs "$AAB" > "$PROOF" 2>&1; then
  echo "FAIL: AAB signature validation command returned an error" >&2
  tail -12 "$PROOF" >&2
  exit 2
fi
if ! grep -Fxq 'jar verified.' "$PROOF"; then
  echo "FAIL: AAB lacks a verified JAR signature; Play upload blocked" >&2
  tail -12 "$PROOF" >&2
  exit 2
fi
CERT="$(keytool -printcert -jarfile "$AAB" 2>&1)"
ACTUAL="$(printf '%s\n' "$CERT" | sed -n 's/^[[:space:]]*SHA256: //p' | head -n 1 | tr -d ':[:space:]' | tr '[:lower:]' '[:upper:]')"
test -n "$ACTUAL" || { echo "FAIL: certificate fingerprint unavailable" >&2; exit 3; }
EXPECTED="$(printf '%s' "${REVE_UPLOAD_CERT_SHA256:-}" | tr -d ':[:space:]' | tr '[:lower:]' '[:upper:]')"
if [[ -z "$EXPECTED" ]]; then
  echo "BLOCKED: the Play upload certificate SHA256 must be provided by the account owner" >&2
  echo "Observed certificate SHA256: $ACTUAL" >&2
  exit 4
fi
if [[ "$EXPECTED" != "$ACTUAL" ]]; then
  echo "FAIL: upload certificate does not match Play Console" >&2
  echo "Observed certificate SHA256: $ACTUAL" >&2
  exit 5
fi
echo "PASS: signed AAB verified; Play upload certificate SHA256 matched: $ACTUAL"
sha256sum "$AAB"
