package com.cop4331.networking;

import com.parse.ParseUser;

public class User {
    private String    mUsername     = "";
    private String    mDisplayName  = "";
    private String    mPhoneNumber  = "";
    private ParseUser mParseUser    = null;

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
}