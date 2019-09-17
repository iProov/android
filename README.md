# iProov Android SDK (v5.0.0-beta2)

## 🤖 Introduction

iProov is an SDK providing a programmatic interface for embedding the iProov technology within a 3rd party application.

> **💠 PRE-RELEASE SOFTWARE:** This version is currently in beta and not all features of the SDK are enabled. Please note that the lighting model/face distance logic is disabled in this version.

iProov has been developed as an Android AAR Library distributed through our Maven repository, and it supports Android API Level 21 (Lollipop 5.0) and above. If you require support for Android API Level 16-19, then you will need to use the previous version of this library (4.4.0).

Within this repository you can find the ficticious "Waterloo Bank" sample Android app, which illustrates an example iProov integration.

## ❗ Android Studio Version Compatibility

Due to breaking changes in Gradle 3.x (bundled in Android Studio 3), the iProov SDK requires compilation target, build tools and android compatibility library versions to be 27 or above in the host project.

## ⬆️ Upgrade Guide

Welcome to the next generation of the iProov SDK! v5 is a substantial overhaul to the SDK and added many new features, and as a result SDK v5 is a major update and includes breaking changes!

Please consult the [Upgrade Guide](https://github.com/iProov/android/wiki/Upgrade-Guide) for detailed information about how to upgrade your app, and look out for the ⬆️ symbol in this README.

## ✍️ Registration

You can obtain API credentials by registering on the [iProov Partner Portal](https://www.iproov.net/).

## 📲 Installation

The Android SDK is provided in AAR format (Android Library Project) as a Maven dependency. The installation guide assumes use of Android Studio.

1. Open the build.gradle file corresponding to your new or existing Android Studio project with which you wish to integrate (commonly, this is the build.gradle file for the `app` module).

2. Add the repositories section at the top level of your build file:

```gradle
repositories {
    maven { url 'https://raw.githubusercontent.com/iProov/android/master/maven/' }
}
```

3. Add the dependencies section at the top level of your build file:

```gradle
dependencies {
    implementation('com.iproov.sdk:iproov:5.0.0-beta2@aar') {
        transitive=true
    }
}
```

> **⬆️ UPGRADING NOTICE:** Take note of the new dependencies & versions!

4. **OPTIONAL:** If you wish to include [support for Firebase](#-firebase-support) in your app, add the following dependency:

```gradle
dependencies {
    implementation('com.iproov.sdk:iproov-firebase:5.0.0-beta2@aar') {
        transitive=true
    }
}
``` 

You may now build your project!

## 🚀 Launch iProov

Using iProov couldn't be simpler. Notice the need for a token.

```java
IProov.Options options = new IProov.Options()
    .ui.setLogoImage(R.mipmap.ic_launcher);
    .ui.setBoldFont("Merriweather-Bold.ttf");

IProov.getIProovConnection(activity).launch(
    "https://eu.rp.secure.iproov.me", // This is optional, showing the default if omitted
    options, 
    token, 
    new IProov.IProovCaptureListener() {
        @Override public void onSuccess(String token) {
            // Successfully registered (enrol) or iProoved (verify)
        }
        @Override public void onFailure(@Nullable String reason, @Nullable String feedback) {
            // Failed to registered (enrol) or iProoved (verify)
            // This is usually due to lighting conditions or face movement
        }
        @Override public void onCancelled() {
            // The user hit back or home and cancelled the scan
        }
        @Override public void onProgressUpdate(String message, double progress) {
            // Scanning is in progress, providing a message/prompt/title for the user and a value 0.0 to 1.0 indicating how far through the process we are
        }
        @Override public void onError(IProovException e) {
            // An unrecoverable error occurred e.g. network problems. See IProovException.Reason.
        }
    }
);

```
> **⬆️ UPGRADING NOTICE:** onCanceled() has been renamed onCancelled() and .ui.setAutostartDisabled() has been renamed to .ui.setAutoStartDisabled() in 5.0.0-beta2

---

> **⬆️ UPGRADING NOTICE:** Just a call and a listener, with a series of callbacks.

---

> **⬆️ UPGRADING NOTICE:** In v5 you no longer need to call `IProov.verify()` or `IProov.enrol()`. There were previously many separate methods to launch iProov, these have now been combined into a single method. (Push & URL launched claims are no longer handled within the SDK itself).

---

> **⬆️ UPGRADING NOTICE:** Previously, after launching iProov, the SDK would handle the entire user experience end-to-end, from getting a token all the way through to the streaming UI and would then pass back a pass/fail/error result to your app. In v5, the SDK flashes the screen and then hands back control to your app, whilst the capture is streamed in the background. This means that you can now control the UI to display your own streaming UI, or allow the user to continue with another activity whilst the iProov capture streams in the background.

### Tokens

We provide an API with endpoints that support logging in, enrolment and validation using tokens.

An example of this, and calling the above launch method, is provided in the ["Waterloo Bank" sample app](https://github.com/iProov/android/tree/master/waterloo-bank), which demonstrates both Java and Kotlin for you to compare and contrast.

This also uses the [sample client api code](https://github.com/iProov/android/tree/master/maven/com/iproov/android-api-client) that we provide to assist both in such simple samples and for you to test out your own apps quickly. However, this code is NOT expected to be used in production code. The code requires an apiKey and secret, which should NEVER be distributed inside an app, for security reasons. For a proper implementation, you are expected to secure your apiKey and secret on your own API servers behind suitable end points, where your servers will call our API when called upon by your app.

### 🎯 IProovCaptureListener

#### `void onProgressUpdate(String message, double progress)`

The iProov progress can be monitored from this event. `progress` ranges from `0.0` to `1.0`.

#### `void onSuccess(String token)`

The iProov session has completed and iProov has successfully verified or enrolled the user. The token is provided so it can be used for verification on the API.

> ⚠️ SECURITY WARNING: Never use iProov as a local authentication method. You cannot rely on the fact that a result was received to prove that the user was authenticated successfully (it is possible the iProov process could be manipulated locally by a malicious app). You can treat the verified result as a hint to your app to update the UI, etc. but must always independently validate the token server-side (using the validate API call) before performing any authenticated user actions.

#### `void onFailure(String reason, String feedback)`

The iProov process has completed and iProov has failed to verify or enrol the user. The reason indicates why the authentication could not be confirmed. This could be a generic message, or could provide tips to the user to improve their chance of iProoving successfully (e.g. “lighting too dark”, etc.). There may also be a `feedback` which provides additional info.

| Feedback                              | Reason                                                |
| ------------------------------------- | ----------------------------------------------------- |
| **ambiguous_outcome**                 | Sorry, ambiguous outcome                              |
| **network_problem**                   | Sorry, network problem                                |
| **user_timeout**                      | Sorry, your session has timed out                     |
| **lighting_flash_reflection_too_low** | Ambient light too strong or screen brightness too low |
| **lighting_backlit**                  | Strong light source detected behind you               |
| **lighting_too_dark**                 | Your environment appears too dark                     |
| **lighting_face_too_bright**          | Too much light detected on your face                  |
| **motion_too_much_movement**          | Please keep still                                     |
| **motion_too_much_mouth_movement**    | Please do not talk while iProoving                    |

#### `void onCancelled()`

The iProov process was halted and cancelled by user action.

#### `void onError(IProovException exception)`

The iProov process failed entirely (i.e. iProov was unable to verify or enrol the user due to a system or network issue). This could be for a number of reasons, for example there was an unrecoverable network issue (`IProovException.Reason.NETWORK_ERROR`).
You may wish to display the `localizedMessage` to the user. You can get one of the following reasons using `exception.getReason()`:

```java
public enum Reason {
    GENERAL_ERROR,
    NETWORK_ERROR,
    STREAMING_ERROR,
    USER_PRESSED_BACK, // comes through as onCancelled instead of onError
    UNSUPPORTED_DEVICE,
    CAMERA_PERMISSION_DENIED,
    SSL_EXCEPTION,
    SERVER_ABORT,
    MULTI_WINDOW_MODE_UNSUPPORTED;
 }
```

A description of these errors are as follows:

- **GENERAL_ERROR** - An unknown error has occurred (this should not happen). Let us know if you get this.
- **NETWORK_ERROR** - An network issue prevented the streamer from starting.
- **STREAMING_ERROR** - An error occurred with the video streaming process.
- **USER_PRESSED_BACK** - The user voluntarily pressed the back button to end the claim.
- **UNSUPPORTED_DEVICE** - The device is not supported, (e.g. does not have a front-facing camera).
- **CAMERA_PERMISSION_DENIED** - The user disallowed access to the camera when prompted.
- **SSL_EXCEPTION** - Certificates provided for pinning were corrupted or otherwise unable to be processed.
- **SERVER_ABORT** - The token was invalidated server-side.
- **MULTI_WINDOW_MODE_UNSUPPORTED** - The user attempted to iProov in split-screen/multi-screen mode,which is not supported.

## ⚙ Configuration Options

Various customization options are available to pass as arguments to the IProov intent. To use these, create an instance of `IProov.IProovConfig`, set required parameters, and pass it via `.setIProovConfig` to your `NativeClaim.Builder`. A list of available parameters for customization is below:

```java
IProov.Options options = new IProov.Options()
    .ui.setAutoStartDisabled(true)             // With autostart, instead of requiring a user tap, there is an auto-countdown from 3 when face is detected. Default false
    .ui.setLocale("")                          // When set, overrides the device locale setting for the iProov SDK. Must be a 2-letter ISO 639-1 code: http://www.loc.gov/standards/iso639-2/php/code_list.php. Currently only supports "en" and "nl".
    .ui.setTitle(title)                        // The message shown during canny preview. Default is provided by the system when this value is null.

    .ui.setBackgroundColor(Color.BLACK)        // background colour shown after the flashing stops. Default Color.BLACK
    .ui.setLineColor(Color.BLACK)              // face outline colour
    .ui.setEnableScreenshots(true)             // for added security, screenshotting is disabled during IProoving; re-enable this here. Default false

    // You can also set the colour of the oval (which also sets the accompanying text color):
    .ui.setLoadingTintColor(Color.RED)         // The app is connecting to the server. Default: grey (#5c5c5c)
    .ui.setNotReadyTintColor(Color.BLUE)       // Cannot start iProoving until the user takes action (e.g. move closer, etc). Default: orange (#f5a623)
    .ui.setReadyTintColor(Color.GREEN)         // Ready to start iProoving. Default: green (#01bf46)
    
    //fonts are identified by filename and must be included in the "assets" directory of the host project
    .ui.setRegularFont("SomeFont.ttf")         // change the default font used within the SDK 
    .ui.setBoldFont("SomeFont-Bold.ttf")       // boldFont is used for feedback messages and the countdown timer

    .ui.setLogo(resourceId)                    // logo to be included in the title - defaults to iProov logo
    .ui.setNotificationImage()                 // foreground service notification image
    .ui.setNotificationTitle()                 // foreground service notification title

    .ui.setScanLineDisabled(true)              // to allow removal the scan line graphic. Default false
    .ui.setFilter(filter)                      // to change the way the canny shader appears: enum Filter (CLASSIC, SHADED, VIBRANT)

    .capture.setMaxPitchAngle(0.25)            // Pose control - max face pitch angle allowed - fraction of 180 degrees off normal e.g. 0.25 is +/-45 degrees
    .capture.setMaxYawAngle(0.25)              // Pose control - max face yaw angle allowed - fraction of 180 degrees off normal e.g. 0.25 is +/-45 degrees
    .capture.setMaxRollAngle(0.25)             // Pose control - max face roll angle allowed - fraction of 180 degrees off normal e.g. 0.25 is +/-45 degrees

    .network.setCertificates(new int[]{R.raw.custom})  //optionally supply an array of paths of certificates to be used for pinning. Useful when using your own baseURL or for overriding the built-in certificate pinning for some other reason.
    //certificates should be generated in DER-encoded X.509 certificate format, eg. with the command $ openssl x509 -in cert.crt -outform der -out cert.der
    .network.setDisableCertificatePinning(false);   // when true (not recommended), disables certificate pinning to the server. Default false
```
> **⬆️ UPGRADING NOTICE:** Take note of the many changes here!

## 🔥 Firebase support

By default, the SDK leverages the [Android built-in face detector](https://developer.android.com/reference/android/media/FaceDetector). This is a simple face detector and is ubiquitous in Android phones, however it is not regularly updated.

Google now direct their efforts into maintaining the [Firebase face detector, part of ML Kit](https://firebase.google.com/docs/ml-kit/detect-faces). The advantage of the Firebase face detector is that it provides more advanced features such as facial landmarks, which allows us to offer detection of the user's pose.

You can therefore opt-into the Firebase functionality by adding the `iproov-firebase` module to your build.gradle (see the Installation instructions).

Please note that adding Firebase support will increase your app size (as it will include the Firebase dependencies) and will also result in poorer performance on low-end devices, since Firebase is more computationally intensive.

## 🌎 String localization & customization

The SDK ships with localized strings for the following locales:

- English
- Norwegian Bokmål
- Norwegian Nynosk
- Dutch
- Turkish

If the user's device language is set to one of the above, the SDK will be localized accordingingly unless `options.ui.localeOverride` is set, in which case this setting will override the default locale.

It is also possible to manually customize any of the strings in the app, regardless of locale.

> **💠 PRE-RELEASE SOFTWARE:** More information on string customization coming soon.

## ❓Help & support

For further help with integrating the SDK, please contact [support@iproov.com](mailto:support@iproov.com).