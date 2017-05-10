package uk.co.waterloobank;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AccountActivity extends Activity {

    private TextView tokenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        setTitle("Your Account");

        String token = getIntent().getStringExtra("token");

        tokenTextView = (TextView) findViewById(R.id.tokenTextView);
        tokenTextView.setText("Your token is: " + token);
    }
}
