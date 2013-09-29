/*
 * Copyright (c) 2013 Amandeep Grewal
 * Initial version taken from http://arnab.ch/blog/2013/08/asynchronous-http-requests-in-android-using-volley/
 */

package com.oak;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.plus.PlusClient;
import com.oak.volley.OkHttpStack;

public class AppController extends Application {

    /**
     * Log or request DEFAULT_VOLLEY_TAG
     */
    public static final String DEFAULT_VOLLEY_TAG = "DefaultVolley";

    private RequestQueue mRequestQueue;
    private PlusClient mPlusClient;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static AppController sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        sInstance = this;
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized AppController getInstance() {
        return sInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new OkHttpStack());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default DEFAULT_VOLLEY_TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? DEFAULT_VOLLEY_TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default DEFAULT_VOLLEY_TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(DEFAULT_VOLLEY_TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified DEFAULT_VOLLEY_TAG, it is important
     * to specify a DEFAULT_VOLLEY_TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public PlusClient getPlusClient(){
        return mPlusClient;
    }

    public void setPlusClient(PlusClient plusClient) {
        this.mPlusClient = plusClient;
    }
}
