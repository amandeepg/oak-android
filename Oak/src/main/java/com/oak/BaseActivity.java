/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

public class BaseActivity extends ActionBarActivity {
    protected final String TAG = ((Object) this).getClass().getSimpleName();

    private PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        Crashlytics.log(Log.DEBUG, TAG, TAG + " onCreate");
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
    }

    PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

    @Override
    public void onStart() {
        super.onStart();
        Crashlytics.log(Log.DEBUG, TAG, TAG + " onStart");
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Crashlytics.log(Log.DEBUG, TAG, TAG + " onStop");
        EasyTracker.getInstance(this).activityStop(this);
    }
}
