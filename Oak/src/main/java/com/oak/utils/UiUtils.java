/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import com.oak.R;

public class UiUtils {

    public static void showDialogWithKeyboard(AlertDialog.Builder builder, View v) {
        final AlertDialog dialog = builder.create();
        View editText = v.findViewById(R.id.course_name);
        if (editText == null) {
            editText = v.findViewById(R.id.course_pass);
        }

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        dialog.show();
    }
}
