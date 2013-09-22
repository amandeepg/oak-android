/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.os.Bundle;
public class CoursesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}
