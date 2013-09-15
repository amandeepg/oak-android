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

import com.oak.Question;

import java.util.Collection;

public class QuestionsContract {

    // Database table
    public static final String TABLE_NAME = "questions";

    public static Uri CONTENT_URI = OakContentProvider.QUESTION_CONTENT_URI;

    public static final String COLUMN__ID = BaseColumns._ID;
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_COURSE_ID = "course_id";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_VOTES = "votes";
    public static final String COLUMN_DEVICE_VOTE = "device_vote";
    public static final String COLUMN_DEVICE_RESOLVE_VOTE = "device_resolve_vote";
    public static final String COLUMN_TIME_CREATED = "time_created";
    public static final String COLUMN_WEIGHTED_IMPORTANCE = "weighted_importance";

    public static final String[] FULL_PROJECTION = {
            COLUMN__ID,
            COLUMN_ID,
            COLUMN_COURSE_ID,
            COLUMN_QUESTION,
            COLUMN_VOTES,
            COLUMN_DEVICE_VOTE,
            COLUMN_DEVICE_RESOLVE_VOTE,
            COLUMN_TIME_CREATED,
            COLUMN_WEIGHTED_IMPORTANCE,
    };

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN__ID + " integer primary key autoincrement, "
            + COLUMN_ID + " integer, "
            + COLUMN_COURSE_ID + " integer, "
            + COLUMN_QUESTION + " text not null, "
            + COLUMN_VOTES + " integer, "
            + COLUMN_DEVICE_VOTE + " integer, "
            + COLUMN_DEVICE_RESOLVE_VOTE + " integer, "
            + COLUMN_WEIGHTED_IMPORTANCE + " integer, "
            + COLUMN_TIME_CREATED + " string not null "
            + ");";

    public static final String DEFAULT_SORT = COLUMN_WEIGHTED_IMPORTANCE + " DESC";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(QuestionsContract.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    private static ContentValues getValues(Question question) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, question.getId());
        values.put(COLUMN_COURSE_ID, question.getCourseId());
        values.put(COLUMN_QUESTION, question.getQuestion());
        values.put(COLUMN_VOTES, question.getVotes());
        values.put(COLUMN_DEVICE_VOTE, question.getDeviceVote());
        values.put(COLUMN_DEVICE_RESOLVE_VOTE, question.isDeviceResolveVote() ? 1 : 0);
        values.put(COLUMN_TIME_CREATED, question.getTimeCreated());
        values.put(COLUMN_WEIGHTED_IMPORTANCE, question.getWeightedImportance());
        return  values;
    }

    public static Uri insert(Question question, ContentResolver resolver) {
        return resolver.insert(CONTENT_URI, getValues(question));
    }

    public static void insert(Collection<Question> questions, ContentResolver resolver) {
        for (Question question: questions) {
            insert(question, resolver);
        }
    }

    public static int update(Question question, ContentResolver resolver) {
        return resolver.update(CONTENT_URI, getValues(question), COLUMN_ID + "=" + "?", new String[] { question.getId() });
    }
}
