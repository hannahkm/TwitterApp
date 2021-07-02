package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.List;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{
    Context context;
    List<Tweet> tweets;
    TwitterClient client;

    public interface onClickListener {
        void onTweetLiked(int position);
        void onTweetReplied(int position);
        void onRetweet(int position);
        void onImageOpened(int position);
    }

    onClickListener onClickListener;

    public TweetsAdapter(Context context, List<Tweet> tweets, onClickListener onClickListener) {
        this.context = context;
        this.tweets = tweets;
        this.onClickListener = onClickListener;
        client = TwitterApp.getRestClient(context);
    }

    @NonNull
    @NotNull
    @Override
    // inflate a layout for each row/Tweet
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, final int position) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(convertView, onClickListener);
    }

    @Override
    // bind values based on position
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // get data at given position
        Tweet tweet = tweets.get(position);

        // bind tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // define a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvUsername;
        TextView tvTimestamp;
        TextView ivLikeCount;
        TextView ivRetweetCount;
        ImageView likeButton;
        ImageView replyButton;
        ImageView retweetButton;
        ImageView ivMedia;
        private WeakReference<onClickListener> listenerRef;

        public ViewHolder(@NonNull View itemView, onClickListener clickListener) {
            super(itemView);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivLikeCount = itemView.findViewById(R.id.ivLikeCount);
            ivRetweetCount = itemView.findViewById(R.id.ivRetweetCount);
            likeButton = itemView.findViewById(R.id.ivLike);
            replyButton = itemView.findViewById(R.id.ivReply);
            retweetButton = itemView.findViewById(R.id.ivRetweet);
            ivMedia = itemView.findViewById(R.id.ivMedia);

            itemView.setOnClickListener(this);
            likeButton.setOnClickListener(this);
            replyButton.setOnClickListener(this);
            retweetButton.setOnClickListener(this);
            ivMedia.setOnClickListener(this);

            listenerRef = new WeakReference<>(clickListener);
        }

        // helper method to get data from tweet and populate activity elements
        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvTimestamp.setText(tweet.getRelativeTimeAgo());
            Glide.with(context).load(tweet.user.profileImageURL).transform(new RoundedCornersTransformation(100, 0)).into(ivProfileImage);

            // populate item view with tweet info and profile pic
            tvBody.setText(tweet.body);
            tvUsername.setText("@"+tweet.user.name);
            tvScreenName.setText(tweet.user.screenName);
            tvTimestamp.setText(tweet.getRelativeTimeAgo());
            ivLikeCount.setText(String.valueOf(tweet.likes));
            ivRetweetCount.setText(String.valueOf(tweet.retweets));

            ImageView ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            Glide.with(context).load(tweet.user.profileImageURL).transform(new RoundedCornersTransformation(100, 0)).into(ivProfileImage);

            // if the tweet included media, also display the first image
            if (tweet.entities.size() > 0){
                ivMedia.setVisibility(View.VISIBLE);

                String baseURL = String.valueOf(tweet.entities.get(0));
                String imageURL = baseURL.substring(0, baseURL.length()-4)+"?format=jpg&name=small";
                Glide.with(context).load(imageURL).override(Target.SIZE_ORIGINAL, 500).transform(new RoundedCornersTransformation(20, 0)).into(ivMedia);

            }

            if (tweet.liked) {
                likeButton.setImageResource(R.drawable.ic_vector_heart);
            } else {
                likeButton.setImageResource(R.drawable.ic_vector_heart_stroke);
            }
            if (tweet.retweeted) {
                retweetButton.setImageResource(R.drawable.ic_vector_retweet);
            } else {
                retweetButton.setImageResource(R.drawable.ic_vector_retweet_stroke);
            }
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            int itemClicked = v.getId();
//            Toast.makeText(context, String.valueOf(itemClicked) + " " + String.valueOf(position), Toast.LENGTH_SHORT).show();

            if (itemClicked == likeButton.getId()){
                // liking tweets
                likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listenerRef.get().onTweetLiked(position);
                        notifyDataSetChanged(); // update the adapter w new data from listener
                    }
                });
            } else if (itemClicked == replyButton.getId()){
                // replying to tweets
                replyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listenerRef.get().onTweetReplied(position);
                    }
                });

            } else if (itemClicked == retweetButton.getId()){
                // retweeting: also changes image on click
                retweetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tweet retweeted = tweets.get(position);
                        if (!retweeted.retweeted) {
                            retweetButton.setImageResource(R.drawable.ic_vector_retweet);
                        }
                        listenerRef.get().onRetweet(position);
                        notifyDataSetChanged();
                    }
                });
            } else if (itemClicked == ivMedia.getId()){
                listenerRef.get().onImageOpened(position);
            } else {
                // viewing details about tweet
                Intent i = new Intent(context, DetailedActivity.class);
                i.putExtra("tweet", Parcels.wrap(tweets.get(position)));
                i.putExtra("tweetPosition", position);
                context.startActivity(i);
            }

        }
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
