/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.oak.Course;

import java.util.Collection;

public class CoursesContract {

    // Database table
    public static final String TABLE_NAME = "courses";

    public static Uri CONTENT_URI = OakContentProvider.COURSE_CONTENT_URI;

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PASSWORD = "password";

    public static final String[] FULL_PROJECTION = {
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_PASSWORD,
    };

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_PASSWORD + " text "
            + ");";

    public static final String DEFAULT_SORT =
            "case when " + COLUMN_PASSWORD + " is null then 1 else 0 end, "
                    + COLUMN_NAME + " ASC";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(CoursesContract.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    private static ContentValues getValues(Course course) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, course.getName());
        values.put(COLUMN_PASSWORD, course.getPassword());
        return  values;
    }

    public static Uri insert(Course course, ContentResolver resolver) {
        return resolver.insert(CONTENT_URI, getValues(course));
    }

    public static void insert(Collection<Course> courses, ContentResolver resolver) {
        for (Course course: courses) {
            insert(course, resolver);
        }
    }

    public static int update(Course course, ContentResolver resolver) {
        return resolver.update(CONTENT_URI, getValues(course), "(" + COLUMN_NAME + "=" + "?)",
                new String[] { course.getName() });
    }
}
