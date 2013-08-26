/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class CursorHeaderAdapter extends CursorAdapter {

    protected static final int TYPE_GROUP_START = 0;
    protected static final int TYPE_GROUP_CONT = 1;

    protected static final int TYPE_COUNT = 2;

    protected final LayoutInflater mInflater;

    public CursorHeaderAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        // There is always a group header for the first data item
        if (isAlwaysHeaderPosition(position)) {
            return TYPE_GROUP_START;
        }

        // For other items, decide based on current data
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        boolean newGroup = isNewGroup(cursor, position);

        // Check item grouping
        if (newGroup) {
            return TYPE_GROUP_START;
        } else {
            return TYPE_GROUP_CONT;
        }
    }

    protected boolean isAlwaysHeaderPosition(int position) {
        return position == 0;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int position = cursor.getPosition();
        final int viewType = getItemViewType(position);

        View v;
        if (viewType == TYPE_GROUP_START) {
            // Inflate a layout to start a new group
            LinearLayout linLay;
            if (Build.VERSION.SDK_INT >= 11) {
                linLay = new HeaderLinearLayout(context);
            } else {
                linLay = new LinearLayout(context);
            }
            linLay.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linLay.setOrientation(LinearLayout.VERTICAL);

            View headerView = mInflater.inflate(android.R.layout.preference_category, linLay, false);
            View itemView = newItemView(context, cursor, linLay);

            if (Build.VERSION.SDK_INT >= 11) {
                ((HeaderLinearLayout) linLay).setTopHeaderView(headerView);
            }
            linLay.addView(headerView);
            linLay.addView(itemView);

            // Ignore clicks on the list header
            headerView.setClickable(true);
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            v = linLay;
        } else {
            // Inflate a layout for "regular" items
            v = newItemView(context, cursor, parent);
        }
        return v;
    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        bindItemView(view, context, cursor);

        // If there is a group header, set its value to just the date
        TextView headerText = (TextView) view.findViewById(android.R.id.title);
        if (headerText != null) {
            bindHeaderView(view, headerText, context, cursor);
        }
    }

    protected abstract void bindHeaderView(View view, TextView headerText, Context context, Cursor cursor);

    protected abstract void bindItemView(View view, Context context, Cursor cursor);

    protected abstract View newItemView(Context context, Cursor cursor, ViewGroup parent);

    protected boolean isNewGroup(Cursor cursor, int position) {
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected static class HeaderLinearLayout extends LinearLayout implements AbsListView.SelectionBoundsAdjuster {

        public HeaderLinearLayout(Context context) {
            super(context);
        }

        private View mTopHeaderView;

        public void setTopHeaderView(View v){
            mTopHeaderView = v;
        }

        @Override
        public void adjustListItemSelectionBounds(Rect bounds) {
            bounds.top += mTopHeaderView.getHeight();
        }
    }
}
