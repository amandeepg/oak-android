/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class OakContentProvider extends ContentProvider {

    // database
    private OakDatabaseHelper database;

    // Used for the UriMacher
    private static final int COURSES = 10;
    private static final int COURSE_ID = 20;
    private static final int QUESTIONS = 30;
    private static final int QUESTION_ID = 40;

    private static final String AUTHORITY = "com.oak.courses.contentprovider";

    private static final String COURSE_BASE_PATH = CoursesContract.TABLE_NAME;
    private static final String QUESTION_BASE_PATH = QuestionsContract.TABLE_NAME;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, COURSE_BASE_PATH, COURSES);
        sURIMatcher.addURI(AUTHORITY, COURSE_BASE_PATH + "/#", COURSE_ID);
        sURIMatcher.addURI(AUTHORITY, QUESTION_BASE_PATH, QUESTIONS);
        sURIMatcher.addURI(AUTHORITY, QUESTION_BASE_PATH + "/#", QUESTION_ID);
    }

    public static final Uri COURSE_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + COURSE_BASE_PATH);

    public static final Uri QUESTION_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + QUESTION_BASE_PATH);

    @Override
    public boolean onCreate() {
        database = new OakDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int uriType = sURIMatcher.match(uri);
        checkColumns(projection, uriType);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTable(uriType));

        switch (uriType) {
            case COURSES:
            case QUESTIONS:
                break;
            case COURSE_ID:
            case QUESTION_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(BaseColumns._ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private String getTable(int uriType) {
        final String table;
        switch (uriType) {
            case COURSES:
            case COURSE_ID:
                table = CoursesContract.TABLE_NAME;
                break;
            case QUESTIONS:
            case QUESTION_ID:
                table = QuestionsContract.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI type: " + uriType);
        }
        return table;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();

        if (checkIfExists(values, sqlDB, uriType)) {
            switch (uriType) {
                case COURSES:
                    values.remove(CoursesContract.COLUMN_PASSWORD);
                    update(uri, values, CoursesContract.COLUMN_ID + "=" + "?",
                            new String[] { values.get(CoursesContract.COLUMN_ID).toString() });
                    break;
                case QUESTIONS:
                    update(uri, values, QuestionsContract.COLUMN_ID + "=" + "?",
                            new String[] { values.get(QuestionsContract.COLUMN_ID).toString() });
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI type: " + uriType);
            }
            return null;
        }

        long id = sqlDB.insert(getTable(uriType), null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(COURSE_BASE_PATH + "/" + id);
    }

    /**
     * Checks if course exists with same name
     */
    private boolean checkIfExists(ContentValues values, SQLiteDatabase sqlDB, int uriType) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTable(uriType));

        switch (uriType) {
            case COURSES:
                queryBuilder.appendWhere(CoursesContract.COLUMN_NAME + "=\"" + values.get(CoursesContract.COLUMN_NAME) + "\"");
                break;
            case QUESTIONS:
                queryBuilder.appendWhere(QuestionsContract.COLUMN_ID + "=\"" + values.get(QuestionsContract.COLUMN_ID) + "\"");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI type: " + uriType);
        }

        Cursor queryCursor = queryBuilder.query(sqlDB, null, null, null, null, null, null);
        boolean exists = queryCursor.getCount() > 0;
        queryCursor.close();
        return exists;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        final String table = getTable(uriType);

        int rowsDeleted;
        switch (uriType) {
            case COURSES:
            case QUESTIONS:
                rowsDeleted = sqlDB.delete(table, selection, selectionArgs);
                break;
            case COURSE_ID:
            case QUESTION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(table,
                            BaseColumns._ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(table,
                            BaseColumns._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();

        final String table = getTable(uriType);

        int rowsUpdated;
        switch (uriType) {
            case COURSES:
            case QUESTIONS:
                rowsUpdated = sqlDB.update(table, values, selection, selectionArgs);
                break;
            case COURSE_ID:
            case QUESTION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(table,
                            values,
                            BaseColumns._ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(table,
                            values,
                            BaseColumns._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection, int uriType) {
        final String[] availableProjection;
        switch (uriType) {
            case COURSES:
            case COURSE_ID:
                availableProjection = CoursesContract.FULL_PROJECTION;
                break;
            case QUESTIONS:
            case QUESTION_ID:
                availableProjection = QuestionsContract.FULL_PROJECTION;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI type: " + uriType);
        }
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(availableProjection));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}