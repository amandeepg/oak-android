/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.content.Context;

import com.oak.OakConfig;

import java.net.URLEncoder;

public class OakGetRequestFactory extends GetRequestFactory {

    public OakGetRequestFactory(String endpoint) {
        super(OakConfig.ROOT_DOMAIN + endpoint);
        add("password", OakConfig.PASSWORD);
    }

    @Override
    public OakGetRequestFactory add(String key, String value) {
        //noinspection deprecation
        return (OakGetRequestFactory) super.add(key, URLEncoder.encode(value));
    }

    public OakGetRequestFactory addDeviceID(Context c) {
        return add("deviceId", Installation.id(c));
    }
}
