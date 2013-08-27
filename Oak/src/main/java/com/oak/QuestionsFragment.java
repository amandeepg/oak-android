/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oak.db.OakContentProvider;
import com.oak.db.QuestionsContract;
import com.oak.utils.AppMsgFactory;
import com.oak.utils.NetworkUtils;
import com.oak.utils.OakGetRequestFactory;
import com.oak.utils.OakPostParams;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class QuestionsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, QuestionsAdapter.QuestionListener {

    private CursorAdapter mAdapter;
    private View mView;
    private Runnable mLoadRunnable;
    private static HashMap<String, Semaphore> mResolveSemaphores = new HashMap<String, Semaphore>();
    private static HashMap<String, Semaphore> mVoteSemaphores = new HashMap<String, Semaphore>();

    public QuestionsFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setHasRefreshMenuItem(R.id.refresh_questions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.questions_fragment, container, false);
        setUpListView();
        setRefreshing(true);
        mView.findViewById(R.id.submit_question).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSubmitQuestion();
            }
        });
        return mView;
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

    private void setUpListView() {
        ListView lv = (ListView) mView.findViewById(R.id.questionListView);
        setHasPullToRefresh(lv);
        lv.setEmptyView(mView.findViewById(R.id.emptyView));

        getLoaderManager().initLoader(0, null, this);
        mAdapter = new QuestionsAdapter(mView.getContext(), null, 0, this);

        lv.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
        inflator.inflate(R.menu.questions_menu, menu);
        super.onCreateOptionsMenu(menu, inflator);
    }

    private void createLoadRequest() {
        if (NetworkUtils.areRequestsPending(mResolveSemaphores) ||
                NetworkUtils.areRequestsPending(mVoteSemaphores)) {
            postLoadDelayed(OakConfig.AUTO_REFRESH_QUESTIONS_MILLIS);
            return;
        }
        JsonObjectRequest loadRequest = new JsonObjectRequest(
                Request.Method.GET,
                new OakGetRequestFactory("QuestionList")
                        .add("courseCode", QMTabActivity.courseCode)
                        .add("coursePassword", QMTabActivity.coursePass)
                        .addDeviceID(getActivity())
                        .url(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        onQuestionsLoaded(json);
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

    private void onQuestionsLoaded(JSONObject json) {
        setRefreshComplete();
        mView.findViewById(R.id.progress).setVisibility(View.GONE);
        mView.findViewById(R.id.emptyText).setVisibility(View.VISIBLE);

        if (!NetworkUtils.areRequestsPending(mResolveSemaphores) &&
                !NetworkUtils.areRequestsPending(mVoteSemaphores)) {
            NetworkUtils.printResponse(TAG, "onQuestionsLoaded", json);
            QuestionsContract.insert(Question.parseJson(json, QMTabActivity.courseCode), getActivity().getContentResolver());
        }
        postLoadDelayed(OakConfig.AUTO_REFRESH_QUESTIONS_MILLIS);
    }

    private void onSubmitQuestion() {
        TextView questionsTextView = (TextView) mView.findViewById(R.id.question_text);
        String questionText = questionsTextView.getText().toString().trim();
        if (TextUtils.isEmpty(questionText)) {
            return;
        }


        AppMsgFactory.startMsg(this, R.string.adding_question);
        questionsTextView.clearComposingText();
        questionsTextView.setText("");
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(questionsTextView.getWindowToken(), 0);

        final JsonPostRequest req = new JsonPostRequest(
                OakConfig.endPoint("AddQuestion"),
                new OakPostParams()
                        .add("courseCode", QMTabActivity.courseCode)
                        .add("coursePassword", QMTabActivity.coursePass)
                        .add("question", questionText)
                        .addDeviceID(getActivity()),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onQuestionSubmitted(response);
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

    private void onQuestionSubmitted(JSONObject json) {
        NetworkUtils.printResponse(TAG, "addQuestion", json);
        if (json != null) {
            AppMsgFactory.finishMsg(this, R.string.question_added);
            createLoadRequest();
        } else {
            AppMsgFactory.somethingWentWrong(getActivity());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                OakContentProvider.QUESTION_CONTENT_URI,
                QuestionsContract.FULL_PROJECTION,
                "(" + QuestionsContract.COLUMN_COURSE_NAME + "=?)",
                new String[] { QMTabActivity.courseCode },
                QuestionsContract.DEFAULT_SORT);
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
    public void onResolve(boolean isResolved, final String id) {
        ContentValues values = new ContentValues();
        values.put(QuestionsContract.COLUMN_DEVICE_RESOLVE_VOTE, isResolved ? 1 : 0);
        getActivity().getContentResolver().update(OakContentProvider.QUESTION_CONTENT_URI,
                values, "(" + QuestionsContract.COLUMN_ID + "=" + "?)", new String [] { id });

        NetworkUtils.incrementFire(mResolveSemaphores, id);
        final JsonPostRequest req = new JsonPostRequest(
                OakConfig.endPoint("ResolveQuestion"),
                new OakPostParams()
                        .add("courseCode", QMTabActivity.courseCode)
                        .add("coursePassword", QMTabActivity.coursePass)
                        .add("resolveVote", isResolved ? "1" : "0")
                        .add("questionId", id)
                        .addDeviceID(getActivity()),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        NetworkUtils.printResponse(TAG, "resolved", response);
                        NetworkUtils.decrementFire(mResolveSemaphores, id);
                        createLoadRequest();
                    }
                },
                null
        );
        req.setShouldCache(false);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtils.shouldFireNow(mResolveSemaphores, id)) {
                    addRequest(req);
                }
            }
        }, OakConfig.NETWORK_POST_BUFFER_MILLIS);
    }

    @Override
    public void onVote(String vote, final String id) {
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor voteQueryCursor = resolver.query(OakContentProvider.QUESTION_CONTENT_URI,
                new String[] { QuestionsContract.COLUMN_VOTES },
                "(" + QuestionsContract.COLUMN_ID + "=" + "?)",
                new String[] { id },
                null);
        voteQueryCursor.moveToFirst();
        int votes = voteQueryCursor.getInt(voteQueryCursor.getColumnIndex(QuestionsContract.COLUMN_VOTES));

        ContentValues values = new ContentValues();
        values.put(QuestionsContract.COLUMN_DEVICE_VOTE, vote);
        values.put(QuestionsContract.COLUMN_VOTES, votes + Integer.parseInt(vote));
        resolver.update(OakContentProvider.QUESTION_CONTENT_URI,
                values, "(" + QuestionsContract.COLUMN_ID + "=" + "?)", new String[]{id});

        NetworkUtils.incrementFire(mVoteSemaphores, id);
        final JsonPostRequest req = new JsonPostRequest(
                OakConfig.endPoint("VoteQuestion"),
                new OakPostParams()
                        .add("courseCode", QMTabActivity.courseCode)
                        .add("coursePassword", QMTabActivity.coursePass)
                        .add("vote", vote)
                        .add("questionId", id)
                        .addDeviceID(getActivity()),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        NetworkUtils.printResponse(TAG, "votedQuestion", response);
                        NetworkUtils.decrementFire(mVoteSemaphores, id);
                        createLoadRequest();
                    }
                },
                null
        );
        req.setShouldCache(false);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtils.shouldFireNow(mVoteSemaphores, id)) {
                    addRequest(req);
                }
            }
        }, OakConfig.NETWORK_POST_BUFFER_MILLIS);
    }

    @Override
    public void onRefreshStarted(View view) {
        createLoadRequest();
    }
}
