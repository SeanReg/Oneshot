package com.cop4331.networking;

import com.parse.ParseUser;

/**
 * Immutable class that contains information about a User
 */
public class User {
    private   String    mUsername     = "";
    private   String    mDisplayName  = "";
    private   String    mPhoneNumber  = "";
    /**
     * The M parse user.
     */
    protected ParseUser mParseUser    = null;

    /**
     * Instantiates a new User.
     */
    public User() {
        
    }

    /**
     * Instantiates a new User.
     * @param username    the username of the user
     * @param displayName the display name of the user
     * @param phonenumber the phonenumber of the user
     */
    public User(String username, String displayName, String phonenumber) {
        mDisplayName = displayName;
        mUsername    = username;
        mPhoneNumber = phonenumber;
    }

    /**
     * Instantiates a new User.
     * @param username    the username of the user
     * @param displayName the display name of the user
     * @param phonenumber the phonenumber of the user
     * @param parseUser   the ParseUser object of the user
     */
    public User(String username, String displayName, String phonenumber, ParseUser parseUser) {
        mDisplayName = displayName;
        mUsername    = username;
        mPhoneNumber = phonenumber;
        mParseUser   = parseUser;
    }

    /**
     * Gets the username of the user
     * @return the username
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Gets the display name of the user
     * @return the display name
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Gets ParseUser object for the user
     * @return the ParseUser
     */
    public ParseUser getParseUser() {
        return mParseUser;
    }

    /**
     * Gets the phone number for the user
     * @return the phone number
     */
    public String getPhoneNumber() { return mPhoneNumber; }

    /**
     * Gets score of the user
     * @return the score
     */
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