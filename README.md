## Tabgram

**Tabgram is an unofficial Telegram client for Android. It is not affiliated with or endorsed by Telegram.** It is built from the official source code below, kept as a small stack of commits on branch `tabgram` so upstream releases can be merged in easily.

Changes from the official app:

- Log in by scanning a QR code from another logged-in device.
- Reorder All Chats among the folder tabs without Premium.
- An optional switch hides Premium-only content and upsell for non-Premium accounts.
- Sponsored messages are shown as a compact pill; they are still reported as viewed and clicked.
- Each chat or channel gets its own notification instead of one grouped notification, so tapping it always opens that chat.
- Fixes: Back on tablets after a layout change, Archive kept on rotation, the New Contacts notification switch, and no repeated photo-access prompt after limited access.

### Install from F-Droid

Tabgram is published in a personal F-Droid repository (arm64-v8a devices only).

1. Install [F-Droid](https://f-droid.org).
2. On the device, open this link, or scan it as a QR code, to add the repository:
   https://fdroid.hilson.net/repo?fingerprint=e818ba438881b7fbaeda975f6e8e4e5a345eb00c72c4a09a7234b78ffad24b7c
3. Refresh the repositories in F-Droid, search for "Tabgram" and install it.

Repository signing key SHA-256: `E8 18 BA 43 88 81 B7 FB AE DA 97 5F 6E 8E 4E 5A 34 5E B0 0C 72 C4 A0 9A 72 34 B7 8F FA D2 4B 7C`. The APK is signed with certificate SHA-256 `888f1be3da580c04d230906d4d03fbb80ceff9a41b0288d4ebfb83c87fb610a3`.

Tabgram has its own application ID (`net.hilson.tabgram`), so it installs next to the official Telegram app rather than replacing it.

### Build

To build it, see [UPSTREAM-UPDATE.md](UPSTREAM-UPDATE.md). You need your own api_id, signing key and Firebase project in the gitignored files it lists.

The upstream README follows.

## Telegram messenger for Android

[Telegram](https://telegram.org) is a messaging app with a focus on speed and security. It’s superfast, simple and free.
This repo contains the official source code for [Telegram App for Android](https://play.google.com/store/apps/details?id=org.telegram.messenger).

## Creating your Telegram Application

We welcome all developers to use our API and source code to create applications on our platform.
There are several things we require from **all developers** for the moment.

1. [**Obtain your own api_id**](https://core.telegram.org/api/obtaining_api_id) for your application.
2. Please **do not** use the name Telegram for your app — or make sure your users understand that it is unofficial.
3. Kindly **do not** use our standard logo (white paper plane in a blue circle) as your app's logo.
3. Please study our [**security guidelines**](https://core.telegram.org/mtproto/security_guidelines) and take good care of your users' data and privacy.
4. Please remember to publish **your** code too in order to comply with the licences.

### API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto

### Compilation Guide

**Note**: In order to support [reproducible builds](https://core.telegram.org/reproducible-builds), this repo contains dummy release.keystore,  google-services.json and filled variables inside BuildVars.java. Before publishing your own APKs please make sure to replace all these files with your own.

You will require Android Studio 2025.1.4, Android NDK 27.2.12479018 and Android SDK 36.

1. Clone the Telegram source code with its submodules:
   ```bash
   git clone --recursive --shallow-submodules https://github.com/DrKLO/Telegram.git Telegram
   ```
   In case you forgot the `--recursive` flag, change to the `Telegram` directory and run:
   ```bash
   git submodule init && git submodule update --init --recursive --depth=1
   ```
2. Copy your release.keystore into TMessagesProj/config
3. Fill out RELEASE_KEY_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_STORE_PASSWORD in gradle.properties to access your  release.keystore
4.  Go to https://console.firebase.google.com/, create two android apps with application IDs org.telegram.messenger and org.telegram.messenger.beta, turn on firebase messaging and download google-services.json, which should be copied to the same folder as TMessagesProj.
5. Open the project in the Studio (note that it should be opened, NOT imported).
6. Fill out values in TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java – there’s a link for each of the variables showing where and which data to obtain.
7. You are ready to compile Telegram.

### Localization

We moved all translations to https://translations.telegram.org/en/android/. Please use it.
