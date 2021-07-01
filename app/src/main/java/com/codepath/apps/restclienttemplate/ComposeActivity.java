package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {
    public static final int MAX_TWEET_LENGTH = 140;

    EditText etCompose;
    Button tweetBtn;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // get a client so we can post tweets
        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etCompose);
        tweetBtn = findViewById(R.id.tweetBtn);

        // checks if the user is replying to a tweet and reformats the EditText if so
        final boolean replying = getIntent().hasExtra("reply");
        String tweetID = "";
        if (replying){
            etCompose.setText("@"+getIntent().getStringExtra("username"));
            tweetID = getIntent().getStringExtra("tweetID");
        } else {
            etCompose.setText("");
        }

        // click listener for tweet button
        final String finalTweetID = tweetID;
        tweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make api call to twitter to post our tweet
                String content = etCompose.getText().toString();
                // we reject tweets that are empty
                if (content.isEmpty()){
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                // we reject tweets that are too long
                if (content.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Your tweet is too long!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // calling different functions for replying vs posting tweets
                if (replying){
                    client.replyTweet(content, finalTweetID, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                Toast.makeText(ComposeActivity.this, "Your reply has been posted!", Toast.LENGTH_SHORT).show();

                                // send data back to the previous activity using intents
                                Intent i = new Intent();
                                i.putExtra("tweet", Parcels.wrap(tweet)); // since tweet isn't recognized by Android, we parcel it
                                setResult(RESULT_OK, i); // set result so we can read it
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("ComposeActivity", "failed to reply to tweet", throwable);
                        }
                    });
                } else {
                    // if no errors happen, we post the tweet!
                    client.publishTweet(content, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                Toast.makeText(ComposeActivity.this, "Your tweet has been posted!", Toast.LENGTH_SHORT).show();

                                // send data back to the previous activity using intents
                                Intent i = new Intent();
                                i.putExtra("tweet", Parcels.wrap(tweet)); // since tweet isn't recognized by Android, we parcel it
                                setResult(RESULT_OK, i); // set result so we can read it
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e("ComposeActivity", "failed to publish tweet", throwable);
                        }
                    });

                }
                finish(); // end current activity
            }
        });
    }
}