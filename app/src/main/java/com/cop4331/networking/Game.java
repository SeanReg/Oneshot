package com.cop4331.networking;

import com.cop4331.networking.User;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final ArrayList<User> mPlayers = new ArrayList<User>();
    private long   mTimeLimit = 0;
    private String mPrompt = "";   
    private String mDatabaseId = "";
	private boolean mIsCompleted = false;
    
    private User mCreator = null;
    
    public Game() {
        
    }
    
    public List<User> getPlayers() {
        return new ArrayList<>(mPlayers);
    }
    
	public void submitShot(Shot shot) {
	
	}


    public boolean isGameCreator(AccountManager manager) {
        return (manager.getCurrentAccount() == mCreator);
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
    
	public List<Shot> getShots() {
	    return null;
	}

    public String getPrompt() {
        return mPrompt;
    }

    public long getTimeLimit() {
        return mTimeLimit;
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

        public Game build(User creator) {
            mGame.mCreator = creator;

            return mGame;
        }
    }
}