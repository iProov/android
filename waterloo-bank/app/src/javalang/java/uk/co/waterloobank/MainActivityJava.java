package uk.co.waterloobank;

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
import com.iproov.sdk.IProov;
import com.iproov.sdk.core.exception.IProovException;

import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivityJava extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private EditText usernameEditText;
    private ProgressBar progressBar;
    private TextView statusTextView;

    private final IProov.Listener listener = new IProov.Listener() {

        @Override
        public void onConnecting() {
            statusTextView.setText(getString(R.string.connecting));
            progressBar.setIndeterminate(true);
        }

        @Override
        public void onConnected() {
            statusTextView.setText(getString(R.string.connected));
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
            statusTextView.setText(message);
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
        setContentView(R.layout.activity_main);

        if (Constants.API_KEY.isEmpty() || Constants.SECRET.isEmpty()) {
            throw new IllegalStateException("You must set the API_KEY and SECRET values in the Constants.kt file!");
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        View.OnClickListener buttonOnClickListener = v -> {
            String username = usernameEditText.getText().toString();
            if (username.isEmpty()) {
                Toast.makeText(this, getString(R.string.user_id_cannot_be_empty), Toast.LENGTH_LONG).show();
                return;
            }

            ClaimType claimType = (v == loginButton) ? ClaimType.VERIFY : ClaimType.ENROL;

            launchIProov(claimType, username);
        };

        loginButton.setOnClickListener(buttonOnClickListener);
        registerButton.setOnClickListener(buttonOnClickListener);

        TextView versionTextView = findViewById(R.id.versionTextView);
        versionTextView.setText(String.format(getString(R.string.java_version_format), IProov.getSDKVersion()));

        IProov.registerListener(listener);
    }

    @Override
    protected void onDestroy() {
        IProov.unregisterListener(listener);
        super.onDestroy();
    }

    private void launchIProov(final ClaimType claimType, final String username) {
        loginButton.setVisibility(View.GONE);
        registerButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setVisibility(View.VISIBLE);

        statusTextView.setText(getString(R.string.getting_token));
        progressBar.setIndeterminate(true);

        ApiClientJavaRetrofit apiClient = new ApiClientJavaRetrofit(
                this,
                Constants.BASE_URL,
                HttpLoggingInterceptor.Level.BODY,
                Constants.API_KEY,
                Constants.SECRET);

        apiClient.getToken(
                ApiClientJavaRetrofit.AssuranceType.GENUINE_PRESENCE,
                claimType,
                username,
                (call, response) -> {

                    IProov.Options options = new IProov.Options();
                    options.ui.logoImageResource = R.mipmap.ic_launcher;

                    try {
                        IProov.launch(this, Constants.BASE_URL, response.body().getToken(), options);
                    } catch (Exception e) {
                        e.printStackTrace();
                        onResult(getString(R.string.error), e.getLocalizedMessage());
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
        statusTextView.setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(resultMessage)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    loginButton.setVisibility(View.VISIBLE);
                    registerButton.setVisibility(View.VISIBLE);
                })
                .show();
    }

}
