package com.cop4331.oneshot;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;


public class InGameActivity extends Activity {

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingame_layout);

    }

}

