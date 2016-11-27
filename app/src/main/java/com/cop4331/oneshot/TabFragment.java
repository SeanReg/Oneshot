package com.cop4331.oneshot;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 3 ;

    public static final int TAB_CREATED_GAMES       = 0;
    public static final int TAB_PARTICIPATING_GAMES = 1;
    public static final int TAB_HISTORY_GAMES       = 2;

    private HomeScreenActivity mHomeScreenActivity = null;

    private TabChangeListener mTabChangeListener = null;

    public interface TabChangeListener {
        public void onTabChanged(int curTab);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
            View x =  inflater.inflate(R.layout.tab_layout,null);
            tabLayout = (TabLayout) x.findViewById(R.id.tabs);
            viewPager = (ViewPager) x.findViewById(R.id.viewpager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mTabChangeListener != null) {
                    mTabChangeListener.onTabChanged(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /**
         *Set an Apater for the View Pager
         */
        viewPager.setAdapter(new TabAdapter(getChildFragmentManager()));

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                    tabLayout.setupWithViewPager(viewPager);
            }
        });

        return x;

    }

    public int getCurrentTab() {
        return viewPager.getCurrentItem();
    }

    public void setTabChangedListener(TabChangeListener listener) {
        mTabChangeListener = listener;
    }

    class TabAdapter extends FragmentPagerAdapter{

        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position)
        {
            Fragment fragment = null;
            switch (position){
                case TAB_CREATED_GAMES:
                    fragment = new CreatedFragment();
                    ((CreatedFragment)fragment).setHomeScreenActivity(mHomeScreenActivity);
                    break;
                case TAB_PARTICIPATING_GAMES:
                    fragment = new ParticipatingFragment();
                    break;
                case TAB_HISTORY_GAMES:
                    fragment = new HistoryFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0 :
                    return "Created";
                case 1 :
                    return "Participating";
                case 2 :
                    return "History";
            }
                return null;
        }
    }

}
