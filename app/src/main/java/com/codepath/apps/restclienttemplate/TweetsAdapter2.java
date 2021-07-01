package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.sql.Time;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter2 extends BaseAdapter {
    Context context;
    List<Tweet> tweets;
    ImageView likeButton;
    ImageView replyButton;
    TwitterClient client;
    Tweet tweet;

    public interface onClickListener {
        void onTweetLiked(int position);
        void onTweetReplied(int position);
        void onImageOpened(int position);
    }

    onClickListener onClickListener;

    // Pass in context and lists of Tweets
    public TweetsAdapter2(Context context, List<Tweet> tweets, onClickListener onClickListener) {
        this.context = context;
        this.tweets = tweets;
        this.onClickListener = onClickListener;
        client = TwitterApp.getRestClient(context);
    }

    @Override
    public int getCount() {
        return tweets.size();
    }

    @Override
    public Tweet getItem(int position) {
        return tweets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);

        tweet = getItem(position);

        // populate item view with tweet info and profile pic
        ((TextView) convertView.findViewById(R.id.tvBody)).setText(tweet.body);
        ((TextView) convertView.findViewById(R.id.tvScreenName)).setText(tweet.user.name);
        ((TextView) convertView.findViewById(R.id.tvUsername)).setText("@"+tweet.user.screenName);
        ((TextView) convertView.findViewById(R.id.tvTimestamp)).setText(tweet.getRelativeTimeAgo());
        ((TextView) convertView.findViewById(R.id.ivLikeCount)).setText(String.valueOf(tweet.likes));
        ((TextView) convertView.findViewById(R.id.ivRetweetCount)).setText(String.valueOf(tweet.retweets));

        ImageView ivProfileImage = convertView.findViewById(R.id.ivProfileImage);
        Glide.with(context).load(tweet.user.profileImageURL).transform(new RoundedCornersTransformation(100, 0)).into(ivProfileImage);

        // if the tweet included media, also display the first image
        if (tweet.entities.size() > 0){
            ImageView ivMedia = convertView.findViewById(R.id.ivMedia);
            ivMedia.setVisibility(View.VISIBLE);

            String baseURL = String.valueOf(tweet.entities.get(0));
            String imageURL = baseURL.substring(0, baseURL.length()-4)+"?format=jpg&name=small";
            Glide.with(context).load(imageURL).override(Target.SIZE_ORIGINAL, 500).transform(new RoundedCornersTransformation(20, 0)).into(ivMedia);

            ivMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onImageOpened(position);
                }
            });
        }

        likeButton = convertView.findViewById(R.id.ivLike);
        if (tweet.liked) {
            likeButton.setImageResource(R.drawable.ic_vector_heart);
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tweet tweetLiked = tweets.get(position);
                if (tweetLiked.liked) {
                    likeButton.setImageResource(R.drawable.ic_vector_heart_stroke);
//                    tweetLiked.liked = false;
                } else {
                    likeButton.setImageResource(R.drawable.ic_vector_heart);
//                    tweetLiked.liked = true;
                }
                onClickListener.onTweetLiked(position);
                notifyDataSetChanged();

            }
        });

        replyButton = convertView.findViewById(R.id.ivReply);

        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onTweetReplied(position);
            }
        });

        return convertView;
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

}
