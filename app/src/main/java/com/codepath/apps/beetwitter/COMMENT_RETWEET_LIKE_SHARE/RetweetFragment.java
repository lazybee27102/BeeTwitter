package com.codepath.apps.beetwitter.COMMENT_RETWEET_LIKE_SHARE;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.beetwitter.OTHER_USEFUL_CLASS.GlobalVariable;
import com.codepath.apps.beetwitter.R;
import com.codepath.apps.beetwitter.models.Tweet;

/**
 * A simple {@link Fragment} subclass.
 */
public class RetweetFragment extends DialogFragment {

    private ImageView profile, mainPhoto;
    private TextView name, others, status;
    private Button retweet;
    private getResponseFromRetweet delegate;


    public RetweetFragment() {
    }

    public static RetweetFragment newInstance(Tweet tweet, getResponseFromRetweet delegate) {

        RetweetFragment frag = new RetweetFragment();

        frag.setDelegate(delegate);

        Bundle args = new Bundle();

        args.putParcelable(GlobalVariable.TWEET_TRANSFER, tweet);

        frag.setArguments(args);

        return frag;

    }

    public getResponseFromRetweet getDelegate() {
        return delegate;
    }

    public void setDelegate(getResponseFromRetweet delegate) {
        this.delegate = delegate;
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_retweet, container);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Tweet tweet = getArguments().getParcelable(GlobalVariable.TWEET_TRANSFER);
        profile = (ImageView) view.findViewById(R.id.imageView_comment_profile_image);
        mainPhoto = (ImageView) view.findViewById(R.id.imageView_retweet_mainPhoto);
        name = (TextView) view.findViewById(R.id.textView_comment_profile_name);
        others = (TextView) view.findViewById(R.id.textView_comment_create_at);
        status = (TextView) view.findViewById(R.id.textView_retweet_status);
        retweet = (Button) view.findViewById(R.id.button_reweet);

        Glide.with(getContext()).load(tweet.getUser().getProfileImageURL()).placeholder(R.drawable.placeholder).into(profile);
        name.setText(tweet.getUser().getName());
        others.setText("@"+tweet.getUser().getScreenName() + " - "+tweet.getCreateAt());
        status.setText(tweet.getBody());

        if (tweet.getPhoto() != null) {
            mainPhoto.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(tweet.getPhoto()).placeholder(R.drawable.placeholder).into(mainPhoto);
        }

        if (tweet.isRetweeted())
            retweet.setText("unretweet");
        else
            retweet.setText("retweet");

        retweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(retweet.getText().equals("retweet"))
                    delegate.accessResponse(true);
                else
                    delegate.accessResponse(false);

                dismiss();
            }
        });


    }


    public interface getResponseFromRetweet {
        void accessResponse(boolean b);
    }
}
