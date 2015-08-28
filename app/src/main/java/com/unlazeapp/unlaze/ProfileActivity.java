package com.unlazeapp.unlaze;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.unlazeapp.R;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListener;
import com.unlazeapp.utils.ApiServiceListenerP;
import com.unlazeapp.utils.GlobalVars;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by AP on 15/07/15.
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView[] pagerViews;
    private ViewPager viewPager;
    private CallbackManager callbackManager;
    private EditText userAbout;
    private TextView userNameAge, userCity;

    private Toolbar toolbar;

    AccessTokenTracker accessTokenTracker;

    static final String TAG = "UNLAZE //";

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETCONTENT
        setContentView(R.layout.activity_profile);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.u_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Init textviews
        userNameAge = (TextView) findViewById(R.id.userNameAge);
        userCity = (TextView) findViewById(R.id.userCity);
        userAbout = (EditText) findViewById(R.id.userAbout);

        // Get current resolution
        final Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        try {

            // Calculate Age
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String dateInString = GlobalVars.getInstance().userDetail.getString("birth");
            Calendar dob = Calendar.getInstance();
            dob.setTime(formatter.parse(dateInString));
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
                age--;
            } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                age--;
            }

            userNameAge.setText(GlobalVars.getInstance().userDetail.getString("name") + ", " + age);

            // Put in about
            if (GlobalVars.getInstance().userDetail.has("about"))
                userAbout.setText(GlobalVars.getInstance().userDetail.getString("about"));

            // Put in City
            if (GlobalVars.getInstance().userDetail.has("city"))
                userCity.append(GlobalVars.getInstance().userDetail.getString("city"));

            // Put in images
            if (GlobalVars.getInstance().userDetail.getJSONArray("face").length() > 0) {

                // Init Viewpager
                pagerViews = new ImageView[5];
                for (int i = 0; i < pagerViews.length; i++) {
                    pagerViews[i] = new ImageView(this);
                }
                viewPager = (ViewPager) findViewById(R.id.profile_pager);
                ImageAdapter adapter = new ImageAdapter(this);
                viewPager.setAdapter(adapter);

                // Set height of viewpager box
                LinearLayout rl = (LinearLayout) findViewById(R.id.profilePagerBox);
                rl.getLayoutParams().height = size.x;

                // init indicatior
                CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.profilePagerIndicator);
                circleIndicator.setViewPager(viewPager);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // get activities
        try {
            ApiService call = ApiService.getInstance();
            call.getActivity(GlobalVars.getInstance().userDetail.getString("id"), new ApiServiceListenerP() {
                @Override
                public void onSuccess(JSONArray result) {

                    // activities fetched -- make global
                    GlobalVars.getInstance().userActivity = result;

                    // init table and buttons
                    final GridLayout gl = (GridLayout) findViewById(R.id.activitiesBox);
                    final ImageButton activityButton[] = new ImageButton[10];

                    // Set click listeners
                    for (int i = 0; i < GlobalVars.getInstance().activityIcons.length; i++) {
                        activityButton[i] = new ImageButton(ProfileActivity.this);
                        activityButton[i].setBackgroundResource(GlobalVars.getInstance().activityIcons[i]);
                        final LinearLayout ll = new LinearLayout(ProfileActivity.this);
                        ll.addView(activityButton[i]);
                        // final TextView tv = new TextView(ProfileActivity.this);
                        // tv.setText(GlobalVars.getInstance().activityList[i]);
                        // tv.setGravity(Gravity.CENTER);
                        // ll.addView(tv);
                        gl.addView(ll);
                        activityButton[i].getLayoutParams().height = size.x / 3;
                        activityButton[i].getLayoutParams().width = size.x / 3;
                        // tv.getLayoutParams().width = size.x / 3;
                        try {

                            // First start
                            activityButton[i].getBackground().setAlpha(66);
                            if (GlobalVars.getInstance().userActivity.length() > 0) {
                                for (int j = 0; j < GlobalVars.getInstance().userActivity.length(); j++) {
                                    if (GlobalVars.getInstance().userActivity.getJSONObject(j).getString("_type").equals(GlobalVars.getInstance().activityList[i])) {
                                        if(GlobalVars.getInstance().userActivity.getJSONObject(j).getBoolean("valid")) {

                                            // active increase alpha
                                            activityButton[i].getBackground().setAlpha(255);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        final int _i = i;
                        activityButton[i].setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                final JSONObject activity = new JSONObject();
                                int dontFade = -1;
                                try {
                                    if (GlobalVars.getInstance().userActivity.length() > 0) {

                                        // if activities exist
                                        Boolean createNew = true;
                                        for (int j = 0; j < GlobalVars.getInstance().userActivity.length(); j++) {
                                            if (GlobalVars.getInstance().userActivity.getJSONObject(j).getString("_type").equals(GlobalVars.getInstance().activityList[_i])) {
                                                if (GlobalVars.getInstance().userActivity.getJSONObject(j).getBoolean("valid")) {

                                                    // already selected
                                                    Toast.makeText(getApplicationContext(), "You have already selected " + GlobalVars.getInstance().activityList[_i] + ".", Toast.LENGTH_LONG).show();
                                                    dontFade = _i;
                                                } else {

                                                    // activate activity
                                                    GlobalVars.getInstance().userActivity.getJSONObject(j).put("valid", true);
                                                    GlobalVars.getInstance().userActivity.getJSONObject(j).put("last", new Date());

                                                    // selected alert
                                                    Toast.makeText(getApplicationContext(), GlobalVars.getInstance().activityList[_i] + " selected.", Toast.LENGTH_LONG).show();
                                                    dontFade = _i;

                                                    // fade in as active
                                                    v.getBackground().setAlpha(255);
                                                }
                                                createNew = false;
                                            } else {
                                                if (GlobalVars.getInstance().userActivity.getJSONObject(j).getBoolean("valid")) {

                                                    // deactivate activity
                                                    GlobalVars.getInstance().userActivity.getJSONObject(j).put("valid", false);
                                                    GlobalVars.getInstance().userActivity.getJSONObject(j).put("last", new Date());
                                                }
                                            }
                                        }
                                        if (createNew) {

                                            // Create activity
                                            activity.put("valid", true);
                                            activity.put("_type", GlobalVars.getInstance().activityList[_i]);
                                            GlobalVars.getInstance().userActivity.put(activity);

                                            // selected alert
                                            Toast.makeText(getApplicationContext(), GlobalVars.getInstance().activityList[_i] + " selected.", Toast.LENGTH_LONG).show();
                                            dontFade = _i;

                                            // fade in as active
                                            v.getBackground().setAlpha(255);
                                        }
                                    } else {

                                        // Create activity
                                        activity.put("valid", true);
                                        activity.put("_type", GlobalVars.getInstance().activityList[_i]);
                                        GlobalVars.getInstance().userActivity.put(activity);

                                        // selected alert
                                        Toast.makeText(getApplicationContext(), GlobalVars.getInstance().activityList[_i] + " selected.", Toast.LENGTH_LONG).show();
                                        dontFade = _i;

                                        // Fade in
                                        v.getBackground().setAlpha(255);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                // fade out all but
                                for (int k = 0; k < 10; k++) {
                                    if (k != dontFade) activityButton[k].getBackground().setAlpha(66);
                                }
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Save button
        Button saveButton = (Button) findViewById(R.id.button_profile_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    // Save changes globally
                    GlobalVars.getInstance().userDetail.put("about", userAbout.getText());

                    //Sync user with API
                    ApiService call = ApiService.getInstance();
                    call.updateDetail(GlobalVars.getInstance().userDetail.getString("id"), GlobalVars.getInstance().userDetail, new ApiServiceListener() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            GlobalVars.getInstance().userDetail = result;
                        }
                    });
                    call.updateActivity(GlobalVars.getInstance().userDetail.getString("id"), GlobalVars.getInstance().userActivity, new ApiServiceListenerP() {
                        @Override
                        public void onSuccess(JSONArray result) {
                            GlobalVars.getInstance().userActivity = result;
                        }
                    });

                    // user updated go back
                    onBackPressed();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // resync facebook and get images
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                // Facebook resync
                GraphRequest request = GraphRequest.newMeRequest(currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        // Getting FB response after login

                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,birthday,gender,about,albums{type,photos{source}}");
                request.setParameters(parameters);
                request.executeAsync();
            }
        };
        accessTokenTracker.startTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class ImageAdapter extends PagerAdapter {

        Context context;

        public ImageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return pagerViews.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (pagerViews[position].getDrawable() == null) {
                try {
                    final DisplayImageOptions options;
                    options = new DisplayImageOptions.Builder()
                            .displayer(new FadeInBitmapDisplayer(600))
                            .cacheInMemory(true)
                            .cacheOnDisc(true)
                            .build();
                    imageLoader.displayImage(GlobalVars.getInstance().userDetail.getJSONArray("face").getJSONObject(position).getString("source"), pagerViews[position], options);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pagerViews[position].setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            container.addView(pagerViews[position], 0);
            return pagerViews[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }

}