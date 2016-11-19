package com.cop4331.oneshot;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CreatedFragment extends Fragment {

    private HomeScreenActivity mHomeScreenActivity = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.created_layout,null);

        ((Button)v.findViewById(R.id.newGameButton)).setOnClickListener(mCreateButton);

        return v;
    }

    public void setHomeScreenActivity(HomeScreenActivity homeScreenActivity) {
        mHomeScreenActivity = homeScreenActivity;
    }


    private final Button.OnClickListener mCreateButton = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getContext(), NewGameActivity.class);
            startActivity(intent);
        }
    };
}
