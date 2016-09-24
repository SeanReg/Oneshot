package com.cop4331.networking;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sean on 9/21/2016.
 */
public class Account {
    private RegisterResponse mRegResponse   = null;
    private LoginResponse    mLoginResponse = null;

    private static final String FIELD_EMAIL        = "email";
    private static final String FIELD_PHONE_NUMBER = "phone";

    public interface RegisterResponse {
        public void onRegistered();
        public void onError();
    }

    public interface LoginResponse {
        public void onLoggedIn();
        public void onError();
    }

    public Account() {

    }

    public void logIn(String email, String password, LoginResponse response) {
        mLoginResponse = response;

        if (isSignedIn()) logOut();

        try {
            ParseUser.logIn(email, password);//logInInBackground(email, password, mLoginCallback);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void requestFriendByEmail(String email) {
        requestFriend(FIELD_EMAIL, email);
    }

    public void requestFriendByNumber(String number) {
        requestFriend(FIELD_PHONE_NUMBER, number);
    }

    public void getFriendRequests() {
        getFriendship(0);
    }

    private void getFriendship(int friendshipType) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FriendRequests");
        query.whereEqualTo("toUser", ParseUser.getCurrentUser());
        query.whereEqualTo("status", friendshipType);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

            }
        });
    }

    private void requestFriend(String fieldType, String compare) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(fieldType, compare);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() == 0) return;

                    final ParseUser reqUser = objects.get(0);

                    //Query current requests that the current user has sent to the potential friend
                    ParseQuery<ParseObject> fromQ = ParseQuery.getQuery("FriendRequests");
                    fromQ.whereEqualTo("toUser", ParseUser.getCurrentUser());
                    fromQ.whereEqualTo("fromUser", reqUser);

                    //Query current requests that the potential frient has sent to the current user
                    ParseQuery<ParseObject> toQ = ParseQuery.getQuery("FriendRequests");
                    toQ.whereEqualTo("toUser", reqUser);
                    toQ.whereEqualTo("fromUser", ParseUser.getCurrentUser());

                    //Perform the complex query
                    ArrayList<ParseQuery<ParseObject>> orList = new ArrayList<ParseQuery<ParseObject>>();
                    orList.add(fromQ);
                    orList.add(toQ);
                    ParseQuery<ParseObject> rQuery = ParseQuery.or(orList);

                    rQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                if (objects.size() == 0) {
                                    //No friend requests exist from either side
                                    //Create a new one
                                    ParseObject friendReq = new ParseObject("FriendRequests");

                                    //From user to user and status
                                    friendReq.put("fromUser", ParseUser.getCurrentUser());
                                    friendReq.put("toUser", reqUser);

                                    //Status 0 for pending - 1 for accepted
                                    friendReq.put("status", 0);

                                    //Update database
                                    friendReq.saveInBackground();
                                } else {
                                    //Should only have find 1 or less
                                    ParseObject pendingRequest = objects.get(0);
                                    String fromObjId = pendingRequest.getParseUser("fromUser").getObjectId();
                                    String curObjId  = ParseUser.getCurrentUser().getObjectId();

                                    //Check if the other user has requested us as a friend
                                    if (fromObjId.compareTo(curObjId) != 0 ) {
                                        //Other user made this request, so accept it!
                                        pendingRequest.put("status", 1);
                                        pendingRequest.saveInBackground();
                                    }
                                }
                            } else {

                            }
                        }
                    });

                } else {

                }
            }
        });
    }

    public boolean isSignedIn() {
        return (ParseUser.getCurrentUser() != null);
    }

    public void logOut() {
        ParseUser.logOut();
    }

    public void register(String email, String password, String phone, RegisterResponse response) {
        mRegResponse = response;

        ParseUser user = new ParseUser();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(password);
        user.put(FIELD_PHONE_NUMBER, phone);

        user.signUpInBackground(mRegisterCallback);
    }

    private final SignUpCallback mRegisterCallback = new SignUpCallback() {
        @Override
        public void done(ParseException e) {
            if (mRegResponse != null) {
                if (e != null) {
                    mRegResponse.onRegistered();
                } else {
                    mRegResponse.onError();
                }
            }
        }
    };

    private final LogInCallback mLoginCallback = new LogInCallback() {
        public void done(ParseUser user, ParseException e) {
            if (mLoginResponse != null) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    mLoginResponse.onLoggedIn();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    mLoginResponse.onError();
                }
            }
        }
    };


}
