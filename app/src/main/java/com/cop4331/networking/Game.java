package com.cop4331.networking;

import com.cop4331.networking.User;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game {
    private final ArrayList<User> mPlayers = new ArrayList<User>();
    private long   mTimeLimit = 0;
    private String mPrompt = "";   
    private String mDatabaseId = "";
	private boolean mIsCompleted = false;

    private Date mCreatedAt = null;

    private User mCreator = null;

    public interface ShotListener {
        public void onGotShots(List<Shot> shots);
    }

    public Game() {
        
    }
    
    public List<User> getPlayers() {
        return new ArrayList<>(mPlayers);
    }
    
	public void submitShot(Shot shot) {
	
	}


    public boolean isGameCreator(AccountManager manager) {
        return (manager.getCurrentAccount().getParseUser().getUsername().equalsIgnoreCase(mCreator.getParseUser().getUsername()));
    }

    public GameController getGameController(AccountManager manager) {
        return null;
    }

	public boolean getGameCompleted() {
	    return mIsCompleted;
	}
    
    public User getGameCreator() {
        return mCreator;
    }
    
	public void getShots(final ShotListener listener) {
        ParseObject tempGame = new ParseObject("Games");
        tempGame.setObjectId(mDatabaseId);

        final ArrayList<Shot> shots = new ArrayList<>();

        final ParseQuery query = new ParseQuery("Shots");
        query.whereEqualTo("game", tempGame);
        query.include("owner");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {
                    for (ParseObject pO : objects) {
                        ParseUser owner = (ParseUser)(pO.get("owner"));
                        shots.add(new Shot(new User(owner.getString(AccountManager.FIELD_USERNAME_CASE), owner.getString(AccountManager.FIELD_DISPLAY_NAME),
                                owner.getString(AccountManager.FIELD_PHONE_NUMBER), owner), pO.getParseFile("image")));
                    }

                    if (listener != null) {
                        listener.onGotShots(shots);
                    }
                }
            }
        });

	}

    public String getPrompt() {
        return mPrompt;
    }

    public long getTimeLimit() {
        return mTimeLimit;
    }

    public String getDatabaseId() {
        return mDatabaseId;
    }

    public Date getExpirationDate() {
        return new Date(mCreatedAt.getTime() + mTimeLimit);
    }

    public static class Builder {
        private static final List<String> PREMADE_PROMPTS = new ArrayList<String>();
        private Game mGame = null;
        
        private static List<String> getPremadePrompts() {
            return new ArrayList<String>(PREMADE_PROMPTS);
        }
        
        public Builder() {
            mGame = new Game();
        }

        public void setTimelimit(long timeLimit) {
            mGame.mTimeLimit = timeLimit;
        }

        public void addPlayers(List<User> player) {
            mGame.mPlayers.addAll(player);
        }

        public void addPlayer(User player) {
            mGame.mPlayers.add(player);
        }

        public void removePlayer(User player) {
            mGame.mPlayers.remove(player);
        }

        public boolean playerIsAdded(User player) {
            return mGame.mPlayers.contains(player);
        }

        public void setPrompt(String prompt) {
            mGame.mPrompt = prompt;
        }

		public void setDatabaseId(String id) {
		    mGame.mDatabaseId = id;
		}

		public void setCompletionStatus(boolean complete) {
		    mGame.mIsCompleted = complete;
		}

        public Game build(User creator, Date createdAt) {
            mGame.mCreator = creator;
            mGame.mCreatedAt = new Date(createdAt.getTime());

            return mGame;
        }
    }
}