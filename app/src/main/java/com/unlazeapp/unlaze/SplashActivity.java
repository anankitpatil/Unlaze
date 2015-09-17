package com.unlazeapp.unlaze;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.unlazeapp.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListener;
import com.unlazeapp.utils.GlobalVars;
import com.unlazeapp.utils.GpsTracker;

import org.json.JSONObject;

import java.io.IOException;

public class SplashActivity extends Activity {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    GoogleCloudMessaging gcm;
    String regid;

    private CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;

    String SENDER_ID = "905594036910";

    static final String TAG = "Unlaze";

    private static final int SPLASH_TIME_OUT = 3000;

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // notification open
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("mode")) {
            if (getIntent().getExtras().getString("mode").equals("request")) {

                // get person from notification
                ApiService call = ApiService.getInstance();
                call.getDetail(getIntent().getExtras().getString("person"), new ApiServiceListener() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        GlobalVars.getInstance().personDetail = result;
                        GlobalVars.getInstance().U_NOTIF_STATE = 1;
                        if (MainActivity.getInstance() != null) {
                            MainActivity.getInstance().notificationRequest();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            } else if (getIntent().getExtras().getString("mode").equals("chat")) {

                // get person from notification
                ApiService call = ApiService.getInstance();
                call.getDetail(getIntent().getExtras().getString("person"), new ApiServiceListener() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        GlobalVars.getInstance().personDetail = result;
                        GlobalVars.getInstance().U_NOTIF_STATE = 2;
                        if (MainActivity.getInstance() != null) {
                            MainActivity.getInstance().notificationChat();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        }

        // check if GPS enabled sync location
        GpsTracker gpsTracker = new GpsTracker(this);
        double lon = gpsTracker.longitude;
        double lat = gpsTracker.latitude;
        GlobalVars.getInstance().longitude = lon;
        GlobalVars.getInstance().latitude = lat;
        try {
            GlobalVars.getInstance().city = gpsTracker.getCity();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // alert for gps
        if (!gpsTracker.isGPSEnabled || !gpsTracker.isGPSEnabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
            dialog.setMessage("UNLAZE requires your location to find an activity for you. Enable location services from settings.");
            dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getApplicationContext().startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.show();
        }

        // check for GCM or register
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                GlobalVars.getInstance().gcm = regid;
                System.out.println("regid: " + regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        // facebook login
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        // refresh token if possible
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {

                // save new token
                AccessToken.setCurrentAccessToken(newAccessToken);

                // get user object from api
                ApiService call = ApiService.getInstance();
                call.getDetail(AccessToken.getCurrentAccessToken().getUserId(), new ApiServiceListener() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        if (result.toString().equals("{}")) {

                            // no users exist or error
                            Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                            startActivity(i);
                            finish();
                        } else {

                            // user exists -- make detail global
                            GlobalVars.getInstance().userDetail = result;

                            // normal start
                            Intent i = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
                    @Override
                    public void onFailure() {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
                        dialog.setMessage("UNLAZE requires internet to find an people to share an activity with you. Enable internet from settings.");
                        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                getApplicationContext().startActivity(myIntent);
                            }
                        });
                        dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                finish();
                            }
                        });
                        dialog.show();
                    }
                });
            }
        };

        // first login
        if(AccessToken.getCurrentAccessToken() == null) {

            // access token null -- intro
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // access token null -- intro
                    Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                    startActivity(i);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }

        // init universal image loader
        if(!imageLoader.isInited()) ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    System.out.println(msg);

                    // Persist the regID - no need to register again.
                    storeRegistrationId(getApplicationContext(), regid);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    System.out.println(msg);
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
