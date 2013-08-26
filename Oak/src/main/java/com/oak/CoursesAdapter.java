/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oak.db.CoursesContract;

public class CoursesAdapter extends CursorHeaderAdapter {

    public CoursesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newItemView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    protected void bindHeaderView(View view, TextView headerText, Context context, Cursor cursor) {
        String thisPwd = cursor.getString(cursor.getColumnIndex(CoursesContract.COLUMN_PASSWORD));
        if (TextUtils.isEmpty(thisPwd)) {
            headerText.setText(R.string.available_courses_title);
        } else {
            headerText.setText(R.string.previously_joined_courses_title);
        }
    }

    @Override
    protected void bindItemView(View view, Context context, Cursor cursor) {
        TextView tv;

        tv = (TextView) view.findViewById(android.R.id.text1);
        tv.setText(cursor.getString(cursor.getColumnIndex(CoursesContract.COLUMN_NAME)));
    }

    @Override
    protected boolean isNewGroup(Cursor cursor, int position) {
        String thisPwd = cursor.getString(cursor.getColumnIndex(CoursesContract.COLUMN_PASSWORD));
        cursor.moveToPosition(position - 1);
        String prevPwd = cursor.getString(cursor.getColumnIndex(CoursesContract.COLUMN_PASSWORD));
        cursor.moveToPosition(position);

        if (TextUtils.isEmpty(thisPwd) && !TextUtils.isEmpty(prevPwd)) {
            return true;
        } else {
            return false;
        }
    }

}
