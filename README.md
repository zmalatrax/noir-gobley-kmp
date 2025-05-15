This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Notes

The Fibonacci Noir circuit is hardcoded inside the common Kotlin code, and passed as an argument to the Rust bindings. Usually (preferably), the circuit should be stored in resources as a JSON, and give the path to the Rust bindings (or read it natively beforehand and pass the content to the Rust bindings).

I've followed [this tutorial](https://gobley.dev/docs/tutorial/) to setup Gobley and create the Rust library suitable for [UniFFI](https://github.com/mozilla/uniffi-rs).
[Gobley docs](https://gobley.dev/docs/).

The Rust lib to generate and verify Noir proof is inspired by [MoPro](https://github.com/zkmopro/mopro/tree/main), using [noir-rs](https://github.com/zkmopro/noir-rs/tree/main) (which is pretty similar to [noir_rs](https://github.com/zkpassport/noir_rs/tree/main) by ZkPassport).

### Setup

If you want to run this project, make sure to have the NDK installed (in Android Studio, go to Tools > SDK Manager > SDK Tools, and enable "NKD (side by side)").

The business logic and the UI are shared in commonMain, written in Kotlin, with Jetpack Compose.

While all the targets were working when doing the tutorial, when integrating the noir-rs library,
the compilation got all messed up. So currently, **only** the Android build targetting ARM64-V8A is working, I've removed the iOS target from `build.gradle.kts` and restrained the Android ABIs to arm64-V8a.

### Issues encountered

- If you have issues saying that "barretenberg" is not available/found", clean your build with `./gradlew clean` at the root of the project.
- Once the Android target is built/assembled, the app can run, but when clicking on the "Generate Noir Proof button" it will crash. This is because the `libc++_shared.so` shared library is missing when the target is built ; I haven't found how to fix it yet so a quickfix is going to your Android NDK (on MacOS the default path is `/Users/<user>/Library/Android/sdk/ndk/<ndk_version>/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/lib/libc++_shared.so`): copy-paste it in `MyFirstGobleyProject/composeApp/build/intermediates/rust/aarch64-linux-android/debug/arm64-v8a/` and re-assemble the APK, now it should work.
