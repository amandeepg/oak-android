/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.database.Cursor;

import com.oak.db.QuestionsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Question {

    private boolean deviceResolveVote;
    private int votes;
    private long timeCreated;
    private String question;
    private String deviceVote;
    private String id;
    private String courseName;
    private int weightedImportance;

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
        courseName = c.getString(c.getColumnIndex(QuestionsContract.COLUMN_COURSE_NAME));
        weightedImportance = c.getInt(c.getColumnIndex(QuestionsContract.COLUMN_WEIGHTED_IMPORTANCE));
    }


    public static ArrayList<Question> parseJson(JSONObject dataFromServer, String courseCode) {
        try {
            return parseJsonAndThrow(dataFromServer, courseCode);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Question> parseJsonAndThrow(JSONObject dataFromServer, String courseCode) throws JSONException {
        JSONArray topQuestionsArr = dataFromServer.getJSONArray("topVotedQuestion");
        JSONArray questionsArr = dataFromServer.getJSONArray("questions");
        ArrayList<Question> newData = new ArrayList<Question>(questionsArr.length() + topQuestionsArr.length());

        parseJsonArray(topQuestionsArr, newData);
        parseJsonArray(questionsArr, newData);

        for (Question q: newData) {
            q.setCourseName(courseCode);
        }

        return newData;
    }

    private static void parseJsonArray(JSONArray questionsArr, ArrayList<Question> newData) throws JSONException {
        for (int x = 0; x < questionsArr.length(); x++) {
            JSONObject question = questionsArr.getJSONObject(x);
            parseJsonSingle(newData, question);
        }
    }

    private static void parseJsonSingle(ArrayList<Question> newData, JSONObject json) throws JSONException {
        Question question = new Question();
        question.question = json.getString("question");
        question.id = json.getString("id");
        question.votes = Integer.parseInt(json.getString("vote").trim());
        question.weightedImportance = Integer.parseInt(json.getString("weightedImportance").trim());
        question.timeCreated = Long.parseLong(json.getString("timeCreated").trim());
        question.deviceVote = json.getString("deviceVote");
        question.deviceResolveVote = json.getString("deviceResolveVote").equals("1");
        newData.add(question);
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getWeightedImportance() {
        return weightedImportance;
    }

    public void setWeightedImportance(int weightedImportance) {
        this.weightedImportance = weightedImportance;
    }
}
