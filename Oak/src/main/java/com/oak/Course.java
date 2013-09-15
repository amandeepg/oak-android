/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.oak.db.CoursesContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Course implements Parcelable {
    private String password;
    private String id;
    private String name;

    public Course() {
    }

    public Course(Cursor c) {
        this();
        name = c.getString(c.getColumnIndex(CoursesContract.COLUMN_NAME));
        password = c.getString(c.getColumnIndex(CoursesContract.COLUMN_PASSWORD));
        id = c.getString(c.getColumnIndex(CoursesContract.COLUMN_ID));
    }

    public Course(JSONObject obj) {
        this();
        name = obj.optString("courseCode");
        id = obj.optString("courseId");
    }

    public static List<Course> parseJson(JSONObject json) {
        JSONArray coursesFromServer = json.optJSONArray("courses");
        ArrayList<Course> newData = new ArrayList<Course>(coursesFromServer.length());

        for (int x = 0; x < coursesFromServer.length(); x++) {
            JSONObject obj = coursesFromServer.optJSONObject(x);
            Course course = new Course(obj);
            newData.add(course);
        }

        return newData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(password);
        out.writeString(id);
        out.writeString(name);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    private Course(Parcel in) {
        password = in.readString();
        id = in.readString();
        name = in.readString();
    }

}
