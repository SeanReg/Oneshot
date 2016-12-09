package com.cop4331;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.oneshot.R;
import com.cop4331.oneshot.SignupActivity;
import com.parse.*;

/**
 * The type Login activity.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int SIGNUP_RESULTS = 1;

    private EditText mUsername      = null;
    private EditText mPasswordText  = null;
    private TextView mError         = null;

    private StatusListener mStatusListener = new StatusListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((Button)findViewById(R.id.loginButton)).setOnClickListener(mLoginListener);
        ((TextView)findViewById(R.id.signupButton)).setOnClickListener(mSignupListener);

        mUsername       = ((EditText)findViewById(R.id.usernameText));
        mPasswordText   = ((EditText)findViewById(R.id.passwordText));
        mError          = ((TextView) findViewById(R.id.errorText));
    }


    private final Button.OnClickListener mLoginListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            AccountManager manager = AccountManager.getInstance();

            String username = mUsername.getText().toString();
            String password = mPasswordText.getText().toString();

            manager.setAccountStatusListener(mStatusListener);
            manager.login(username, password);
        }
    };

    private final TextView.OnClickListener mSignupListener = new TextView.OnClickListener() {
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
            finish();
        }

        @Override
        public void onRegistered(AccountManager.Account account) {

        }

        @Override
        public void onLoginError(ParseException e) {
            mError.setText(e.getMessage());
        }

        @Override
        public void onRegistrationError(ParseException e) {

        }
    }
}
