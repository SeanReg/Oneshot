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

/**
 * Activity to show relationships
 */
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
        curAcc.getRelationships();

        (findViewById(R.id.searchButton)).setOnClickListener(mSearchListener);
        mSearchText = ((EditText)findViewById(R.id.searchText));

    }

    /**
     * Listener for the search button clicked
     */
    private final Button.OnClickListener mSearchListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            parentLayout.removeAllViews();
            String search = mSearchText.getText().toString().toLowerCase();

            //Check if we already have the user
            for (Relationship rel : mRelationships) {
                User currUser = rel.getUser();
                if (currUser.getDisplayName().equalsIgnoreCase(search)
                        || currUser.getUsername().equalsIgnoreCase(search)
                        || currUser.getPhoneNumber().equalsIgnoreCase(search)) {
                    buildCard(rel);
                    return;
                }
            }

            //Perform query
            curAcc.searchUser(search);
        }
    };

    /**
     * Builds a CardView for the provided relationship
     * @param rel the relationship to build from
     * @return the newly created CardView
     */
    public CardView buildCard(Relationship rel) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
        card.setTag(rel);
        ((TextView)card.findViewById(R.id.nameText)).setText(rel.getUser().getDisplayName());
        ((TextView)card.findViewById(R.id.usernameDisplay)).setText(rel.getUser().getUsername());
        if(rel.getStatus() == Relationship.STATUS_ACCEPTED) {
            setCardTrash(card);
        } else if (rel.getStatus() == Relationship.STATUS_PENDING) {
            if (rel.isSentByMe()) {
                ((ImageView) card.findViewById(R.id.pendingView)).setVisibility(View.VISIBLE);
            } else {
                Button add = ((Button)card.findViewById(R.id.addButton));
                add.setVisibility(View.VISIBLE);
                add.setOnClickListener(mAddListener);
                add.setTag(card);
                card.setTag(rel);
            }
        }

        parentLayout.addView(card);
        return card;
    }

    /**
     * Adds the trash can icon to the provided CardView
     * @param card the CardView to add the icon to
     */
    private void setCardTrash(CardView card) {
        Button delete = ((Button)card.findViewById(R.id.deleteButton));
        delete.setVisibility(View.VISIBLE);
        delete.setTag(card);
        delete.setOnClickListener(mDeleteListener);
    }

    /**
     * Builds a CardView for the provided User
     * @param user the User to build from
     * @return the newly created CardView
     */
    public CardView buildCard(User user) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
        ((TextView)card.findViewById(R.id.nameText)).setText(user.getDisplayName());
        ((TextView)card.findViewById(R.id.usernameDisplay)).setText(user.getUsername());
        parentLayout.addView(card);
        return card;
    }

    /**
     * Listener from when the Add friend button is clicked
     */
    private final Button.OnClickListener mAddListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            CardView card = (CardView)view.getTag();

            //Change icon on card
            User user = null;
            if (card.getTag() instanceof Relationship) {
                user = ((Relationship) card.getTag()).getUser();
                setCardTrash(card);
            } else {
                user = (User) card.getTag();
                card.findViewById(R.id.pendingView).setVisibility(View.VISIBLE);
            }

            view.setVisibility(View.GONE);

            //Send request
            curAcc.requestFriendByUsername(user.getUsername());
        }
    };


    /**
     * Listener for deleting a friend
     */
    private final Button.OnClickListener mDeleteListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            CardView card = (CardView)view.getTag();
            ((ViewGroup)card.getParent()).removeView(card);
            mRelationships.remove(card.getTag());
            curAcc.removeFriend((Relationship)card.getTag());
        }
    };

    /**
     * Listener used to get the relationships for the Account and search user queries
     */
    private final AccountManager.Account.QueryListener mQueryListener = new AccountManager.Account.QueryListener() {
        @Override
        public void onGotScore(long score) {

        }

        @Override
        public void onGotRelationships(List<Relationship> relationships) {

            //Build cards for relationships
            LinearLayout parentLayout = ((LinearLayout) findViewById(R.id.friends_linearlayout));
            for (Relationship rel : relationships) {
               buildCard(rel);
            }

            //Sort by pending and accepted
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

            //Build cards for Users
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
