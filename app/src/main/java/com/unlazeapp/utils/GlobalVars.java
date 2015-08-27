package com.unlazeapp.utils;

import com.unlazeapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class GlobalVars {

    private static GlobalVars mInstance = null;

    public JSONObject userDetail;
    public JSONArray userActivity;
    public JSONArray userConnect;
    public JSONArray userRequest;
    public JSONObject personDetail;
    public JSONArray personActivity;
    public JSONArray personConnect;
    public JSONArray personRequest;
    public String activityList[] = new String[10];
    public int activityIcons[] = new int[10];
    public int activityRIcons[] = new int[10];
    public String selectedActivity;
    public Double latitude, longitude;
    public String city;
    public String gcm;
    public int U_NOTIF_STATE = 0;

    protected GlobalVars() {

        // Activities list
        activityList[0] = "Table Tennis";
        activityList[1] = "Cycling";
        activityList[2] = "Football";
        activityList[3] = "Swimming";
        activityList[4] = "Badminton";
        activityList[5] = "Golf";
        activityList[6] = "Running";
        activityList[7] = "Cricket";
        activityList[8] = "Pool";
        activityList[9] = "Lawn Tennis";

        // Activities icons
        activityIcons[0] = R.drawable.activity_icon_1;
        activityIcons[1] = R.drawable.activity_icon_2;
        activityIcons[2] = R.drawable.activity_icon_3;
        activityIcons[3] = R.drawable.activity_icon_4;
        activityIcons[4] = R.drawable.activity_icon_5;
        activityIcons[5] = R.drawable.activity_icon_6;
        activityIcons[6] = R.drawable.activity_icon_7;
        activityIcons[7] = R.drawable.activity_icon_8;
        activityIcons[8] = R.drawable.activity_icon_9;
        activityIcons[9] = R.drawable.activity_icon_10;

        // Activities round icons
        activityRIcons[0] = R.drawable.activity_1;
        activityRIcons[1] = R.drawable.activity_2;
        activityRIcons[2] = R.drawable.activity_3;
        activityRIcons[3] = R.drawable.activity_4;
        activityRIcons[4] = R.drawable.activity_5;
        activityRIcons[5] = R.drawable.activity_6;
        activityRIcons[6] = R.drawable.activity_7;
        activityRIcons[7] = R.drawable.activity_8;
        activityRIcons[8] = R.drawable.activity_9;
        activityRIcons[9] = R.drawable.activity_10;

    }

    public static synchronized GlobalVars getInstance(){
        if(null == mInstance){
            mInstance = new GlobalVars();
        }
        return mInstance;
    }

}