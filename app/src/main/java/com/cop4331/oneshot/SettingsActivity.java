package com.cop4331.oneshot;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;

public class SettingsActivity extends Fragment implements View.OnClickListener {

    private View mView                  = null;
    private Button mSaveButton          = null;
    private TextView mResult            = null;
    private EditText mUsername          = null;
    private EditText mDisplayName       = null;
    private EditText mPhoneNumber       = null;
    AccountManager manager = AccountManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView               = inflater.inflate(R.layout.settings_layout,null);
        mSaveButton         = (Button)mView.findViewById(R.id.saveButton);
        mResult             = (TextView)mView.findViewById(R.id.resultText);
        mUsername           = (EditText)mView.findViewById(R.id.usernameDisplay);
        mDisplayName        = (EditText)mView.findViewById(R.id.displayNameText);
        mPhoneNumber        = (EditText)mView.findViewById(R.id.phoneNumberText);

        mUsername.setText(manager.getCurrentAccount().getUsername());
        mDisplayName.setText(manager.getCurrentAccount().getDisplayName());
        mPhoneNumber.setText(manager.getCurrentAccount().getPhoneNumber());
        mSaveButton.setOnClickListener(this);
        return mView;
    }

    @Override
    public void onClick(View v) {
        String username             = mUsername.getText().toString();
        String displayName          = mDisplayName.getText().toString();
        String phoneNumber          = mPhoneNumber.getText().toString();
        phoneNumber = phoneNumber.replaceAll("\\D+","");
        if(phoneNumber.length() != 10) {
            mResult.setTextColor(Color.RED);
            mResult.setText("Invalid phone number.");
            return;
        }
        if(displayName.trim().length() < 1) {
            displayName = username;
        }
        manager.getCurrentAccount().setDisplayName(displayName);
        manager.getCurrentAccount().setPhoneNumber(phoneNumber);
        manager.getCurrentAccount().updateAccount();
        mResult.setText("Update successful!")  ;
    }

}
