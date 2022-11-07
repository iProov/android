![iProov: Flexible authentication for identity assurance](images/banner.jpg)

# iProov Android Biometrics SDK v8.0.0-beta

> **Note**: This is a beta version of the SDK, which means that there may be missing or broken functionality, or features which may vary between this beta and the production release. This version of the SDK is for testing, feedback and evaluation purposes only and should not be deployed to production without prior express approval from iProov.

## Contents of this Package

The framework package is provided via this GitHub repository, which contains:

* **README.md** - this document
* **maven** - a Maven repository for the Biometrics SDK
* **example-app** - a sample iProov project to demonstrate the integration
* **resources** - a directory containing additional development resources

## Introduction

This guide describes how to integrate iProov biometric assurance technologies into your Android app.

iProov offers Genuine Presence Assurance™ technology and Liveness Assurance™ technology:

* [**Genuine Presence Assurance**](https://www.iproov.com/iproov-system/technology/genuine-presence-assurance) verifies that an online remote user is the right person, a real person and that they are authenticating right now, for purposes of access control and security.
* [**Liveness Assurance**](https://www.iproov.com/iproov-system/technology/liveness-assurance) verifies a remote online user is the right person and a real person for access control and security.

Find out more about how to use iProov in your user journeys in the [Implementation Guide](https://docs.iproov.com/docs/Content/ImplementationGuide/implementation-intro.htm).

iProov also supports [iOS](https://github.com/iproov/ios), [Xamarin](https://github.com/iproov/xamarin), [Flutter](https://github.com/iproov/flutter), [React Native](https://github.com/iproov/react-native), and [Web](https://github.com/iProov/web).

## Requirements

- Android Studio
- `minSdkVersion` API Level 21 (Android 5 Lollipop) and above
- Compilation target, build tools, and Android compatibility libraries must be API level 31 or above
- AndroidX

## Upgrading from Earlier Versions

See the [Upgrade Guide](https://github.com/iProov/android/wiki/Upgrade-Guide) for information about upgrading from earlier versions of the SDK.

## Obtain API Credentials

Obtain your API credentials by registering on [iPortal](https://portal.iproov.com/).

## Install the SDK

The Android SDK is provided in Android Library Project (AAR) format as a Maven dependency.

1. Open the `build.gradle` file corresponding to the new, or existing, Android Studio project that you want to integrate. Typically, this is the `build.gradle` file for the `app` module.

2. Add maven to the `repositories` section in your `build.gradle` file:

    ```groovy
    repositories {
        maven { url 'https://raw.githubusercontent.com/iProov/android/master/maven/' }
    }
    ```

3. Add the SDK version to the `dependencies` section in your `build.gradle` file:

    ```groovy
    dependencies {
        implementation('com.iproov.sdk:iproov:8.0.0-beta')
    }
    ```

4. Add support for Java 8 to your `build.gradle` file. Skip this step if Java 8 is enabled:

    ```groovy
    android {
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
    }
    ```

5. Build your project

## Get Started

To enrol (register) or verify (login) a user, follow the steps below.

### Get a Token

Obtain these tokens:

- A **verify** token for logging in an existing user
- An **enrol** token for registering a new user

See the [REST API documentation](https://secure.iproov.me/docs.html) for details about how to generate tokens.

> **TIP:** In a production app you typically obtain tokens via a server-to-server back-end call. For demos and testing, iProov provides Kotlin and Java sample code for obtaining tokens via [iProov API v2](https://eu.rp.secure.iproov.me/docs.html) with our open-source [Android API Client](https://github.com/iProov/android-api-client).

### Choose an Architecture

There are two ways to launch the SDK:

[IProovCallbackLauncher](#iproovcallbacklauncher) is most suitable for:

- Developers already familiar with previous versions of the iProov SDK who are looking for a simple upgrade path.
- Java-only apps.
- Kotlin apps which do not make use of [Flows](https://kotlinlang.org/docs/flow.html).

[IProovFlowLauncher](#iproovflowlauncher) is most suitable for:

- Kotlin apps.
- Developers interested in the most modern API integration and who are familiar with [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flows](https://kotlinlang.org/docs/flow.html).

Both of these methods are demonstrated in the included [Example App](https://github.com/iProov/android/tree/master/example-app).

### IProovCallbackLauncher

#### 1. Create an instance of IProovCallbackLauncher

```kotlin
val iProovCallbackLauncher: IProovCallbackLauncher = IProovCallbackLauncher()
```

#### 2. Register a Listener

To monitor the progress of an iProov claim and receive the result, register an `IProovCallbackLauncher.Listener` class by assigning it to `iProovCallbackLauncher.listener = myListener`.

When the listener is no longer required, unregister it by unassigning this way: `iProovCallbackLauncher.listener = null`.

Complete the functions to handle events coming out of the claim.

```kotlin
class MainActivityCallback : AppCompatActivity() {
    private val iProovCallbackLauncher = IProovCallbackLauncher()
    private val listener = object : IProovCallbackLauncher.Listener {
        override fun onConnecting() {
            // Called when the SDK is connecting to the server. You could provide an indeterminate
            // progress indication to let the user know that the connection is being established.
        }

        override fun onConnected() {
            // The SDK has connected and the iProov user interface is now displayed. You
            // could hide any progress indicator at this point.
        }

        override fun onProcessing(progress: Double, message: String) {
            // The SDK updates your app with the streaming progress to the server and the user authentication.
            // Called multiple time as the progress updates. You could update a determinate progress indicator.
        }

        override fun onSuccess(result: IProov.SuccessResult) {
            // The user was successfully verified/enrolled.
            // You must always independently validate the token server-side (using the /validate API call) before performing any authenticated user actions.
        }

        override fun onFailure(result: IProov.FailureResult) {
            val reason: FailureReason = result.reason
            // The user was not successfully verified/enrolled as their identity could not be verified.
            // Or there was another issue with their verification/enrollment.
            // You might provide feedback to the user as to how to retry.
        }

        override fun onCancelled(canceller: IProov.Canceller) {
            // Either the user cancelled iProov by pressing the Close button at the top right or
            // the Home button (canceller == USER)
            // Or the app cancelled using Session.cancel() (canceller = APP).
            // You should use this to determine the next step in your flow.
        }

        override fun onError(e: IProovException) {
            // The user was not successfully verified/enrolled due to an error, for example, a lost internet connection.
            // You can obtain the reason from the reason property. It will be called once or not at all.
            // You should establish an actionable based on the kind of error preventing claim completion.
        }
    }

    // Overrides ----

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iProovCallbackLauncher.listener = listener
    }

    override fun onDestroy() {
        iProovCallbackLauncher.listener = null
        super.onDestroy()
    }
}
```

> ##### Notes:
>
> * You should register the listener in `Activity.onCreate()`.
> * You can register only one listener.
> * Registering the same listener more than once has no effect.
> * You should unregister a listener in `onDestroy()` when you are finished with it, by setting to `null`.
> * You should maintain a reference to `iProovCallbackLauncher` whilst you have the listener registered.



#### 3. Launch a Claim

Call `iProovCallbackLauncher.launch()` to initiate a claim:

```kotlin
class MainActivityCallback : AppCompatActivity() {
    private val iProovCallbackLauncher: IProovCallbackLauncher = IProovCallbackLauncher()

    private fun launchIProov() {
        val options = IProov.Options()
        // Here you can customize any iProov options...
        val session = iProovCallback.launch(
            this, // Reference to current activity
            "wss://beta.rp.secure.iproov.me/ws", // Streaming URL
            "{{ your token here }}", // iProov token
            options // Optional
        )
    }
}

```

### IProovFlowLauncher

#### 1. Create an Instance of `IProovFlowLauncher`

```kotlin
val iProovFlowLauncher = IProovFlowLauncher()
```

#### 2. Collect from the Main Output Flow

To monitor the progress of an iProov claim and receive the result, you collect from the `iProovFlowLauncher.sessionStates`. Each element of the Flow is an `IProovSessionState`, as shown below, consisting of a `Session` and an `IProovState`.

```kotlin
data class IProovSessionState(
    val session: Session,
    val state: IProovState
)
```

Collecting from this Flow provides each `IProovState` from the `Session` we just launched. This Flow is continuous and can be used for multiple `Session`s, although iProov only allows one `Session` to be active at a time.

Below is code with events completed (as used by the Example App). See the same events in [IProovCallbackLauncher](#iproovcallback) above for detailed explanations and usage suggestions.

```kotlin
class MainActivityFlow : AppCompatActivity() {
    private val iProovFlowLauncher: IProovFlowLauncher = IProovFlowLauncher()

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                iProovFlowLauncher.sessionsStates.collect { sessionState: IProov.IProovSessionState? ->
                    sessionState?.state?.let { state ->
                        withContext(Dispatchers.Main) {
                            when (state) {
                                is IProov.IProovState.Connecting -> binding.progressBar.isIndeterminate =
                                    true
                                is IProov.IProovState.Connected -> binding.progressBar.isIndeterminate =
                                    false
                                is IProov.IProovState.Processing -> binding.progressBar.progress =
                                    state.progress.times(100).toInt()
                                is IProov.IProovState.Success -> onResult(getString(R.string.success), "")
                                is IProov.IProovState.Failure -> onResult(
                                    state.failureResult.reason.feedbackCode,
                                    getString(state.failureResult.reason.description)
                                )
                                is IProov.IProovState.Error -> onResult(
                                    getString(R.string.error),
                                    state.exception.localizedMessage
                                )
                                is IProov.IProovState.Cancelled -> onResult(
                                    getString(R.string.cancelled),
                                    null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

#### 3. Launch a Claim

Call `iProovFlowLauncher.launch()` in a (Default dispatcher) coroutine to initiate a claim.

This starts background tasks to connect to the backend, then the iProov Activity starts and performs the scan, and finally it closes, returning to the original Activity while completing network activity and getting a result in the background.

```kotlin
class MainActivityFlow : AppCompatActivity() {
    private val iProovFlowLauncher: IProovFlowLauncher = IProovFlowLauncher()

    private fun launchIProov() {
        val options = IProov.Options()
        // Here you can customize any iProov options...
        lifecycleScope.launch(Dispatchers.Default) {
            session = iProovFlowLauncher.launch(
                applicationContext,
                Constants.BASE_URL,
                token,
                options
            )
        }
    }
}
```

### Session

A `Session` represents the lifecycle of a single claim. It has:

- `token` - this is unique, and when using `IProovFlowLauncher` is can be used to distinguish events of one Session from another
- `currentState` - the current state of the claim, of the form `IProov.IProovState`, which `IProovFlowLauncher` receives
- `isActive` - whether the claim is still in progress (another cannot be started if this is true)
- `cancel()` - this allows the application to abort the claim

As an alternative to keeping the `Session` returned from `launch()`, the current or last `Session` can also be acquired (and cancelled) these ways, respectively:

```kotlin
iProovCallbackLauncher.currentSession()?.cancel()
```
or

```kotlin
scope.launch {
    iProovFlowLauncher.currentSession()?.cancel()
}
```

> **Note**: You can [customize]( #customize-the-user-experience) the user experience by passing in an `IProov.Options` object.

> **Warning**:
>
> - The iProov process can be manipulated locally by a malicious user therefore never use iProov as a local authentication method. You cannot trust the returned success result to prove that the user was authenticated or enrolled successfully.
> - You can treat the success callback as a hint to your app to update the user interface but you must always independently validate the token server-side (using the `validate` API call) before performing any authenticated user actions.

> **Warning**:
>
> - [Google](https://developer.android.com/guide/topics/manifest/activity-element#lmode) states that `singleInstance` and `singleTask` are not recommended for general use. iProov does not recommend the calling activity to have a `launchMode` of `singleInstance` - when tested, `back` does not always work correctly, particularly after the task switcher has momentarily put any `standard` Activity (like `IProov`) into the background.

## Customize the User Experience

You can customize the iProov user experience by passing in an instance of `IProov.Options` to the `launch()` function of instances of either `IProovCallbackLauncher` or `IProovFlowLauncher`.

> **Note**: The defaults defined support accessibility requirements and have been verified to [comply with WCAG 2.1 AA guidelines](https://www.iproov.com/blog/biometric-authentication-liveness-accessibility-inclusivity-wcag-regulations). Changing any of these could invalidate compliance.

### General Options

The following values are found at the top level of `IProov.Options`:

| Option name             | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | Default                                               |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| `title`                 | The custom title displayed during a claim scan.                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | `""` (Empty string)                                   |
| `titleTextColor`        | The color of the text in the title.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         | `Color.WHITE`                                         |
| `filter`                | The filter applied to the camera preview as either `LineDrawingFilter` or `NaturalFilter`.                                                                                                                                                                                                                                                                                                                                                                                                                                  | `LineDrawingFilter()` |
| `surroundColor`         | The color of the area outside the guideline oval.                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | `#66000000`                                           |
| `font`                  | Optional custom font for the title and prompt as either `PathFont` or `ResourceFont`.                                                                                                                                                                                                                                                                                                                                                                                                                                       | `null`                                                |
| `logo`                  | Optional custom logo in the header as `BitmapIcon`, `DrawableIcon` or `ResourceIcon`.                                                                                                                                                                                                                                                                                                                                                                                                                                       | `null`                                                |
| `enableScreenshots`     | Whether screenshots are enabled during the iProov scan. Disabled by default for security reasons.                                                                                                                                                                                                                                                                                                                                                                         | `false`                                               |
| `closeButton`           | Customize the Close button.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | `R.drawable.ic_arrow_back` in `Color.WHITE`           |
| `promptTextColor`       | The color of text in prompt box.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | `Color.WHITE`                                         |
| `promptBackgroundColor` | The color of the prompt box.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | `#CC000000`                                           |
| `promptRoundedCorners`  | Whether the prompt has rounded corners.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | `true`                                                |
| `certificates`          | Optionally supply certificates used for pinning. If you are using a reverse proxy you may need to provide your own certificates. Certificate pinning is enabled by default. Certificates should be passed as a list of resource IDs or byte arrays (certificate content).<br />You can disable pinning by passing an empty array (but not in a production environment). <br />Generate certificates in *DER-encoded X.509* certificate format, for example:<br /> `$ openssl x509 -in cert.crt -outform der -out cert.der`. | AlphaSSL intermediate certificate                     |
| `timeoutSecs`           | The WebSocket streaming timeout in seconds. To disable timeout, set to 0.                                                                                                                                                                                                                                                                                                                                                                                                                                                             | `10`                                                  |
| `orientation`           | Set the orientation of the iProov activity. Possible values are (`PORTRAIT`, `REVERSE_PORTRAIT`, `LANDSCAPE` or `REVERSE_LANDSCAPE`.<br />**Note**: This option rotates the UI, not the camera. Supports USB cameras on `LANDSCAPE` displays, such as tablets and kiosks, where the camera is oriented normally.                                                                                                                                                                                                                         | `PORTRAIT`                                            |
| `camera`                | Either use the in-built front-facing camera (`FRONT`) or USB `EXTERNAL` camera support for kiosks.                                                                                                                                                                                                                                                                                                                                                                                                                                                                | `FRONT`                                            |
| `faceDetector` (deprecated)          | Optionally select the face detector. May require the addition of other dependencies. This is a trade-off between speed, accuracy, and application size impact.<br />`AUTO` will try to use the BlazeFace or ML Kit face detector and fallback to `CLASSIC` if unavailable. See [Alternative Face Detectors](https://github.com/iProov/android/wiki/Alternative-Face-Detectors-(Deprecated-in-v8)).                                                                                                                    | `CLASSIC`                                             |

### Genuine Presence Assurance Options

The following values are found under `IProov.Options.genuinePresenceAssurance`.

| Option name         | Description                                                                                                                            | Defaults      |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------|---------------|
| `readyOvalColor`    | Color for oval stroke when in a GPA "not ready" state.                                              | `#01AC41`     |
| `notReadyOvalColor` | Color for oval stroke when in the GPA "ready" state.                                                               | `Color.WHITE` |
| `maxYaw` (deprecated)           | Specify the maximum deviation on the yaw axis of the user’s face. Do not change without advice. Applies only to MLKit Face Detector.   | `0.25`        |
| `maxRoll` (deprecated)           | Specify the maximum deviation on the roll axis of the user’s face. Do not change without advice. Applies only to MLKit Face Detector.  | `0.25`        |
| `maxPitch` (deprecated)          | Specify the maximum deviation on the pitch axis of the user’s face. Do not change without advice. Applies only to MLKit Face Detector. | `0.25`        |

### Liveness Assurance Options

The following values are found under `IProov.Options.livenessPresenceAssurance`.

| Option name                | Description                                     | Defaults      |
|----------------------------|-------------------------------------------------|---------------|
| `ovalStrokeColor`          | Color for oval stroke during LA scan.       | `Color.WHITE` |
| `completedOvalStrokeColor` | Color for oval stroke after LA scan completes.        | `#01AC41`     |


## String Localization & Customization

The SDK ships with support for the following languages:

- English (United States) - `en-US`
- Dutch - `de`
- French - `fr`
- German - `de`
- Italian - `it`
- Portuguese - `pt`
- Portuguese (Brazil) - `pt-BR`
- Spanish - `es`
- Spanish (Columbia) - `es-CO`
- Welsh - `cy-GB`

You can customize the strings in the app or localize them into a different language,

All strings are prefixed with `iproov__` and you can override them in `strings.xml` (download a copy from [GitHub](https://github.com/iProov/android/blob/master/resources/strings.xml)).

## Failures and Errors

A **failure** occurs when iProov successfully processes a claim but the user's identity cannot be verified.

- The capture was successfully received and processed by the server, which returns a result.
- The failure results in a `FailureResult`, which includes an enum called `reason` which has the following properties:
    - `feedbackCode` - A string representation of the feedback code.
    - `description` - You should present this to the user as it may provide an informative hint for the user to increase their chances of iProoving successfully next time.

An **error** occurs when a capture claim fails completely and iProov is unable to process it. Errors result in an `IProovException`.

#### Genuine Presence Assurance

The available failure reasons for Genuine Presence Assurance claims are as follows:

| Enum value          | `description` (English)                |
|---------------------|----------------------------------------|
| `UNKNOWN`           | Try again                              |
| `TOO_MUCH_MOVEMENT` | Keep still                             |
| `TOO_BRIGHT`        | Move somewhere darker                  |
| `TOO_DARK`          | Move somewhere brighter                |
| `MISALIGNED_FACE`   | Keep your face in the oval             |
| `EYES_CLOSED`       | Keep your eyes open                    |
| `FACE_TOO_FAR`      | Move your face closer to the screen    |
| `FACE_TOO_CLOSE`    | Move your face farther from the screen |
| `SUNGLASSES`        | Remove sunglasses                      |
| `OBSCURED_FACE`     | Remove any face coverings              |
| `USER_TIMEOUT`      | Try again                              |
| `NOT_SUPPORTED`     | Device is not supported                |

#### Liveness Assurance

The available failure reasons for Liveness Assurance claims are as follows:

| Enum value          | `description` (English)                |
|---------------------|----------------------------------------|
| `UNKNOWN`           | Try again                              |
| `USER_TIMEOUT`      | Try again                              |
| `NOT_SUPPORTED`     | Device is not supported                |

### IProovException subclasses

In the event of an error, one of the following `IProovException` classes will be surfaced:

| Exception subclass                | Further details                                                                                                                                                                                |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `MultiWindowUnsupportedException` | The user attempted to iProov in split-screen/multi-screen mode, which is not supported.                                                                                                    |
| `CaptureAlreadyActiveException`   | An existing iProov capture is already in progress. Wait until the current capture completes before starting a new one.                                                                     |
| `CameraException`                 | An error occurred acquiring or using the camera. Applicable when using the external camera support. See `options.camera`.                                                                  |
| `CameraPermissionException`       | The user did not allow access to the camera when prompted. Prompt the user to enable the camera permission (in Settings).                                                                  |
| `FaceDetectorException`           | An error occurred with the face detector.                                                                                                                                                  |
| `UnexpectedErrorException`        | An unrecoverable error occurred during the transaction.                                                                                                                                    |
| `ServerException`                 | The token was invalidated server-side or some other unrecoverable server error occurred.                                                                                                   |
| `NetworkException`                | An error occurred with communications to the server. Typically indicates a device connectivity issue, for example, the user's session has timed out or the internet service has been lost. |
| `UnsupportedDeviceException`      | The device is not supported, for example, does not have a front-facing camera.                                                                                                             |
| `InvalidOptionsException`         | An error occurred when trying to apply the options you specified.                                                                                                                          |


## Example Project

For a simple iProov experience that is ready to run out-of-the-box, see the example project on [GitHub](https://github.com/iProov/android/tree/master/example-app):

1. Open the `example-app` project in Android Studio.

2. Open the `Constants.kt` file and insert your API Key and Secret at the relevant points.

3. Choose between `callback` (`IProovCallbackLauncher`) and `flows` (`IProovFlowLauncher`) variants by changing the build variant in Android Studio.

> **Warning**: The example app uses the [Android API Client](https://github.com/iProov/android-api-client) to directly fetch tokens on-device, which is insecure. Production implementations of iProov should always obtain tokens securely from a server-to-server call.

## Additional Documentation

- [Documentation Center](https://docs.iproov.com/) - Implementation Guide, Glossary, API reference
- [Release Notes](https://github.com/iProov/android/releases)
- [FAQs](https://github.com/iProov/android/wiki/Frequently-Asked-Questions)
- [Wiki](https://github.com/iProov/android/wiki)

## Support

For help with integrating the SDK, contact [support@iproov.com](mailto:support@iproov.com).
