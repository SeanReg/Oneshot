package com.cop4331.oneshot;

import android.Manifest;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cop4331.LoginActivity;
import com.cop4331.com.cop4331.permissions.PermissionRequester;
import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.networking.Relationship;
import com.cop4331.networking.User;
import com.parse.Parse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeScreenActivity extends AppCompatActivity{
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

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
            TabFragment tabFragment = new TabFragment();
            tabFragment.setHomeScreenActivity(this);
             mFragmentTransaction.replace(R.id.containerView, tabFragment).commit();
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
                        TabFragment tabFragment = new TabFragment();
                        tabFragment.setHomeScreenActivity(HomeScreenActivity.this);
                        xfragmentTransaction.replace(R.id.containerView, tabFragment).commit();
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

        AccountManager.getInstance().getCurrentAccount().setQuerylistener(new AccountManager.Account.QueryListener() {
            @Override
            public void onGotScore(long score) {

            }

            @Override
            public void onGotRelationships(List<Relationship> relationships) {

            }

            @Override
            public void onGotGames(List<Game> games) {
                mCurrentGames = games;
                LinearLayout parentView = ((LinearLayout)findViewById(R.id.createdLinearLayout));
                for (Game g : games) {
                    CardView card = null;

                    if (g.isGameCreator(AccountManager.getInstance())) {
                        card = (CardView) getLayoutInflater().inflate(R.layout.games_card, parentView, false);
                        ((TextView)card.findViewById(R.id.promptText)).setText(g.getPrompt());

                        long diff = (g.getExpirationDate().getTime() - (new Date()).getTime());
                        String remainingTimeH = Long.toString(diff / (60 * 60 * 1000) % 24) + " hours";
                        String remainingTimeM = Long.toString(diff / (60 * 1000) % 60 ) + " minutes";
                        ((TextView)card.findViewById(R.id.timeRemainingText)).setText(remainingTimeH + " " + remainingTimeM);

                    } else {

                    }

                    parentView.addView(card);
                }
            }

            @Override
            public void onSearchUser(List<User> users) {

            }

            @Override
            public void onError() {

            }
        });
        AccountManager.getInstance().getCurrentAccount().getCurrentGames();

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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermission.onPermissionResult(requestCode, permissions, grantResults);
    }
}