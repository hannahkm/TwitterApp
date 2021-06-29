package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    TwitterClient client;
    public static final String TAG = "TimelineActivity";
    public static final int REQUEST_CODE = 20;
    ListView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    TweetsAdapter2 adapter2;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        // get recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // initialize list of tweets and the adapter
        tweets = new ArrayList<>();
        adapter2 = new TweetsAdapter2(this, tweets);

        // set up the recycler view (layout manager and adapter)
        //layout manager
//        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        //adapter
        rvTweets.setAdapter(adapter2);

        populateHomeTimeline();

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });
    }

    // to help us reset our timeline on pull down (reload)
    private void fetchTimelineAsync(int page) {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // clear old items before generating and appending in the new ones
                adapter2.clear();
                populateHomeTimeline();
                adapter2.addAll(tweets);
                // signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error: " + throwable.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true; // return true to populate the menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logoutButton){
            // when the user clicks the log out button, end the intent and log the user out
            Toast.makeText(getApplicationContext(), "You've been logged out", Toast.LENGTH_SHORT).show();
            client.clearAccessToken(); // forget user
            finish(); // exit to log in page

            return true; // true to consume tap and activate
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    // we get data back from the compose activity
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        // if the returned data is valid
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
           // get data from the intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

           // update our recycler view with the new post
            // 1. modify data source (aka the list of tweets)
            tweets.add(0, tweet); // add to first position

            // 2. update the adapter
            adapter2.notifyDataSetChanged(); // new item at position 0
            rvTweets.smoothScrollToPosition(0); // when going back, go to position 0
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "population success!"+json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    // tell the adapter the set is changed so it can be updated
                    adapter2.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "population failed"+response, throwable);
            }
        });
    }

    public void composeTweet(View v) {
        // we are clicking the compose button
        // go the compose activity
        Intent i = new Intent(this, ComposeActivity.class);
        startActivityForResult(i, REQUEST_CODE);
    }
}