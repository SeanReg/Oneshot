package com.cop4331.oneshot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;
import com.parse.ParseException;

/**
 * Signup Activity responsible for user registration
 */
public class SignupActivity extends AppCompatActivity {


    private static final int DUPLICATE_ACCOUNT = 202;
    private EditText mUsername          = null;
    private EditText mDisplayName       = null;
    private EditText mPhoneNumber       = null;
    private EditText mPassword          = null;
    private EditText mConfirmedPassword = null;
    private TextView mError             = null;

    StringBuilder sb = new StringBuilder();

    private StatusListener mStatusListener = new StatusListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ((Button)findViewById(R.id.registerButton)).setOnClickListener(mSignupListener);

        mUsername           = ((EditText)findViewById(R.id.usernameText));
        mDisplayName        = ((EditText)findViewById(R.id.displayNameText));
        mPhoneNumber        = ((EditText)findViewById(R.id.phoneNumberText));
        mPassword           = ((EditText)findViewById(R.id.passwordText));
        mConfirmedPassword  = ((EditText)findViewById(R.id.confirmPasswordText));
        mError              = ((TextView)findViewById(R.id.errorText));
    }

    /**
     * Listener for when Signup button is clicked
     */
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

            // Error checking
            sb.setLength(0);
            username =  username.replaceAll(" ", "");
            phoneNumber = phoneNumber.replaceAll("\\D+","");
            if(username.length() < 1) {
                sb.append("Invalid username.\n");
            }
            if(displayName.trim().length() < 1) {
                displayName = username;
            }
            if(phoneNumber.length() != 10) {
                sb.append("Invalid phone number.\n");
            }
            if(password.length() < 8) {
                sb.append("Password must be minimum of 8 characters.\n");
            }
            if(!password.equals(confirmedPassword)) {
                sb.append("Passwords do not match.\n");
            }
            if (sb.length() > 0) {
                mStatusListener.onRegistrationError(sb.toString());
            } else {
                manager.register(username, password, displayName, phoneNumber);
            }
        }
    };

    /**
     * Listener for registration status
     */
    private class StatusListener implements AccountManager.onAccountStatus {
        @Override
        public void onLogin(AccountManager.Account account) {

        }

        @Override
        public void onRegistered(AccountManager.Account account) {
            //Account registered so close the activity
            finish();
        }

        @Override
        public void onLoginError(ParseException e) {

        }

        @Override
        public void onRegistrationError(ParseException e) {
            //Registration errors
            if(e.getCode() == DUPLICATE_ACCOUNT) {
                onRegistrationError(sb.append(e.getMessage()).toString());
            }
            if (e.getMessage().contains("java.lang")){
                return;
            }
        }

        /**
         * On registration error.
         *
         * @param error the error
         */
        public void onRegistrationError(String error) {
            mError.setText(error);
        }
    }
}
