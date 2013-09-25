/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.oak.utils.AppMsgFactory;
import com.oak.utils.OakJSONObject;
import com.oak.utils.OakUrlFactory;

import org.json.JSONObject;

public class OakApi {

    public static final String TAG = "OakApi";

    public static final String COURSE_CODE = "courseCode";
    public static final String COURSE_ID = "<course_id>";
    public static final String COURSE_PASSWORD = "coursePassword";
    public static final String QUESTION = "question";
    public static final String RESOLVE_VOTE = "deviceResolveVote";
    public static final String QUESTION_ID = "<question_id>";
    public static final String DEVICE_ID = "deviceId";
    public static final String VOTE = "vote";
    public static final String DEVICE_VOTE = "deviceVote";

    public static Request getCourses(final Activity activity,
                                     final Response.Listener<JSONObject> responseListener,
                                     final Response.ErrorListener errorListener){
        Crashlytics.log(Log.DEBUG, TAG, "start load: getCourses");
        return new JsonObjectRequest(
                Request.Method.GET,
                new OakUrlFactory("courses").url(),
                null,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request getQuestions(final FragmentActivity activity,
                                       final Response.Listener<JSONObject> responseListener,
                                       final Response.ErrorListener errorListener,
                                       final Bundle data) {
        Crashlytics.log(Log.DEBUG, TAG, "start load: getQuestions");
        return new JsonObjectRequest(
                Request.Method.GET,
                new OakUrlFactory("courses/<course_id>/questions")
                        .add(COURSE_ID, data.getString(COURSE_ID))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .addDeviceID(activity)
                        .url(),
                null,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request getUnderstanding(final FragmentActivity activity,
                                           final Response.Listener<JSONObject> responseListener,
                                           final Response.ErrorListener errorListener,
                                           final Bundle data) {
        Crashlytics.log(Log.DEBUG, TAG, "start load: getUnderstanding");
        return new JsonObjectRequest(
                Request.Method.GET,
                new OakUrlFactory("courses/<course_id>")
                        .add(COURSE_ID, data.getString(COURSE_ID))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .addDeviceID(activity)
                        .url(),
                null,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postCourse(final Activity activity,
                                     final Response.Listener<JSONObject> responseListener,
                                     final Response.ErrorListener errorListener,
                                     final JSONObject requestData) {
        Crashlytics.log(Log.DEBUG, TAG, "start load: postCourse: " + requestData.toString());
        return new JsonObjectRequest(
                new OakUrlFactory("courses").url(),
                requestData,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postQuestion(final Activity activity,
                                       final Response.Listener<JSONObject> responseListener,
                                       final Response.ErrorListener errorListener,
                                       final Bundle data,
                                       final OakJSONObject requestData) {
        Crashlytics.log(Log.DEBUG, TAG, "start load: postQuestion: " + requestData.toString());
        return new JsonObjectRequest(
                new OakUrlFactory("courses/<course_id>/questions")
                        .add(COURSE_ID, data.getString(COURSE_ID))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .addDeviceID(activity)
                        .url(),
                requestData,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postQuestionVote(final Activity activity,
                                           final Response.Listener<JSONObject> responseListener,
                                           final Response.ErrorListener errorListener,
                                           final Bundle data,
                                           final OakJSONObject requestData) {
        Crashlytics.log(Log.DEBUG, TAG, "start load: postQuestionVote: " + requestData.toString());
        return new JsonObjectRequest(
                new OakUrlFactory("courses/<course_id>/questions/<question_id>")
                        .add(COURSE_ID, data.getString(COURSE_ID))
                        .add(QUESTION_ID, data.getString(QUESTION_ID))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .addDeviceID(activity)
                        .url(),
                requestData,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postUnderstanding(final Activity activity,
                                            final Response.Listener<JSONObject> responseListener,
                                            final Response.ErrorListener errorListener,
                                            final Bundle data,
                                            final OakJSONObject requestData) {
        Crashlytics.log(Log.DEBUG, TAG, "start load: postUnderstanding: " + requestData.toString());
        return new JsonObjectRequest(
                new OakUrlFactory("courses/<course_id>")
                        .add(COURSE_ID, data.getString(COURSE_ID))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .addDeviceID(activity)
                        .url(),
                requestData,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    private static Response.ErrorListener createErrorListener(final Activity activity,
                                                              final Response.ErrorListener errorListener) {
        return  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                String errSt = response != null ? String.valueOf(response.statusCode) : error.toString();
                Crashlytics.log(Log.DEBUG, TAG, "load error: " + errSt);
                AppMsgFactory.somethingWentWrong(activity);
                if (errorListener != null) {
                    errorListener.onErrorResponse(error);
                }
            }
        };
    }

    private static Response.Listener<JSONObject> createResponseListener(
            final Response.Listener<JSONObject> responseListener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Crashlytics.log(Log.DEBUG, TAG, "load success: " + response.optString("status"));
                if (responseListener != null) {
                    responseListener.onResponse(response.optJSONObject("data"));
                }
            }
        };
    }
}
