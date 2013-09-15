/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.content.Context;

import com.oak.OakApi;
import com.oak.OakConfig;

import java.net.URLEncoder;

public class OakUrlFactory extends GetRequestFactory {

    public OakUrlFactory(String endpoint) {
        super(OakConfig.ROOT_DOMAIN + endpoint);
    }

    @Override
    public OakUrlFactory add(String key, String value) {
        int index = indexOf(key);
        if (index != -1) {
            replace(index, index + key.length(), value);
            return this;
        } else {
            //noinspection deprecation
            return (OakUrlFactory) super.add(key, URLEncoder.encode(value));
        }
    }

    public OakUrlFactory addDeviceID(Context c) {
        return add(OakApi.DEVICE_ID, Installation.id(c));
    }

    public OakUrlFactory add(String key, long value) {
        return add(key, String.valueOf(value));
    }
}
