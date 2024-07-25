// Copyright (c) 2020 iProov Ltd. All rights reserved.
package com.iproov.example

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.json.jsonDeserializer
import com.iproov.androidapiclient.AssuranceType
import com.iproov.androidapiclient.ClaimType
import com.iproov.androidapiclient.DemonstrationPurposesOnly
import com.iproov.androidapiclient.kotlinfuel.ApiClientFuel
import com.iproov.example.databinding.ActivityMainBinding
import com.iproov.sdk.api.IProov
import com.iproov.sdk.api.exception.SessionCannotBeStartedTwiceException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var binding: ActivityMainBinding
    private var sessionStateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (Constants.API_KEY.isEmpty() || Constants.SECRET.isEmpty()) {
            throw IllegalStateException("You must set the API_KEY and SECRET values in the Constants.kt file!")
        }

        arrayOf(binding.enrolGpaButton, binding.verifyLaButton, binding.verifyGpaButton).forEach { it ->
            it.setOnClickListener {
                val username = binding.usernameEditText.text.toString()
                if (username.isEmpty()) {
                    Toast.makeText(this, getString(R.string.user_id_cannot_be_empty), Toast.LENGTH_SHORT).show()
                } else {
                    val claimType = when (it) {
                        binding.enrolGpaButton -> ClaimType.ENROL
                        binding.verifyGpaButton -> ClaimType.VERIFY
                        binding.verifyLaButton -> ClaimType.VERIFY
                        else -> throw NotImplementedError()
                    }

                    val assuranceType = when (it) {
                        binding.enrolGpaButton -> AssuranceType.GENUINE_PRESENCE
                        binding.verifyGpaButton -> AssuranceType.GENUINE_PRESENCE
                        binding.verifyLaButton -> AssuranceType.LIVENESS
                        else -> throw NotImplementedError()
                    }
                    launchIProov(claimType, username, assuranceType)
                }
            }
        }

        binding.versionTextView.text = getString(R.string.kotlin_version_format, IProov.sdkVersion)

        // In case this activity was recycled during the Session, we reconnect to the current one
        IProov.session?.let { session ->
            observeSessionState(session)
        }
    }

    private fun observeSessionState(session: IProov.Session, whenReady: (() -> Unit)? = null) {
        sessionStateJob?.cancel()
        sessionStateJob = lifecycleScope.launch(Dispatchers.IO) {
            session.state
                .onSubscription { whenReady?.invoke() }
                .collect { state ->
                    if (sessionStateJob?.isActive == true) {
                        withContext(Dispatchers.Main) {
                            when (state) {
                                is IProov.State.Starting -> {
                                }
                                is IProov.State.Connecting ->
                                    binding.progressBar.isIndeterminate =
                                        true

                                is IProov.State.Connected ->
                                    binding.progressBar.isIndeterminate =
                                        false

                                is IProov.State.Processing ->
                                    binding.progressBar.progress =
                                        state.progress.times(100).toInt()

                                is IProov.State.Success -> onResult(
                                    getString(R.string.success),
                                    "",
                                )

                                is IProov.State.Failure -> onResult(
                                    state.failureResult.reason.feedbackCode,
                                    getString(state.failureResult.reason.description),
                                )

                                is IProov.State.Error -> onResult(
                                    getString(R.string.error),
                                    state.exception.localizedMessage,
                                )

                                is IProov.State.Canceled -> onResult(
                                    getString(R.string.canceled),
                                    null,
                                )
                            }
                        }
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @DemonstrationPurposesOnly
    private fun launchIProov(claimType: ClaimType, username: String, assuranceType: AssuranceType) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = true

        val apiClientFuel = ApiClientFuel(
            this,
            Constants.FUEL_URL,
            Constants.API_KEY,
            Constants.SECRET,
        )

        uiScope.launch(Dispatchers.IO) {
            try {
                val token = apiClientFuel.getToken(
                    assuranceType,
                    claimType,
                    username,
                )

                if (!job.isActive) return@launch
                startScan(token)
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    ex.printStackTrace()
                    if (ex is FuelError) {
                        val json = jsonDeserializer().deserialize(ex.response)
                        val description = json.obj().getString("error_description")
                        onResult(getString(R.string.error), description)
                    } else {
                        onResult(getString(R.string.error), getString(R.string.failed_to_get_token))
                    }
                }
            }
        }
    }

    @Throws(SessionCannotBeStartedTwiceException::class)
    private fun startScan(token: String) {
        IProov.createSession(applicationContext, Constants.BASE_URL, token).let { session ->
            // Observe first, then start
            observeSessionState(session) {
                session.start()
            }
        }
    }

    private fun onResult(title: String?, resultMessage: String?) {
        binding.progressBar.progress = 0
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setMessage(resultMessage)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.cancel() }
            .show()
    }
}