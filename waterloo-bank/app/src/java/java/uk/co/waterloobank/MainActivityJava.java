package uk.co.waterloobank;

import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = MainActivityJava.class.getSimpleName();

    private Button loginButton;
    private Button registerButton;
    private EditText userNameEditText;
    private ProgressBar progressBar;
    private TextView captureStatus;
    private TextView textViewVersion;
    private Constants constants;

    private IProov.Listener listener = new IProov.Listener() {

        @Override
        public void onConnecting() {
            Log.w(TAG, "Connecting");
        }

        @Override
        public void onConnected() {
            Log.w(TAG, "Connected");
        }

        @Override
        public void onSuccess(String token) {
            onResult("Success", "Successfully iProoved.\nToken:" + token);
        }

        @Override
        public void onFailure(String reason, String feedback) {
            onResult("Failed", "Failed to register\nreason: " + reason + "feedback: " + feedback);
        }

        @Override
        public void onProcessing( double progress, String message) {
            onProcessingUpdate(message, (int) progress * 100);
        }

        @Override
        public void onError(IProovException e) {
            onResult("Error", "Error: " + e.getLocalizedMessage());
        }

        @Override
        public void onCancelled() {
            onResult("Cancelled", "User action: cancelled");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.constants = new Constants(this);

        // Demonstration of library to change all strings
        // Lingver.getInstance().setLocale(this, "fr");

        userNameEditText = findViewById(R.id.usernameEditText);
        progressBar = findViewById(R.id.progressBar);
        captureStatus = findViewById(R.id.captureStatus);
        textViewVersion = findViewById(R.id.textViewVersion);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            String userId = userNameEditText.getText().toString();
            if (userId.isEmpty()) {
                Toast.makeText(this, "User ID can't be empty", Toast.LENGTH_LONG).show();
                return;
            }

            login(userId);
        });

        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            String userId = userNameEditText.getText().toString();
            if (userId.isEmpty()) {
                Toast.makeText(this, "User ID can't be empty", Toast.LENGTH_LONG).show();
                return;
            }

            register(userNameEditText.getText().toString());
        });

        textViewVersion.setText("Java using SDK Version [" + IProov.getSDKVersion() + "]");

        Log.w(TAG, "registerListener");
        IProov.registerListener(listener);
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "unregisterListener");
        IProov.unregisterListener();
        super.onDestroy();
    }

    private void hideLoadingViews() {
        progressBar.setVisibility(View.GONE);
        progressBar.setProgress(0);
        captureStatus.setVisibility(View.GONE);
    }

    private void hideButtons() {
        loginButton.setVisibility(View.GONE);
        registerButton.setVisibility(View.GONE);
    }

    private void showLoadingViews() {
        progressBar.setVisibility(View.VISIBLE);
        captureStatus.setVisibility(View.VISIBLE);
    }

    private void showButtons() {
        loginButton.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.VISIBLE);
    }

    private void login(final String userID) {
        hideButtons();
        showLoadingViews();

        ApiClientJavaRetrofit apiClient = new ApiClientJavaRetrofit(
                this,
                Constants.BASE_URL,
                HttpLoggingInterceptor.Level.BODY,
                constants.getApiKey(),
                constants.getSecret());

        apiClient.getToken(
                ApiClientJavaRetrofit.AssuranceType.GENUINE_PRESENCE,
                ClaimType.VERIFY,
                userID,
                (call, response) -> {
                    IProov.launch(this, response.body().getToken(), createOptions());
                },
                throwable -> {
                    onResult("Filed", "Failed to get token.");
                });
    }

    private void register(final String userID) {
        hideButtons();
        showLoadingViews();

        ApiClientJavaRetrofit apiClient = new ApiClientJavaRetrofit(
                this,
                Constants.BASE_URL,
                HttpLoggingInterceptor.Level.BODY,
                constants.getApiKey(),
                constants.getSecret());

        apiClient.getToken(
                ApiClientJavaRetrofit.AssuranceType.GENUINE_PRESENCE,
                ClaimType.ENROL,
                userID,
                (call, response) -> {
                    IProov.launch(this, response.body().getToken(), createOptions());
                },
                throwable -> {
                    onResult("Failed", "Failed to get token.");
                });
    }

    private void onResult(final String title, final String resultMessage) {
        Log.w(TAG, "onResult");
        hideLoadingViews();
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(resultMessage)
                .setPositiveButton("OK", ((dialog, which) -> showButtons()))
                .setCancelable(false)
                .show();
    }

    private void onProcessingUpdate(final String status, final int progressValue) {
        captureStatus.setText(status);
        progressBar.setProgress(progressValue);
    }

    private IProov.Options createOptions() {
        IProov.Options options = new IProov.Options();
        options.ui.autoStartDisabled = false;
        options.ui.logoImageResource = R.mipmap.ic_launcher;
        return options;
    }
}
