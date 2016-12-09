package com.cop4331.networking;

import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;

import com.cop4331.networking.Relationship;
import com.cop4331.networking.User;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * SINGLETON design pattern
 * The signleton design pattern is used here to limit
 * the amount of concurrent logged in accounts to 1
 * The type Account manager.
 */
public class AccountManager {
    private Account        mCurrAcc = null;

    /**
     * The constant FIELD_USERNAME_CASE.
     */
    public static final String FIELD_USERNAME_CASE  = "usernameCase";
    /**
     * The constant FIELD_USERNAME.
     */
    public static final String FIELD_USERNAME       = "username";
    /**
     * The constant FIELD_PHONE_NUMBER.
     */
    public static final String FIELD_PHONE_NUMBER   = "phone";
    /**
     * The constant FIELD_DISPLAY_NAME.
     */
    public static final String FIELD_DISPLAY_NAME   = "displayName";

    private onAccountStatus mAccountStatusListener = null;

    private static AccountManager mManager = new AccountManager();

    /**
     * OBSERVER design pattern
     * Notifies other parts of the application about the status of the user's
     * authentication
     *
     * The interface On account status.
     */
    public interface onAccountStatus {
        /**
         * Called when a user has successfully been
         * authenticated with the Parse server
         * @param account the Account of the newly logged in user
         */
        public void onLogin(Account account);

        /**
         * Called when a user has successfully been
         * registered with the Parse server
         * @param account the Account of the newly registered in user
         */
        public void onRegistered(Account account);

        /**
         * Called when an authentication error has occured
         * with the Parse server while attempting to log a user
         * in
         * @param e contains the ParseException and reason that the the login failed
         */
        public void onLoginError(ParseException e);

        /**
         * Called when a signup error has occured
         * with the Parse server while attempting to register a
         * new user
         * @param e contains the ParseException and reason that the the registration failed
         */
        public void onRegistrationError(ParseException e);
    }

    /**
     * Gets the current instance of the ACcountManager
     * @return the instance
     */
    public static AccountManager getInstance() {
        //Create a new AccountManager if needed
        if (mManager.isLoggedIn() && mManager.mCurrAcc == null) mManager.mCurrAcc = new Account(ParseUser.getCurrentUser());

        return mManager;
    }

