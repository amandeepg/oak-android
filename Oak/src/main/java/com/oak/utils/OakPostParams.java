/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.content.Context;
import android.util.Log;

import com.oak.OakConfig;

import java.util.HashMap;

public class OakPostParams extends HashMap<String, String> {

    private static final String TAG = "OakPostParams";

    public OakPostParams() {
        super();
        add("password", OakConfig.PASSWORD);
    }

    public OakPostParams add(String key, String val) {
        put(key, val);
        return this;
    }

    public OakPostParams addDeviceID(Context c) {
        printMap();
        return add("deviceId", Installation.id(c));
    }

    public OakPostParams printMap() {
        Log.d(TAG, toString());
        return this;
    }
}
