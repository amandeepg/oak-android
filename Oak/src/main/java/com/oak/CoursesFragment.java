/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.oak.db.CoursesContract;
import com.oak.db.OakContentProvider;
import com.oak.utils.AppMsgFactory;
import com.oak.utils.NetworkUtils;
import com.oak.utils.OakGetRequestFactory;
import com.oak.utils.OakPostParams;
import com.oak.utils.UiUtils;
import com.oak.volley.JsonPostRequest;

import org.json.JSONObject;

public class CoursesFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private CursorAdapter mAdapter;
    private View mView;
    private Runnable mLoadRunnable;

    public CoursesFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setHasRefreshMenuItem(R.id.refresh);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.courses, container, false);
        setUpListView();
        setRefreshing(true);
        return mView;
    }

    private void setUpListView() {
        ListView lv = (ListView) mView.findViewById(R.id.courseListView);
        setHasPullToRefresh(lv);
        lv.setOnItemClickListener(this);
        lv.setEmptyView(mView.findViewById(R.id.emptyView));

        getLoaderManager().initLoader(0, null, this);
        mAdapter = new CoursesAdapter(mView.getContext(), null, 0);

        lv.setAdapter(mAdapter);
    }

    @Override
     public void onStart() {
        super.onStart();
        createLoadRequest();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mLoadRunnable);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
        inflator.inflate(R.menu.courses_menu, menu);
        super.onCreateOptionsMenu(menu, inflator);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_course:
                final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_course_dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setView(dialogView)
                        .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                addCourse(dialogView);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null);
                UiUtils.showDialogWithKeyboard(builder, dialogView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createLoadRequest() {
        JsonObjectRequest loadRequest = new JsonObjectRequest(
                Request.Method.GET,
                new OakGetRequestFactory("CourseList").url(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onCoursesLoaded(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setRefreshComplete();
                        AppMsgFactory.somethingWentWrong(getActivity());
                    }
                }
        );
        addRequest(loadRequest);
    }

    private void onCoursesLoaded(JSONObject json) {
        NetworkUtils.printResponse(TAG, "courses", json);
        setRefreshComplete();

        mView.findViewById(R.id.progress).setVisibility(View.GONE);
        mView.findViewById(R.id.emptyText).setVisibility(View.VISIBLE);

        CoursesContract.insert(Course.parseJson(json), getActivity().getContentResolver());
        postLoadDelayed(OakConfig.AUTO_REFRESH_COURSES_MILLIS);
    }

    private void onCourseAdded(JSONObject json) {
        NetworkUtils.printResponse(TAG, "addCourse", json);
        if (json != null) {
            AppMsgFactory.finishMsg(this, R.string.course_added);
            postLoadDelayed(0);
        } else {
            AppMsgFactory.somethingWentWrong(getActivity());
        }
    }

    private void onCourseJoined(String s, Course course, String coursePass) {
        NetworkUtils.printResponse(TAG, "joinCourse", s);

        if (s == null || !s.contains("1")) {
            AppMsgFactory.finishMsg(this, R.string.wrong_password, R.color.alert);
            return;
        }

        AppMsgFactory.finishMsg(getActivity());
        course.setPassword(coursePass);
        CoursesContract.update(course, getActivity().getContentResolver());
        final Intent mIntent = new Intent(getActivity(), QMTabActivity.class);
        mIntent.putExtra("courseCode", course.getName());
        mIntent.putExtra("coursePass", coursePass);
        startActivity(mIntent);
    }

    private void postLoadDelayed(final long delayMillis) {
        if (mLoadRunnable != null) {
            mHandler.removeCallbacks(mLoadRunnable);
        }
        mLoadRunnable = new Runnable() {
            @Override
            public void run() {
                createLoadRequest();
            }
        };
        mHandler.postDelayed(mLoadRunnable, delayMillis);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        final Course course = new Course(cursor);

        if (!TextUtils.isEmpty(course.getPassword())) {
            joinCourse(course, course.getPassword());
            return;
        }

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.join_course_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setPositiveButton(R.string.join, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface _dialog, int _i) {
                        EditText coursePassText = (EditText) dialogView.findViewById(R.id.course_pass);
                        String coursePass = coursePassText.getText().toString();
                        joinCourse(course, coursePass);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        UiUtils.showDialogWithKeyboard(builder, dialogView);
    }

    private void joinCourse(final Course course, final String coursePass) {
        AppMsgFactory.startMsg(this, R.string.joining_course);

        StringRequest req = new StringRequest(
                Request.Method.GET,
                new OakGetRequestFactory("VerifyCoursePassword")
                        .add("courseCode", course.getName())
                        .add("coursePassword", coursePass)
                        .url(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        onCourseJoined(s, course, coursePass);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppMsgFactory.somethingWentWrong(getActivity());
                    }
                }
        );
        addRequest(req);
    }

    private void addCourse(View dialogView) {
        EditText courseCodeText = (EditText) dialogView.findViewById(R.id.course_name);
        EditText coursePassText = (EditText) dialogView.findViewById(R.id.course_pass);
        final String courseCode = courseCodeText.getText().toString();
        final String coursePass = coursePassText.getText().toString();

        if (courseCode.length() == 0) {
            Toast.makeText(getActivity(), R.string.empty_course_code, Toast.LENGTH_SHORT).show();
            return;
        }
        if (coursePass.length() == 0) {
            Toast.makeText(getActivity(), R.string.empty_course_password, Toast.LENGTH_SHORT).show();
            return;
        }

        AppMsgFactory.startMsg(getActivity(), R.string.adding_course);

        JsonPostRequest req = new JsonPostRequest(
                OakConfig.endPoint("AddCourse"),
                new OakPostParams()
                        .add("courseCode", courseCode)
                        .add("coursePassword", coursePass),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onCourseAdded(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppMsgFactory.somethingWentWrong(getActivity());
                    }
                }
        );
        addRequest(req);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                OakContentProvider.COURSE_CONTENT_URI,
                CoursesContract.FULL_PROJECTION,
                null,
                null,
                CoursesContract.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            mView.findViewById(R.id.progress).setVisibility(View.GONE);
            mView.findViewById(R.id.emptyText).setVisibility(View.VISIBLE);
        }
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRefreshStarted(View view) {
        createLoadRequest();
    }
}
