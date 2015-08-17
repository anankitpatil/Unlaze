package com.unlazeapp.unlaze;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

/**
 * Created by AP on 15/07/15.
 */
public class PersonActivity extends AppCompatActivity {

    private ImageView[] pagerViews;
    private ViewPager viewPager;
    private TextView userNameAge, userCity, userAbout;

    private Toolbar toolbar;

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETCONTENT
        setContentView(R.layout.activity_person);

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

        // Get current resolution
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        try {

            // set title
            String arr[] = GlobalVars.getInstance().personDetail.getString("name").split(" ", 2);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(arr[0] + "'s Profile");

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
                viewPager = (ViewPager) findViewById(R.id.person_pager);
                ImageAdapter adapter = new ImageAdapter(this);
                viewPager.setAdapter(adapter);

                // Set height of viewpager box
                LinearLayout rl = (LinearLayout) findViewById(R.id.personPagerBox);
                rl.getLayoutParams().height = size.x;

                // init indicatior
                CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.personPagerIndicator);
                circleIndicator.setViewPager(viewPager);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // ping button
        Button pingButton = (Button) findViewById(R.id.button_profile_ping);
        pingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    // get person request
                    ApiService call = ApiService.getInstance();
                    call.getRequest(GlobalVars.getInstance().personDetail.getString("id"), new ApiServiceListenerP() {
                        @Override
                        public void onSuccess(JSONArray result) {
                            try {

                                // add request on the person
                                final JSONObject request = new JSONObject();
                                request.put("_with", GlobalVars.getInstance().userDetail.getString("id"));
                                request.put("_type", GlobalVars.getInstance().selectedActivity);
                                request.put("name", GlobalVars.getInstance().userDetail.getString("name"));
                                request.put("valid", true);
                                result.put(request);

                                // update person request and send notif
                                ApiService call = ApiService.getInstance();
                                call.sendNotification(result, new ApiServiceListenerP() {
                                    @Override
                                    public void onSuccess(JSONArray result) {

                                        // make person request global
                                        GlobalVars.getInstance().personRequest = result;

                                        // go back
                                        onBackPressed();
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