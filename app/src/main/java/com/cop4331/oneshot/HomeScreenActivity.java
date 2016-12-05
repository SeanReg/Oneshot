package com.cop4331.oneshot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cop4331.LoginActivity;
import com.cop4331.com.cop4331.permissions.PermissionRequester;
import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.networking.Relationship;
import com.cop4331.networking.Shot;
import com.cop4331.networking.User;
import com.parse.Parse;
import com.parse.ParseCloud;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeScreenActivity extends AppCompatActivity{
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    private TabFragment mTabFragment = null;

    private PermissionRequester mPermission = null;

    private static boolean mParseInitialized = false;

    private List<Game> mCurrentGames = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!mParseInitialized) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId("kyNJHeJgXmP4K4TxmeaFrU09D0faUvwQ2RSBGv5s")
                    .clientKey("uRdkVn6jcjdZF7kMQxKAAK39JpNG98nJFPwfbhwo")
                    .server("https://parseapi.back4app.com/").build()
            );
            mParseInitialized = true;
        }

        mPermission = new PermissionRequester(this);
        mPermission.requestPermission(Manifest.permission.CAMERA);

        /**
         *Setup the DrawerLayout and NavigationView
         */

             mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
             mNavigationView = (NavigationView) findViewById(R.id.navigationview) ;

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mTabFragment = new TabFragment();
        mTabFragment.setTabChangedListener(mTabChangeListener);
        mFragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
        /**
         * Setup click events on the Navigation View Items.
         */

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                 @Override
                 public boolean onNavigationItemSelected(MenuItem menuItem) {
                    mDrawerLayout.closeDrawers();

                     if (menuItem.getItemId() == R.id.nav_item_settings) {
                         Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                         startActivity(settingsIntent);
                     }

                    if (menuItem.getItemId() == R.id.nav_item_home) {
                        FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                        mTabFragment = new TabFragment();
                        mTabFragment.setTabChangedListener(mTabChangeListener);
                        xfragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
                    }

                     if (menuItem.getItemId() == R.id.nav_item_friends) {
                         Intent settingsIntent = new Intent(getApplicationContext(), FriendsActivity.class);
                         startActivity(settingsIntent);
                     }

                     if (menuItem.getItemId() == R.id.nav_item_logout) {
                         if (AccountManager.getInstance().isLoggedIn()) AccountManager.getInstance().getCurrentAccount().logout();
                         onStart();
                     }

                     return false;
                }
        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
        R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

