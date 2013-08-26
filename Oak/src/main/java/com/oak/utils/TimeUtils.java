/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.text.format.DateUtils;

public class TimeUtils {
    public static String getTimeAgo(long lastVoteTime) {
        return DateUtils.getRelativeTimeSpanString(lastVoteTime, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
    }
}
