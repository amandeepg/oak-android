/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;
import com.devspark.appmsg.MsgManager;
import com.oak.R;

public class AppMsgFactory {

    public static AppMsg startMsg(Fragment frag, int resId) {
        return startMsg(frag.getActivity(), resId);
    }

    public static AppMsg startMsg(Activity act, int resId) {
        AppMsg appMsg = AppMsg.makeText(act, resId, AppMsg.STYLE_CONFIRM);
        appMsg.show();
        return appMsg;
    }

    public static void finishMsg(Activity act, int resId) {
        finishMsg(act, resId, R.color.info);
    }

    public static void finishMsg(Fragment frag, int resId) {
        finishMsg(frag.getActivity(), resId, R.color.info);
    }

    public static void finishMsg(Fragment frag, int resId, int colour) {
        finishMsg(frag.getActivity(), resId, colour);
    }

    public static void finishMsg(Activity act, int resId, int colour) {
        finishMsg(act);
        AppMsg.makeText(act, resId, new Style(AppMsg.LENGTH_SHORT, colour)).show();
    }

    public static void finishMsg(Activity act) {
        MsgManager.getInstance().clearAllMsg();
        View v = act.findViewById(R.id.app_msg_holder);
        if (v != null && v.getParent() != null) {
            ((ViewGroup) v.getParent()).removeView(v);
        }
    }

    public static void somethingWentWrong(Activity act) {
        finishMsg(act, R.string.something_went_wrong, R.color.alert);
    }
}
