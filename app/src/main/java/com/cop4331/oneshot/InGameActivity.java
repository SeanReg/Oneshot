package com.cop4331.oneshot;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cop4331.camera.CameraActivity;
import com.cop4331.networking.AccountManager;
import com.cop4331.image_manipulation.ImageManipulateActivity;
import com.cop4331.networking.Game;
import com.cop4331.networking.Shot;
import com.cop4331.networking.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * The InGame Activity view
 */
public class InGameActivity extends GameAssociativeActivity {
    private LinearLayout parentLayout = null;
    private List<User> mPlayers = null;
    private AccountManager.Account curAcc = null;

    private final HashMap<String, CardView> mPlayerCards = new HashMap<>();

    private static final int SHOT_SENT = 1;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingame_layout);
        (findViewById(R.id.cameraButton)).setOnClickListener(mCameraListener);

        curAcc = AccountManager.getInstance().getCurrentAccount();
        parentLayout = ((LinearLayout) findViewById(R.id.ingame_linearlayout));
        mPlayerCards.clear();
        mPlayers = mThisGame.getPlayers();

        TextView remainingText = ((TextView) findViewById(R.id.timeRemainingView));
        if(mThisGame.getGameCompleted()) {
            remainingText.setText("Completed on: " + mThisGame.getExpirationDate().toString());
        } else {
            long diff = (mThisGame.getExpirationDate().getTime() - (new Date()).getTime());
            String remainingTimeH = Long.toString(diff / (60 * 60 * 1000) % 24) + " hours";
            String remainingTimeM = Long.toString(diff / (60 * 1000) % 60) + " minutes";

            remainingText.setText("Time remaining: " + remainingTimeH + " " + remainingTimeM);
        }
        TextView promptText = ((TextView) findViewById(R.id.inGamePromptText));
        promptText.setText(mThisGame.getPrompt());

        Button camButton = (Button)findViewById(R.id.cameraButton);
        if (mThisGame.isGameCreator() || mThisGame.getGameCompleted()) {
            camButton.setVisibility(View.GONE);
        } else {
            camButton.setEnabled(false);
            camButton.setAlpha(0.2f);
        }

        for(User u : mPlayers) {
            mPlayerCards.put(u.getUsername(), buildCard(u));
        }

        mThisGame.getShots(mShotListener);
    }


    /**
     * Listener for when the Camera button is clicked
     */
    private final Button.OnClickListener mCameraListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(InGameActivity.this, CameraActivity.class);
            ImageManipulateActivity.setGameActivityOpenedListener(new GameActivityOpenedListener() {
                @Override
                public Game onGameActivityOpened(GameAssociativeActivity act) {
                    return mThisGame;
                }
            });
            startActivityForResult(myIntent, SHOT_SENT);
        }
    };

    /**
     * Builds a CardView from the specified used
     * @param user the user to build the view for
     * @return the newly constructed CardView
     */
    public CardView buildCard(User user) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.shot_card, parentLayout, false);
        CheckBox winner = (CheckBox)card.findViewById(R.id.winnerStatus);
        winner.setVisibility(View.GONE);
        declareWinner(user, card);
        ((TextView)card.findViewById(R.id.nameText)).setText(user.getDisplayName());
        ((TextView)card.findViewById(R.id.usernameDisplay)).setText(user.getUsername() + " [" + user.getScore() + "]");
        parentLayout.addView(card);
        return card;
    }

    /**
     * Selects a user as the winner and disables all check boxes and shows the winning box
     * @param winner the User that won
     * @param card the CardView of the User that won
     */
    private void declareWinner(User winner, CardView card) {
        CheckBox winnerBox = (CheckBox)card.findViewById(R.id.winnerStatus);
        if(mThisGame.getWinner() != null) {
            if(mThisGame.getWinner().getUsername().equalsIgnoreCase(winner.getUsername())) {
                winnerBox.setChecked(true);
            }
            winnerBox.setVisibility(View.VISIBLE);
            winnerBox.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SHOT_SENT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /**
     * Listener to get the Shots from the game
     */
    private final Game.ShotListener mShotListener = new Game.ShotListener() {
        @Override
        public void onGotShots(List<Shot> shots) {
            boolean shotSubmitted = false;

            //Got a list of shots
            for (Shot shot : shots) {

                CardView card = mPlayerCards.get(shot.getUser().getUsername());

                if(shot.getUser().getUsername().equalsIgnoreCase(curAcc.getUsername())) {
                    shotSubmitted = true;
                }
                ((ImageView)(card.findViewById(R.id.shotImageView))).setImageDrawable(getResources().getDrawable(R.drawable.checkmark));

                if(mThisGame.getGameCompleted() || mThisGame.isGameCreator()) {
                    //Download the images when the Game is completed
                    shot.downloadImage(new Shot.DownloadListener() {
                        @Override
                        public void onDownloadCompleted(final Shot shot) {
                            CardView plrCard = mPlayerCards.get(shot.getUser().getUsername());
                            if (plrCard != null) {
                                //Show the image on the correct card
                                ImageView imgView = (ImageView)(plrCard.findViewById(R.id.shotImageView));
                                imgView.setImageBitmap(BitmapFactory.decodeFile(shot.getImage().getAbsolutePath()));
                                imgView.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent enlargeShotIntent = new Intent(getApplicationContext(), ShotEnlargedActivity.class);
                                        enlargeShotIntent.putExtra("Shot", shot.getImage().getAbsolutePath());
                                        startActivity(enlargeShotIntent);
                                    }
                                });

                                if (mThisGame.isGameCreator() && mThisGame.getGameCompleted()){
                                    //If this is the winner show the checkbox
                                    CheckBox winnerCheck = (CheckBox)plrCard.findViewById(R.id.winnerStatus);
                                    winnerCheck.setVisibility(View.VISIBLE);
                                    winnerCheck.setOnClickListener(mWinnerClicked);
                                }
                            }
                        }

                        @Override
                        public void onDownloadError(Shot shot) {

                        }
                    });
                }
            }

            //Allow the camera button to be clicked if this User hasn't submitted yet
            if (!shotSubmitted && !mThisGame.isGameCreator()) {
                Button camButton = (Button)findViewById(R.id.cameraButton);
                camButton.setEnabled(true);
                camButton.setAlpha(1.0f);
            }
        }

        /**
         * Listener for when a Winner checkbox is clicked
         */
        private final View.OnClickListener mWinnerClicked = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the card object
                ViewParent findCard = view.getParent();
                while (!(findCard instanceof CardView)) {
                    findCard = findCard.getParent();
                }

                //Find a match for the card
                for (String cardKey : mPlayerCards.keySet()) {
                    CardView plrCard = mPlayerCards.get(cardKey);
                    if (plrCard == findCard) {

                        //Found a match - now we have the winner's username
                        for (User plr : mPlayers) {

                            //Find the User object for this username
                            if (plr.getUsername().equals(cardKey)) {
                                //Pick this player as the winner
                                try {
                                    mThisGame.pickWinner(plr);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                    plrCard.findViewById(R.id.winnerStatus).setEnabled(false);
                }

            }
        };

    };

}




