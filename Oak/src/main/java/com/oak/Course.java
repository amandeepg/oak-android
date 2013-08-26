/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.database.Cursor;

import com.oak.db.CoursesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String password;
    private long id;
    private String name;

    public Course() {
    }

    public Course(Cursor c) {
        this();
        name = c.getString(c.getColumnIndex(CoursesContract.COLUMN_NAME));
        password = c.getString(c.getColumnIndex(CoursesContract.COLUMN_PASSWORD));
    }

    public static List<Course> parseJson(JSONObject json) {
        try {
            return parseJsonAndThrow(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Course> parseJsonAndThrow(JSONObject json) throws JSONException {
        JSONArray coursesFromServer = json.getJSONArray("courses");
        ArrayList<Course> newData = new ArrayList<Course>(coursesFromServer.length());

        for (int x = 0; x < coursesFromServer.length(); x++) {
            Course course = new Course();
            course.setName(coursesFromServer.getString(x));
            newData.add(course);
        }

        return newData;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}