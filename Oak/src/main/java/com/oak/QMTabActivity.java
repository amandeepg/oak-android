/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.os.Bundle;

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

        addTab(R.string.umeter_title, UMeterFragment.class);
        addTab(R.string.questions_title, QuestionsFragment.class);
        setTitle(course.getName());
    }
}
