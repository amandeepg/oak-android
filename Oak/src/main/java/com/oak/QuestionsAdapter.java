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
        super(context, c, flags);
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
        TextView questionText = (TextView) view.findViewById(R.id.questionText);
        TextView pointsText = (TextView) view.findViewById(R.id.pointsText);
        final CheckBox resolvedCheck = ((CheckBox) view.findViewById(R.id.resolvedCheckBox));
        final Button upButton = (Button) view.findViewById(R.id.upButton);
        final Button downButton = (Button) view.findViewById(R.id.downButton);
        final Button upButtonSel = (Button) view.findViewById(R.id.upButtonSel);
        final Button downButtonSel = (Button) view.findViewById(R.id.downButtonSel);

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

        if (q.getDeviceVote().equals("-1")) {
            flipVisibility(downButtonSel, downButton);
        } else {
            flipVisibility(downButton, downButtonSel);
        }

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionListener.onVote("1", q.getId());
                flipVisibility(upButtonSel, upButton);
            }
        });
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionListener.onVote("-1", q.getId());
                flipVisibility(downButtonSel, downButton);
            }
        });
        upButtonSel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionListener.onVote("0", q.getId());
                flipVisibility(upButton, upButtonSel);
            }
        });
        downButtonSel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionListener.onVote("0", q.getId());
                flipVisibility(downButton, downButtonSel);
            }
        });
        resolvedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                questionListener.onResolve(isChecked, q.getId());
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
