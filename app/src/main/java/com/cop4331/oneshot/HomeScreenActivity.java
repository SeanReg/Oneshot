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
import android.view.MenuItem;

import com.cop4331.LoginActivity;
import com.cop4331.com.cop4331.permissions.PermissionRequester;
import com.cop4331.networking.AccountManager;
import com.parse.Parse;

public class HomeScreenActivity extends AppCompatActivity{
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    private PermissionRequester mPermission = null;

    private static boolean mParseInitialized = false;

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
             mNavigationView = (NavigationView) findViewById(R.id.shitstuff) ;

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

             mFragmentManager = getSupportFragmentManager();
             mFragmentTransaction = mFragmentManager.beginTransaction();
             mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
        /**
         * Setup click events on the Navigation View Items.
         */

             mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                 @Override
                 public boolean onNavigationItemSelected(MenuItem menuItem) {
                    mDrawerLayout.closeDrawers();

                     if (menuItem.getItemId() == R.id.nav_item_settings) {
                         FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                         fragmentTransaction.replace(R.id.containerView,new SettingsActivity()).commit();

                     }

                    if (menuItem.getItemId() == R.id.nav_item_home) {
                        FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                        xfragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
                    }

                     if (menuItem.getItemId() == R.id.nav_item_friends) {
                         FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                         xfragmentTransaction.replace(R.id.containerView,new FriendsFragment()).commit();
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

        AccountManager.getInstance().getCurrentAccount().getRelationships(null);

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