package com.cop4331.networking;

public class User {
    private String mUsername     = "";
    private String mDisplayName  = "";
    private String mPhoneNumber  = "";

    public User() {
        
    }

    public User(String username, String displayName, String phonenumber) {
        mDisplayName = displayName;
        mUsername    = username;
        mPhoneNumber = phonenumber;
    }
    
    public String getUsername() {
        return mUsername;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getPhoneNumber() { return mPhoneNumber; }
}