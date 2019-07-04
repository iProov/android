package uk.co.waterloobank;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.iproov.androidapiclient.kotlinfuel.ApiClientFuel;
import com.iproov.sdk.IProov;
import com.iproov.sdk.IProovException;
import com.iproov.sdk.model.Claim;

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
        loginButton.setOnClickListener(v -> login(userNameEditText.getText().toString()));

        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> register(userNameEditText.getText().toString()));

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
//        hideButtons();
//        showLoadingViews();
//
//        ApiClientFuel apiClientFuel = new ApiClientFuel(
//                this,
//                "https://eu.rp.secure.iproov.me/api/v2/",
//                "0b7f668c0c3295056e574fcb973a58a2e68fe196",
//                "ac3057d5f5f6cde818a11c50633c416ad8488ae9"
//        );
//
//        final String token = apiClientFuel.getToken();
//
//        connection.launch(createOptions(), token, new IProov.IProovCaptureListener() {
//
//            @Override
//            public void onSuccess(String token) {
//                onResult("Success", "Successfully iProoved.\nToken:" + token);
//            }
//
//            @Override
//            public void onFailure(String reason, String feedback) {
//                onResult("Failed", "Failed to iProov\nreason: " + reason + "feedback: " + feedback);
//            }
//
//            @Override
//            public void onProgressUpdate(String message, double progress) {
//                onProgress(message, (int) progress);
//            }
//
//            @Override
//            public void onError(IProovException e) {
//                onResult("Error", "Error: " + e.getLocalizedMessage());
//            }
//
//            @Override
//            public void log(String title, String message) {
//                //You can add logging here
//            }
//        });
    }

    private void register(final String userID) {
//        hideButtons();
//        showLoadingViews();
//
//        ApiClientFuel apiClientFuel = new ApiClientFuel(
//                this,
//                "https://eu.rp.secure.iproov.me/api/v2/",
//                "0b7f668c0c3295056e574fcb973a58a2e68fe196",
//                "ac3057d5f5f6cde818a11c50633c416ad8488ae9"
//        );
//
//        final String token = apiClientFuel.getToken();
//
//        connection.launch(createOptions(), token, new IProov.IProovCaptureListener() {
//
//            @Override
//            public void onSuccess(String token) {
//                onResult("Success", "Successfully registered.\nToken:" + token);
//            }
//
//            @Override
//            public void onFailure(String reason, String feedback) {
//                onResult("Failed", "Failed to register\nreason: " + reason + "feedback: " + feedback);
//            }
//
//            @Override
//            public void onProgressUpdate(String message, double progress) {
//                onProgress(message, (int) progress);
//            }
//
//            @Override
//            public void onError(IProovException e) {
//                onResult("Error", "Error: " + e.getLocalizedMessage());
//            }
//
//            @Override
//            public void log(String title, String message) {
//                //You can add logging here
//            }
//        });
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
        final IProov.Options options = new IProov.Options();
        options.setAutostart(true);
        options.setLogoImage( R.mipmap.ic_launcher);
        options.setBoldFont("Merriweather-Bold.ttf");
        return options;
    }
}