package com.cop4331.oneshot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;

import com.cop4331.camera.CameraActivity;
import com.cop4331.networking.Game;
import com.cop4331.networking.User;


public class InGameActivity extends Activity {

    private String mThisGame = null;
    private static InGameActivity mInstance = null;


    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingame_layout);
        (findViewById(R.id.cameraButton)).setOnClickListener(mCameraListener);

        mThisGame = getIntent().getStringExtra("gameId");
    }

    private final Button.OnClickListener mCameraListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(InGameActivity.this, CameraActivity.class);
            myIntent.putExtra("gameId", mThisGame);
            startActivity(myIntent);
        }
    };

}

