package com.unlazeapp.unlaze;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.unlazeapp.R;

/**
 * Created by AP on 15/07/15.
 */
public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETCONTENT
        setContentView(R.layout.activity_chat);

        // init toolbar
        toolbar = (Toolbar) findViewById(R.id.u_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
