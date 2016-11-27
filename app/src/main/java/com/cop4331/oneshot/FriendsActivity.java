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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.networking.Relationship;
import com.cop4331.networking.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FriendsActivity extends Activity {

    private EditText mSearchText = null;
    private AccountManager.Account curAcc = null;
    private List<Relationship> mRelationships = null;
    private List<Relationship> mPendingRelationsips = null;
    private LinearLayout parentLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_layout);

        parentLayout = ((LinearLayout) findViewById(R.id.friends_linearlayout));
        curAcc = AccountManager.getInstance().getCurrentAccount();
        curAcc.setQuerylistener(mQueryListener);
        curAcc.getRelationships(null);

        (findViewById(R.id.searchButton)).setOnClickListener(mSearchListener);
        mSearchText = ((EditText)findViewById(R.id.searchText));

    }

    private final Button.OnClickListener mSearchListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            parentLayout.removeAllViews();
            String search = mSearchText.getText().toString();
            for (Relationship rel : mRelationships) {
                User currUser = rel.getUser();
                if (currUser.getDisplayName().equalsIgnoreCase(search)
                        || currUser.getUsername().equalsIgnoreCase(search)
                        || currUser.getPhoneNumber().equalsIgnoreCase(search)) {
                    buildCard(rel);
                    return;
                }
            }
            curAcc.searchUser(search);
        }
    };

    public CardView buildCard(Relationship rel) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
        card.setTag(rel);
        ((TextView)card.findViewById(R.id.nameText)).setText(rel.getUser().getDisplayName());
        ((TextView)card.findViewById(R.id.usernameDisplay)).setText(rel.getUser().getUsername());
        if(rel.getStatus() == Relationship.STATUS_ACCEPTED) {
            Button delete = ((Button)card.findViewById(R.id.deleteButton));
            delete.setVisibility(View.VISIBLE);
            delete.setTag(card);
            delete.setOnClickListener(mDeleteListener);
        } else if (rel.getStatus() == Relationship.STATUS_PENDING) {
            if (rel.isSentByMe()) {
                ((ImageView) card.findViewById(R.id.pendingView)).setVisibility(View.VISIBLE);
            } else {
                Button add = ((Button)card.findViewById(R.id.addButton));
                add.setVisibility(View.VISIBLE);
                add.setOnClickListener(mAddListener);
                add.setTag(card);
                card.setTag(rel.getUser());
            }
        }

        parentLayout.addView(card);
        return card;
    }

    public CardView buildCard(User user) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
        ((TextView)card.findViewById(R.id.nameText)).setText(user.getDisplayName());
        ((TextView)card.findViewById(R.id.usernameDisplay)).setText(user.getUsername());
        parentLayout.addView(card);
        return card;
    }

    private final Button.OnClickListener mAddListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            CardView card = (CardView)view.getTag();
            User user = (User)card.getTag();
            card.findViewById(R.id.pendingView).setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            curAcc.requestFriendByUsername(user.getUsername());
        }
    };

    private final Button.OnClickListener mDeleteListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            CardView card = (CardView)view.getTag();
            ((ViewGroup)card.getParent()).removeView(card);
            mRelationships.remove(card.getTag());
            curAcc.removeFriend((Relationship)card.getTag());
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
               buildCard(rel);
            }
            Collections.sort(relationships, new Comparator<Relationship>() {
                @Override
                public int compare(Relationship one, Relationship two) {
                    return one.getStatus() > two.getStatus() ? -1 : (one.getStatus() < two.getStatus() ) ? 1 : 0;
                }
            });
            mRelationships = relationships;
        }
        @Override
        public void onGotGames(List<Game> games) {

        }

        @Override
        public void onSearchUser(List<User> users) {
            if (users.size() == 0) return;
            for(User user : users) {
                CardView card = buildCard(user);
                Button add = ((Button)card.findViewById(R.id.addButton));
                add.setVisibility(View.VISIBLE);
                add.setOnClickListener(mAddListener);
                add.setTag(card);
                card.setTag(user);
            }
        }

        @Override
        public void onError() {

        }
    };


}