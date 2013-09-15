/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.database.Cursor;

import com.oak.db.QuestionsContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Question {

    private boolean deviceResolveVote;
    private int votes;
    private long timeCreated;
    private String question;
    private String deviceVote;
    private String id;
    private String courseId;
    private long weightedImportance;

    public Question() {
    }

    public Question(Cursor c) {
        this();

        deviceResolveVote = c.getInt(c.getColumnIndex(QuestionsContract.COLUMN_DEVICE_RESOLVE_VOTE)) == 1;
        votes = c.getInt(c.getColumnIndex(QuestionsContract.COLUMN_VOTES));
        timeCreated = c.getLong(c.getColumnIndex(QuestionsContract.COLUMN_TIME_CREATED));
        question = c.getString(c.getColumnIndex(QuestionsContract.COLUMN_QUESTION));
        deviceVote = c.getString(c.getColumnIndex(QuestionsContract.COLUMN_DEVICE_VOTE));
        id = c.getString(c.getColumnIndex(QuestionsContract.COLUMN_ID));
        courseId = c.getString(c.getColumnIndex(QuestionsContract.COLUMN_COURSE_ID));
        weightedImportance = c.getInt(c.getColumnIndex(QuestionsContract.COLUMN_WEIGHTED_IMPORTANCE));
    }

    public static ArrayList<Question> parseJson(JSONObject dataFromServer, String courseId) {
        JSONArray questionsArr = dataFromServer.optJSONArray("questions");
        ArrayList<Question> newData = new ArrayList<Question>(questionsArr.length());

        for (int x = 0; x < questionsArr.length(); x++) {
            JSONObject question = questionsArr.optJSONObject(x);
            Question q = parseJsonSingle(question);
            q.setCourseId(courseId);
            newData.add(q);
        }

        return newData;
    }

    private static Question parseJsonSingle(JSONObject json) {
        Question question = new Question();
        question.question = json.optString("question");
        question.id = json.optString("questionId");
        question.votes = Integer.parseInt(json.optString("votes").trim());
        question.weightedImportance = Long.parseLong(json.optString("weightedImportance").trim());
        question.timeCreated = Long.parseLong(json.optString("timeCreated").trim());
        question.deviceVote = json.optString("deviceVote");
        question.deviceResolveVote = json.optString("deviceResolveVote").equals("1");
        return question;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceVote() {
        return deviceVote;
    }

    public void setDeviceVote(String deviceVote) {
        this.deviceVote = deviceVote;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public boolean isDeviceResolveVote() {
        return deviceResolveVote;
    }

    public void setDeviceResolveVote(boolean deviceResolveVote) {
        this.deviceResolveVote = deviceResolveVote;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public long getWeightedImportance() {
        return weightedImportance;
    }

    public void setWeightedImportance(long weightedImportance) {
        this.weightedImportance = weightedImportance;
    }
}
