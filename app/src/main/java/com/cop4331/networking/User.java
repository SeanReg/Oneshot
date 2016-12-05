package com.cop4331.networking;

import com.parse.ParseUser;

public class User {
    private   String    mUsername     = "";
    private   String    mDisplayName  = "";
    private   String    mPhoneNumber  = "";
    protected ParseUser mParseUser    = null;

    public User() {
        
    }

    public User(String username, String displayName, String phonenumber) {
        mDisplayName = displayName;
        mUsername    = username;
        mPhoneNumber = phonenumber;
    }

    public User(String username, String displayName, String phonenumber, ParseUser parseUser) {
        mDisplayName = displayName;
        mUsername    = username;
        mPhoneNumber = phonenumber;
        mParseUser   = parseUser;
    }
    
    public String getUsername() {
        return mUsername;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public ParseUser getParseUser() {
        return mParseUser;
    }

    public String getPhoneNumber() { return mPhoneNumber; }

    public int getScore() {
        if (mParseUser != null) {
            //Make sure to get the most recent score occasionally
            if (AccountManager.getInstance().getCurrentAccount() == this)
                mParseUser.fetchInBackground();

            return mParseUser.getInt("score");
        } else {
            return 0;
        }
    }
}