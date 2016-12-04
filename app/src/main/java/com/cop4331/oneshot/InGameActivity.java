package com.cop4331.oneshot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cop4331.camera.CameraActivity;
import com.cop4331.networking.AccountManager;
import com.cop4331.image_manipulation.ImageManipulateTest;
import com.cop4331.networking.Game;
import com.cop4331.networking.Shot;
import com.cop4331.networking.User;

import java.util.HashMap;
import java.util.List;

public class InGameActivity extends GameAssociativeActivity {

    private Game mThisGame = null;
    private LinearLayout parentLayout = null;
    private List<User> mPlayers = null;
    private AccountManager.Account curAcc = null;

    private final HashMap<String, CardView> mPlayerCards = new HashMap<>();

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

        Button camButton = (Button)findViewById(R.id.cameraButton);
        if (mThisGame.isGameCreator(AccountManager.getInstance()) || mThisGame.getGameCompleted()) {
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


    private final Button.OnClickListener mCameraListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(InGameActivity.this, CameraActivity.class);
            ImageManipulateTest.setGameActivityOpenedListener(new GameActivityOpenedListener() {
                @Override
                public Game onGameActivityOpened(GameAssociativeActivity act) {
                    return mThisGame;
                }
            });
            startActivity(myIntent);
        }
    };

    public CardView buildCard(User user) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.shot_card, parentLayout, false);
        card.findViewById(R.id.winnerStatus).setVisibility(View.GONE);
        ((TextView)card.findViewById(R.id.nameText)).setText(user.getDisplayName());
        ((TextView)card.findViewById(R.id.usernameDisplay)).setText(user.getUsername());
        parentLayout.addView(card);
        return card;
    }

    private final Game.ShotListener mShotListener = new Game.ShotListener() {
        @Override
        public void onGotShots(List<Shot> shots) {
            boolean shotSubmitted = false;
            for (Shot shot : shots) {
                if(shot.getUser().getUsername().equalsIgnoreCase(curAcc.getUsername())) {
                    shotSubmitted = true;
                }
                shot.downloadImage(new Shot.DownloadListener() {
                    @Override
                    public void onDownloadCompleted(final Shot shot) {
                        CardView plrCard = mPlayerCards.get(shot.getUser().getUsername());
                        if (plrCard != null) {
                            ImageView imgView = (ImageView)(plrCard.findViewById(R.id.shotImageView));
                            imgView.setImageBitmap(BitmapFactory.decodeFile(shot.getImage().getAbsolutePath()));
                            imgView.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent enlargeShotIntent = new Intent(getApplicationContext(), ShotEnlargedActivity.class);
                                    enlargeShotIntent.putExtra("Shot", shot.getImage().getAbsolutePath());
                                    startActivity(enlargeShotIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onDownloadError(Shot shot) {

                    }
                });
            }
            if (!shotSubmitted && !mThisGame.isGameCreator(AccountManager.getInstance())) {
                Button camButton = (Button)findViewById(R.id.cameraButton);
                camButton.setEnabled(true);
                camButton.setAlpha(1.0f);
            } else if (mThisGame.isGameCreator(AccountManager.getInstance())){
                findViewById(R.id.winnerStatus).setVisibility(View.VISIBLE);
            }
        }
    };

}




