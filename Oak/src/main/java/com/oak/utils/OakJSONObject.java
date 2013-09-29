/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import org.json.JSONException;

public class OakJSONObject extends org.json.JSONObject {

    public OakJSONObject() {
        super();
    }

    public <T> OakJSONObject safePut(String name, T value) {
        try {
            super.put(name, value);
            return this;
        } catch (JSONException e) {
            handleError(e);
            return null;
        }
    }

    private static void handleError(JSONException e) {
        throw new RuntimeException(e);
    }
}
