package com.cop4331.oneshot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.networking.Relationship;
import com.cop4331.networking.User;

import java.util.List;

public class FriendsActivity extends Activity {

    private EditText mSearchText = null;
    private AccountManager.Account curAcc = null;
    private List<Relationship> mRelationships = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_layout);

        curAcc = AccountManager.getInstance().getCurrentAccount();
        curAcc.setQuerylistener(mQueryListener);
        curAcc.getRelationships(null);

        (findViewById(R.id.searchButton)).setOnClickListener(mSearchListener);
        mSearchText = ((EditText)findViewById(R.id.searchText));

    }

    private final Button.OnClickListener mSearchListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            LinearLayout parentLayout = ((LinearLayout) findViewById(R.id.friends_linearlayout));
            parentLayout.removeAllViews();
            String search = mSearchText.getText().toString();
            for (Relationship rel : mRelationships) {
                User currUser = rel.getUser();
                if (currUser.getDisplayName().equalsIgnoreCase(search)
                        || currUser.getUsername().equalsIgnoreCase(search)
                        || currUser.getPhoneNumber().equalsIgnoreCase(search)) {

                    CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
                    ((TextView)card.findViewById(R.id.nameText)).setText(rel.getUser().getDisplayName());
                    ((TextView)card.findViewById(R.id.usernameDisplay)).setText(rel.getUser().getUsername());
                    ((Button)card.findViewById(R.id.deleteButton)).setVisibility(View.VISIBLE);
                    parentLayout.addView(card);

                    return;
                }
            }

            curAcc.searchUser(search);

        }
    };

    private final AccountManager.Account.QueryListener mQueryListener = new AccountManager.Account.QueryListener() {
        @Override
        public void onGotScore(long score) {

        }

        @Override
        public void onGotRelationships(List<Relationship> relationships) {
            LinearLayout parentLayout = ((LinearLayout) findViewById(R.id.friends_linearlayout));
            for (Relationship rel : relationships) {
                CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
                ((TextView)card.findViewById(R.id.nameText)).setText(rel.getUser().getDisplayName());
                ((TextView)card.findViewById(R.id.usernameDisplay)).setText(rel.getUser().getUsername());
                parentLayout.addView(card);
            }
            mRelationships = relationships;
        }
        @Override
        public void onGotGames(List<Game> games) {

        }

        @Override
        public void onSearchUser(List<User> users) {
            if (users.size() == 0) return;
            for(User user : users) {
                LinearLayout parentLayout = ((LinearLayout) findViewById(R.id.friends_linearlayout));
                CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
                ((TextView)card.findViewById(R.id.nameText)).setText(user.getDisplayName());
                ((TextView)card.findViewById(R.id.usernameDisplay)).setText(user.getUsername());
                parentLayout.addView(card);
            }
        }

        @Override
        public void onError() {

        }
    };


}
