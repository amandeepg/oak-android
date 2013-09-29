/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.oak.utils.TabSwipeActivity;

public class QMTabActivity extends TabSwipeActivity {
    public static Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.qm_tabs);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            course = b.getParcelable("course");
        }

        Crashlytics.log(Log.DEBUG, TAG, "Viewing " + course.getName());

        addTab(R.string.umeter_title, UMeterFragment.class);
        addTab(R.string.questions_title, QuestionsFragment.class);
        setTitle(course.getName());

        // Show the Up button in the action bar.
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
