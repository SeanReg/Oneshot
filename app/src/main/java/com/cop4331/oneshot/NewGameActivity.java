package com.cop4331.oneshot;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.networking.Relationship;
import com.cop4331.networking.User;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class NewGameActivity extends Activity {
    private EditText mPrompt    = null;
    private Spinner mSpinner    = null;

    private static final long[] mSpinnerTimes = new long[] {600000, 1800000, 3600000, 18000000};

    private ArrayList<User> mSelectedUsers = new ArrayList<>();

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame_layout);

        mSpinner = (Spinner) findViewById(R.id.gameDurationSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gameduration_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        (findViewById(R.id.createGameButton)).setOnClickListener(mCreateGameListener);

        mPrompt = ((EditText)findViewById(R.id.promptText));


        AccountManager.Account curAcc = AccountManager.getInstance().getCurrentAccount();
        curAcc.setQuerylistener(new AccountManager.Account.QueryListener() {
            @Override
            public void onGotScore(long score) {

            }

            @Override
            public void onGotRelationships(List<Relationship> relationships) {
                LinearLayout parentLayout = ((LinearLayout) findViewById(R.id.friend_selection_list));
                for (Relationship rel : relationships) {
                    if (rel.getStatus() != Relationship.STATUS_ACCEPTED) continue;

                    CardView card = (CardView) getLayoutInflater().inflate(R.layout.friends_card, parentLayout, false);
                    ((TextView)card.findViewById(R.id.nameText)).setText(rel.getUser().getDisplayName());
                    ((TextView)card.findViewById(R.id.usernameDisplay)).setText(rel.getUser().getUsername());
                    CheckBox selBox = ((CheckBox)card.findViewById(R.id.selectButton));
                    selBox.setVisibility(View.VISIBLE);
                    selBox.setTag(rel);
                    selBox.setOnClickListener(mSelectionListener);
                    parentLayout.addView(card);
                }
            }

            @Override
            public void onGotGames(List<Game> games) {

            }

            @Override
            public void onSearchUser(List<User> users) {
            }

            @Override
            public void onError() {

            }
        });
        curAcc.getRelationships(null);
    }

    private final CheckBox.OnClickListener mSelectionListener = new CheckBox.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (((CheckBox)view).isChecked()) {
                mSelectedUsers.add(((Relationship)view.getTag()).getUser());
            } else {
                mSelectedUsers.remove(((Relationship)view.getTag()).getUser());
            }
        }
    };

    private final Button.OnClickListener mCreateGameListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            AccountManager manager = AccountManager.getInstance();
            String prompt   = mPrompt.getText().toString();
            long duration   = mSpinnerTimes[mSpinner.getSelectedItemPosition()];

            Game.Builder builder = new Game.Builder();
            builder.setCompletionStatus(false);
            builder.addPlayers(mSelectedUsers);
            builder.setPrompt(prompt);
            builder.setTimelimit(duration);
            AccountManager.getInstance().getCurrentAccount().startGame(builder);

            finish();
        }
    };

}

