/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.text.format.DateUtils;
import android.text.format.Time;

public class OakConfig {

    public static final String PASSWORD = "EngSci";
    public static final String ROOT_DOMAIN = "http://oak-server.amandeep.ca/";

    public static final long AUTO_REFRESH_COURSES_MILLIS = 10 * DateUtils.SECOND_IN_MILLIS;
    public static final long AUTO_REFRESH_QUESTIONS_MILLIS = 10 * DateUtils.SECOND_IN_MILLIS;
    public static final long AUTO_REFRESH_UNDERSTANDING_MILLIS = DateUtils.SECOND_IN_MILLIS;

    public static final long NETWORK_POST_BUFFER_MILLIS = 5* DateUtils.SECOND_IN_MILLIS;

    public static String endPoint(String endPointStr) {
        return ROOT_DOMAIN + endPointStr;
    }
}
