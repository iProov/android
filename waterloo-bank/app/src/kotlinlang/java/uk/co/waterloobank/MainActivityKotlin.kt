package uk.co.waterloobank

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iproov.androidapiclient.AssuranceType
import com.iproov.androidapiclient.ClaimType
import com.iproov.androidapiclient.DemonstrationPurposesOnly
import com.iproov.androidapiclient.kotlinfuel.ApiClientFuel
import com.iproov.sdk.IProov
import com.iproov.sdk.cameray.Orientation
import com.iproov.sdk.core.exception.IProovException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityKotlin : AppCompatActivity() {

    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val listener = object : IProov.Listener {

        override fun onConnecting() {
            statusTextView.text = getString(R.string.connecting)
            progressBar.isIndeterminate = true
        }

        override fun onConnected() {
            statusTextView.text = getString(R.string.connected)
            progressBar.isIndeterminate = false
        }

        override fun onSuccess(result: IProov.SuccessResult) =
                onResult(getString(R.string.success), getString(R.string.token_format, result.token))

        override fun onFailure(result: IProov.FailureResult) =
                onResult(result.feedbackCode, result.reason)

        override fun onProcessing(progress: Double, message: String) {
            statusTextView.text = message
            progressBar.progress = progress.times(100).toInt()
        }

        override fun onError(e: IProovException) =
                onResult(getString(R.string.error), e.localizedMessage)

        override fun onCancelled() =
                onResult(getString(R.string.cancelled), null)
    }

    @DemonstrationPurposesOnly
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Constants.API_KEY.isEmpty() || Constants.SECRET.isEmpty()) {
            throw IllegalStateException("You must set the API_KEY and SECRET values in the Constants.kt file!")
        }

        arrayOf(loginButton, registerButton).forEach {
            it.setOnClickListener {
                val username = usernameEditText.text.toString()
                if (username.isEmpty()) {
                    Toast.makeText(this, getString(R.string.user_id_cannot_be_empty), Toast.LENGTH_SHORT).show()
                } else {
                    val claimType = when(it) {
                        loginButton -> ClaimType.VERIFY
                        registerButton -> ClaimType.ENROL
                        else -> throw NotImplementedError()
                    }
                    launchIProov(claimType, username)
                }
            }
        }

        versionTextView.text = getString(R.string.kotlin_version_format, IProov.getSDKVersion())

        IProov.registerListener(listener);
    }

    override fun onDestroy() {
        IProov.unregisterListener(listener)
        super.onDestroy()
    }

    @DemonstrationPurposesOnly
    private fun launchIProov(claimType: ClaimType, username: String) {
        arrayOf(loginButton, registerButton).forEach { it.visibility = View.GONE }
        arrayOf(progressBar, statusTextView).forEach { it.visibility = View.VISIBLE }

        statusTextView.text = getString(R.string.getting_token)
        progressBar.isIndeterminate = true

        val apiClientFuel = ApiClientFuel(
                this,
                Constants.BASE_URL,
                Constants.API_KEY,
                Constants.SECRET
        )

        uiScope.launch(Dispatchers.IO) {
            try {
                val token = apiClientFuel.getToken(
                        AssuranceType.GENUINE_PRESENCE,
                        claimType,
                        username)

                try {
                    val options = IProov.Options().apply {
                        ui.logoImageResource = R.mipmap.ic_launcher
                    }

                    IProov.launch(this@MainActivityKotlin, Constants.BASE_URL, token, options)
                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        onResult(getString(R.string.error), ex.localizedMessage)
                    }
                }

            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(getString(R.string.error), getString(R.string.failed_to_get_token))
                }
            }

        }
    }

    private fun onResult(title: String?, resultMessage: String?) {
        progressBar.progress = 0
        arrayOf(progressBar, statusTextView).forEach { it.visibility = View.GONE }

        AlertDialog.Builder(this@MainActivityKotlin)
                .setTitle(title)
                .setMessage(resultMessage)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    arrayOf(loginButton, registerButton).forEach { it.visibility = View.VISIBLE }
                }
                .show()
    }

}