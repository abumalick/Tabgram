# Tabgram

Tabgram is an unofficial Android client built from upstream `DrKLO/Telegram`. It is a patch stack on branch `tabgram`: one commit per feature or fix on top of an upstream release commit. `master` mirrors upstream; never work on it.

Read `UPSTREAM-UPDATE.md` first: it has the layout, build and test commands, the update procedure and the manual checklist.

## Rules for changes

- **Upstream files get hooks, not logic.** A change to an upstream file is ideally one line that calls into `org.telegram.tabgram.*` by its fully qualified name, with no new imports. Put the decision in a pure function under `TMessagesProj/src/tabgram/java/org/telegram/tabgram/` so it can be unit-tested. When upstream code must be disabled, neutralise it in place (`if (false && ...)`, early `return`) rather than deleting it, so rebase conflicts stay small and recognisable.
- **Resources and manifest changes go in the overlay** (`TMessagesProj_App/src/tabgram/`, overriding upstream resources by name or removing manifest entries with `tools:node="remove"`). Never edit or delete upstream binaries.
- **Strings:** new strings go in `TMessagesProj/src/tabgram/res/values/strings.xml`; `tabgram.gradle` feeds them to the localization task.
- **Tests first:** add or extend a JVM test in `TMessagesProj/src/test/java/org/telegram/tabgram/` and run `./gradlew :TMessagesProj:testDebugUnitTest` (no native build involved). UI-only behaviour that cannot be tested on the JVM is verified on a device and the commit message says so.
- **History:** a follow-up fix to an existing feature is folded into that feature's commit (fixup + autosquash), keeping one commit per feature. A new feature or bug fix gets its own commit, placed before the final docs commit. Keep `UPSTREAM-UPDATE.md`'s checklist in sync.
- **Measure the footprint** with `scripts/tabgram-footprint.sh` after a change; the upstream-files diffstat is what future rebases must carry.

## Constraints that must not regress

- The app name never contains "Telegram" unless preceded by "Unofficial"; no Telegram logo (Telegram API Terms of Service).
- Sponsored messages are still fetched, shown, and reported as viewed and clicked; only their rendering is compact.
- Hide Premium must not change behaviour for Premium accounts. Star gifts, Stars and other users' Premium badges stay.
- The applicationId, signing key and `versionCode` floor must stay the same, or installed copies can no longer update in place and users lose their login.
- Never commit `unofficial.properties`, the keystore or `google-services.json`.

## Release build

```sh
./gradlew :TMessagesProj_App:assembleAfatRelease -Pandroid.injected.build.abi=arm64-v8a -Pandroid.injected.testOnly=false
```

Output: `TMessagesProj_App/build/intermediates/apk/afat/release/app.apk`.
