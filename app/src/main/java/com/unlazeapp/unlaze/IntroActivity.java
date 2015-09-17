package com.unlazeapp.unlaze;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.unlazeapp.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListener;
import com.unlazeapp.utils.GlobalVars;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by AP on 15/07/15.
 */
public class IntroActivity extends Activity {

    private ViewPager viewPager;

    private int[] introSlides = {
            R.drawable.instruction_a,
            R.drawable.instruction_b,
            R.drawable.instruction_c
    };

    private String TAG = "UNLAZE //";

    private CallbackManager callbackManager;
    private List<String> permissionNeeds= Arrays.asList("email", "user_birthday", "public_profile", "user_about_me", "user_actions.fitness", "user_location", "user_photos");

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETCONTENT
        setContentView(R.layout.activity_intro);

        // ViewPager init
        viewPager = (ViewPager)findViewById(R.id.introPager);
        viewPager.setAdapter(new IntroAdapter());

        CirclePageIndicator circleIndicator = (CirclePageIndicator)findViewById(R.id.introPagerIndicator);
        circleIndicator.setViewPager(viewPager);

        // FB Login button
        Button loginButton = (Button) findViewById(R.id.introFacebookButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(IntroActivity.this, permissionNeeds);
            }
        });

        // Facebook login 1st run
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(final LoginResult loginResults) {
                    GraphRequest request = GraphRequest.newMeRequest(
                        loginResults.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.i(TAG, response.toString());

                                    // init global detail object
                                    GlobalVars.getInstance().userDetail = new JSONObject();

                                    // Put in data from FB
                                    GlobalVars.getInstance().userDetail.put("id", object.getString("id"));
                                    GlobalVars.getInstance().userDetail.put("email", object.getString("email"));
                                    GlobalVars.getInstance().userDetail.put("name", object.getString("name"));
                                    if(object.getString("gender").equals("male")) GlobalVars.getInstance().userDetail.put("gender", true);
                                    else GlobalVars.getInstance().userDetail.put("gender", false);
                                    if(object.has("birthday")) GlobalVars.getInstance().userDetail.put("birth", object.getString("birthday"));
                                    if(!GlobalVars.getInstance().userDetail.has("about")) {
                                        if(object.has("about")) GlobalVars.getInstance().userDetail.put("about", object.getString("about"));
                                        else GlobalVars.getInstance().userDetail.put("about", "Enter a short description");
                                    }

                                    // Last five profile images
                                    if(!GlobalVars.getInstance().userDetail.has("face")) {
                                        JSONArray _face = new JSONArray();
                                        GlobalVars.getInstance().userDetail.put("face", _face);
                                    }
                                    for(int i = 0; i < object.getJSONObject("albums").getJSONArray("data").length(); i++) {
                                        if(object.getJSONObject("albums").getJSONArray("data").getJSONObject(i).getString("type").equals("profile")) {
                                            for(int j = 0; j < 5; j++) {
                                                final JSONObject face = new JSONObject();
                                                face.put("id", object.getJSONObject("albums").getJSONArray("data").getJSONObject(i).getJSONObject("photos").getJSONArray("data").getJSONObject(j).getString("id"));
                                                face.put("source", object.getJSONObject("albums").getJSONArray("data").getJSONObject(i).getJSONObject("photos").getJSONArray("data").getJSONObject(j).getString("source"));
                                                if(GlobalVars.getInstance().userDetail.getJSONArray("face").optJSONObject(j) == null) GlobalVars.getInstance().userDetail.getJSONArray("face").put(j, face);
                                            }
                                        }
                                    }

                                    // add coordinates and gcm id
                                    GlobalVars.getInstance().userDetail.put("gcm", GlobalVars.getInstance().gcm);
                                    JSONArray coordinates = new JSONArray();
                                    coordinates.put(0, GlobalVars.getInstance().longitude);
                                    coordinates.put(1, GlobalVars.getInstance().latitude);
                                    JSONObject coords = new JSONObject();
                                    coords.put("coordinates", coordinates);
                                    coords.put("type", "Point");
                                    GlobalVars.getInstance().userDetail.put("coords", coords);
                                    GlobalVars.getInstance().userDetail.put("city", GlobalVars.getInstance().city);

                                    // add selected defaults
                                    GlobalVars.getInstance().userDetail.put("selected_gender", 0);
                                    GlobalVars.getInstance().userDetail.put("selected_radius", 1000);

                                    // create user on API
                                    ApiService call = ApiService.getInstance();
                                    call.createUser(object.getString("id"), new ApiServiceListener() {
                                        @Override
                                        public void onSuccess(JSONObject result) {

                                            // user created -- login
                                            Intent i = new Intent(IntroActivity.this, MainActivity.class);
                                            startActivity(i);
                                            finish();
                                        }

                                        @Override
                                        public void onFailure() {

                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "FB Registration & Sync complete");
                            }
                        });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,birthday,gender,about,albums{type,photos{source}}");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_LONG).show();
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

    private class IntroAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return introSlides.length;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(container.getContext(), R.layout.slide_intro, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.introPagerSlide);
            container.addView(view, 0);
            if(!imageLoader.isInited()) ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
            final DisplayImageOptions options;
            options = new DisplayImageOptions.Builder()
                    .displayer(new FadeInBitmapDisplayer(600))
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
            imageLoader.displayImage("drawable://" + introSlides[position], imageView, options);

            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }
        @Override
        public Parcelable saveState() {
            return null;
        }
        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