    /**
     * Attempts to log in the user with the given username and password. If a login is successful
     * then a session token is returned, allowing the user to remain logged in until they sign out
     * @param username the username of the user's account
     * @param password the password of the user's account
     */
    public void login(String username, String password) {
       // if (isSignedIn()) logOut();

        try {
            ParseUser.logInInBackground(username, password, mLoginCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a new user with the Parse server. A successful registration
     * will produce a onRegistered callback. If there were errors while registering then
     * a onRegistrationError callback will occur
     * @param username the username of the new account - an error will be produced if this
     * username is already taken
     * @param password    the password  the password of the account - must meet a length requirement
     * of 8
     * @param displayName the display name for the account
     * @param phone       (optional) the phonenumber for the account allowing a user to be searched
     * by their phone number
     */
    public void register(String username, String password, String displayName, String phone) {
        ParseUser user = new ParseUser();
        //user.setEmail(username);
        //Lower case the username so that usernames for login are not case sensistive
        user.setUsername(username.toLowerCase());
        user.setPassword(password);
        user.put(FIELD_DISPLAY_NAME, displayName);
        user.put(FIELD_PHONE_NUMBER, phone);
        //Save a correctly cased version of the username for display purposes
        user.put(FIELD_USERNAME_CASE, username);

        user.signUpInBackground(mRegisterCallback);
    }

    /**
     * Sets the onAccountStatus Listener that should be used for Account callbacks
     * @param listener the onAccountStatus to be used
     */
    public void setAccountStatusListener(onAccountStatus listener) {
        mAccountStatusListener = listener;
    }

    /**
     * Checks if a user is currently logged in.
     * @return true if an account is logged in, otherwise false
     */
    public boolean isLoggedIn() {
        return (ParseUser.getCurrentUser() != null);
    }

    /**
     * Gets the currently signed in Account
     * @return the Account object of the currently signed in user
     */
    public Account getCurrentAccount() {
        return mCurrAcc;
    }

    /**
     * Listener for a Parse Signup
     */
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


    /**
     * Listener for Parse Login
     */
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

    /**
     * PROXY Pattern
     * Proxy pattern is used to abstract the server queieries into simplified method
     * calls and callbacks
     *
     * Account class which manages the actions of the currently authenticated user
     */
    public static class Account extends User {
        private static final int REQUEST_EMAIL = 0;
        private static final int REQUEST_PHONE = 1;

        private final ParseUser mUser;

        private QueryListener mQuerylistener;

        private RelationshipManager mRelationshipManager = new RelationshipManager();

        /**
         * QueryListener insterface which provides all callback for a running query
         * (This should be moved to an abstract class)
         */
        public interface QueryListener {
            /**
             * Callback that provides the score of the Account
             * @param score the Account's current score
             */
            public void onGotScore(long score);

            /**
             * Callback that provides a list of relationships that the Account
             * has (friends, pending, declined)
             * @param relationships a List<Relationship> of all relationships that the Account has
             */
            public void onGotRelationships(List<Relationship> relationships);

            /**
             * Callback that provides the list of Games that the Account is/has participated in
             * @param games a list of Games that the Account has been in
             */
            public void onGotGames(List<Game> games);

            /**
             * Callback that provides the results of a user search
             * @param users the users found by a user search
             */
            public void onSearchUser(List<User> users);

            /**
             * Callback that indicates an error with the last query
             */
            public void onError();
		}

        /**
         * Constructs an Account object which secures and abstracts the server's
         * representation of the current account
         * @param user the ParseUser that this account belongs to
         */
        private Account(ParseUser user) {
            //New user logged in - Associate with device
            //For notification purposes
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("user", user);
            installation.saveInBackground();

            mParseUser = user;
            mUser = user;
        }

        /**
         * Queries for a list of games that the Account has participated in
         */
        public void getCurrentGames() {
            //Query for created games
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Games");
            query.whereEqualTo("owner", mUser);
            query.include("owner");
            query.include("winner");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        final ArrayList<Game> games = new ArrayList<Game>();

                        //Worker thread to provide callback once all nested queries have been
                        //completed
                        final GameWorker worker = new GameWorker(new Runnable() {
                            @Override
                            public void run() {
                                GameWorker w = (GameWorker)Thread.currentThread();
                                //Wait until queries have completed
                                while (!w.getQuerySizesSet() || games.size() != w.getInnerQueryCount() + w.getOuterQueryCount()) {

                                }

                                //This callback is going to occur on the worker thread
                                if (mQuerylistener != null) {
                                    mQuerylistener.onGotGames(games);
                                }
                            }
                        });
                        worker.start();

                        //Query for players within the game
                        //Set the outer query count
                        worker.setOuterQueryCount(objects.size());
                        for (final ParseObject pO : objects) {
                            pO.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> plrs, ParseException e) {
                                    if (e != null) {
                                        games.add(null);
                                        return;
                                    }
                                    games.add(parseToGame(pO, plrs));
                                }
                            });
                        }

                        //Query games that the Account is participating in
                        ParseQuery<ParseObject> qParticipant = ParseQuery.getQuery("Games");
                        qParticipant.whereEqualTo("players", mUser);
                        qParticipant.include("owner");
                        qParticipant.include("winner");
                        qParticipant.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> participating, ParseException e) {
                                if (e == null) {

                                    //Query for players within the game
                                    //Set the inner query count
                                    worker.setInnerQueryCount(participating.size());
                                    for (final ParseObject pO : participating) {
                                        pO.getRelation("players").getQuery().findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> plrs, ParseException e) {
                                                if (e != null) {
                                                    games.add(null);
                                                    return;
                                                }
                                                games.add(parseToGame(pO, plrs));
                                            }
                                        });
                                    }
                                } else {
                                    worker.setInnerQueryCount(0);
                                }
                            }
                        });
                    }
                }
            });
        }

        /**
         * GameWorker class which extends Thread. This is a dispatch thread that waits for all
         * queries associated with getCurrentGames to finish before the onGotGames callback occurs
         */
        private class GameWorker extends Thread {
            private int outerQueryCount = -1;
            private int innerQueryCount = -1;

            /**
             * Constrcuts a new GameWorker object
             * @param run the Runnable code to run
             */
            public GameWorker(Runnable run) {
                super(run);
            }

            /**
             * Gets if the inner and outer query sizes have been set
             * @return true if the inner and outer query sizes have been set, otherwise false
             */
            public boolean getQuerySizesSet() {
                return (outerQueryCount != -1 && innerQueryCount != -1);
            }

            /**
             * Set the query count of the outer queries
             * @param count number of outer queires
             */
            public void setOuterQueryCount(int count) {
                outerQueryCount = count;
            }

            /**
             * Set the query count of the inner queries
             * @param count number of inner queires
             */
            public void setInnerQueryCount(int count) {
                innerQueryCount = count;
            }

            /**
             * Gets the outer query count
             * @return the outer query count
             */
            public int getOuterQueryCount() {
                return outerQueryCount;
            }

            /**
             * Gets the inner query count
             * @return the inner query count
             */
            public int getInnerQueryCount() {
                return innerQueryCount;
            }
        }

        /**
         * Sets the QueryListener object for callbacks to occur on
         * @param listener the listener to use for callbacks
         */
        public void setQuerylistener(QueryListener listener) {
            mQuerylistener = listener;
        }

        /**
         * Converts a Parse Game into a Game object
         * @param obj the ParseObject to convert
         * @param playerObjs the list of players in the Game
         * @return a Game object that represents the Parse Game
         */
        private Game parseToGame(ParseObject obj, List<ParseObject> playerObjs) {
            //Rebuild the game
            Game.Builder builder = new Game.Builder();
            builder.setTimelimit((int)obj.get("timelimit"));
            builder.setPrompt((String)obj.get("prompt"));
            builder.setDatabaseId(obj.getObjectId());
            builder.setCompletionStatus(obj.getBoolean("completed"));
            if (obj.getBoolean("completed")) {
                ParseUser winner = (ParseUser)obj.get("winner");
                if (winner != null) {
                    builder.setWinner(parseuserToUser(winner));
                }
            }

            //Add all the players to the Game
            for (ParseObject player : playerObjs) {
                ParseUser plr = (ParseUser)player;
                builder.addPlayer(parseuserToUser(plr));
            }

            Date expTime = new Date(obj.getCreatedAt().getTime() + (int)obj.get("timelimit"));

            ParseUser owner = (ParseUser)obj.get("owner");
            return builder.build(parseuserToUser(owner), obj.getCreatedAt());
        }

        /**
         * Constructs a User object from a ParseUser
         * @param pUser the ParseUser to convert
         * @return a User object
         */
        private User parseuserToUser(ParseUser pUser) {
            return new User(pUser.get(AccountManager.FIELD_USERNAME_CASE).toString(), pUser.get(AccountManager.FIELD_DISPLAY_NAME).toString(),
                    pUser.get(AccountManager.FIELD_PHONE_NUMBER).toString(), pUser);
        }

        /**
         * Uploads an image to the specified Game as the Account's submission
         * @param submitTo the Game to submit to
         * @param shot     the File to submit
         */
        public void submitShot(final Game submitTo, File shot) {
            if (submitTo != null) {
                final ParseFile img = new ParseFile(shot);
                img.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            //Create a ParseObject to hold the shot
                            ParseObject shotSubmit = new ParseObject("Shots");

                            final ParseObject tempGame = new ParseObject("Games");
                            tempGame.setObjectId(submitTo.getDatabaseId());

                            shotSubmit.put("game", tempGame);
                            shotSubmit.put("owner", mUser);
                            shotSubmit.put("image", img);
                            shotSubmit.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    //Increment the Game's shot count
                                    tempGame.increment("shotCount");
                                    tempGame.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) return;

                                            //Have the server check if the Game has been completed
                                            HashMap<String, String> comp = new HashMap<String, String>();
                                            comp.put("gameId", tempGame.getObjectId());
                                            ParseCloud.callFunctionInBackground("CompleteGame", comp);
                                        }
                                    });

                                    List<User> players = submitTo.getPlayers();
                                    //Add the prompter too
                                    players.add(submitTo.getGameCreator());
                                    for (User player : players) {
                                        if (player.getParseUser().getObjectId().equals(getParseUser().getObjectId()))
                                            continue;

                                        //Notify other players that you have submitted a shot
                                        HashMap<String, String> push = new HashMap<String, String>();
                                        push.put("userId", player.getParseUser().getObjectId());
                                        push.put("message", getDisplayName() + " has submitted a shot!");
                                        ParseCloud.callFunctionInBackground("PushUser", push);
                                    }

                                }
                            });
                        }
                    }
                });
            }
        }

        /**
         * Completes the construction of the Game.Builder object and
         * pushes it to the server
         * @param builder the Game.Builder object that contains all the settings for the game
         */
        public void startGame(final Game.Builder builder) {
            Game newGame = builder.build(this, new Date());

            //Need to push this game to the databse now
            final ParseObject pGame = new ParseObject("Games");
            pGame.put("owner", ParseUser.getCurrentUser());
            pGame.put("prompt", newGame.getPrompt());
            pGame.put("timelimit", newGame.getTimeLimit());
            pGame.put("completed", newGame.getGameCompleted());
            pGame.put("playerCount", newGame.getPlayers().size());
            pGame.put("shotCount", 0);

            //Put all players into a Parse relation
            ParseRelation relation = pGame.getRelation("players");
            for (User user : newGame.getPlayers()) {
                relation.add(user.getParseUser());

                //Send a push notification to each invited player
                HashMap<String, String> push = new HashMap<String, String>();
                push.put("userId", user.getParseUser().getObjectId());
                push.put("message", getDisplayName() + " has sent you a game invite!");
                ParseCloud.callFunctionInBackground("PushUser", push);
            }

            //Post the Game to the database
            pGame.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        builder.setDatabaseId(pGame.getObjectId());
                    }
                }
            });
        }

        /**
         * Gets the list of the Account's current relationships
         */
        public void getRelationships() {
            mRelationshipManager.getRelationships(mQuerylistener);
        }

        /**
         * Sends a friend request to the user with the provided phone number
         * @param phonenumber the phone number of the user to add
         */
        public void requestFriendByNumber(String phonenumber) {
            requestFriend(FIELD_PHONE_NUMBER, phonenumber);
        }

        /**
         * Sends a friend request to the user with the provided username
         * @param username the username of the user to add
         */
        public void requestFriendByUsername(String username) {
            requestFriend(FIELD_USERNAME, username.toLowerCase());
        }

        /**
         * Sends a friends request to the specified user
         * @param requestType the field to find a match for
         * @param reqString the string to match
         */
        private void requestFriend(String requestType, String reqString) {
            mRelationshipManager.requestFriend(requestType, reqString);
        }

        /**
         * Searches for a user with the specified string
         * @param search a String containing either the phone number or username of
         * the user to search for
         */
        public void searchUser(String search) {
            mRelationshipManager.searchUser(search, mQuerylistener);
        }

        /**
         * Removes a friend from the Account
         * @param relationship the Relationship to remove from the Account
         */
        public void removeFriend(Relationship relationship) {
            mRelationshipManager.removeFriend(relationship);
        }

        /**
         * Setting to change the Account's display name
         * @param displayName the new display name for the Account
         */
        public void setDisplayName(String displayName) {
            ParseUser.getCurrentUser().put("displayName", displayName);
        }

        /**
         * Setting to change the Account's phone number
         * @param phoneNumber the new phone number for the Account
         */
        public void setPhoneNumber(String phoneNumber) {
            ParseUser.getCurrentUser().put("phone", phoneNumber);
        }

        /**
         * Update's the Account's new settings
         */
        public void updateAccount() {
            ParseUser.getCurrentUser().saveInBackground();
        }

        /**
         * Gets the username of the Account
         * @return a String containing the Username of the logged in Account
         */
        @Override
        public String getUsername() {
            return ParseUser.getCurrentUser().getUsername();
        }

        /**
         * Gets the display name of the Account
         * @return a String containing the display name of the Account
         */
        @Override
        public String getDisplayName() {
            return ParseUser.getCurrentUser().get("displayName").toString();
        }

        /**
         * Gets the phone number of the Account
         * @return a String containing the phone number of the Account
         */
        @Override
        public String getPhoneNumber() {
            return ParseUser.getCurrentUser().get("phone").toString();
        }

        /**
         * Logs out the Account
         */
        public void logout() {
            if (ParseUser.getCurrentUser() == mUser) {
                ParseUser.logOut();
            }
        }
    }
}