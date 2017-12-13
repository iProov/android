package uk.co.waterloobank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.iproov.sdk.IProov;

public class MainActivity extends Activity {

    private static final String WB_SERVICE_PROVIDER = null; //TODO: place your API key here

    private Button loginButton;
    private Button registerButton;
    private EditText usernameEditText;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("uk.co.waterloobank", Context.MODE_PRIVATE);

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);

        String username = sharedPreferences.getString("username", null);
        usernameEditText.setText(username);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = usernameEditText.getText().toString();

                if (username.length() == 0) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage("Please enter your username/email to login.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                sharedPreferences.edit().putString("username", username).apply();
                login(username);

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();

                if (username.length() == 0) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage("Please enter your username/email to register.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                sharedPreferences.edit().putString("username", username).apply();
                register(username);
            }
        });

    }

    private void login(String username) {

        if(WB_SERVICE_PROVIDER == null || WB_SERVICE_PROVIDER.isEmpty()){
            new AlertDialog.Builder(this)
                    .setTitle("Missing API key")
                    .setMessage("Please edit WB_SERVICE_PROVIDER in MainActivity.java")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        Intent i = new IProov.NativeClaim.Builder(this)
                .setMode(IProov.Mode.Verify)
                .setUsername(username)
                .setServiceProvider(WB_SERVICE_PROVIDER)
                .getIntent();
        startActivityForResult(i, 0);
    }

    private void register(String username) {

        if(WB_SERVICE_PROVIDER == null || WB_SERVICE_PROVIDER.isEmpty()){
            new AlertDialog.Builder(this)
                    .setTitle("Missing API key")
                    .setMessage("Please edit WB_SERVICE_PROVIDER in MainActivity.java")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        Intent i = new IProov.NativeClaim.Builder(this)
                .setMode(IProov.Mode.Enrol)
                .setUsername(username)
                .setServiceProvider(WB_SERVICE_PROVIDER)
                .getIntent();
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == IProov.RESULT_SUCCESS) {

            String encryptedToken = data.getStringExtra(IProov.EXTRA_ENCRYPTED_TOKEN);

            Intent intent = new Intent(this, AccountActivity.class);
            intent.putExtra("token", encryptedToken);
            startActivity(intent);

            usernameEditText.setText(null);

        } else {

            final IProov.Mode mode;

            if(data != null) {
                mode = (IProov.Mode) data.getSerializableExtra(IProov.EXTRA_MODE);
            }
            else{
                mode = IProov.Mode.Device;
            }
            String title = (mode == IProov.Mode.Verify) ? "Login failed" : "Registration failed";
            String message = null;

            if (resultCode == IProov.RESULT_FAILURE) {
                message = data.getStringExtra(IProov.EXTRA_REASON);

            } else if (resultCode == IProov.RESULT_ERROR) {
                Exception e = (Exception) data.getSerializableExtra(IProov.EXTRA_EXCEPTION);
                message = e.getLocalizedMessage();

            }

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String username = usernameEditText.getText().toString();
                            if (mode == IProov.Mode.Verify) login(username);
                            else register(username);
                        }
                    })
                    .show();

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
