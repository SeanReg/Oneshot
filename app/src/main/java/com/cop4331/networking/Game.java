package com.cop4331.networking;

import com.cop4331.networking.User;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A Class that holds information about a Game
 */
public class Game {
    private final ArrayList<User> mPlayers = new ArrayList<User>();
    private long   mTimeLimit = 0;
    private String mPrompt = "";   
    private String mDatabaseId = "";
	private boolean mIsCompleted = false;
    private User    mWinner = null;

    private Date mCreatedAt = null;

    private User mCreator = null;

    /**
     * OBSERVER pattern
     * Listener that provides callbacks for queries on the Game's shots
     */
    public interface ShotListener {
        /**
         * Callback provides a list of shots from the getShots query
         * @param shots the list of shots submitted to the Game so far
         */
        public void onGotShots(List<Shot> shots);
    }

    /**
     * Instantiates a new Game.
     */
    public Game() {
        
    }

    /**
     * Gets the list of player participating in the game
     * @return the players participating
     */
    public List<User> getPlayers() {
        return new ArrayList<>(mPlayers);
    }


    /**
     * Checks if the current Account is the one that created the Game
     * @return true if the logged in user created the Game, otherwise false
     */
    public boolean isGameCreator() {
        AccountManager manager = AccountManager.getInstance();
        return (manager.getCurrentAccount().getParseUser().getUsername().equalsIgnoreCase(mCreator.getParseUser().getUsername()));
    }

    /**
     * Checks if the game has been completed
     * @return true if the game has been completed, otherwise false
     */
    public boolean getGameCompleted() {
	    return mIsCompleted;
	}

    /**
     * Gets the creator of the game
     * @return a User object containing the game's creator
     */
    public User getGameCreator() {
        return mCreator;
    }

    /**
     * Queries for all currently submitted shots
     * @param listener the listener to notify when the query has been complted
     */
    public void getShots(final ShotListener listener) {
        //Construct query
        ParseObject tempGame = new ParseObject("Games");
        tempGame.setObjectId(mDatabaseId);

        final ArrayList<Shot> shots = new ArrayList<>();

        final ParseQuery query = new ParseQuery("Shots");
        query.whereEqualTo("game", tempGame);
        query.include("owner");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null) {
                    //Convert ParseObject to Shot object
                    for (ParseObject pO : objects) {
                        ParseUser owner = (ParseUser)(pO.get("owner"));
                        User shotOwner = new User(owner.getString(AccountManager.FIELD_USERNAME_CASE),
                                owner.getString(AccountManager.FIELD_DISPLAY_NAME),
                                owner.getString(AccountManager.FIELD_PHONE_NUMBER), owner);
                        shots.add(new Shot(shotOwner, pO.getParseFile("image")));
                    }

                    //Notify listener
                    if (listener != null) {
                        listener.onGotShots(shots);
                    }
                }
            }
        });
	}

    /**
     * Allows the Game's creator to pick the Game's winner
     * @param winner the winner of the game
     * @throws IllegalAccessException thrown when the current Account is not the creator of the Game
     */
    public void pickWinner(User winner) throws IllegalAccessException {
        //Can only be used by the creator of the game
        if (!isGameCreator())
            throw new IllegalAccessException("Winner can only be picked by the Game's creator!");

        if (mWinner != null) return;

        //Add winner to Parse Game
        ParseObject game = new ParseObject("Games");
        game.setObjectId(mDatabaseId);
        game.put("completed", true);
        game.put("winner", winner.getParseUser());


        //Increment winner's score
        HashMap<String, String> score = new HashMap<String, String>();
        score.put("userId", winner.getParseUser().getObjectId());
        score.put("incSize", Integer.toString(mPlayers.size()));
        ParseCloud.callFunctionInBackground("AwardScore", score);

        //Save game to server
        game.saveInBackground();

        for (User plr : mPlayers) {
            //Notify all players that a winner has been picked
            HashMap<String, String> push = new HashMap<String, String>();
            push.put("userId", plr.getParseUser().getObjectId());
            push.put("message", getGameCreator().getDisplayName() + " has chosen a winner!");
            ParseCloud.callFunctionInBackground("PushUser", push);
        }

        mWinner = winner;
    }

    /**
     * Gets the winner of the game
     * @return a User object containing the winner of the game. If there is no winner,
     * then null is returned
     */
    public User getWinner() {
        return mWinner;
    }

    /**
     * Gets the Game's prompt
     * @return a String containing the Game's prompt
     */
    public String getPrompt() {
        return mPrompt;
    }

    /**
     * Gets the Game's timelimit in milliseconds
     * @return the Game's timelimit in milliseconds
     */
    public long getTimeLimit() {
        return mTimeLimit;
    }

    /**
     * Gets the Parse database Id of the game
     * @return the database id of the Game
     */
    public String getDatabaseId() {
        return mDatabaseId;
    }

    /**
     * Gets expiration date of the Game
     * @return a Date object containig the expiration of the Game
     */
    public Date getExpirationDate() {
        return new Date(mCreatedAt.getTime() + mTimeLimit);
    }

    /**
     * BUILDER Pattern
     * Allows various settings to be applied to a Game before construction
     *
     * Class that allows a Game to be built
     */
    public static class Builder {
        private static final List<String> PREMADE_PROMPTS = new ArrayList<String>();
        private Game mGame = null;
        
        private static List<String> getPremadePrompts() {
            return new ArrayList<String>(PREMADE_PROMPTS);
        }

        /**
         * Instantiates a new Builder.
         */
        public Builder() {
            mGame = new Game();
        }

        /**
         * Sets timelimit.
         * @param timeLimit the time limit
         */
        public void setTimelimit(long timeLimit) {
            mGame.mTimeLimit = timeLimit;
        }

        /**
         * Add players to the Game
         * @param player the List of Users to add to the Game
         */
        public void addPlayers(List<User> player) {
            mGame.mPlayers.addAll(player);
        }

        /**
         * Add a player to the Game
         * @param player the User to add to the Game
         */
        public void addPlayer(User player) {
            mGame.mPlayers.add(player);
        }

        /**
         * Removes a player from the Game
         * @param player the User to remove from the Game
         */
        public void removePlayer(User player) {
            mGame.mPlayers.remove(player);
        }

        /**
         * Checks if a player has been added to the Game
         * @param player the player to check
         * @return true if added, otherwise false
         */
        public boolean playerIsAdded(User player) {
            return mGame.mPlayers.contains(player);
        }

        /**
         * Sets the prompt for the Game
         * @param prompt the prompt for the Game
         */
        public void setPrompt(String prompt) {
            mGame.mPrompt = prompt;
        }

        /**
         * Sets the Parse database id for the Game
         * @param id the id of the Game on the database
         */
        public void setDatabaseId(String id) {
		    mGame.mDatabaseId = id;
		}

        /**
         * Sets the winning User of the Game
         * @param winner the User that won the gGame
         */
        public void setWinner(User winner) {
            mGame.mWinner = winner;
        }

        /**
         * Sets completion status of the Game
         * @param complete true if the Game has been completed or false if not
         */
        public void setCompletionStatus(boolean complete) {
		    mGame.mIsCompleted = complete;
		}

        /**
         * Finializes and constrcuts the Game object
         * @param creator   the creator of the Game
         * @param createdAt the Date that the game was created
         * @return a Game object with all of its settings
         */
        public Game build(User creator, Date createdAt) {
            mGame.mCreator = creator;
            mGame.mCreatedAt = new Date(createdAt.getTime());

            return mGame;
        }
    }
}