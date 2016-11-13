package com.cop4331.networking;

import android.util.Log;

import com.cop4331.networking.Relationship;
import com.cop4331.networking.User;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private Account        mCurrAcc = null;

    private static final String FIELD_EMAIL        = "email";
    private static final String FIELD_PHONE_NUMBER = "phone";

    private onAccountStatus mAccountStatusListener = null;

    private static AccountManager mManager = new AccountManager();

    public interface onAccountStatus {
        public void onLogin(Account account);
        public void onRegistered(Account account);
        
        public void onLoginError();
        public void onRegistrationError(ParseException e);
    }
    
    public static AccountManager getInstance() {
        return mManager;
    }

    public void login(String username, String password) {
       // if (isSignedIn()) logOut();

        try {
            ParseUser.logInInBackground(username, password, mLoginCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(String username, String password, String phone) {
        ParseUser user = new ParseUser();
        //user.setEmail(username);
        user.setUsername(username);
        user.setPassword(password);
        user.put(FIELD_PHONE_NUMBER, phone);

        user.signUpInBackground(mRegisterCallback);
    }

    public void setAccountStatusListener(onAccountStatus listener) {
        mAccountStatusListener = listener;
    }

    public boolean isLoggedIn() {
        return (ParseUser.getCurrentUser() != null);
    }

	public List<Relationship> getRelationships() {
	    return null;
	}
    
    public Account getCurrentAccount() {
        return mCurrAcc;
    }

    private final SignUpCallback mRegisterCallback = new SignUpCallback() {
        @Override
        public void done(ParseException e) {
            if (mAccountStatusListener != null) {
                if (e == null) {
                    mCurrAcc = new Account(ParseUser.getCurrentUser());
                    mAccountStatusListener.onRegistered(mCurrAcc);
                } else {
                    mAccountStatusListener.onRegistrationError(e);
                }
            }
        }
    };

    private final LogInCallback mLoginCallback = new LogInCallback() {
        public void done(ParseUser user, ParseException e) {
            if (mAccountStatusListener != null) {
                if (user != null) {
                    // Hooray! The user is logged in
                    mCurrAcc = new Account(ParseUser.getCurrentUser());
                    mAccountStatusListener.onLogin(mCurrAcc);
                } else {
                    // Login failed. Look at the ParseException to see what happened.
                    mAccountStatusListener.onLoginError();
                }
            }
        }
    };

    public static class Account extends User {
        private static final int REQUEST_EMAIL = 0;
        private static final int REQUEST_PHONE = 1;

        private final ParseUser mUser;

        private QueryListener mQuerylistener;

		public interface QueryListener {
			public void onGotScore(long score);
			public void onGotRelationships(List<Relationship> relationships);
			public void onGotGames(List<Game> games);

			public void onError();
		}

        private Account(ParseUser user) {
            mUser = user;
        }
        
        public void getCurrentGames() {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Games");
            query.whereEqualTo("owner", mUser);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        final ArrayList<Game> games = new ArrayList<Game>();
                        for (ParseObject pO : objects) {
                            games.add(parseToGame(pO));
                        }

                        ParseQuery<ParseObject> qParticipant = ParseQuery.getQuery("Games");
                        qParticipant.whereEqualTo("players", mUser);
                        qParticipant.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> participating, ParseException e) {
                                if (e == null) {
                                    for (ParseObject pO : participating) {
                                        games.add(parseToGame(pO));
                                    }

                                    if (mQuerylistener != null) {
                                        mQuerylistener.onGotGames(games);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }

        public void setQuerylistener(QueryListener listener) {
            mQuerylistener = listener;
        }

        private Game parseToGame(ParseObject obj) {
            Game.Builder builder = new Game.Builder();
            builder.setTimelimit((int)obj.get("timelimit"));
            builder.setPrompt((String)obj.get("prompt"));
            builder.setDatabaseId(obj.getObjectId());

            return builder.build(new User());
        }

        public void startGame(Game.Builder builder) {
            Game newGame = builder.build(this);

            //Need to push this game to the databse now
            ParseObject pGame = new ParseObject("Games");
            pGame.put("owner", ParseUser.getCurrentUser());
            pGame.put("prompt", newGame.getPrompt());
            pGame.put("timelimit", newGame.getTimeLimit());
            ParseRelation relation = pGame.getRelation("players");

            pGame.saveInBackground();
        }
        
        public void getRelationships(QueryListener listener) {

        }
        
		public void getScore(QueryListener listener) {
			
		}

        public void requestFriendByNumber(String phonenumber) {
            
        }
        
        public void requestFriendByEmail(String email) {
            
        }
        
        private void requestFriend(int requestType, String reqString) { 
            
        }
        
        public void logout() {
            if (ParseUser.getCurrentUser() == mUser) {
                ParseUser.logOut();
            }
        }
    }
}