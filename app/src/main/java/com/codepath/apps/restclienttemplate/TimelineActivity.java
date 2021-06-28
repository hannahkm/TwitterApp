package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        // get recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // initialize list of tweets and the adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        // set up the recycler view (layout manager and adapter)
        //layout manager
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        //adapter
        rvTweets.setAdapter(adapter);

        populateHomeTimeline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true; // return true to populate the menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose){
            // we are clicking the compose button
            // go the compose activity
            Intent i = new Intent(this, ComposeActivity.class);
            startActivityForResult(i, REQUEST_CODE);

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
            adapter.notifyItemInserted(0); // new item at position 0
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
                    adapter.notifyDataSetChanged();
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

    public void logout(View v){
        // when the user clicks the log out button, end the intent and log the user out
        Toast.makeText(getApplicationContext(), "You've been logged out", Toast.LENGTH_SHORT).show();
        client.clearAccessToken(); // forget user
        finish(); // exit to log in page
    }
}