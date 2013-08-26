/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.util.Log;

public class GetRequestFactory {

    private static final String TAG = "GetRequestFactory";
    StringBuilder sb;
    boolean keyAdded;

    public GetRequestFactory(String root) {
        sb = new StringBuilder();
        sb.append(root);
        keyAdded = false;
    }

    public GetRequestFactory add(String key, String value) {
        if (!keyAdded) {
            sb.append("?");
            keyAdded = true;
        } else {
            sb.append("&");
        }
        sb.append(key).append("=").append(value);
        return this;
    }

    public String url() {
        Log.d(TAG, "firing get: " + sb.toString());
        return sb.toString();
    }

    public String toString() {
        return sb.toString();
    }
}
