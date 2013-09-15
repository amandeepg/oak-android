/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;

public class CoursesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        // Check if this activity was created before
        if (savedInstanceState == null) {
            // Create a fragment
            CoursesFragment fragment = new CoursesFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment, null)
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
}
