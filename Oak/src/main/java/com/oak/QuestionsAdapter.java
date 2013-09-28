/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.oak.utils.TimeUtils;

public class QuestionsAdapter extends CursorHeaderAdapter {

    private final QuestionListener questionListener;

    public QuestionsAdapter(Context context, Cursor c, int flags, QuestionListener ql) {
        super(context, c, flags, R.layout.question_title_row, R.id.title);
        questionListener = ql;
    }

    protected boolean isAlwaysHeaderPosition(int position) {
        return super.isAlwaysHeaderPosition(position) || position == 1;
    }

    @Override
    public View newItemView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.question_row, parent, false);
    }

    @Override
    protected void bindItemView(View view, Context context, Cursor cursor) {
        final Question q = new Question(cursor);

        final TextView countText = (TextView) view.findViewById(R.id.votesText);
        final TextView questionText = (TextView) view.findViewById(R.id.questionText);
        final TextView pointsText = (TextView) view.findViewById(R.id.pointsText);
        final CheckBox resolvedCheck = ((CheckBox) view.findViewById(R.id.resolvedCheckBox));
        final Button upButton = (Button) view.findViewById(R.id.upButton);
        final Button upButtonSel = (Button) view.findViewById(R.id.upButtonSel);

        resolvedCheck.setOnCheckedChangeListener(null);

        countText.setText(String.valueOf(q.getVotes()));
        pointsText.setText(TimeUtils.getTimeAgo(q.getTimeCreated()));
        questionText.setText(q.getQuestion());
        resolvedCheck.setChecked(q.isDeviceResolveVote());

        if (q.getDeviceVote().equals("1")) {
            flipVisibility(upButtonSel, upButton);
        } else {
            flipVisibility(upButton, upButtonSel);
        }

        final String id = q.getId();
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionListener.onVote("1", id);
                flipVisibility(upButtonSel, upButton);
            }
        });
        upButtonSel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionListener.onVote("0", id);
                flipVisibility(upButton, upButtonSel);
            }
        });
        resolvedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                questionListener.onResolve(isChecked, id);
            }
        });
    }

    private void flipVisibility(View viewToShow, View viewToHide) {
        viewToShow.setVisibility(View.VISIBLE);
        viewToHide.setVisibility(View.GONE);
    }

    @Override
    protected void bindHeaderView(View view, TextView headerText, Context context, Cursor cursor) {
        if (cursor.getPosition() == 0) {
            headerText.setText(context.getString(R.string.top_question));
        } else {
            headerText.setText(context.getString(R.string.rising_questions));
        }
    }

    public interface QuestionListener {
        public void onResolve(boolean isResolved, String id);
        public void onVote(String vote, String id);
    }

}
