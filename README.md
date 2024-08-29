
![iProov: Flexible authentication for identity assurance](images/banner.jpg)

# iProov Android Biometrics SDK v10.0.1

## Contents of this Package

The framework package is provided via this GitHub repository, which contains:

* **README.md** - this document
* **maven** - a Maven repository for the Biometrics SDK
* **example-app** - a sample iProov project to demonstrate the integration
* **resources** - a directory containing additional development resources

## Introduction

This guide describes how to integrate iProov biometric assurance technologies into your Android app.

iProov offers Genuine Presence Assurance™ technology (also known as "Dynamic Liveness") and Liveness Assurance™ technology (also known as "Express Liveness"):

* [**Genuine Presence Assurance**](https://www.iproov.com/iproov-system/technology/genuine-presence-assurance) verifies that an online remote user is the right person, a real person and that they are authenticating right now, for purposes of access control and security.
* [**Liveness Assurance**](https://www.iproov.com/iproov-system/technology/liveness-assurance) verifies a remote online user is the right person and a real person for access control and security.

Find out more about how to use iProov in your user journeys in the [Implementation Guide](https://docs.iproov.com/docs/Content/ImplementationGuide/implementation-intro.htm).

iProov also supports [iOS](https://github.com/iproov/ios), [Xamarin](https://github.com/iproov/xamarin), [Flutter](https://github.com/iproov/flutter), [React Native](https://github.com/iproov/react-native), and [Web](https://github.com/iProov/web).

## Requirements

- Android Studio
- `minSdkVersion` API Level 26 (Android 8) and above
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

Alternatively, in `settings.gradle` if you have opted to use `dependencyResolutionManagement { ... }`, then add here instead.

3. Add the SDK version to the `dependencies` section in your `build.gradle` file:

    ```groovy
    dependencies {
        implementation('com.iproov.sdk:iproov:10.0.1')
    }
    ```

4. Build your project

> **TIP:** When testing development, using debuggable apps, iProov can provide "development" SPs for you to use.

## Get Started

To enrol (register) or verify (login) a user, follow the steps below.

### Get a Token

Obtain these tokens:

- A **verify** token for logging in an existing user
- An **enrol** token for registering a new user

See the [REST API documentation](https://secure.iproov.me/docs.html) for details about how to generate tokens.

> **TIP:** In a production app you typically obtain tokens via a server-to-server back-end call. For demos and testing, iProov provides Kotlin and Java sample code for obtaining tokens via [iProov API v2](https://eu.rp.secure.iproov.me/docs.html) with our open-source [Android API Client](https://github.com/iProov/android-api-client).

### Create a Session

For each scan, you need an `IProov.Session`, which you can then call `IProov.Session.start()` when you are ready to start (but only once).

Everything you need to run a scan is available in the `IProov` Object. Here you will find the function to create a `IProov.Session` as follows:

```kotlin
val session: IProov.Session = IProov.createSession(context, baseUrl, token, options)
```

- `context` - can be any Android context (your Activity or the Application context)
- `baseUrl` - is the address of the server (SP) you are using
- `token` - the single-use claim token you acquired in the prior step
- `options` - are optional and referenced later in this document (they control the look and feel of the scan's UI among other aspects)

You now have a session and can start a scan, but first we need to talk about states, so you can know what happened.

### States

For the duration of a Scan, you can monitor its state. The primary state let's you know the progress and ultimately concludes with one of four terminal states.

`IProov.State` is a sealed class with four intermediate states and four terminal states.

Initially, the scan will begin with `IProov.State.Starting`, then it moves to `IProov.State.Connecting` as it tries to communicate with the server and reach `IProov.State.Connected` when it has done so successfully. Then the UI begins and over time various iterations of `IProov.State.Processing` will happen until a terminal state is produced.

IProov's four terminal states are as follows:

- `IProov.State.Success` - providing a `IProov.SuccessResult` (can contain a selfie frame, if set up for your account)
- `IProov.State.Failure` - providing a `IProov.FailureResult` (contains a reason code; can contain a selfie frame, if set up for your account)
- `IProov.State.Canceled` - providing a value indicating whether the `IProov.Canceler.USER` hit the back button or otherwise moved away from the app, or the `IProov.Session` was canceled by the `IProov.Canceler.INTEGRATION` i.e. the app called `IProov.Session.cancel()`
- `IProov.State.Error` - providing an `IProovException` subclass indicating the cause of the error that prevented the claim from being completed

The last two (`Canceled` and `Error`) can interrupt the sequence at any time.

### Listening to the State

The Session provides a `val state: StateFlow<IProov.State>` to be collected from.

### UIStates (optional)

Additionally, for those wanting to monitor the user experience, there are also UIStates, which indicate when the Session's UI starts and stops.

Similarly, the Session provides a `val uiState: StateFlow<IProov.UIState>` to be collected from.

### Starting the Session

Now the hard work is done, and starting the `IProov.Session` is simply achieved with a call to `IProov.Session.start()`.

Two possible errors at this point to be aware of:

1. `session.start()` might cause an `IProov.State.Error` in the Flow, containing a `CaptureAlreadyActiveException`. This is caused if a previous `IProov.Session.start()` has yet to complete. You will need to create a new `Session` to try again.
2. `session.start()` might throw a `SessionCannotBeStartedTwiceException`. This is caused if `IProov.Session.start()` is called twice on the same session, no matter the outcome of the first call (a new session is always required).

### Examples

This is demonstrated in the included [Example App](https://github.com/iProov/android/tree/master/example-app), from where the following snippets were taken.

Starting a Session, once you have obtained a token, and then observing on the State is as simple as this:

```kotlin
    @Throws(SessionCannotBeStartedTwiceException::class)
    private fun startScan(token: String) {
        IProov.createSession(applicationContext, Constants.BASE_URL, token).let { session ->
            // Observe first, then start
            observeSessionState(session) {
                session.start()
            }
        }
    }

    private fun observeSessionState(session: IProov.Session, whenReady: (() -> Unit)? = null) {
        sessionStateJob?.cancel()
        sessionStateJob = lifecycleScope.launch(Dispatchers.IO) {
            session.state
                .onSubscription { whenReady?.invoke() }
                .collect { state ->
                    //TODO Action on State
                }
        }
    }
```

Since you might need to cope with configuration changes you can fetch the current Session again this way:

```kotlin
class MainActivity : AppCompatActivity() {
    private var sessionStateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In case this activity was recycled during the Session, we reconnect to the current one
        IProov.session?.let { session ->
            observeSessionState(session)
        }
    }
}
```

Of course, using a `ViewModel` would prevent the need for this.

> **Note**: You can [customize]( #customize-the-user-experience) the user experience by passing in an `IProov.Options` object.

> **Warning**:
>
> - The iProov process can be manipulated locally by a malicious user therefore never use iProov as a local authentication method. You cannot trust the returned success result to prove that the user was authenticated or enrolled successfully.
> - You can treat the success state as a hint to your app to update the user interface but you must always independently validate the token server-side (using the `validate` API call) before performing any authenticated user actions.

> **Warning**:
>
> - [Google](https://developer.android.com/guide/topics/manifest/activity-element#lmode) states that `singleInstance` and `singleTask` are not recommended for general use. iProov does not recommend the calling activity to have a `launchMode` of `singleInstance` - when tested, `back` does not always work correctly, particularly after the task switcher has momentarily put any `standard` Activity (like `IProov`) into the background.

## Customize the User Experience

You can customize the iProov user experience by passing in an instance of `IProov.Options` to the call to `IProov.createSession()`.

> **Note**: The defaults defined support accessibility requirements and have been verified to [comply with WCAG 2.1 AA guidelines](https://www.iproov.com/blog/biometric-authentication-liveness-accessibility-inclusivity-wcag-regulations). Changing any of these could invalidate compliance.

### General Options

The following values are found at the top level of `IProov.Options`:

| Option name                 | Description                                                                                                                                                                                                                                                                                                                                                                              | Default                                     |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------|
| `title`                     | The custom title displayed during a claim scan.                                                                                                                                                                                                                                                                                                                                          | `""` (Empty string)                         |
| `titleTextColor`            | The color of the text in the title.                                                                                                                                                                                                                                                                                                                                                      | `Color.WHITE`                               |
| `headerBackgroundColor`     | The background color of the header bar.                                                                                                                                                                                                                                                                                                                                                  | `Transparent`                               |
| `filter`                    | The filter applied to the camera preview as either `LineDrawingFilter` or `NaturalFilter`. With `NaturalFilter` in LA, `surroundColor` will be opaque on the new rounded mask area.                                                                                                                                                                                                      | `LineDrawingFilter()`                       |
| `surroundColor`             | The color of the area outside the guideline oval. With Clear and Blur natural filters in LA, this colour will be fully opaque on the new rounded mask area.                                                                                                                                                                                                                              | `#66000000`                                 |
| `font`                      | Optional custom font for the title and prompt as either `PathFont` or `ResourceFont`.                                                                                                                                                                                                                                                                                                    | `null`                                      |
| `logo`                      | Optional custom logo in the header as `BitmapIcon`, `DrawableIcon` or `ResourceIcon`.                                                                                                                                                                                                                                                                                                    | `null`                                      |
| `enableScreenshots`         | Whether screenshots are enabled during the iProov scan. Disabled by default for security reasons.                                                                                                                                                                                                                                                                                        | `false`                                     |
| `closeButton`               | Customize the Close button.                                                                                                                                                                                                                                                                                                                                                              | `R.drawable.ic_arrow_back` in `Color.WHITE` |
| `promptTextColor`           | The color of text in prompt box.                                                                                                                                                                                                                                                                                                                                                         | `Color.WHITE`                               |
| `promptBackgroundColor`     | The color of the prompt box.                                                                                                                                                                                                                                                                                                                                                             | `#CC000000`                                 |
| `promptRoundedCorners`      | Whether the prompt has rounded corners.                                                                                                                                                                                                                                                                                                                                                  | `true`                                      |
| `disableExteriorEffects`    | Optionally disable blur and vignette outside the oval.                                                                                                                                                                                                                                                                                                                                   | `false`                                     |
| `certificates`              | Optionally supply certificates used for pinning. If you are using a reverse proxy you may need to provide your own certificates. Certificate pinning is enabled by default. Certificate should be passed as a string (certificate`s subject public key info as SHA-256 hash).<br /> See [below](#certificate-pinning) for more information                                               | iProov Server Certificates                  |
| `timeoutSecs`               | The WebSocket streaming timeout in seconds. To disable timeout, set to 0.                                                                                                                                                                                                                                                                                                                | `10`                                        |
| `orientation`               | Set the orientation of the iProov activity. Possible values are (`PORTRAIT`, `REVERSE_PORTRAIT`, `LANDSCAPE` or `REVERSE_LANDSCAPE`.<br />**Note**: This option rotates the UI, not the camera. Supports USB cameras on `LANDSCAPE` displays, such as tablets and kiosks, where the camera is oriented normally. For Liveness Assurance, LANDSCAPE and REVERSE_LANDSCAPE is not allowed. | `PORTRAIT`                                  |
| `camera`                    | Either use the in-built front-facing camera (`FRONT`) or USB `EXTERNAL` camera support for kiosks.                                                                                                                                                                                                                                                                                       | `FRONT`                                     |

#### Certificate Pinning

By default, the iProov SDK pins to the iProov server certificates, which are used by `*.rp.secure.iproov.me`.

If you are using your own reverse-proxy, you will need to update the pinning configuration to pin to your own certificate(s) instead.

Certificates should be passed as a `String`, which is base64-encoded SHA-256 hash of a certificate's Subject Public Key Info. You can load a certificate as follows:

```kotlin
options.certificates = listOf(Certificate("O8qZKEXWWkMPISIpvB7DUow++JzIW2g+k9z3U/l5V94="))
```

To get Subject Public Key Info from a `.crt`, use the following command:

```sh
$ openssl x509  -in cert.crt -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
```

To get Subject Public Key Info from a `.der`, use the following command:

```sh
$ openssl x509 -inform der -in cert.der -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
```

When multiple certificates are passed, as long as the server matches **any** of the certificates, the connection will be allowed. Pinning is performed against the **whole** of the certificate.

You can also disable certificate pinning entirely, by passing an empty array:

```kotlin
options.certificates = listOf()
```

> **Warning**: Never disable certificate pinning in production apps!

Should your app require additional certificate pinning at app level using the [Network Configuration file](https://developer.android.com/privacy-and-security/security-config), please contact our support team which can assist you with the implementation and will provide the latest list of certificates.

Please note that this approach will require you to maintain and update the certificate list for your app to ensure ongoing security and functionality of the iProov SDK

### Genuine Presence Assurance Options

The following values are found under `IProov.Options.genuinePresenceAssurance`.

| Option name               | Description                                              | Defaults      |
|---------------------------|----------------------------------------------------------|---------------|
| `readyOvalStrokeColor`    | Color for oval stroke when in a GPA "ready" state.       | `#01AC41`     |
| `notReadyOvalStrokeColor` | Color for oval stroke when in the GPA "not ready" state. | `Color.WHITE` |

### Liveness Assurance Options

The following values are found under `IProov.Options.livenessPresenceAssurance`.

| Option name                | Description                                    | Defaults      |
|----------------------------|------------------------------------------------|---------------|
| `ovalStrokeColor`          | Color for oval stroke during LA scan.          | `Color.WHITE` |
| `completedOvalStrokeColor` | Color for oval stroke after LA scan completes. | `#01AC41`     |


## String Localization & Customization

The SDK ships with support for the following languages:

| Language                | Code    |
|-------------------------|---------|
| English (United States) | `en-US` |
| Dutch                   | `nl`    |
| French                  | `fr`    |
| German                  | `de`    |
| Italian                 | `it`    |
| Portuguese              | `pt`    |
| Portuguese (Brazil)     | `pt-BR` |
| Spanish                 | `es`    |
| Spanish (Columbia)      | `es-CO` |
| Welsh                   | `cy-GB` |

You can customize the strings in the app or localize them into a different language,

All strings are prefixed with `iproov__` and you can override them in `strings.xml` (download a copy from [GitHub](https://github.com/iProov/android/blob/master/resources/strings.xml)).

## Failures and Errors

### Failures

A **failure** occurs when iProov successfully processes a claim but the user's identity cannot be verified.

- The capture was successfully received and processed by the server, which returns a result.
- The failure results in a `FailureResult`, which includes an enum called `FailureReason` which has the following properties:
    - `feedbackCode` - A string representation of the feedback code.
    - `description` - You should present this to the user as it may provide an informative hint for the user to increase their chances of iProoving successfully next time.

An **error** occurs when a capture claim fails completely and iProov is unable to process it. Errors result in an `IProovException`.

The available failure reasons for claims are as follows:

| `FailureReason` value | `description` (English)                | GPA | LA |
|-----------------------|----------------------------------------|---|----|
| `UNKNOWN`             | Try again                              | ✅ | ✅  |
| `TOO_MUCH_MOVEMENT`   | Keep still                             | ✅ | ⚠️ |
| `TOO_BRIGHT`          | Move somewhere darker                  | ✅ | ⚠️ |
| `TOO_DARK`            | Move somewhere brighter                | ✅ | ⚠️ |
| `MISALIGNED_FACE`     | Keep your face in the oval             | ✅ | ❌  |
| `EYES_CLOSED`         | Keep your eyes open                    | ✅ | ⚠️ |
| `FACE_TOO_FAR`        | Move your face closer to the screen    | ✅ | ❌  |
| `FACE_TOO_CLOSE`      | Move your face farther from the screen | ✅ | ❌  |
| `SUNGLASSES`          | Remove sunglasses                      | ✅ | ⚠️ |
| `OBSCURED_FACE`       | Remove any face coverings              | ✅ | ❌  |
| `MULTIPLE_FACES`      | Ensure only one person is visible      | ⚠️ | ⚠️ |

Key: ✅ = will be returned, ❌ = will not be returned, ⚠️ = may be returned in the future

These are not an indication of tests being performed but only whether reasons of failure are reported.

### Errors

An **error** occurs when an iProov claim cannot be processed completely, in which case one of the following `IProovException` classes will be surfaced:

| Exception subclass                | Further details                                                                                                                                                                            |
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

> **Warning**: The example app uses the [Android API Client](https://github.com/iProov/android-api-client) to directly fetch tokens on-device, which is insecure. Production implementations of iProov should always obtain tokens securely from a server-to-server call.

## Additional Documentation

- [Documentation Center](https://docs.iproov.com/) - Implementation Guide, Glossary, API reference
- [Release Notes](https://github.com/iProov/android/releases)
- [FAQs](https://github.com/iProov/android/wiki/Frequently-Asked-Questions)
- [Wiki](https://github.com/iProov/android/wiki)

## Support

For help with integrating the SDK, contact [support@iproov.com](mailto:support@iproov.com).
