package com.codepath.apps.beetwitter.MAIN_ACTIVITIES;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.beetwitter.OTHER_USEFUL_CLASS.EndlessRecyclerViewScrollListener;
import com.codepath.apps.beetwitter.OTHER_USEFUL_CLASS.GlobalVariable;
import com.codepath.apps.beetwitter.R;
import com.codepath.apps.beetwitter.TWEET_RECYCLERVIEW.TweetAdapter;
import com.codepath.apps.beetwitter.TWITTER_CLIENT.TwitterApplication;
import com.codepath.apps.beetwitter.TWITTER_CLIENT.TwitterClient;
import com.codepath.apps.beetwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class HomeTimelineActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    private TwitterClient client;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private TweetAdapter adapter;
    private ArrayList<Tweet> tweets;
    private FloatingActionButton button;
    private SwipeRefreshLayout swipeContainer;
    private LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);

        registerWidgets();
        handleEvent();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
        client = TwitterApplication.getInstance(this);
        populateTimeline();
    }

    private void handleEvent() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getIntent();
                String id =i.getStringExtra(GlobalVariable.CURRENT_USER_ID);
                Intent compose = new Intent(HomeTimelineActivity.this,ComposeTweetActivity.class);
                compose.putExtra(GlobalVariable.CURRENT_USER_ID,id);
                startActivity(compose);
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override

            public void onRefresh() {
                populateTimeline();
                swipeContainer.setRefreshing(false);
            }

        });

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Long tweet_uid = tweets.get(tweets.size()-1).getUid();
                client.getTimelineWithMaxId(tweet_uid - 1L, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        Toast.makeText(HomeTimelineActivity.this, "Loading more...", Toast.LENGTH_SHORT).show();
                        int size = adapter.getItemCount();
                        tweets.addAll(Tweet.fromJSONArray(response));
                        adapter.notifyItemRangeInserted(size, tweets.size() - 1);

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d("ERROR", errorResponse.toString());
                        Toast.makeText(HomeTimelineActivity.this, "There is some problem when connecting to twitter", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    private void registerWidgets() {



        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_tweets);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        tweets = new ArrayList<>();
        button = (FloatingActionButton)findViewById(R.id.fab);
        adapter = new TweetAdapter(HomeTimelineActivity.this,tweets);
        recyclerView.setAdapter(adapter);
    }

    private void populateTimeline() {
        client.getTimeline(1L,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                tweets.clear();
                int size = adapter.getItemCount();
                tweets.addAll(Tweet.fromJSONArray(response));
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("ERROR",errorResponse.toString());
                Toast.makeText(HomeTimelineActivity.this, "There is some problem when connecting to twitter", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_action_profile:
            {
                Intent i = getIntent();
                String id = i.getStringExtra(GlobalVariable.CURRENT_USER_ID);
                Intent profile = new Intent(HomeTimelineActivity.this,ProfileActivity.class);
                profile.putExtra(GlobalVariable.CURRENT_USER_ID,id);
                startActivity(profile);
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateTimeline();
    }
}
