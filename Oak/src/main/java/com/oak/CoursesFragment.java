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
import android.util.Log;
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
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.oak.db.CoursesContract;
import com.oak.db.OakContentProvider;
import com.oak.db.QuestionsContract;
import com.oak.utils.AppMsgFactory;
import com.oak.utils.OakJSONObject;
import com.oak.utils.UiUtils;

import org.json.JSONObject;

public class CoursesFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener,
        PlusClient.OnPersonLoadedListener {

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
        setRefreshing(true);
        createLoadRequest();
        AppController.getInstance().getPlusClient().loadPerson(this, "me");
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
        PlusClient plusClient = AppController.getInstance().getPlusClient();

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
            case R.id.sign_out:
                if (plusClient.isConnected()) {
                    plusClient.clearDefaultAccount();
                    plusClient.disconnect();
                    plusClient.connect();
                }
                goBack();
                return true;
            case R.id.revoke_account_access:
                if (plusClient.isConnected()) {
                    plusClient.clearDefaultAccount();
                    plusClient.revokeAccessAndDisconnect(new PlusClient.OnAccessRevokedListener() {
                        @Override
                        public void onAccessRevoked(ConnectionResult status) {
                            goBack();
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goBack() {
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void createLoadRequest() {
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onCoursesLoaded(response);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setRefreshComplete();
            }
        };
        Request loadRequest = OakApi.getCourses(getActivity(), responseListener, errorListener);
        addRequest(loadRequest);
    }

    private void onCoursesLoaded(JSONObject json) {
        setRefreshComplete();

        mView.findViewById(R.id.progress).setVisibility(View.GONE);
        mView.findViewById(R.id.emptyText).setVisibility(View.VISIBLE);

        CoursesContract.insert(Course.parseJson(json), getActivity().getContentResolver());
        postLoadDelayed(OakConfig.AUTO_REFRESH_COURSES_MILLIS);
    }

    private void onCourseAdded(JSONObject json) {
        if (json.optInt("courseId") != 0) {
            AppMsgFactory.finishMsg(this, R.string.course_added);
            postLoadDelayed(0);
        } else {
            AppMsgFactory.somethingWentWrong(getActivity());
        }
    }

    private void onCourseJoined(JSONObject json, Course course, String coursePass) {
        if (json == null) {
            AppMsgFactory.finishMsg(this, R.string.wrong_password, R.color.alert);
            return;
        }

        QuestionsContract.insert(Question.parseJson(json, course.getId()), getActivity().getContentResolver());

        AppMsgFactory.finishMsg(getActivity());
        course.setPassword(coursePass);
        CoursesContract.update(course, getActivity().getContentResolver());
        final Intent intent = new Intent(getActivity(), QMTabActivity.class);
        intent.putExtra("course", course);
        startActivity(intent);
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
                    public void onClick(DialogInterface dialog, int _i) {
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

        Bundle data = new Bundle();
        data.putString(OakApi.COURSE_ID, course.getId());
        data.putString(OakApi.COURSE_PASSWORD, coursePass);

        Request req = OakApi.getQuestions(getActivity(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        onCourseJoined(json, course, coursePass);
                    }
                },
                null,
                data
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

        OakJSONObject data = new OakJSONObject();
        data.safePut(OakApi.COURSE_CODE, courseCode);
        data.safePut(OakApi.COURSE_PASSWORD, coursePass);

        Request req = OakApi.postCourse(getActivity(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onCourseAdded(response);
                    }
                },
                null,
                data
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

    @Override
    public void onPersonLoaded(ConnectionResult connectionResult, Person person) {
        Crashlytics.log(Log.DEBUG, TAG, "me = " + person.getId() + ":" + person.getName());
    }
}