/*
        AccountManager.getInstance().getCurrentAccount().requestFriendByUsername("jason");
*/

    }

    public void refreshGameList() {
        if (mCurrentGames == null) return;

        LinearLayout cLayout = ((LinearLayout)findViewById(R.id.createdLinearLayout));
        LinearLayout pLayout = ((LinearLayout)findViewById(R.id.participatingLinearLayout));
        LinearLayout hLayout = ((LinearLayout)findViewById(R.id.historyLinearLayout));

        if (cLayout != null) cLayout.removeAllViews();
        if (pLayout != null) pLayout.removeAllViews();
        if (hLayout != null) hLayout.removeAllViews();

        for (Game g : mCurrentGames) {
/*            g.getShots(new Game.ShotListener() {
                @Override
                public void onGotShots(List<Shot> shots) {
                    if (shots.size() == 0) return;

                    shots.get(0).downloadImage(new Shot.DownloadListener() {
                        @Override
                        public void onDownloadCompleted(Shot shot) {
                            Log.d("Got shots", shot.getImage().getAbsolutePath());
                        c}

                        @Override
                        public void onDownloadError(Shot shot) {

                        }
                    });

                }
            });*/

            switch (mTabFragment.getCurrentTab()) {
                case TabFragment.TAB_CREATED_GAMES:
                    if (g.isGameCreator(AccountManager.getInstance()) && !g.getGameCompleted() && cLayout != null) {
                        CardView c = inflateGameCard(g, cLayout);
                        c.findViewById(R.id.invitedByText).setVisibility(View.INVISIBLE);
                    }
                    break;
                case TabFragment.TAB_PARTICIPATING_GAMES:
                    if (!g.isGameCreator(AccountManager.getInstance()) && !g.getGameCompleted() && pLayout != null) {
                        inflateGameCard(g, pLayout);
                    }
                    break;
                case TabFragment.TAB_HISTORY_GAMES:
                    if (g.getGameCompleted() && hLayout != null) {
                        inflateGameCard(g, hLayout);
                    }
                    break;
            }
        }
    }

    public void inflateGameCreation() {

    }

    @Override
    protected void onStart() {
        super.onStart();

/*        if (AccountManager.getInstance().getCurrentAccount() != null) {
            AccountManager.getInstance().getCurrentAccount().logout();
        }*/
        if (!AccountManager.getInstance().isLoggedIn()) {
            Intent camIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(camIntent);
        } else {
            AccountManager.Account acc = AccountManager.getInstance().getCurrentAccount();
            acc.setQuerylistener(mAccountQueryListener);
            acc.getCurrentGames();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermission.onPermissionResult(requestCode, permissions, grantResults);
    }

    private CardView inflateGameCard(final Game g, ViewGroup parentView) {
        final CardView card = (CardView) getLayoutInflater().inflate(R.layout.games_card, parentView, false);
        ((TextView) card.findViewById(R.id.promptText)).setText(g.getPrompt());

        long diff = (g.getExpirationDate().getTime() - (new Date()).getTime());
        String remainingTimeH = Long.toString(diff / (60 * 60 * 1000) % 24) + " hours";
        String remainingTimeM = Long.toString(diff / (60 * 1000) % 60) + " minutes";
        TextView remainingText = ((TextView) card.findViewById(R.id.timeRemainingText));
        if (!g.getGameCompleted()) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("CachedShot", Context.MODE_PRIVATE);
            boolean hasSubmitted = sharedPref.getBoolean(AccountManager.getInstance().getCurrentAccount().getUsername() + g.getDatabaseId(), false);
            if (hasSubmitted) {
                card.setCardBackgroundColor(Color.rgb(152,189,249));
            }

//            g.getShots(new Game.ShotListener() {
//                @Override
//                public void onGotShots(List<Shot> shots) {
//                    for(Shot shot : shots) {
//                        if(shot.getUser().getUsername().equalsIgnoreCase(AccountManager.getInstance().getCurrentAccount().getUsername())) {
//                            card.setCardBackgroundColor(Color.rgb(255,255,255));
//                            return;
//                        }
//                    }
//                }
//            });
            remainingText.setText(remainingTimeH + " " + remainingTimeM);
        } else {
            User winner = g.getWinner();
            if(winner == null) {
                if(g.isGameCreator(AccountManager.getInstance())) {
                    remainingText.setText("PICK A WINNER");
                    card.setCardBackgroundColor(Color.rgb(246,153,138));
                } else {
                    card.setCardBackgroundColor(Color.rgb(215,211,210));
                }
            } else {
                remainingText.setText("Won by: " + winner.getUsername());
            }
        }

        TextView invitedText = (TextView)card.findViewById(R.id.invitedByText);
        invitedText.setVisibility(View.VISIBLE);
        if (!g.isGameCreator(AccountManager.getInstance())) {
            invitedText.setText("Created by " + g.getGameCreator().getDisplayName());
        } else {
            invitedText.setText("Created by me");
        }

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InGameActivity.setGameActivityOpenedListener(new GameAssociativeActivity.GameActivityOpenedListener() {
                    @Override
                    public Game onGameActivityOpened(GameAssociativeActivity act) {
                        return g;
                    }
                });

                Intent intent = new Intent(getApplicationContext(), InGameActivity.class);
                startActivity(intent);
            }
        });

        parentView.addView(card);

        return card;
    }

    private TabFragment.TabChangeListener mTabChangeListener = new TabFragment.TabChangeListener() {
        @Override
        public void onTabChanged(int curTab) {
            Log.d("Tab", "Changed to " + curTab);
            refreshGameList();
        }
    };

    private AccountManager.Account.QueryListener mAccountQueryListener = new AccountManager.Account.QueryListener() {
        @Override
        public void onGotScore(long score) {

        }

        @Override
        public void onGotRelationships(List<Relationship> relationships) {

        }

        @Override
        public void onGotGames(List<Game> games) {
            mCurrentGames = games;
            refreshGameList();
        }

        @Override
        public void onSearchUser(List<User> users) {

        }

        @Override
        public void onError() {

        }
    };
}