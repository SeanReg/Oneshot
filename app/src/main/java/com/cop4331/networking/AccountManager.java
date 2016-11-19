package com.cop4331.networking;

import android.util.Log;

import com.cop4331.networking.Relationship;
import com.cop4331.networking.User;
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
import java.util.List;

public class AccountManager {
    private Account        mCurrAcc = null;

    private static final String FIELD_USERNAME_CASE  = "usernameCase";
    private static final String FIELD_USERNAME       = "username";
    private static final String FIELD_PHONE_NUMBER   = "phone";

    private onAccountStatus mAccountStatusListener = null;

    private static AccountManager mManager = new AccountManager();

    public interface onAccountStatus {
        public void onLogin(Account account);
        public void onRegistered(Account account);
        
        public void onLoginError(ParseException e);
        public void onRegistrationError(ParseException e);
    }
    
    public static AccountManager getInstance() {
        if (mManager.isLoggedIn()) mManager.mCurrAcc = new Account(ParseUser.getCurrentUser());

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
        user.setUsername(username.toLowerCase());
        user.setPassword(password);
        user.put(FIELD_PHONE_NUMBER, phone);
        user.put(FIELD_USERNAME_CASE, username);

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
                    mAccountStatusListener.onLoginError(e);
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

            ParseQuery fromQuery = ParseQuery.getQuery("Relationships");
            fromQuery.whereEqualTo("from", ParseUser.getCurrentUser());

            ParseQuery toQuery = ParseQuery.getQuery("Relationships");
            toQuery.whereEqualTo("to", ParseUser.getCurrentUser());

            ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
            queries.add(fromQuery);
            queries.add(toQuery);

            ParseQuery compoundQuery = ParseQuery.or(queries);
            compoundQuery.include("from");
            compoundQuery.include("to");

            compoundQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (mQuerylistener != null || true) {
                        ArrayList<Relationship> relationships = new ArrayList<Relationship>();

                        for (ParseObject o : objects) {
                            ParseUser userRel = null;
                            boolean   fromMe  = true;
                            if (o.get("from") != ParseUser.getCurrentUser()) {
                                userRel = (ParseUser)o.get("from");
                                fromMe  = false;
                            } else {
                                userRel = (ParseUser)o.get("to");
                            }

                            String displayName = userRel.get("displayName").toString();
                            if (displayName == null) displayName = "";
                            Relationship rel = new Relationship(new User(userRel.getUsername(), displayName), fromMe, userRel.getInt("status"));
                            relationships.add(rel);
                        }

                        mQuerylistener.onGotRelationships(relationships);
                    }
                }
            });
        }
        
		public void getScore(QueryListener listener) {
			
		}

        public void requestFriendByNumber(String phonenumber) {
            requestFriend(FIELD_PHONE_NUMBER, phonenumber);
        }
        
        public void requestFriendByUsername(String username) {
            requestFriend(FIELD_USERNAME, username.toLowerCase());
        }
        
        private void requestFriend(String requestType, String reqString) {
            ParseQuery query = ParseUser.getQuery();
            query.whereEqualTo(requestType, reqString);
            query.setLimit(1);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(final List<ParseUser> foundUsers, ParseException e) {
                    if (e != null) {

                    } else {
                        //Lets first see if a request for us already exists
                        ParseQuery rQuery = ParseQuery.getQuery("Relationships");
                        rQuery.whereEqualTo("to", ParseUser.getCurrentUser());
                        rQuery.whereEqualTo("from", foundUsers.get(0));
                        rQuery.setLimit(1);
                        rQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e != null) {

                                } else {
                                    if (objects.size() > 0) {
                                        if ((int)objects.get(0).get("status") == Relationship.STATUS_PENDING)
                                            objects.get(0).put("status", Relationship.STATUS_ACCEPTED);
                                        objects.get(0).saveInBackground();
                                    } else {
                                        //Found user - so add him
                                        ParseObject pRelationship = new ParseObject("Relationships");
                                        pRelationship.put("from", ParseUser.getCurrentUser());
                                        pRelationship.put("to", foundUsers.get(0));
                                        pRelationship.put("status", Relationship.STATUS_PENDING);

                                        pRelationship.saveInBackground();
                                    }
                                }
                            }
                        });


                    }
                }
            });
        }
        
        public void logout() {
            if (ParseUser.getCurrentUser() == mUser) {
                ParseUser.logOut();
            }
        }
    }
}