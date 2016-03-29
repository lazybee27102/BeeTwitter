package com.codepath.apps.beetwitter.TWEET_RECYCLERVIEW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.codepath.apps.beetwitter.COMMENT_RETWEET_LIKE_SHARE.CommentFragment;
import com.codepath.apps.beetwitter.OTHER_USEFUL_CLASS.GlobalVariable;
import com.codepath.apps.beetwitter.R;
import com.codepath.apps.beetwitter.COMMENT_RETWEET_LIKE_SHARE.RetweetFragment;
import com.codepath.apps.beetwitter.TWITTER_CLIENT.TwitterApplication;
import com.codepath.apps.beetwitter.TWITTER_CLIENT.TwitterClient;
import com.codepath.apps.beetwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by beeiscoding on 28/03/2016.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {
    Context context;
    private ArrayList<Tweet> tweets;

    public TweetAdapter(Context context, ArrayList<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        TweetViewHolder viewHolder = new TweetViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TweetViewHolder holder, int position) {

        final TweetViewHolder tweetViewHolder = (TweetViewHolder) holder;
        final Tweet tweet = tweets.get(position);
        if (tweet.getVideo() != null) {
            holder.main_image.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, GlobalVariable.getScreenHeight((Activity) context) * 4 / 10);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            holder.videoView.setLayoutParams(params);
            holder.videoView.setVideoPath(tweet.getVideo());
            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(holder.videoView);
            holder.videoView.setMediaController(mediaController);
            holder.videoView.requestFocus();
            holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                // Close the progress bar and play the video
                public void onPrepared(MediaPlayer mp) {

                }
            });
        } else {
            if (tweet.getPhoto() != null) {
                holder.videoView.setVisibility(View.GONE);
                holder.main_image.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.getPhoto()).into(holder.main_image);
            }
        }


        holder.name.setText(tweet.getUser().getName());
        holder.createAt.setText("@"+tweet.getUser().getScreenName() + " - "+tweet.getCreateAt());
        holder.status.setText(tweet.getBody());

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "font/Roboto-Regular.ttf");
        holder.status.setTypeface(typeface);

        if (tweet.getUser().getProfileImageURL() != null)
            Glide.with(context).load(tweet.getUser().getProfileImageURL()).into(holder.profile_image);

        //comment,share,retweet,share
        holder.retweet_count.setText(tweet.getRetweeted_count() + "");
        holder.favorite_count.setText(tweet.getFavorited_count() + "");

        if (tweet.isFavorited() == true) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_favorite_white_24dp);
            holder.favorited.setImageDrawable(drawable);
        } else {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_favorite_black_24dp);
            holder.favorited.setImageDrawable(drawable);
        }

        if (tweet.isRetweeted() == true) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
            holder.retweet.setImageDrawable(drawable);
        } else {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_repeat_black_24dp);
            holder.retweet.setImageDrawable(drawable);
        }

        //Event
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                CommentFragment fragment = CommentFragment.newInstance(tweet, new CommentFragment.getResponseFromComment() {
                    @Override
                    public void accessResponse(String s) {
                        TwitterClient client = TwitterApplication.getInstance(context);
                        client.postComment(String.valueOf(tweet.getUid()), s, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);
                                Drawable drawable = context.getResources().getDrawable(R.drawable.ic_chat_bubble_white_24dp);
                                holder.comment.setImageDrawable(drawable);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                super.onFailure(statusCode, headers, throwable, errorResponse);
                            }
                        });
                    }
                });
                fragment.show(fragmentManager,"COMMENT");

            }
        });

        holder.retweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                RetweetFragment retweetFragment = RetweetFragment.newInstance(tweet, new RetweetFragment.getResponseFromRetweet() {
                    @Override
                    public void accessResponse(boolean b) {
                        TwitterClient client = TwitterApplication.getInstance(context);
                        if (b) {
                            //isretweet ===> true
                            client.postRetweet(String.valueOf(tweet.getUid()), new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    super.onSuccess(statusCode, headers, response);
                                    Toast.makeText(context, "Retweet Successfully", Toast.LENGTH_SHORT).show();
                                    tweet.setRetweeted(true);
                                    Drawable drawable = context.getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
                                    holder.retweet.setImageDrawable(drawable);
                                    long tweeter_count = Long.valueOf(holder.retweet_count.getText().toString().trim());
                                    holder.retweet_count.setText(String.valueOf(tweeter_count + 1));
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                    super.onFailure(statusCode, headers, throwable, errorResponse);
                                    Toast.makeText(context, "Retweet Failed", Toast.LENGTH_SHORT).show();

                                }
                            });


                        } else {
                            //isretweet ===> false
                            client.postUnRetweet(String.valueOf(tweet.getUid()), new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    super.onSuccess(statusCode, headers, response);
                                    Toast.makeText(context, "UnRetweet Successfully", Toast.LENGTH_SHORT).show();
                                    tweet.setRetweeted(false);
                                    Drawable drawable = context.getResources().getDrawable(R.drawable.ic_repeat_black_24dp);
                                    holder.retweet.setImageDrawable(drawable);

                                    long tweeter_count = Long.valueOf(holder.retweet_count.getText().toString().trim());
                                    holder.retweet_count.setText(String.valueOf(tweeter_count - 1));
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                    super.onFailure(statusCode, headers, throwable, errorResponse);
                                    Toast.makeText(context, "UnRetweet Failed", Toast.LENGTH_SHORT).show();

                                }
                            });


                        }
                    }
                });
                retweetFragment.show(fragmentManager, "RETWEET");


            }
        });

        holder.favorited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = TwitterApplication.getInstance(context);
                if (tweet.isFavorited() == true) {
                    client.postUnFavorite(String.valueOf(tweet.getUid()), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            tweet.setFavorited(false);
                            long favorite_count = Long.valueOf(holder.favorite_count.getText().toString());
                            holder.favorite_count.setText(String.valueOf(favorite_count - 1));
                            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_favorite_black_24dp);
                            holder.favorited.setImageDrawable(drawable);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }
                    });



                } else {
                    client.postFavorite(String.valueOf(tweet.getUid()), new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            tweet.setFavorited(true);
                            long favorite_count = Long.valueOf(holder.favorite_count.getText().toString());
                            holder.favorite_count.setText(String.valueOf(favorite_count + 1));
                            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_favorite_white_24dp);
                            holder.favorited.setImageDrawable(drawable);
                        }
                    });

                }
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                shareIntent.setType("text/plain");

                shareIntent.putExtra(Intent.EXTRA_TEXT, tweet.getBody());

                context.startActivity(Intent.createChooser(shareIntent, "Share via "));

                Drawable drawable = context.getResources().getDrawable(R.drawable.ic_share_white_24dp);
                holder.share.setImageDrawable(drawable);
            }
        });


    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public static class TweetViewHolder extends RecyclerView.ViewHolder {
        private ImageView profile_image, main_image;
        private VideoView videoView;
        private TextView name, createAt, status;
        private ImageView comment, share, retweet, favorited;
        private TextView retweet_count, favorite_count;

        public TweetViewHolder(View itemView) {
            super(itemView);
            profile_image = (ImageView) itemView.findViewById(R.id.imageView_comment_profile_image);
            videoView = (VideoView) itemView.findViewById(R.id.videoView_video);
            name = (TextView) itemView.findViewById(R.id.textView_comment_profile_name);
            createAt = (TextView) itemView.findViewById(R.id.textView_comment_create_at);
            status = (TextView) itemView.findViewById(R.id.textView_tweet_status);
            retweet_count = (TextView) itemView.findViewById(R.id.textView_tweet_retweetcount);
            favorite_count = (TextView) itemView.findViewById(R.id.textView_tweet_favorite_count);
            main_image = (ImageView) itemView.findViewById(R.id.imageView_tweet_imageURL);
            comment = (ImageView) itemView.findViewById(R.id.imageView_tweet_comment);
            share = (ImageView) itemView.findViewById(R.id.imageView_tweet_share);
            retweet = (ImageView) itemView.findViewById(R.id.imageView_tweet_retweet);
            favorited = (ImageView) itemView.findViewById(R.id.imageView_Tweet_like);


        }


    }
}
