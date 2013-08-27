/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.oak.utils.AppMsgFactory;
import com.oak.utils.OakGetRequestFactory;
import com.oak.utils.OakPostParams;
import com.oak.volley.JsonPostRequest;

import org.json.JSONObject;

public class OakApi {

    public static final String COURSE_CODE = "courseCode";
    public static final String COURSE_PASSWORD = "coursePassword";
    public static final String QUESTION = "question";
    public static final String RESOLVE_VOTE = "resolveVote";
    public static final String QUESTION_ID = "questionId";
    public static final String VOTE = "vote";

    public static Request getCourses(final Activity activity,
                                     final Response.Listener<JSONObject> responseListener,
                                     final Response.ErrorListener errorListener){
        return new JsonObjectRequest(
                Request.Method.GET,
                new OakGetRequestFactory("CourseList").url(),
                null,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request getQuestions(final FragmentActivity activity,
                                       final Response.Listener<JSONObject> responseListener,
                                       final Response.ErrorListener errorListener,
                                       final Bundle data) {
        return new JsonObjectRequest(
                Request.Method.GET,
                new OakGetRequestFactory("QuestionList")
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
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
        return new JsonObjectRequest(
                Request.Method.GET,
                new OakGetRequestFactory("UnderstandingStatus")
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .addDeviceID(activity)
                        .url(),
                null,
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postCoursePassword(final Activity activity,
                                             final Response.Listener<String> responseListener,
                                             final Response.ErrorListener errorListener,
                                             final Bundle data){
        return new StringRequest(
                Request.Method.GET,
                new OakGetRequestFactory("VerifyCoursePassword")
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .url(),
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postCourse(final Activity activity,
                                     final Response.Listener<JSONObject> responseListener,
                                     final Response.ErrorListener errorListener,
                                     final Bundle data){
        return new JsonPostRequest(
                OakConfig.endPoint("AddCourse"),
                new OakPostParams()
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD)),
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postQuestion(final Activity activity,
                                       final Response.Listener<JSONObject> responseListener,
                                       final Response.ErrorListener errorListener,
                                       final Bundle data){
        return new JsonPostRequest(
                OakConfig.endPoint("AddQuestion"),
                new OakPostParams()
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .add(QUESTION, data.getString(QUESTION)),
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postResolve(final Activity activity,
                                      final Response.Listener<JSONObject> responseListener,
                                      final Response.ErrorListener errorListener,
                                      final Bundle data){
        return new JsonPostRequest(
                OakConfig.endPoint("ResolveQuestion"),
                new OakPostParams()
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .add(RESOLVE_VOTE, data.getString(RESOLVE_VOTE))
                        .add(QUESTION_ID, data.getString(QUESTION_ID))
                        .addDeviceID(activity),
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postQuestionVote(final Activity activity,
                                           final Response.Listener<JSONObject> responseListener,
                                           final Response.ErrorListener errorListener,
                                           final Bundle data){
        return new JsonPostRequest(
                OakConfig.endPoint("VoteQuestion"),
                new OakPostParams()
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .add(VOTE, data.getString(VOTE))
                        .add(QUESTION_ID, data.getString(QUESTION_ID))
                        .addDeviceID(activity),
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    public static Request postUnderstanding(final Activity activity,
                                            final Response.Listener<JSONObject> responseListener,
                                            final Response.ErrorListener errorListener,
                                            final Bundle data){
        return new JsonPostRequest(
                OakConfig.endPoint("VoteCourse"),
                new OakPostParams()
                        .add(COURSE_CODE, data.getString(COURSE_CODE))
                        .add(COURSE_PASSWORD, data.getString(COURSE_PASSWORD))
                        .add(VOTE, data.getString(VOTE))
                        .addDeviceID(activity),
                createResponseListener(responseListener),
                createErrorListener(activity, errorListener)
        );
    }

    private static Response.ErrorListener createErrorListener(final Activity activity,
                                                              final Response.ErrorListener errorListener) {
        return  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AppMsgFactory.somethingWentWrong(activity);
                if (errorListener != null) {
                    errorListener.onErrorResponse(error);
                }
            }
        };
    }

    private static <T> Response.Listener<T> createResponseListener(final Response.Listener<T> responseListener) {
        return new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                if (responseListener != null) {
                    responseListener.onResponse(response);
                }
            }
        };
    }
}
