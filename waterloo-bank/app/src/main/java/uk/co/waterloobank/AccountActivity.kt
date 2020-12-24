package uk.co.waterloobank

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_account)

        title = "Your Account"

        tokenTextView.text = "Your token is: ${intent.getStringExtra("token")}";
    }
}
