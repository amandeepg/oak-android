/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.inputmethod.InputMethodManager;

import com.oak.BaseActivity;

public class TabSwipeActivity extends BaseActivity implements OnPageChangeListener {

    private static final String TAB_STATE = "tab";

    private TabsAdapter mTabsAdapter;
    private ActionBar mActionBar;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, -1);
    }

    protected void onCreate(Bundle savedInstanceState, int pagerViewID) {
        super.onCreate(savedInstanceState);

        if (pagerViewID != -1) {
            setContentView(pagerViewID);
            mViewPager = (ViewPager) findViewById(android.R.id.tabhost);
        } else {
            mViewPager = new ViewPager(this);
            mViewPager.setId(android.R.id.tabhost);
            setContentView(mViewPager);
        }

        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mTabsAdapter = new TabsAdapter(this, mViewPager, this);

        if (savedInstanceState != null) {
            try {
                mActionBar.setSelectedNavigationItem(savedInstanceState.getInt(TAB_STATE, 0));
            } catch (Exception ignored) {
            }
        }
    }

    protected void addTab(ActionBar.Tab tab, Class<?> _class, Bundle args) {
        mTabsAdapter.addTab(tab, _class, args);
    }

    protected void addTab(int resId, Class<?> _class) {
        addTab(resId, _class, null);
    }

    protected void addTab(int resId, Class<?> _class, Bundle args) {
        addTab(mActionBar.newTab().setText(resId), _class, args);
    }

    protected void setTitle(String s) {
        mActionBar.setTitle(s);
    }

    protected IBinder getApplicationWindowToken() {
        return mViewPager.getApplicationWindowToken();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAB_STATE, getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }
}
