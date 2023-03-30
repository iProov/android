// Copyright (c) 2020 iProov Ltd. All rights reserved.
package com.iproov.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.iproov.androidapiclient.javaretrofit.ApiClientJavaRetrofit;
import com.iproov.androidapiclient.javaretrofit.ClaimType;
import com.iproov.example.databinding.ActivityMainBinding;
import com.iproov.sdk.IProov;
import com.iproov.sdk.core.exception.IProovException;
import java.io.IOException;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivityJava extends AppCompatActivity {

    private Button enrolGpaButton;
    private Button verifyGpaButton;
    private Button verifyLaButton;
    private EditText usernameEditText;
    private ProgressBar progressBar;
    private TextView versionTextView;
    private ActivityMainBinding binding;

    private final IProov.Listener listener = new IProov.Listener() {

        @Override
        public void onConnecting() {
            progressBar.setIndeterminate(true);
        }

        @Override
        public void onConnected() {
            progressBar.setIndeterminate(false);
        }

        @Override
        public void onSuccess(IProov.SuccessResult result) {
            onResult(getString(R.string.success), getString(R.string.token_format, result.token));
        }

        @Override
        public void onFailure(IProov.FailureResult result) {
            onResult(result.feedbackCode, result.reason);
        }

        @Override
        public void onProcessing(double progress, String message) {
            progressBar.setProgress((int) (progress * 100.0));
        }

        @Override
        public void onError(IProovException e) {
            onResult(getString(R.string.error), e.getLocalizedMessage());
        }

        @Override
        public void onCancelled() {
            onResult(getString(R.string.cancelled), null);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Constants.API_KEY.isEmpty() || Constants.SECRET.isEmpty()) {
            throw new IllegalStateException("You must set the API_KEY and SECRET values in the Constants.kt file!");
        }

        enrolGpaButton = binding.enrolGpaButton;
        verifyGpaButton = binding.verifyGpaButton;
        verifyLaButton = binding.verifyLaButton;
        usernameEditText = binding.usernameEditText;
        progressBar = binding.progressBar;
        versionTextView = binding.versionTextView;

        View.OnClickListener buttonOnClickListener = v -> {
            String username = usernameEditText.getText().toString();
            if (username.isEmpty()) {
                Toast.makeText(this, getString(R.string.user_id_cannot_be_empty), Toast.LENGTH_LONG).show();
                return;
            }

            ClaimType claimType = (v == verifyGpaButton || v == verifyLaButton) ? ClaimType.VERIFY : ClaimType.ENROL;
            ApiClientJavaRetrofit.AssuranceType assuranceType = (v == enrolGpaButton || v == verifyGpaButton)
                    ? ApiClientJavaRetrofit.AssuranceType.GENUINE_PRESENCE
                    : ApiClientJavaRetrofit.AssuranceType.LIVENESS;

            launchIProov(claimType, username, assuranceType);
        };

        enrolGpaButton.setOnClickListener(buttonOnClickListener);
        verifyGpaButton.setOnClickListener(buttonOnClickListener);
        verifyLaButton.setOnClickListener(buttonOnClickListener);

        versionTextView.setText(String.format(getString(R.string.java_version_format), IProov.getSDKVersion()));

        IProov.registerListener(listener);
    }

    @Override
    protected void onDestroy() {
        IProov.unregisterListener(listener);
        super.onDestroy();
    }

    private void launchIProov(final ClaimType claimType, final String username, final ApiClientJavaRetrofit.AssuranceType assuranceType) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        ApiClientJavaRetrofit apiClient = new ApiClientJavaRetrofit(
                this,
                Constants.BASE_URL,
                HttpLoggingInterceptor.Level.BODY,
                Constants.API_KEY,
                Constants.SECRET);

        apiClient.getToken(
                assuranceType,
                claimType,
                username,
                (call, response) -> {

                    try {
                        IProov.launch(this, Constants.BASE_URL, response.body().getToken());
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (response.errorBody() == null) {
                            onResult(getString(R.string.error), e.getLocalizedMessage());
                            return;
                        }

                        String errorBody = null;

                        try {
                            errorBody = response.errorBody().string();
                            JSONObject json = new JSONObject(errorBody);
                            onResult(getString(R.string.error), json.getString("error_description"));
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            onResult(getString(R.string.error), errorBody);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            onResult(getString(R.string.error), e.getLocalizedMessage());
                        }
                    }
                },
                throwable -> {
                    throwable.printStackTrace();
                    onResult(getString(R.string.error), getString(R.string.failed_to_get_token));
                },
                null);
    }

    private void onResult(final String title, final String resultMessage) {
        progressBar.setVisibility(View.GONE);
        progressBar.setProgress(0);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(resultMessage)

                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> {
                            dialog.cancel();
                        })
                .show();
    }
}
