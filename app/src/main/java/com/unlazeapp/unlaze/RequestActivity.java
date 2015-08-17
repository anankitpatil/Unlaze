package com.unlazeapp.unlaze;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by AP on 15/07/15.
 */
public class RequestActivity extends AppCompatActivity {

    private ImageView[] pagerViews;
    private ViewPager viewPager;
    private TextView userNameAge, userCity, userAbout, titleAbout;

    private Toolbar toolbar;

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETCONTENT
        setContentView(R.layout.activity_request);

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

        // Init textviews
        userNameAge = (TextView) findViewById(R.id.userNameAge);
        userCity = (TextView) findViewById(R.id.userCity);
        userAbout = (TextView) findViewById(R.id.userAbout);
        titleAbout = (TextView) findViewById(R.id.titleAbout);

        // Get current resolution
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        try {

            // set title
            String arr[] = GlobalVars.getInstance().personDetail.getString("name").split(" ", 2);
            getSupportActionBar().setTitle(arr[0] + "'s Profile");
            titleAbout.setText("About " + arr[0]);

            // Calculate Age
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String dateInString = GlobalVars.getInstance().personDetail.getString("birth");
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

            userNameAge.setText(GlobalVars.getInstance().personDetail.getString("name") + ", " + age);

            // Put in about
            if (GlobalVars.getInstance().personDetail.has("about"))
                userAbout.setText(GlobalVars.getInstance().personDetail.getString("about"));

            // Put in City
            if (GlobalVars.getInstance().personDetail.has("city"))
                userCity.append(GlobalVars.getInstance().personDetail.getString("city"));

            // Put in images
            if (GlobalVars.getInstance().personDetail.getJSONArray("face").length() > 0) {

                // Init Viewpager
                pagerViews = new ImageView[5];
                for (int i = 0; i < pagerViews.length; i++) {
                    pagerViews[i] = new ImageView(this);
                }
                viewPager = (ViewPager) findViewById(R.id.request_pager);
                ImageAdapter adapter = new ImageAdapter(this);
                viewPager.setAdapter(adapter);

                // Set height of viewpager box
                LinearLayout rl = (LinearLayout) findViewById(R.id.requestPagerBox);
                rl.getLayoutParams().height = size.x;

                // init indicatior
                CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.requestPagerIndicator);
                circleIndicator.setViewPager(viewPager);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // accept button
        Button acceptButton = (Button) findViewById(R.id.button_profile_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    // invalidate request on user and add date
                    ApiService call = ApiService.getInstance();
                    if (GlobalVars.getInstance().userRequest.length() > 0) {
                        for (int j = 0; j < GlobalVars.getInstance().userRequest.length(); j++) {
                            if (GlobalVars.getInstance().userRequest.getJSONObject(j).getBoolean("valid")) {
                                if (GlobalVars.getInstance().userRequest.getJSONObject(j).getString("_type").equals(GlobalVars.getInstance().selectedActivity)) {
                                    GlobalVars.getInstance().userRequest.getJSONObject(j).put("valid", false);
                                    GlobalVars.getInstance().userRequest.getJSONObject(j).put("outcome", true);
                                    GlobalVars.getInstance().userRequest.getJSONObject(j).put("finish", new Date());
                                }
                            }
                        }
                    }

                    // update user request on API
                    call.updateRequest(GlobalVars.getInstance().userDetail.getString("id"), GlobalVars.getInstance().userRequest, new ApiServiceListenerP() {
                        @Override
                        public void onSuccess(JSONArray result) {

                            // make request global
                            GlobalVars.getInstance().userRequest = result;
                        }
                    });

                    // add connections
                    call.getConnect(GlobalVars.getInstance().userDetail.getString("id"), new ApiServiceListenerP() {
                        @Override
                        public void onSuccess(JSONArray result) {

                            // make connect global
                            GlobalVars.getInstance().userConnect = result;

                            // add connection on the user
                            try {
                                final JSONObject connect = new JSONObject();
                                connect.put("_with", GlobalVars.getInstance().personDetail.getString("id"));
                                final JSONArray _type = new JSONArray();
                                final JSONObject type = new JSONObject();
                                type.put("name", GlobalVars.getInstance().selectedActivity);
                                _type.put(type);
                                connect.put("_type", _type);
                                connect.put("valid", true);
                                GlobalVars.getInstance().userConnect.put(connect);

                                // update connection on API for user
                                ApiService call = ApiService.getInstance();
                                call.updateConnect(GlobalVars.getInstance().userDetail.getString("id"), GlobalVars.getInstance().userConnect, new ApiServiceListenerP() {
                                    @Override
                                    public void onSuccess(JSONArray result) {

                                        // make updated connection global
                                        GlobalVars.getInstance().userConnect = result;

                                        // back
                                        onBackPressed();
                                    }
                                });

                                // get person connect
                                call.getConnect(GlobalVars.getInstance().personDetail.getString("id"), new ApiServiceListenerP() {
                                    @Override
                                    public void onSuccess(JSONArray result) {

                                        // make person connection global
                                        GlobalVars.getInstance().personConnect = result;

                                        // add connection on the person
                                        try {
                                            connect.put("_with", GlobalVars.getInstance().userDetail.getString("id"));
                                            GlobalVars.getInstance().personConnect.put(connect);
                                            ApiService call = ApiService.getInstance();
                                            call.updateConnect(GlobalVars.getInstance().personDetail.getString("id"), GlobalVars.getInstance().personConnect, new ApiServiceListenerP() {
                                                @Override
                                                public void onSuccess(JSONArray result) {

                                                    // make updated connection global
                                                    GlobalVars.getInstance().personConnect = result;
                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // reject button
        Button rejectButton = (Button) findViewById(R.id.button_profile_reject);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    // invalidate request on user and add date
                    if (GlobalVars.getInstance().userRequest.length() > 0) {
                        for (int j = 0; j < GlobalVars.getInstance().userRequest.length(); j++) {
                            if (GlobalVars.getInstance().userRequest.getJSONObject(j).getBoolean("valid")) {
                                if (GlobalVars.getInstance().userRequest.getJSONObject(j).getString("_type").equals(GlobalVars.getInstance().selectedActivity)) {
                                    GlobalVars.getInstance().userRequest.getJSONObject(j).put("valid", false);
                                    GlobalVars.getInstance().userRequest.getJSONObject(j).put("outcome", false);
                                    GlobalVars.getInstance().userRequest.getJSONObject(j).put("finish", new Date());
                                }
                            }
                        }
                    }

                    // update user
                    ApiService call = ApiService.getInstance();
                    call.updateRequest(GlobalVars.getInstance().userDetail.getString("id"), GlobalVars.getInstance().userRequest, new ApiServiceListenerP() {
                        @Override
                        public void onSuccess(JSONArray result) {

                            // make updated user request global
                            GlobalVars.getInstance().userRequest = result;

                            // user and person updated show message
                            Toast.makeText(RequestActivity.this, "User Updated.", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                    imageLoader.displayImage(GlobalVars.getInstance().personDetail.getJSONArray("face").getJSONObject(position).getString("source"), pagerViews[position], options);
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