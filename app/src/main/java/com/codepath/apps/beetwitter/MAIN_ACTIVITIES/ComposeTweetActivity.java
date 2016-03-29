package com.codepath.apps.beetwitter.MAIN_ACTIVITIES;

import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.codepath.apps.beetwitter.OTHER_USEFUL_CLASS.GlobalVariable;
import com.codepath.apps.beetwitter.R;
import com.codepath.apps.beetwitter.TWITTER_CLIENT.TwitterApplication;
import com.codepath.apps.beetwitter.TWITTER_CLIENT.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ComposeTweetActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView profile_image;
    private EditText status;
    private Menu menu;
    private MenuItem post;
    private TwitterClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_tweet);
        client = TwitterApplication.getInstance(this);
        registerWidgets();
        handleEvent();

    }

    private void handleEvent() {
        String id = getIntent().getStringExtra(GlobalVariable.CURRENT_USER_ID);
        client.getCurrentUserTimeLine(id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    String userProfile = response.getJSONObject(0).getJSONObject("user").getString("profile_image_url_https");
                    Glide.with(ComposeTweetActivity.this).load(userProfile).placeholder(R.drawable.placeholder).into(profile_image);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });



        status.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(status.getText().toString().trim().length()>0)
                    post.setEnabled(true);
                else
                    post.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(status.getText().toString().trim().length()>0)
                    post.setEnabled(true);
                else
                    post.setEnabled(false);
            }
        });
    }

    private void registerWidgets() {
        toolbar = (Toolbar)findViewById(R.id.toolbar_compose);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        status = (EditText) findViewById(R.id.editText_compose_status);
        profile_image = (ImageView) findViewById(R.id.imageView_compose_profile_image);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose_tweet_menu,menu);
        this.menu = menu;
        post = menu.findItem(R.id.menu_action_compose);
        post.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }else if (item.getItemId() == R.id.menu_action_compose)
        {
            client.postNewTweet(status.getText().toString().trim(),new JsonHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (status.getText().toString().trim().length()!=0) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Discard change?");
            builder.setMessage("If you go back now,your draft will be discarded?");
            builder.setPositiveButton("DISCARD", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            builder.setNegativeButton("KEEP", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create();
            builder.show();
        }else
        {
            finish();
        }

    }
}
