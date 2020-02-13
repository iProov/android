# iProov Android SDK (v5.0.0-beta6)

## 🤳 Introduction

The iProov Android SDK enables you to integrate iProov into your Android app. We also have an [iOS SDK](https://github.com/iproov/ios) and [HTML5 client](https://github.com/iProov/html5).

### Requirements

- Android Studio
- API Level 19 (4.4 KitKat) and above
- Compilation target, build tools and Android compatibility libraries must be 27+

Within this repository you can find the fictitious "Waterloo Bank" sample Android app, which illustrates an example iProov integration.

## 📖 Contents

The framework package is provided via this repository, which contains the following:

* **README.md** - This document
* **maven** - Maven repository for the SDK
* **waterloo-bank** - A sample project of iProov for the fictitious _Waterloo Bank_.
* **resources** - Directory containing additional development resources you may find helpful.


## ⬆️ Upgrading from earlier versions

Welcome to the next generation of the iProov SDK! v5 is a substantial overhaul to the SDK and added many new features, and as a result SDK v5 is a major update and includes breaking changes, so please read this document carefully.

Please consult the [Upgrade Guide](https://github.com/iProov/android/wiki/Upgrade-Guide) for detailed information about how to upgrade your app, and look out for the ⬆️ symbol in this README.

If you are upgrading from earlier betas of SDK v5, please read the integration documentation carefully as the APIs have changed significantly.

## ✍️ Registration

You can obtain API credentials by registering on the [iProov Partner Portal](https://www.iproov.net/).

## 📲 Installation

The Android SDK is provided in AAR format (Android Library Project) as a Maven dependency.

1. Open the build.gradle file corresponding to your new or existing Android Studio project with which you wish to integrate (commonly, this is the build.gradle file for the `app` module).

2. Add the repositories section to your build.gradle file:

	```groovy
	repositories {
	    maven { url 'https://raw.githubusercontent.com/iProov/android/nextgen/maven/' }
	}
	```

3. Add the dependencies section to your app build.gradle file:

	```groovy
	dependencies {
	    implementation('com.iproov.sdk:iproov:5.0.0-beta6@aar') {
	        transitive=true
	    }
	}
	```

	> **⬆️ UPGRADING NOTICE:** Take note of the new dependencies & versions!

4. Add support for Java 8 to your app build.gradle file (you can skip this step if you already have Java 8 enabled):

	```groovy
	android {
	    compileOptions {
	        sourceCompatibility JavaVersion.VERSION_1_8
	        targetCompatibility JavaVersion.VERSION_1_8
	    }
	}
	```

5. **OPTIONAL:** If you wish to include [support for Firebase](#-firebase-support) in your app, add the following dependency:

	```groovy
	dependencies {
	    implementation('com.iproov.sdk:iproov-firebase:5.0.0-beta6@aar') {
	        transitive=true
	    }
	}
	``` 

You may now build your project!

## 🚀 Get started

> **⬆️ UPGRADING NOTICE:** Take notice! The API has been changed significantly in v5.0.0-beta6 compared with earlier betas.

Before being able to launch iProov, you need to get a token to iProov against. There are 3 different token types:

* A **verify** token - for logging in an existing user
* An **enrol** token - for registering a new user
* An **ID match** token - for matching a user against a scanned ID document image.

In a production app, you normally would want to obtain the token via a server-to-server back-end call. For the purposes of on-device demos/testing, we provide Kotlin/Java sample code for obtaining tokens via [iProov API v2](https://eu.rp.secure.iproov.me/docs.html) with our open-source [Android API Client](https://github.com/iProov/android-api-client).

Once you have obtained the token, you can simply call `IProov.launch()`:

##### Kotlin

```kotlin
val options = IProov.Options()
// ...customise any iProov options...

IProov.launch(this, // Reference to current activity
	"https://eu.rp.secure.iproov.me", // Streaming URL (optional)
	options, // Optional
	"{{ token }}", // iProov token
	object : IProov.Listener {
	
	    override fun onProcessing(progress: Double, message: String) {
			// The SDK will update your app with the progress of streaming to the server and authenticating
			// the user. This will be called multiple time as the progress updates.
	    }
	
	    override fun onSuccess(token: String) {
		    // The user was successfully verified/enrolled and the token has been validated.
		    // The token passed back will be the same as the one passed in to the original call.
	    }
	    
	    override fun onFailure(reason: String?, feedback: String?) {
			// The user was not successfully verified/enrolled, as their identity could not be verified,
			// or there was another issue with their verification/enrollment. A reason (as a string)
			// is provided as to why the claim failed, along with a feedback code from the back-end.
	    }
	    
	    override fun onCancelled() {
	   		// The user cancelled iProov, either by pressing the close button at the top right, or pressing
	   		// the home button.
	    }

	    override fun onError(e: IProovException) {
	    	// The user was not successfully verified/enrolled due to an error (e.g. lost internet connection)
	    	// You can obtain the reason from the reason property.
	    	// It will be called once, or never.
		
	    }
	    
})
```

##### Java

```java
IProov.Options options = new IProov.Options();
// ...customise any iProov options...
    
IProov.launch(this, // Reference to current activity
    "https://eu.rp.secure.iproov.me", // Streaming URL (optional)
    options, // Optional
    "{{ token }}", // iProov token
    new IProov.Listener() {
    
        @Override
        public void onProcessing(double progress, String message) {
			// The SDK will update your app with the progress of streaming to the server and authenticating
			// the user. This will be called multiple time as the progress updates.
        }
    
        @Override
        public void onSuccess(String token) {
		 	// The user was successfully verified/enrolled and the token has been validated.
		 	// The token passed back will be the same as the one passed in to the original call.
        }
        
        @Override
        public void onFailure(@Nullable String reason, @Nullable String feedbackCode) {
			// The user was not successfully verified/enrolled, as their identity could not be verified,
			// or there was another issue with their verification/enrollment. A reason (as a string)
			// is provided as to why the claim failed, along with a feedback code from the back-end.
        }
        
        @Override
        public void onCancelled() {
	   		// The user cancelled iProov, either by pressing the close button at the top right, or pressing
	   		// the home button.
        }
                
        @Override
        public void onError(IProovException e) {
        	// The user was not successfully verified/enrolled due to an error (e.g. lost internet connection)
	    	// You can obtain the reason by calling getReason().
			// It will be called once, or never.
        }
        
    }
);

```

By default, iProov will stream to our EU back-end platform. If you wish to stream to a different back-end, you can pass a `streamingURL` as the first parameter to `IProov.launch()` with the base URL of the back-end to stream to.

> **⚠️ SECURITY NOTICE:** You should never use iProov as a local authentication method. You cannot rely on the fact that the success result was returned to prove that the user was authenticated or enrolled successfully (it is possible the iProov process could be manipulated locally by a malicious user). You can treat the success callback as a hint to your app to update the UI, etc. but you must always independently validate the token server-side (using the validate API call) before performing any authenticated user actions.

---

> **⬆️ UPGRADING NOTICE:** In v5 you no longer need to call `IProov.verify()` or `IProov.enrol()`. There were previously many separate methods to launch iProov, these have now been combined into a single method. (Push & URL launched claims are no longer handled within the SDK itself).

---

> **⬆️ UPGRADING NOTICE:** Previously, after launching iProov, the SDK would handle the entire user experience end-to-end, from getting a token all the way through to the streaming UI and would then pass back a pass/fail/error result to your app. In v5, the SDK flashes the screen and then hands back control to your app, whilst the capture is streamed in the background. This means that you can now control the UI to display your own streaming UI, or allow the user to continue with another activity whilst the iProov capture streams in the background.

## ⚙ Options

Various customization options are available to pass as arguments to the IProov intent. To use these, create an instance of `IProov.Options` and set the parameters of your choice. A list of available parameters for customization is below:

```kotlin
val options = IProov.Options()

/*
    options.ui
    Configure options relating to the user interface
*/

options.ui.autoStartDisabled = true; // With autostart, instead of requiring a user tap, there is an auto-countdown a face is detected. Default false.
options.ui.title = "Authenticating to ACME Bank" // The message shown during canny preview. Default null.

// Adjust various colors for the camera preview:
options.ui.backgroundColor = Color.BLACK
options.ui.lineColor = Color.CYAN
options.ui.loadingTintColor = Color.RED
options.ui.notReadyTintColor = Color.BLUE
options.ui.readyTintColor = Color.GREEN

options.ui.enableScreenshots = true // For added security, screenshotting is disabled during IProoving; re-enable this here. Default: false.
options.ui.fontAsset = "SomeFont.ttf" // Set the default font from assets directory.
options.ui.fontResource = R.font.some_font // Set the default font from font resources.
options.ui.logoImage = resourceId // Logo to be included in the title. Defaults to iProov logo.
options.ui.scanLineDisabled = true // Disable the scan-line whilst scanning the face. Default: false.
options.ui.filter = filter // Adjust the filter used for the face preview this can be CLASSIC (as in pre-v5), SHADED or VIBRANT. Default: SHADED.
options.ui.orientation = orientation // Set the orientation of the iProov activity: enum Orientation (PORTRAIT, REVERSE_PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE). Note that this rotates the UI and does not rotate the camera; this is because it is intended to support USB cameras on a LANDSCAPE display, where the camera is oriented normally.

/*
    options.network
    Configure options relating to networking & security
*/

options.network.disableCertificatePinning = false // When true (not recommended), disables certificate pinning to the server. Default false
options.network.certificates = arrayOf(R.raw.iproov__certificate) // Optionally supply an list of resourceIDs of certificates files to be used for pinning. Useful when using your own baseURL or for overriding the built-in certificate pinning for some other reason. Certificates should be generated in DER-encoded X.509 certificate format, eg. with the command $ openssl x509 -in cert.crt -outform der -out cert.der
options.network.timeoutSecs = duration // The streaming timeout in seconds - setting to 0 disables timeout (default 10)
options.network.path = path // The path to use when streaming, defaults to "/socket.io/v2/". You should not need to change this unless directed to do so by iProov.

/*
    options.capture
    Configure options relating to the capture functionality
*/

// Allow for an alternate camera to be used. Provide an ordered set of the following to use in priority order when IProov searches for a camera to use: FRONT, BACK, EXTERNAL. When empty, default of FRONT is used except for a small set of known devices. In cases of USB cameras, {CameraLensFacing.EXTERNAL, CameraLensFacing.BACK} (Java) or arrayOf(CameraLensFacing.EXTERNAL, CameraLensFacing.BACK) (Kotlin) might be used since such cameras can be registered either way - hence this is an array and not a single value.
options.capture.cameraLensFacing = arrayOf(CameraLensFacing.EXTERNAL)

// You can specify max yaw/roll/pitch deviation of the user's face to ensure a given pose. Values are provided in normalised units.
// These options should not be set for general use. Please contact iProov for further information if you would like to use this feature.
options.capture.maxPitch = 0.25
options.capture.maxYaw = 0.25
options.capture.maxRoll = 0.25
```
> **⬆️ UPGRADING NOTICE:** Take note of the many changes here!
 
## 🌎 String localization & customization

The SDK only ships with English language strings. You are free to localise/customise these strings in your app, if you choose to do so.

All iProov strings are prefixed with `iproov__` and can be overriden by your app's strings.xml file. A copy of the iProov strings.xml file can be found in the resources directory.

Strings for failure reasons are handled in a special way, in the form `R.string.iproov__failure_<feedback code>` e.g. `iproov__failure_ambiguous_outcome` exist and will be used for `reason`, allowing it to provide localised translations for all current and future failure codes.

## 💥 Handling failures & errors

### Failures

Failures occur when the user's identity could not be verified for some reason. A failure means that the capture was successfully received and processed by the server, which returned a result. Crucially, this differs from an error, where the capture itself failed due to a system failure.

| `feedbackCode` | `reason` |
|-----------------------------------|---------------------------------------------------------------|
| `ambiguous_outcome` | Sorry, ambiguous outcome |
| `network_problem` | Sorry, network problem |
| `motion_too_much_movement` | Please do not move while iProoving |
| `lighting_flash_reflection_too_low` | Ambient light too strong or screen brightness too low |
| `lighting_backlit` | Strong light source detected behind you |
| `lighting_too_dark` | Your environment appears too dark |
| `lighting_face_too_bright` | Too much light detected on your face |
| `motion_too_much_mouth_movement` | Please do not talk while iProoving |
| `user_timeout` | Sorry, your session has timed out |

The list of feedback codes and reasons is subject to change.

### Errors

The iProov process failed entirely (i.e. iProov was unable to verify or enrol the user due to a system failure of some kind). This could be for a number of reasons, for example there was an unrecoverable streaming issue.

You may wish to display the `localizedMessage` to the user. You can get one of the following reasons using `exception.getReason()`:

```java
    public enum Reason {
        ENCODER_ERROR,
        STREAMING_ERROR,
        UNSUPPORTED_DEVICE,
        CAMERA_PERMISSION_DENIED,
        SERVER_ERROR,
        MULTI_WINDOW_MODE_UNSUPPORTED,
        CAMERA_ERROR,
        LIGHTING_MODEL_ERROR
     }
```

A description of these errors are as follows:

- `ENCODER_ERROR` An error occurred with the video encoding process.
- `STREAMING_ERROR` An error occurred with the video streaming process.
- `UNSUPPORTED_DEVICE` The device is not supported, (e.g. does not have a front-facing camera).
- `CAMERA_PERMISSION_DENIED` The user disallowed access to the camera when prompted.
- `SERVER_ERROR` The token was invalidated server-side, or some other error occurred.
- `MULTI_WINDOW_MODE_UNSUPPORTED` The user attempted to iProov in split-screen/multi-screen mode,which is not supported.
- `CAMERA_ERROR` An error occurred acquiring or using the camera. This could happen when a non-phone is used with/without an external/USB camera. See Options.capture.setCameraLensFacing().
- `LIGHTING_MODEL_ERROR` An error occurred with the lighting model.

## 🐞 Known issues

- Note that you may experience performance drop when running with the Android debugger attached. It is advised not to attempt iProoving whilst using the debugger.

## 🔥 Firebase support

By default, the SDK leverages the [Android built-in face detector](https://developer.android.com/reference/android/media/FaceDetector). This is a simple face detector and is ubiquitous in Android phones, however it is not regularly updated.

Google now direct their efforts into maintaining the [Firebase face detector, part of ML Kit](https://firebase.google.com/docs/ml-kit/detect-faces). The advantage of the Firebase face detector is that it provides more advanced features such as facial landmarks, which allows us to offer detection of the user's pose.

You can therefore opt-into the Firebase functionality by adding the `iproov-firebase` module to your build.gradle (see the Installation instructions).

Please note that adding Firebase support will increase your app size (as it will include the Firebase dependencies) and will also result in poorer performance on low-end devices, since Firebase is more computationally intensive.

## ❓Help & support

For further help with integrating the SDK, please contact [support@iproov.com](mailto:support@iproov.com).