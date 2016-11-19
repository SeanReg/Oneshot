package com.cop4331.networking;

public class User {
    private String mUsername     = "";
    private String mDisplayName  = "";

    public User() {
        
    }

    public User(String username, String displayName) {
        mDisplayName = displayName;
        mUsername    = username;
    }
    
    public String getUsername() {
        return mUsername;
    }

    public String getDisplayName() {
        return mDisplayName;
    }
}