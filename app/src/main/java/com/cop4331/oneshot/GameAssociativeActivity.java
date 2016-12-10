package com.cop4331.oneshot;

import android.app.Activity;
import android.os.Bundle;

import com.cop4331.networking.Game;

/**
 * Created by Sean on 12/3/2016.
 *
 * Abstract class that provides an interface for passing a Game object
 * between activities
 */
public abstract class GameAssociativeActivity extends Activity {

    protected Game mThisGame = null;
    private static GameActivityOpenedListener mActivityOpenedListener = null;

    /**
     * Listener for when the activity is created
     */
    public interface GameActivityOpenedListener {
        /**
         * Called when the activity is opened
         * @param act the activity opened
         * @return the Game object that should be associated with the activity
         */
        public Game onGameActivityOpened(GameAssociativeActivity act);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mActivityOpenedListener != null) {
            //Set the Game object
            mThisGame = mActivityOpenedListener.onGameActivityOpened(this);
        } else {
            new RuntimeException("Listener not set! Cannot get game object!");
        }
    }

    /**
     * Sets game activity opened listener.
     * @param listener the listener
     */
    public static void setGameActivityOpenedListener(GameActivityOpenedListener listener) {
        mActivityOpenedListener = listener;
    }
}
