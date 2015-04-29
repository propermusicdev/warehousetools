/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.proper.warehousetools.replen.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.proper.data.core.IFragmentSwitcher;
import com.proper.data.core.IViewPagerFragmentSwitcher;
import com.proper.data.customcontrols.NonSwipeableViewPager;
import com.proper.data.customcontrols.SlidingTabLayout;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenManageWork;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic sample which shows how to use {@link com.proper.data.customcontrols.SlidingTabLayout}
 * to display a custom {@link android.support.v4.view.ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsColorsFragment extends Fragment implements IViewPagerFragmentSwitcher {

    @Override
    public void switchFragment(int target) {
        //Allow tab navigation from outside this scope
        ((ActReplenManageWork)getActivity()).setCurrentTab(target);
        mViewPager.setCurrentItem(target);
    }

    /**
     * This class represents a tab to be displayed by {@link android.support.v4.view.ViewPager} and it's associated
     * {@link com.proper.data.customcontrols.SlidingTabLayout}.
     */
    static class SamplePagerItem {
        private final CharSequence mTitle;
        private final int mIndicatorColor;
        private final int mDividerColor;

        SamplePagerItem(CharSequence title, int indicatorColor, int dividerColor) {
            mTitle = title;
            mIndicatorColor = indicatorColor;
            mDividerColor = dividerColor;
        }

        /**
         * @return A new {@link android.support.v4.app.Fragment} to be displayed by a {@link android.support.v4.view.ViewPager}
         */
        Fragment createFragment(int position) {
            //return ContentFragment.newInstance(mTitle, mIndicatorColor, mDividerColor);
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new ManageWorkFragment();
                    break;
                case 1:
                    fragment = new ManageMoveLineFragment();
                    break;
//                case 2:
//                    fragment = new zzUpdateMoveLineFragment();
//                    break;
//                case 3:
//                    fragment = new zzSplitLineFragment();
//                    break;
            }
            return fragment;
        }

        /**
         * @return the title which represents this tab. In this sample this is used directly by
         * {@link android.support.v4.view.PagerAdapter#getPageTitle(int)}
         */
        CharSequence getTitle() {
            return mTitle;
        }

        /**
         * @return the color to be used for indicator on the {@link com.proper.data.customcontrols.SlidingTabLayout}
         */
        int getIndicatorColor() {
            return mIndicatorColor;
        }

        /**
         * @return the color to be used for right divider on the {@link com.proper.data.customcontrols.SlidingTabLayout}
         */
        int getDividerColor() {
            return mDividerColor;
        }
    }

    static final String LOG_TAG = "SlidingTabsColorsFragment";

    /**
     * A custom {@link android.support.v4.view.ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link android.support.v4.view.ViewPager} which will be used in conjunction with the {@link com.proper.data.customcontrols.SlidingTabLayout} above.
     */

    //private ViewPager mViewPager;
    private NonSwipeableViewPager mViewPager;

    public NonSwipeableViewPager getmViewPager() {
        if (mViewPager!=null) mViewPager.setClickable(false);
        return mViewPager;
    }

    public void setmViewPager(NonSwipeableViewPager mViewPager) {
        this.mViewPager = mViewPager;
    }

    /**
     * List of {@link --com.example.android.slidingtabscolors.SlidingTabsColorsFragment.SamplePagerItem} which represent this sample's tabs.
     */
    private List<SamplePagerItem> mTabs = new ArrayList<SamplePagerItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BEGIN_INCLUDE (populate_tabs)
        /**
         * Populate our tab list with tabs. Each item contains a title, indicator color and divider
         * color, which are used by {@link com.proper.data.customcontrols.SlidingTabLayout}.
         */
        mTabs.add(new SamplePagerItem(
                "Move List", // Title
                Color.BLUE, // Indicator color
                Color.GRAY // Divider color
        ));

        mTabs.add(new SamplePagerItem(
                "Move Line", // Title
                Color.RED, // Indicator color
                Color.GRAY // Divider color
        ));

//        mTabs.add(new SamplePagerItem(
//                "Update Line", // Title
//                Color.YELLOW, // Indicator color
//                Color.GRAY // Divider color
//        ));
//
//        mTabs.add(new SamplePagerItem(
//                "Split Line", // Title
//                Color.GREEN, // Indicator color
//                Color.GRAY // Divider color
//        ));
        // END_INCLUDE (populate_tabs)
    }

    /**
     * Inflates the {@link android.view.View} which will be displayed by this {@link android.support.v4.app.Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lyt_replen_manage_movelist, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} has finished.
     * Here we can pick out the {@link android.view.View}s we need to configure from the content view.
     *
     * We set the {@link android.support.v4.view.ViewPager}'s adapter to be an instance of
     * {@link --com.example.android.slidingtabscolors.SlidingTabsColorsFragment.SampleFragmentPagerAdapter}. The {@link com.proper.data.customcontrols.SlidingTabLayout} is then given the
     * {@link android.support.v4.view.ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (NonSwipeableViewPager) view.findViewById(R.id.viewpager);
        mViewPager = (NonSwipeableViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SampleFragmentPagerAdapter(getChildFragmentManager()));
        mViewPager.setClickable(false); /**Disable click interaction**/
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return pagerTouched(v, event);
                //return false;
            }
        });
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        // BEGIN_INCLUDE (tab_colorizer)
        // Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
        // the tab at the position, and return it's set color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return mTabs.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return mTabs.get(position).getDividerColor();
            }

        });
        // END_INCLUDE (tab_colorizer)
        // END_INCLUDE (setup_slidingtablayout)
    }

    private Boolean pagerTouched(View v, MotionEvent event) {
        //TODO - Create some
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        } else {
            return false;
        }
    }
    // END_INCLUDE (fragment_onviewcreated)

    /**
     * The {@link android.support.v4.app.FragmentPagerAdapter} used to display pages in this sample. The individual pages
     * are instances of {@link --com.example.android.slidingtabscolors.ContentFragment} which just display three lines of text. Each page is
     * created by the relevant {@link --com.example.android.slidingtabscolors.SlidingTabsColorsFragment.SamplePagerItem} for the requested position.
     * <p>
     * The important section of this class is the {@link #getPageTitle(int)} method which controls
     * what is displayed in the {@link com.proper.data.customcontrols.SlidingTabLayout}.
     */
    //private class SampleFragmentPagerAdapter extends FragmentPagerAdapter implements IFragmentSwitcher {
    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter implements IFragmentSwitcher {

        SampleFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return the {@link android.support.v4.app.Fragment} to be displayed at {@code position}.
         * <p>
         * Here we return the value returned from {@link --com.example.android.slidingtabscolors.SlidingTabsColorsFragment.SamplePagerItem#createFragment()}.
         */
        @Override
        public Fragment getItem(int i) {
            return mTabs.get(i).createFragment(i);
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link com.proper.data.customcontrols.SlidingTabLayout}.
         * <p>
         * Here we return the value returned from {@link --com.example.android.slidingtabscolors.SlidingTabsColorsFragment.SamplePagerItem#getTitle()}.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }

        @Override
        public Fragment switchFragment(int target) {
            return getItem(target);
        }
        // END_INCLUDE (pageradapter_getpagetitle)

    }

}