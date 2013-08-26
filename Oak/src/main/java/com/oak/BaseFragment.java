/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import de.greenrobot.event.EventBus;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

public class BaseFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener {

    protected final String TAG = ((Object) this).getClass().getSimpleName();
    protected RequestQueue mQueue;
    protected Handler mHandler;
    protected final EventBus mBus;
    private boolean mRefreshing;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private int mRefreshMenuItemResId;

    public BaseFragment() {
        mBus = EventBus.getDefault();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mPullToRefreshAttacher = ((BaseActivity) getActivity()).getPullToRefreshAttacher();
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        //mBus.register(this);
        mQueue.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        //mBus.unregister(this);
        mQueue.cancelAll(this);
        mQueue.stop();
    }

    protected void addRequest(Request request) {
        request.setShouldCache(false);
        request.setTag(this);
        mQueue.add(request);
    }

    protected void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            mRefreshing = refreshing;
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == mRefreshMenuItemResId) {
            setRefreshing(true);
            onRefreshStarted(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mRefreshMenuItemResId != 0) {
            MenuItem refresh = menu.findItem(mRefreshMenuItemResId);
            if (mRefreshing) {
                MenuItemCompat.setActionView(refresh, R.layout.actionbar_intermediate_progress);
            } else {
                MenuItemCompat.setActionView(refresh, null);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected void setHasRefreshMenuItem(int refreshMenuItemResId) {
        mRefreshMenuItemResId = refreshMenuItemResId;
    }

    protected void setHasPullToRefresh(View v) {
        mPullToRefreshAttacher.addRefreshableView(v, this);
    }

    protected void setRefreshComplete() {
        mPullToRefreshAttacher.setRefreshComplete();
        setRefreshing(false);
    }

    @Override
    public void onRefreshStarted(View view) {
    }
}
