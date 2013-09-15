/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

public class GetRequestFactory {

    private static final String TAG = "GetRequestFactory";
    private StringBuilder sb;
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
        return sb.toString();
    }

    public String toString() {
        return sb.toString();
    }

    public int indexOf(String string){
        return sb.indexOf(string);
    }

    public void replace(int start, int end, String replacer) {
        sb.replace(start, end, replacer);
    }
}
