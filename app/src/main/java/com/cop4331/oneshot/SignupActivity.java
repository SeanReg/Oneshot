package com.cop4331.oneshot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;
import com.parse.Parse;
import com.parse.ParseException;

public class SignupActivity extends AppCompatActivity {


    private static final int DUPLICATE_ACCOUNT = 202;
    private EditText mUsername          = null;
    private EditText mDisplayName       = null;
    private EditText mPhoneNumber       = null;
    private EditText mPassword          = null;
    private EditText mConfirmedPassword = null;
    private TextView mError             = null;

    private StatusListener mStatusListener = new StatusListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ((Button)findViewById(R.id.registerButton)).setOnClickListener(mSignupListener);

        mUsername    = ((EditText)findViewById(R.id.usernameText));
        mDisplayName = ((EditText)findViewById(R.id.displayNameText));
        mPhoneNumber    = ((EditText)findViewById(R.id.phoneNumberText));
        mPassword = ((EditText)findViewById(R.id.passwordText));
        mConfirmedPassword = ((EditText)findViewById(R.id.confirmPasswordText));
        mError = ((TextView)findViewById(R.id.errorText));
    }

    private final Button.OnClickListener mSignupListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            AccountManager manager = AccountManager.getInstance();

            String username             = mUsername.getText().toString();
            String displayName          = mDisplayName.getText().toString();
            String phoneNumber          = mPhoneNumber.getText().toString();
            String password             = mPassword.getText().toString();
            String confirmedPassword    = mConfirmedPassword.getText().toString();

            manager.setAccountStatusListener(mStatusListener);
            manager.register(username, password, phoneNumber);
        }
    };

    private class StatusListener implements AccountManager.onAccountStatus {
        @Override
        public void onLogin(AccountManager.Account account) {

        }

        @Override
        public void onRegistered(AccountManager.Account account) {
            finish();
        }

        @Override
        public void onLoginError() {

        }

        @Override
        public void onRegistrationError(ParseException e) {
            if(e.getCode() == DUPLICATE_ACCOUNT) {
                mError.setText("Username taken.");
            } else {
                Log.d("", "" + e.getCode());
            }
        }
    }
}
