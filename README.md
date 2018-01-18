# iProov Android SDK (v4.1.1)

## 🤖 Introduction

The iProov Android SDK provides a programmatic interface for embedding the iProov technology within a 3rd party Android application (“host app”).

The iProov SDK supports Android API Level 16 (Android 4.1) and above, which as of May 2017 encompasses ~98% of active Android devices.

Within this repository you can find the Waterloo Bank sample Android app, which illustrates an example iProov integration.

## ❗ Android Studio Version Compatibility

Due to breaking changes in Gradle 3.x (bundled in Android Studio 3), version 4.1+ of the iProov SDK requires compilation target, build tools and android compatibility library versions to be 27 or above in the host project. If you require Android Studio 2.x, Gradle 2.x, and/or build tools version 25, instead **please use version 4.0.x**, the latest version of which will until further notice retain feature and support parity with version 4.1.x.


## 🛠 Upgrade Guide

The upgrade guide has been moved to the [Wiki](https://github.com/iProov/android/wiki). If you are updating from SDK version 3.x or 2.x, please see the relevant section for further info.

## 📲 Installation

The Android SDK is provided in AAR format (Android Library Project) as a Maven dependency. The installation guide assumes use of Android Studio.

1. Open the build.gradle file corresponding to your new or existing Android Studio project with which you wish to integrate (commonly, this is the build.gradle file for the `app` module).

2. Add the repositories section at the top level of your build file:

```gradle
repositories {
    maven { url 'https://raw.githubusercontent.com/iProov/android/master/maven/' }
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}
```

3. Add the dependencies section at the top level of your build file:

```gradle
dependencies {
    compile('com.iproov.sdk:iproov:4.1.1@aar') {
        transitive=true
    }
}
```

*Or, for Android Studio 2.x:*

```gradle
dependencies {
    compile('com.iproov.sdk:iproov:4.0.3@aar') {
        transitive=true
    }
}
```

You may now build your project!

## 🚀 Launch Modes

There are 2 primary ways iProov can be launched for verification or enrollment:

* By being called natively from within a host application.

* From a GCM (push) notification.

iProov is always launched via an Intent from your application.

When starting a new iProov session, the starting point is always to create a new Intent using one of the static Intent-creating methods, as shown below.

### 1. Verify (with Service Provider)

You would use this launch mode where you are a service provider who knows the username you want to authenticate against, but nothing else. iProov will handle the entire end-to-end process of generating a new token and authenticating the user.

```java
Intent i = new IProov.NativeClaim.Builder(this)
    .setMode(IProov.Mode.Verify)
    .setServiceProvider("{{api-key}}")
    .setUsername("{{user-id}}")
    .getIntent();
startActivityForResult(i, 0);
```

For an explanation of receiving the result from the Intent, see the “Intent Result” section below.

### 2. Verify (with Token)

You would use this launch mode where you already have a token for the user you wish to authenticate (you have already generated this by calling the REST API from your server and now wish to authenticate the user).

```java
Intent i = new IProov.NativeClaim.Builder(this)
    .setMode(IProov.Mode.Verify)
    .setServiceProvider("{{api-key}}")
    .setEncryptedToken("{{token}}")
    .getIntent();
startActivityForResult(i, 0);
```

### 3. Enrol (with Service Provider)

You would launch this mode where you are a service provider who wishes to enrol a new user, with a given user ID.

```java
Intent i = new IProov.NativeClaim.Builder(this)
    .setMode(IProov.Mode.Enrol)
    .setServiceProvider("{{api-key}}")
    .setUsername("{{user-id}")
    .getIntent();
startActivityForResult(i, 0);
```

### 4. Enrol (with Token)

You would launch this mode where you are a service provider who wishes to enrol a new user, where you already have the encrypted token for the user you wish to enrol (you have already generated this by calling the REST API from your server and now wish to authenticate the user).

```java
Intent i = new IProov.NativeClaim.Builder(this)
    .setMode(IProov.Mode.Enrol)
    .setServiceProvider("{{api-key}}")
    .setEncryptedToken("{{token}}")
    .getIntent();
startActivityForResult(i, 0);
```

### 5. iProov with Notification

When the notification is received, you can use a `NotificationClaim.Builder` to attach the `Bundle` from the notification:

```java
Intent i = new IProov.NativeClaim.Builder(this)
    .setBundle(bundle)
    .getIntent();
```

In most cases rather than launch the `Intent` directly, you would then most likely wrap it into a `PendingIntent` and attach it to a Notification to be displayed to the user via the `NotificationManager`.

When launching the `Intent` from a `PendingIntent` (as opposed to `startActivityForResult()`), the iProov session is launched as a standalone session. When the iProov session completes, your application cannot handle the result.

Providing a full tutorial on integrating push with your app is beyond the scope of this documentation. Please see Google's [Cloud Messaging Documentation](https://developers.google.com/cloud-messaging/) for further information.

## 🎯 Intent Result

When launching iProov from an Intent, your application will in most cases (aside from GCM notifications) wish to handle the result.

In order to do so, make sure you always launch your Intent with `startActivityForResult()`. When the iProov session is complete, `onActivityResult()` will then be called in the Activity which launched the Intent.

The `resultCode` parameter will be one of 3 values:

#### `IProov.RESULT_SUCCESS`

The iProov session has completed and iProov has successfully verified or enrolled the user. You can obtain the token for this user from the returned Intent with:

```java
String token = data.getStringExtra(IProov.EXTRA_ENCRYPTED_TOKEN)
```

> SECURITY WARNING: Never use iProov as a local authentication method. You cannot rely on the fact that a result was received to prove that the user was authenticated successfully (it is possible the iProov process could be manipulated locally by a malicious app). You can treat the verified result as a hint to your app to update the UI, etc. but must always independently validate the token server-side (using the validate API call) before performing any authenticated user actions.

#### `IProov.RESULT_FAILURE`

The iProov process has completed and iProov has failed to verify or enrol the user. The result Intent provides a reason that the authentication could not be confirmed. This could be a generic message, or could provide tips to the user to improve their chance of iProoving successfully (e.g. “lighting too dark”, etc.). There may also be an `EXTRA_FEEDBACK` which provides additional info.

```java
String reason = data.getStringExtra(IProov.EXTRA_REASON)
String feedback = data.getStringExtra(IProov.EXTRA_FEEDBACK)
```

`EXTRA_FEEDBACK` are fixed codes and could be one of:
* `ambiguous_outcome`
* `network_problem`
* `user_timeout`

#### `IProov.RESULT_ERROR`

The iProov process failed entirely (i.e. iProov was unable to verify or enrol the user due to a system or network issue). This could be for a number of reasons, for example the user cancelled the iProov process, or there was an unrecoverable network issue.
You can obtain an Exception relating to the cause of the failure as follows:

```java
Exception e = (Exception) data.getSerializableExtra(IProov.EXTRA_EXCEPTION);
```

You may wish to display the `localizedMessage` to the user. You can get one of the following reasons using `e.getReason()` when `e` is an instance of `IProovException`:

```java
public enum Reason {
    GENERAL_ERROR,
    NETWORK_ERROR,
    STREAMING_ERROR,
    UNKNOWN_IDENTITY,
    ALREADY_ENROLLED,
    USER_PRESSED_BACK,
    USER_PRESSED_HOME,
    UNSUPPORTED_DEVICE,
    CAMERA_PERMISSION_DENIED,
    GOOGLE_PLAY_SERVICES_MISSING;
 }
```

A description of these errors are as follows:

- **GENERAL_ERROR** - An unknown error has occurred (this should not happen). Let us know if you get this.
- **NETWORK_ERROR** - An issue occurred with the API (e.g. timeout, disconnection, etc.).
- **STREAMING_ERROR** - An error occurred with the video streaming process.
- **UNKNOWN_IDENTITY** - Some Service Providers will reject user IDs that have not enrolled.
- **ALREADY_ENROLLED** - During enrolment, a user with this user ID has already enrolled.
- **USER_PRESSED_BACK** - The user voluntarily pressed the back button to end the claim.
- **USER_PRESSED_HOME** - The user voluntarily sent the app to the background.
- **UNSUPPORTED_DEVICE** - The device is not supported, (e.g. does not have a front-facing camera).
- **CAMERA_PERMISSION_DENIED** - The user disallowed access to the camera when prompted.
- **GOOGLE_PLAY_SERVICES_MISSING** - This should never happen when downloading an iProov-embedded app from the Google Play store, but you may encounter it during testing. To resolve, visit the Play store and download any available updates.

## ⚙ Configuration Options

Various customization options are available to pass as arguments to the IProov intent. To use these, create an instance of `IProov.IProovConfig`, set required parameters, and pass it via `.setIProovConfig` to your `NativeClaim.Builder`. A list of available parameters for customization is below:

```java
IProov.IProovConfig config = new IProov.IProovConfig()
    .setBackgroundTint(Color.BLACK)         //background colour shown after the flashing stops. Default Color.BLACK
    .setShowIndeterminateSpinner(true)      //when true, shows an indeterminate upload progress instead of a progress bar. Default false
    .setSpinnerTint(Color.WHITE)            //only has an effect when setShowIndeterminateSpinner is true. Default Color.WHITE
    .setTextTint(Color.WHITE)               //only has an effect when setShowIndeterminateSpinner is true. Default Color.WHITE
    .setAutostart(true)                     //instead of requiring a user tap, auto-countdown from 3 when face is detected. Default false
    .setPrivacyPolicyDisabled(true)         //disables the privacy policy. Default false
    .setInstructionsDialogDisabled(true)    //disables the instructions dialog. Default false
    .setMessageDisabled(true)               //disables the message shown during canny preview. Default false
    .setLocaleOverride("")                  //overrides the device locale setting for the iProov SDK. Must be a 2-letter ISO 639-1 code: http://www.loc.gov/standards/iso639-2/php/code_list.php. Currently only supports "en" and "nl".
    .setEnableScreenshots(true)             //for added security, screenshotting is disabled during IProoving; re-enable this here. Default false

    //change the colour of the edge and background for the starting face visualisation, for normal light and low light conditions
    //NB: for low light colour scheme, please use a background colour sufficiently bright to allow the face to be illuminated for face detection purposes.
    .setStartingBackgroundColor(Color.WHITE)
    .setStartingEdgeColor(Color.BLACK)
    .setLowLightBackgroundColor(Color.WHITE)
    .setLowLightEdgeColor(Color.BLACK)

    .setBaseURL("https://eu.rp.secure.iproov.me") //change the server base URL. This is an advanced setting - please contact us if you wish to use your own base URL (eg. for proxying requests)
    .setCertificateFiles(new int[]{R.raw.custom})  //optionally supply an array of paths of certificates to be used for pinning. Useful when using your own baseURL or for overriding the built-in certificate pinning for some other reason.
    //certificates should be generated in DER-encoded X.509 certificate format, eg. with the command $ openssl x509 -in cert.crt -outform der -out cert.der
    .setPinningDisabled(false);                    //when true (not recommended), disables certificate pinning to the server. Default false

Intent i = new IProov.NativeClaim.Builder(this)
    .setMode(IProov.Mode.Verify)
    .setServiceProvider("{{api-key}}")
    .setUsername("{{user-id}}")
    .setIProovConfig(config)
    .getIntent();
startActivityForResult(i, 0);
```
