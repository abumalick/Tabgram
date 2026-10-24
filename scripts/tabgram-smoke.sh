#!/usr/bin/env bash
# Usage: ADBX=<helper> DEVICE=<name> scripts/tabgram-smoke.sh [apk]
# ADBX: an adbx-compatible device helper exposing connect/use/unlock/ui/release.
# DEVICE: the device name ADBX knows. ANDROID_HOME: SDK root (default ~/Android/Sdk).
# No -e: every check must run and report; failures are tracked in $failed.
set -uo pipefail

cd "$(git rev-parse --show-toplevel)" || exit 1
APK=${1:-TMessagesProj_App/build/intermediates/apk/afat/release/app.apk}
if [ -z "${ADBX:-}" ] || [ -z "${DEVICE:-}" ]; then
  echo "usage: ADBX=<adbx-compatible helper: connect/use/unlock/ui/release> DEVICE=<name> $0 [apk]" >&2
  exit 2
fi
PKG=net.hilson.tabgram
SDK=${ANDROID_HOME:-$HOME/Android/Sdk}
BT=$(find "$SDK/build-tools" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort -V | tail -1)
AAPT2=$BT/aapt2
APKSIGNER=$BT/apksigner
WORK=.tmp/tabgram-smoke
mkdir -p "$WORK"

failed=0
pass() { echo "PASS $1"; }
fail() { echo "FAIL $1"; failed=1; }
check() { # name, command...
  local name=$1; shift
  if "$@"; then pass "$name"; else fail "$name"; fi
}
dev() { "$ADBX" "$DEVICE" "$@"; }
ui_has() { local out; out=$(dev ui) || return 1; grep -Eq "$1" <<<"$out"; }
wait_ui() { # regex, seconds
  local end=$((SECONDS + $2))
  while ((SECONDS < end)); do ui_has "$1" && return 0; sleep 1; done
  return 1
}
cert_of() { local out; out=$("$APKSIGNER" verify --print-certs "$1" 2>/dev/null) || return 1; sed -n 's/.*certificate SHA-256 digest: //p' <<<"$out" | head -1; }

cleanup() { "$ADBX" release "$DEVICE" >/dev/null 2>&1; }
trap cleanup EXIT

[ -f "$APK" ] || { echo "FAIL apk-present ($APK)"; exit 1; }
[ -x "$AAPT2" ] && [ -x "$APKSIGNER" ] || { echo "FAIL build-tools (aapt2/apksigner not found under $SDK)"; exit 1; }

# Static checks on the APK
badging=$("$AAPT2" dump badging "$APK" 2>/dev/null)
check apk-package grep -q "^package: name='$PKG'" <<<"$badging"
check apk-label grep -q "^application-label:'Tabgram'" <<<"$badging"
resources=$("$AAPT2" dump resources "$APK" 2>/dev/null)
check apk-res-notification grep -q 'drawable/notification' <<<"$resources"
check apk-res-ic_launcher grep -q 'mipmap/ic_launcher' <<<"$resources"

"$ADBX" connect "$DEVICE" || { fail device-connect; exit 1; }
"$ADBX" use "$DEVICE" || { fail device-lease; exit 1; }
dev unlock || { fail device-unlock; exit 1; }
pass device-unlock

# Never overwrite an install signed with another key
installed=$(dev shell pm path "$PKG" 2>/dev/null | tr -d '\r' | sed -n '1s/^package://p')
if [ -n "$installed" ]; then
  dev pull "$installed" "$WORK/installed.apk" >/dev/null 2>&1
  old=$(cert_of "$WORK/installed.apk"); new=$(cert_of "$APK")
  if [ -n "$old" ] && [ "$old" = "$new" ]; then pass cert-match
  else fail "cert-match (installed=$old apk=$new)"; echo "refusing to install"; exit 1; fi
fi

out=$(dev install -r "$APK" 2>&1)
if grep -q '^Success' <<<"$out"; then pass install; else fail "install: $out"; exit 1; fi

dev shell am force-stop "$PKG" >/dev/null 2>&1
dev shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
check launch wait_ui 'Log in by QR code|Start Messaging|Chats' 30

if ui_has 'Start Messaging'; then dev ui tap 'Start Messaging' >/dev/null; sleep 2; fi
if wait_ui 'Log in by QR code' 5; then
  pass login-qr-button
else
  check toolbar-title ui_has 'Tabgram'
  if dev ui tap '^Settings$' >/dev/null 2>&1; then
    sleep 2
    if ui_has 'Telegram Premium'; then fail settings-no-premium; else pass settings-no-premium; fi
    dev shell input keyevent KEYCODE_BACK >/dev/null 2>&1
  else
    fail settings-tab
  fi
fi

exit "$failed"
