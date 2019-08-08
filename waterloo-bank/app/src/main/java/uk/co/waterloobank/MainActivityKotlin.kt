package uk.co.waterloobank

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.iproov.androidapiclient.DemonstrationPurposesOnly
import com.iproov.androidapiclient.kotlinfuel.ApiClientFuel
import com.iproov.sdk.IProov
import com.iproov.sdk.IProovException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivityKotlin : AppCompatActivity() {

    lateinit var connection: IProov.IProovConnection
    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)


    @DemonstrationPurposesOnly
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton.setOnClickListener {
            login(usernameEditText.text.toString())
        }

        registerButton.setOnClickListener {
            register(usernameEditText.text.toString())
        }

        connection = IProov.getIProovConnection(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.stop()
    }

    private fun hideLoadingViews() {
        progressBar.visibility = View.GONE
        progressBar.progress = 0
        captureStatus.visibility = View.GONE
    }

    private fun hideButtons() {
        loginButton.visibility = View.GONE
        registerButton.visibility = View.GONE
    }

    private fun showLoadingViews() {
        progressBar.visibility = View.VISIBLE
        captureStatus.visibility = View.VISIBLE
    }

    private fun showButtons() {
        loginButton.visibility = View.VISIBLE
        registerButton.visibility = View.VISIBLE
    }

    @DemonstrationPurposesOnly
    private fun login(userID: String) {
        hideButtons()
        showLoadingViews()

        val apiClientFuel = ApiClientFuel(
                this,
                Constants.BASE_URL,
                Constants.API_KEY,
                Constants.SECRET
        )

        uiScope.launch(Dispatchers.IO) {

            try {
                val token = apiClientFuel.getToken(com.iproov.androidapiclient.ClaimType.VERIFY, userID)
                connection.launch(createOptions(), token, object : IProov.IProovCaptureListener {
                    override fun onSuccess(token: String) {
                        onResult("Success", "Successfully iProoved.\nToken:$token")
                    }

                    override fun onFailure(reason: String, feedback: String) {
                        onResult("Failed", "Failed to iProov\nreason: $reason feedback:$feedback")
                    }

                    override fun onProgressUpdate(message: String, progress: Double) {
                        onProgress(message, progress.toInt())
                    }

                    override fun onError(e: IProovException) {
                        onResult("Error", "Error: ${e.localizedMessage}")
                    }

                    override fun onCanceled() {
                        onResult("Canceled", "User action: canceled")
                    }
                })
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Failed", "Failed to get token.")
                }
            }
        }
    }

    @DemonstrationPurposesOnly
    private fun register(userID: String) {

        hideButtons()
        showLoadingViews()

        val apiClientFuel = ApiClientFuel(
                this,
                Constants.BASE_URL,
                Constants.API_KEY,
                Constants.SECRET
        )

        uiScope.launch(Dispatchers.IO) {
            try {
                val token = apiClientFuel.getToken(com.iproov.androidapiclient.ClaimType.ENROL, userID)
                connection.launch(createOptions(), token, object : IProov.IProovCaptureListener {
                    override fun onSuccess(token: String) {
                        onResult("Success", "Successfully registered.\nToken:$token")
                    }

                    override fun onFailure(reason: String, feedback: String) {
                        onResult("Failed", "Failed to register\nreason: $reason feedback:$feedback")
                    }

                    override fun onProgressUpdate(message: String, progress: Double) {
                        onProgress(message, progress.toInt())
                    }

                    override fun onError(e: IProovException) {
                        onResult("Error", "Error: ${e.localizedMessage}")
                    }

                    override fun onCanceled() {
                        onResult("Canceled", "User action: canceled")
                    }
                })
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Failed", "Failed to get token.")
                }
            }
        }
    }

    private fun onResult(title: String, resultMessage: String?) {
        hideLoadingViews()
        AlertDialog.Builder(this@MainActivityKotlin)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(resultMessage)
                .setPositiveButton("OK") { _, _ ->
                    showButtons()
                }
                .setCancelable(false)
                .show()
    }

    private fun onProgress(status: String, progressValue: Int) {
        captureStatus.text = status
        progressBar.progress = progressValue
    }

    private fun createOptions(): IProov.Options {
        return IProov.Options()
                .apply {
                    ui.autostartDisabled = false
                    ui.boldFont = "Merriweather-Bold.ttf"
                    ui.logoImage = R.mipmap.ic_launcher
                }
    }
}