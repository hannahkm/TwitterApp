package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{
    Context context;
    List<Tweet> tweets;
    ImageView likeButton;
    ImageView replyButton;
    ImageView retweetButton;
    TwitterClient client;
    Tweet tweet;

    public interface onClickListener {
        void onTweetLiked(int position);
        void onTweetReplied(int position);
        void onRetweet(int position);
        void onImageOpened(int position);
    }

    TweetsAdapter2.onClickListener onClickListener;
    // Pass in context and lists of Tweets


    public TweetsAdapter(Context context, List<Tweet> tweets, TweetsAdapter2.onClickListener onClickListener) {
        this.context = context;
        this.tweets = tweets;
        this.onClickListener = onClickListener;
        client = TwitterApp.getRestClient(context);
    }

    public Tweet getItem(int position) {
        return tweets.get(position);
    }

    @NonNull
    @NotNull
    @Override
    // inflate a layout for each row/Tweet
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, final int position) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);

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

            // open the image in a popup window when clicked on
            ivMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onImageOpened(position);
                }
            });
        }

        // liking tweets: changes the drawable on click
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
                } else {
                    likeButton.setImageResource(R.drawable.ic_vector_heart);
                }
                onClickListener.onTweetLiked(position);
                notifyDataSetChanged(); // update the adapter w new data from listener
            }
        });

        // replying to tweets
        replyButton = convertView.findViewById(R.id.ivReply);
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onTweetReplied(position);
            }
        });

        // retweeting: also changes image on click
        retweetButton = convertView.findViewById(R.id.ivRetweet);
        if (tweet.retweeted) {
            retweetButton.setImageResource(R.drawable.ic_vector_retweet);
        }

        retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tweet retweeted = tweets.get(position);
                if (!retweeted.retweeted) {
                    retweetButton.setImageResource(R.drawable.ic_vector_retweet);
                }
                onClickListener.onRetweet(position);
                notifyDataSetChanged();
            }
        });

        return new ViewHolder(convertView);
    }

    @Override
    // bind values based on position
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // get data at given position
        Tweet tweet = tweets.get(position);

        // bind tweet with the view holder
        holder.bind(tweet);
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

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // define a view holder
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        // helper method to get data from tweet and populate activity elements
        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvTimestamp.setText(tweet.getRelativeTimeAgo());
            Glide.with(context).load(tweet.user.profileImageURL).transform(new RoundedCornersTransformation(100, 0)).into(ivProfileImage);

        }
    }

}
