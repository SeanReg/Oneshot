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

/**
 * Tab fragment to control the HomeScreen Activity tabs
 */
public class TabFragment extends Fragment {

    /**
     * The constant tabLayout.
     */
    public static TabLayout tabLayout;
    /**
     * The constant viewPager.
     */
    public static ViewPager viewPager;
    /**
     * The constant int_items.
     */
    public static int int_items = 3 ;

    /**
     * The constant TAB_CREATED_GAMES.
     */
    public static final int TAB_CREATED_GAMES       = 0;
    /**
     * The constant TAB_PARTICIPATING_GAMES.
     */
    public static final int TAB_PARTICIPATING_GAMES = 1;
    /**
     * The constant TAB_HISTORY_GAMES.
     */
    public static final int TAB_HISTORY_GAMES       = 2;

    private HomeScreenActivity mHomeScreenActivity = null;

    private TabChangeListener mTabChangeListener = null;

    /**
     * The interface Tab change listener.
     */
    public interface TabChangeListener {
        /**
         * On tab changed.
         *
         * @param curTab the cur tab
         */
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

    /**
     * Gets current tab.
     *
     * @return the current tab
     */
    public int getCurrentTab() {
        return viewPager.getCurrentItem();
    }

    /**
     * Sets tab changed listener.
     *
     * @param listener the listener
     */
    public void setTabChangedListener(TabChangeListener listener) {
        mTabChangeListener = listener;
    }

    /**
     * The type Tab adapter.
     */
    class TabAdapter extends FragmentPagerAdapter{

        /**
         * Instantiates a new Tab adapter.
         *
         * @param fm the fm
         */
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
