# Android build (Capacitor)
Prereqs: Node 20, JDK 21, Android Studio (SDK). App ID `com.tipswala.app`.
1. `npm install && npx cap add android`
2. `python3 scripts/patch-android.py YOUR-DOMAIN` (adds notification permission and https deep links for `https://YOUR-DOMAIN`). For verified App Links host `https://YOUR-DOMAIN/.well-known/assetlinks.json` with your signing certificate fingerprint.
3. Icons/splash: convert `resources/icon.svg` → `resources/icon.png` (1024), `resources/splash.svg` → `resources/splash.png` (2732), then `npm run assets` (run it on your own computer; it is not installed in CI because its `sharp` dependency downloads a binary and can fail).
4. Push: add `google-services.json` from Firebase to `android/app/`.
5. Debug APK: `npm run apk:debug` → `android/app/build/outputs/apk/debug/`.
6. **Release keystore (do once, keep OFF git, back it up – losing it means you cannot update the app):**
   `keytool -genkey -v -keystore tipswala-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias tipswala`
7. Signed build locally:
   `cd android && ./gradlew bundleRelease assembleRelease -Pandroid.injected.signing.store.file=/path/tipswala-release.jks -Pandroid.injected.signing.store.password=… -Pandroid.injected.signing.key.alias=tipswala -Pandroid.injected.signing.key.password=…`
   Outputs: `app/build/outputs/bundle/release/*.aab` (Play Store) and `apk/release/*.apk`.
8. CI: workflow `android-release.yml` does steps 1–7 using GitHub secrets (listed at the top of the file). Version code = workflow run number; bump `versionName` in `android/app/build.gradle`.
9. Play Store: needs privacy policy URL, data-safety form, content rating, and a financial-features declaration. Review Play's policies for finance/crypto apps before submitting.
Native behaviour already coded in `frontend/js/app.js`: Android back button, deep-link handling, push registration, offline notice.
Not yet done: native file-picker upload, biometric lock, in-app update prompt.
