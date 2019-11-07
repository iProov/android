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
import com.iproov.sdk.IProovException;
import com.iproov.sdk.model.Claim;

import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivityJava extends AppCompatActivity {

    private IProov.IProovConnection connection;
    private Button loginButton;
    private Button registerButton;
    private EditText userNameEditText;
    private ProgressBar progressBar;
    private TextView captureStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameEditText = findViewById(R.id.usernameEditText);
        progressBar = findViewById(R.id.progressBar);
        captureStatus = findViewById(R.id.captureStatus);

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

        connection = IProov.getIProovConnection(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.stop();
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
                Constants.API_KEY,
                Constants.SECRET);

        apiClient.getToken(
                ClaimType.VERIFY,
                userID,
                (call, response) -> {
                    startIproovForVerifyClaim(response.body().getToken(), Claim.ClaimType.VERIFY);
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
                Constants.API_KEY,
                Constants.SECRET);

        apiClient.getToken(
                ClaimType.ENROL,
                userID,
                (call, response) -> {
                    startIproovForEnrollrClaim(response.body().getToken(), Claim.ClaimType.ENROL);
                },
                throwable -> {
                    onResult("Filed", "Failed to get token.");
                });
    }

    private void startIproovForEnrollrClaim(final String token, final Claim.ClaimType claimType) {
        connection.launch(createOptions(), token, new IProov.IProovCaptureListener() {

            @Override
            public void onSuccess(String token) {
                onResult("Success", "Successfully registered.\nToken:" + token);
            }

            @Override
            public void onFailure(String reason, String feedback) {
                onResult("Failed", "Failed to register\nreason: " + reason + "feedback: " + feedback);
            }

            @Override
            public void onProgressUpdate(String message, double progress) {
                onProgress(message, (int) progress * 100);
            }

            @Override
            public void onError(IProovException e) {
                onResult("Error", "Error: " + e.getLocalizedMessage());
            }

            @Override
            public void onCancelled() {
                onResult("Cancelled", "User action: cancelled");
            }
        });
    }

    private void startIproovForVerifyClaim(final String token, final Claim.ClaimType claimType) {
        connection.launch(createOptions(), token, new IProov.IProovCaptureListener() {

            @Override
            public void onSuccess(String token) {
                onResult("Success", "Successfully iProoved.\nToken:" + token);
            }

            @Override
            public void onFailure(String reason, String feedback) {
                onResult("Failed", "Failed to iProov\nreason: " + reason + "feedback: " + feedback);
            }

            @Override
            public void onProgressUpdate(String message, double progress) {
                onProgress(message, (int) progress * 100);
            }

            @Override
            public void onError(IProovException e) {
                onResult("Error", "Error: " + e.getLocalizedMessage());
            }

            @Override
            public void onCancelled() {
                onResult("Cancelled", "User action: cancelled");
            }
        });
    }

    private void onResult(final String title, final String resultMessage) {
        hideLoadingViews();
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(resultMessage)
                .setPositiveButton("OK", ((dialog, which) -> showButtons()))
                .setCancelable(false)
                .show();
    }

    private void onProgress(final String status, final int progressValue) {
        captureStatus.setText(status);
        progressBar.setProgress(progressValue);
    }

    private IProov.Options createOptions() {
        return new IProov.Options()
                .ui.setAutoStartDisabled(false)
                .ui.setLogoImage(R.mipmap.ic_launcher)
                .ui.setFontAsset("Merriweather-Bold.ttf");
    }
}
