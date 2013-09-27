/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OakDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "oak.db";
    private static final int DATABASE_VERSION = 5;

    public OakDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        CoursesContract.onCreate(database);
        QuestionsContract.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        CoursesContract.onUpgrade(database, oldVersion, newVersion);
        QuestionsContract.onUpgrade(database, oldVersion, newVersion);
    }
}
 