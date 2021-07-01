package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class DetailedActivity extends AppCompatActivity {
    public interface onClickListener {
        void onTweetLiked(int position);
        void onTweetReplied(int position);
    }

    onClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        final Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        final int position = getIntent().getIntExtra("position", 0);

        // populate item view with tweet info and profile pic
        ((TextView) findViewById(R.id.dvBody)).setText(tweet.body);
        ((TextView) findViewById(R.id.dvName)).setText(tweet.user.name);
        ((TextView) findViewById(R.id.dvUsername)).setText("@"+tweet.user.screenName);
//        ((TextView) findViewById(R.id.tvTimestamp)).setText(tweet.getRelativeTimeAgo());
        ((TextView) findViewById(R.id.dvLikeCount)).setText(String.valueOf(tweet.likes) + " Likes");
        ((TextView) findViewById(R.id.dvRetweetCount)).setText(String.valueOf(tweet.retweets) + " Retweets");

        ImageView dvProfileImage = findViewById(R.id.dvProfileImage);
        Glide.with(this).load(tweet.user.profileImageURL).transform(new RoundedCornersTransformation(100, 0)).into(dvProfileImage);

        // if the tweet included media, also display the first image
        if (tweet.entities.size() > 0){
            ImageView dvMedia = findViewById(R.id.dvMedia);
            dvMedia.setVisibility(View.VISIBLE);
            String baseURL = String.valueOf(tweet.entities.get(0));
            String imageURL = baseURL.substring(0, baseURL.length()-4)+"?format=jpg&name=medium";
            Glide.with(this).load(imageURL).override(Target.SIZE_ORIGINAL, 800).transform(new RoundedCornersTransformation(20, 0)).into(dvMedia);
        }

    }
}