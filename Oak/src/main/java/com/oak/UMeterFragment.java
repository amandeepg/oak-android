/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.michaelpardo.android.widget.chartview.ChartView;
import com.michaelpardo.android.widget.chartview.LinearSeries;
import com.oak.R.id;
import com.oak.utils.AppMsgFactory;
import com.oak.utils.NetworkUtils;
import com.oak.utils.OakPostParams;
import com.oak.utils.OakGetRequestFactory;
import com.oak.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UMeterFragment extends BaseFragment {

    private LinearSeries mSeries;
    private int mCurrGraphPosX;
    private ChartView mChartView;
    private TextView mVotedAgoTextView;
    private int mWaitingChanges;
    private long mLastVoteTime;
    private SeekBar mVoteBar;

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

        // Create the data points
        mSeries.setLineColor(0xFF0099CC);
        mSeries.setLineWidth(3);

        // Add chart view data
        mChartView.addSeries(mSeries);

        mVotedAgoTextView = (TextView) v.findViewById(id.time_ago);
        mVoteBar = (SeekBar) v.findViewById(id.my_understanding);

        mVoteBar.setProgress(50);

        setTimeAgoText();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        createLoadRequest();
    }

    @Override
     public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mCurrGraphPosX", mCurrGraphPosX);
        outState.putLong("mLastVoteTime", mLastVoteTime);
        outState.putParcelable("mSeries", mSeries);
    }

    private void createLoadRequest() {
        JsonObjectRequest loadRequest = new JsonObjectRequest(
                Request.Method.GET,
                new OakGetRequestFactory("UnderstandingStatus")
                        .add("courseCode", QMTabActivity.courseCode)
                        .add("coursePassword", QMTabActivity.coursePass)
                        .addDeviceID(getActivity())
                        .url(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        onUnderstandingLoaded(json);
                    }
                },
                null
        );

        addRequest(loadRequest);
    }

    private void postLoadDelayed(final long delayMillis) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createLoadRequest();
            }
        }, delayMillis);
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

                    final JsonPostRequest req = new JsonPostRequest(
                            OakConfig.endPoint("VoteCourse"),
                            new OakPostParams()
                                    .add("courseCode", QMTabActivity.courseCode)
                                    .add("coursePassword", QMTabActivity.coursePass)
                                    .add("vote", "" + progress)
                                    .addDeviceID(getActivity()),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    NetworkUtils.printResponse(TAG, "understandingVote", response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    AppMsgFactory.somethingWentWrong(getActivity());
                                }
                            }
                    );
                    addRequest(req);
                }
            }
        }, OakConfig.NETWORK_POST_BUFFER_MILLIS);
    }


    private boolean set_vote = false;

    public void onUnderstandingLoaded(JSONObject json) {
        NetworkUtils.printResponse(TAG, "understanding", json);

        if (json != null) {
            try {
                int understanding = json.getInt("understanding");
                if (!set_vote) {

                    try {
                        mVoteBar.setProgress(json.getInt("deviceVote"));
                    } catch (JSONException e) {
                        mVoteBar.setProgress(50);
                    }
                    try {
                        mLastVoteTime = json.getLong("timeLastVoted");
                    } catch (JSONException e) {
                        mLastVoteTime = -1;
                    }
                    setVoteBarListener();
                    set_vote = true;
                }

                mSeries.addPoint(new LinearSeries.LinearPoint(mCurrGraphPosX, understanding + (int) (Math.random() * 1)));
                mChartView.setRange(mCurrGraphPosX - 20, mCurrGraphPosX, 0, 100);
                mSeries.setRange(mCurrGraphPosX - 20, mCurrGraphPosX, 0, 100);
                mCurrGraphPosX++;
                mChartView.invalidate();

                setTimeAgoText();
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
}
