/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.michaelpardo.android.widget.chartview.ChartView;
import com.michaelpardo.android.widget.chartview.LinearSeries;
import com.oak.R.id;
import com.oak.utils.OakJSONObject;
import com.oak.utils.TimeUtils;

import org.json.JSONObject;

public class UMeterFragment extends BaseFragment {

    private LinearSeries mSeries;
    private int mCurrGraphPosX;
    private ChartView mChartView;
    private TextView mVotedAgoTextView;
    private int mWaitingChanges;
    private long mLastVoteTime;
    private SeekBar mVoteBar;
    private Runnable mLoadRunnable;

    public UMeterFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState != null) {
            mCurrGraphPosX = savedInstanceState.getInt("mCurrGraphPosX");
            mLastVoteTime = savedInstanceState.getLong("mLastVoteTime");
            mSeries = savedInstanceState.getParcelable("mSeries");
        } else {
            mCurrGraphPosX = 20;
            mLastVoteTime = -1;
            mSeries = new LinearSeries();
        }

        final View v = inflater.inflate(R.layout.umeter, container, false);

        // Find the chart view
        mChartView = (ChartView) v.findViewById(R.id.chart_view);
        mChartView.setGridLineColor(Color.LTGRAY);

        // Create the data points
        mSeries.setLineColor(0xFF0099CC);
        mSeries.setLineWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));

        // Add chart view data
        mChartView.addSeries(mSeries);

        mVotedAgoTextView = (TextView) v.findViewById(id.time_ago);
        mVoteBar = (SeekBar) v.findViewById(id.my_understanding);

        mVoteBar.setProgress(50);

        setTimeAgoText();
        setVoteBarListener();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        createLoadRequest();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mLoadRunnable);
    }

    @Override
     public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mCurrGraphPosX", mCurrGraphPosX);
        outState.putLong("mLastVoteTime", mLastVoteTime);
        outState.putParcelable("mSeries", mSeries);
    }

    private void createLoadRequest() {
        Request loadRequest = OakApi.getUnderstanding(getActivity(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        onUnderstandingLoaded(json);
                    }
                },
                null,
                getCourseDataBundle()
        );
        addRequest(loadRequest);
    }

    private void postLoadDelayed(final long delayMillis) {
        mLoadRunnable = new Runnable() {
            @Override
            public void run() {
                createLoadRequest();
            }
        };
        mHandler.postDelayed(mLoadRunnable, delayMillis);
    }

    private void setVoteBarListener() {
        mVoteBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mLastVoteTime = System.currentTimeMillis();
                setTimeAgoText();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myUnderstandingChanged(seekBar.getProgress());
            }
        });
    }

    private void myUnderstandingChanged(final int progress) {
        mWaitingChanges++;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mWaitingChanges--;
                if (mWaitingChanges == 0) {
                    Log.d(TAG, "progress = " + progress);

                    OakJSONObject data = new OakJSONObject();
                    data.safePut(OakApi.VOTE, String.valueOf(progress));

                    Request req = OakApi.postUnderstanding(
                            getActivity(),
                            null, null,
                            getCourseDataBundle(), data);
                    addRequest(req);
                }
            }
        }, OakConfig.NETWORK_POST_BUFFER_MILLIS);
    }

    public void onUnderstandingLoaded(JSONObject json) {
        if (json != null) {
            int understanding = json.optInt("understanding");

            if (mLastVoteTime == -1) {
                mVoteBar.setProgress(json.optInt("deviceUnderstanding"));
                mLastVoteTime = json.optString("deviceTimeLastVoted").equals("N/A") ? -1 : json.optLong("deviceTimeLastVoted");
            }

            mSeries.addPoint(new LinearSeries.LinearPoint(mCurrGraphPosX, understanding + (int) (Math.random() * 1)));
            mChartView.setRange(mCurrGraphPosX - 20, mCurrGraphPosX, 0, 100);
            mSeries.setRange(mCurrGraphPosX - 20, mCurrGraphPosX, 0, 100);
            mCurrGraphPosX++;
            mChartView.invalidate();

            setTimeAgoText();
        }
        postLoadDelayed(OakConfig.AUTO_REFRESH_UNDERSTANDING_MILLIS);
    }

    private void setTimeAgoText() {
        String timeAgo;
        if (mLastVoteTime == -1) {
            timeAgo = getString(R.string.never);
        } else if (System.currentTimeMillis() - mLastVoteTime < 1000) {
            timeAgo = getString(R.string.now);
        } else {
            timeAgo = TimeUtils.getTimeAgo(mLastVoteTime);
        }

        mVotedAgoTextView.setText(getString(R.string.last_vote) + " " + timeAgo);
    }

    private Bundle getCourseDataBundle() {
        Bundle data = new Bundle();
        data.putString(OakApi.COURSE_ID, QMTabActivity.course.getId());
        data.putString(OakApi.COURSE_PASSWORD, QMTabActivity.course.getPassword());
        return data;
    }
}
