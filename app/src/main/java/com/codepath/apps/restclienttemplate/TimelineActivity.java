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

        // initialize list of tweets and the adapter
        tweets = new ArrayList<>();
        timeline = new ArrayList<>();
        adapter2 = new TweetsAdapter2(this, tweets, this);

        // set up the recycler view (layout manager and adapter)
        rvTweets.setAdapter(adapter2);
        rvTweets.setOnItemClickListener(messageClickedHandler);

        // fill user's timeline w tweets
        populateHomeTimeline();

        // allows user to pull container down to refresh
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });

    }

    // allows user to click on an item and view its details
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
            tweets.add(0, tweet); // add to first position
            adapter2.notifyDataSetChanged(); // new item at position 0
            rvTweets.smoothScrollToPosition(-1); // when going back, go to position 0
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // add tweets to the user's timeline to be viewed
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
    // also opens compose activity to reply to tweets
    public void onTweetReplied(int position) {
        final Tweet tweet = tweets.get(position);
        Intent i = new Intent(this, ComposeActivity.class);
        i.putExtra("reply", true);
        i.putExtra("username", tweet.user.name);
        i.putExtra("tweetID", tweet.id);
        startActivityForResult(i, REQUEST_CODE);
    }

    @Override
    // automatically retweets for the user
    public void onRetweet(int position) {
        final Tweet tweet = tweets.get(position);
        final String tweetID = tweet.id;
        client.retweet(tweetID, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                tweet.retweeted = true;
                adapter2.notifyDataSetChanged();
                rvTweets.smoothScrollToPosition(0);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("TweetsAdapter", "Fetch like error: " + throwable.toString() + statusCode);
            }
        });

    }

    @Override
    // opens a popup window to view an image
    // also allows user to close the popup using a FAB close button
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
    // checks if tweet is liked/disliked and dislikes/likes it accordingly
    public void onTweetLiked(int position) {
        final Tweet tweet = tweets.get(position);
        if (tweet.liked){
                client.dislikeTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        tweet.liked = false;
                        tweet.likes -= 1;
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.d("TweetsAdapter", "Fetch like error: " + throwable.toString() + statusCode);
                    }
                });
        } else {
                client.likeTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        tweet.liked = true;
                        tweet.likes += 1;
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.d("TweetsAdapter", "Fetch like error: " + throwable.toString() + statusCode);
                    }
                });
        }
        // update the adapter
        adapter2.notifyDataSetChanged();
    }
}