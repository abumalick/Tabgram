#!/usr/bin/env bash
# Usage: scripts/tabgram-footprint.sh [base]   (default: $TABGRAM_BASE or 0aae9598)
set -euo pipefail

cd "$(git rev-parse --show-toplevel)" || exit 1
base=${1:-${TABGRAM_BASE:-0aae9598}}

fork_owned=(
  ':!tabgram.gradle'
  ':!UPSTREAM-UPDATE.md'
  ':!scripts/tabgram-*'
  ':!TMessagesProj/src/tabgram'
  ':!TMessagesProj/src/test/java/org/telegram/tabgram'
  ':!TMessagesProj_App/src/tabgram'
)

echo "base: $(git rev-parse --short "$base")"
echo
echo "(a) everything:"
git diff --shortstat "$base" HEAD
echo
echo "(b) upstream files only:"
git diff --shortstat "$base" HEAD -- . "${fork_owned[@]}"
echo
git diff --stat=100 "$base" HEAD -- . "${fork_owned[@]}"
