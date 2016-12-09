package com.cop4331.oneshot;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cop4331.image_manipulation.ImageManipulateTest;
import com.cop4331.networking.Game;

/**
 * Created by Sean on 12/3/2016.
 */
public abstract class GameAssociativeActivity extends Activity {

    /**
     * The M this game.
     */
    protected Game mThisGame = null;
    private static GameActivityOpenedListener mActivityOpenedListener = null;

    /**
     * The interface Game activity opened listener.
     */
    public interface GameActivityOpenedListener {
        /**
         * On game activity opened game.
         *
         * @param act the act
         * @return the game
         */
        public Game onGameActivityOpened(GameAssociativeActivity act);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mActivityOpenedListener != null) {
            mThisGame = mActivityOpenedListener.onGameActivityOpened(this);
        } else {
            new RuntimeException("Listener not set! Cannot get game object!");
        }
    }

    /**
     * Sets game activity opened listener.
     *
     * @param listener the listener
     */
    public static void setGameActivityOpenedListener(GameActivityOpenedListener listener) {
        mActivityOpenedListener = listener;
    }
}
