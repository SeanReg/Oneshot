package com.cop4331;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cop4331.networking.AccountManager;
import com.cop4331.oneshot.R;
import com.cop4331.oneshot.SignupActivity;
import com.parse.Parse;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    private static final int SIGNUP_RESULTS = 1;

    private EditText mEmailText    = null;
    private EditText mPasswordText = null;

    private StatusListener mStatusListener = new StatusListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("kyNJHeJgXmP4K4TxmeaFrU09D0faUvwQ2RSBGv5s")
                .clientKey("uRdkVn6jcjdZF7kMQxKAAK39JpNG98nJFPwfbhwo")
                .server("https://parseapi.back4app.com/").build()
        );

        ((Button)findViewById(R.id.loginButton)).setOnClickListener(mLoginListener);
        ((Button)findViewById(R.id.signupButton)).setOnClickListener(mSignupListener);

        mEmailText    = ((EditText)findViewById(R.id.emailText));
        mPasswordText = ((EditText)findViewById(R.id.passwordText));
    }


    private final Button.OnClickListener mLoginListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            AccountManager manager = AccountManager.getInstance();

            String email    = mEmailText.getText().toString();
            String password = mPasswordText.getText().toString();

            manager.setAccountStatusListener(mStatusListener);
            manager.login(email, password);
        }
    };

    private final Button.OnClickListener mSignupListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent signup = new Intent(getApplicationContext(), SignupActivity.class);
            startActivityForResult(signup, SIGNUP_RESULTS);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGNUP_RESULTS:
                if (ParseUser.getCurrentUser() != null) {
                    //Successful login
                    //Allow into the app
                    finish();
                }
                break;
        }
    }

    private class StatusListener implements AccountManager.onAccountStatus {
        @Override
        public void onLogin(AccountManager.Account account) {

        }

        @Override
        public void onRegistered(AccountManager.Account account) {

        }

        @Override
        public void onLoginError() {
            Log.d("Login: ", "Error Logining in!");
        }

        @Override
        public void onRegistrationError() {

        }
    }
}
