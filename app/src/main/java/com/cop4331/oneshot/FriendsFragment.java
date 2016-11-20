package com.cop4331.oneshot;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.networking.Relationship;

import java.util.List;

public class FriendsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.friends_layout,null);

        AccountManager.Account curAcc = AccountManager.getInstance().getCurrentAccount();
        curAcc.setQuerylistener(new AccountManager.Account.QueryListener() {
            @Override
            public void onGotScore(long score) {

            }

            @Override
            public void onGotRelationships(List<Relationship> relationships) {
                LinearLayout parentLayout = ((LinearLayout) fragmentView.findViewById(R.id.friends_linearlayout));
                for (Relationship rel : relationships) {
                    CardView card = (CardView) inflater.inflate(R.layout.friends_card, parentLayout, false);
                    ((TextView)card.findViewById(R.id.nameText)).setText(rel.getUser().getDisplayName());
                    ((TextView)card.findViewById(R.id.usernameDisplay)).setText(rel.getUser().getUsername());
                    parentLayout.addView(card);
                }
            }
            @Override
            public void onGotGames(List<Game> games) {

            }

            @Override
            public void onError() {

            }
        });
        curAcc.getRelationships(null);



        return fragmentView;
    }
}
