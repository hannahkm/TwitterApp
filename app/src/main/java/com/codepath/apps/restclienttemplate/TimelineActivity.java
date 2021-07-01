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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements TweetsAdapter2.onClickListener{
    TwitterClient client;
    public static final String TAG = "TimelineActivity";
    public static final int REQUEST_CODE = 20;
    ListView rvTweets;
    List<Tweet> timeline;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    TweetsAdapter2 adapter2;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        // get list view
        rvTweets = findViewById(R.id.rvTweets);

        rvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);
                // or loadNextDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        // initialize list of tweets and the adapter
        tweets = new ArrayList<>();
        timeline = new ArrayList<>();
        adapter2 = new TweetsAdapter2(this, tweets, this);

        // set up the recycler view (layout manager and adapter)
        //layout manager
//        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        //adapter
        rvTweets.setAdapter(adapter2);
        rvTweets.setOnItemClickListener(messageClickedHandler);

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

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyDataSetChanged()`
    }

    // Create a message handling object as an anonymous class.
    AdapterView.OnItemClickListener messageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            Intent i = new Intent(TimelineActivity.this, DetailedActivity.class);
            i.putExtra("tweet", Parcels.wrap(tweets.get(position)));
            i.putExtra("tweetPosition", position);
            startActivity(i);
        }
    };

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
                Log.d("DEBUG", "Fetch timeline error: " + response + throwable.toString());
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

    @Override
    public void onTweetReplied(int position) {
        final Tweet tweet = tweets.get(position);
        Intent i = new Intent(this, ComposeActivity.class);
        i.putExtra("reply", true);
        i.putExtra("username", tweet.user.name);
        i.putExtra("tweetID", tweet.id);
        startActivityForResult(i, REQUEST_CODE);
    }

    @Override
    public void onImageOpened(int position) {
        final ImageView popup = findViewById(R.id.popupImage);
        final ImageView closeButton = findViewById(R.id.closePopup);
        Tweet tweet = tweets.get(position);

        popup.setVisibility(View.VISIBLE);
        String baseURL = String.valueOf(tweet.entities.get(0));
        String imageURL = baseURL.substring(0, baseURL.length()-4)+"?format=jpg&name=medium";
        Glide.with(this).load(imageURL).fitCenter().into(popup);

        closeButton.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.setVisibility(View.GONE);
                closeButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTweetLiked(int position) {
        final Tweet tweet = tweets.get(position);

        if (tweet.liked){
            tweet.liked = false;
            tweet.likes -= 1;
                client.dislikeTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        return;
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.d("TweetsAdapter", "Fetch like error: " + throwable.toString() + statusCode);
                    }
                });
        } else {
            tweet.liked = true;
            tweet.likes += 1;
                client.likeTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        tweet.liked = true;
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.d("TweetsAdapter", "Fetch like error: " + throwable.toString() + statusCode);
                    }
                });
        }
        adapter2.notifyDataSetChanged();
    }
}