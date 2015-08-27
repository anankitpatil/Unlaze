package com.unlazeapp.unlaze;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.unlazeapp.R;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListener;
import com.unlazeapp.utils.ApiServiceListenerP;
import com.unlazeapp.utils.GlobalVars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by AP on 15/07/15.
 */
public class ConnectionFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String KEY_TITLE = "Conversations";

    ImageLoader imageLoader = ImageLoader.getInstance();

    SwipeRefreshLayout swipeLayout;

    public ConnectionFragment() {
        // Required empty public constructor
    }

    public static ConnectionFragment newInstance(String title) {
        ConnectionFragment f = new ConnectionFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        f.setArguments(args);
        return (f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_connections, container, false);

        // pull to refresh
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        // get requests
        connectionFetch(v);
        return v;
    }

    public void connectionFetch (final View v) {
        try {
            ApiService call = ApiService.getInstance();
            call.getConnect(GlobalVars.getInstance().userDetail.getString("id"), new ApiServiceListenerP() {
                @Override
                public void onSuccess(JSONArray result) {

                    // clear
                    final TableLayout tl = (TableLayout) v.findViewById(R.id.connection_results);
                    tl.removeAllViews();

                    // make requests global
                    GlobalVars.getInstance().userConnect = result;

                    if (GlobalVars.getInstance().userConnect.length() > 0) {
                        for (int k = 0; k < GlobalVars.getInstance().userConnect.length(); k++) {
                            try {
                                if (GlobalVars.getInstance().userConnect.getJSONObject(k).getBoolean("valid")) {
                                    ApiService call = ApiService.getInstance();
                                    call.getDetail(GlobalVars.getInstance().userConnect.getJSONObject(k).getString("_with"), new ApiServiceListener() {
                                        @Override
                                        public void onSuccess(final JSONObject result) {

                                            // Create search results view
                                            final LayoutInflater inflater = getActivity().getLayoutInflater();
                                            final TableRow row = (TableRow) inflater.inflate(R.layout.item_search, null, true);
                                            final ImageView face = (ImageView) row.findViewById(R.id.icon);
                                            final TextView firstLine = (TextView) row.findViewById(R.id.firstLine);
                                            final TextView secondLine = (TextView) row.findViewById(R.id.secondLine);
                                            final ImageView icon = (ImageView) row.findViewById(R.id.activity_icon);
                                            try {

                                                // Calculate age
                                                final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                                                final String dateInString = result.getString("birth");
                                                Calendar dob = Calendar.getInstance();
                                                try {
                                                    dob.setTime(formatter.parse(dateInString));
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                Calendar today = Calendar.getInstance();
                                                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                                                if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
                                                    age--;
                                                } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                                                        && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                                                    age--;
                                                }
                                                try {
                                                    firstLine.setText(result.getString("name") + ", " + age);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                // Get distance
                                                float results[] = new float[2];
                                                Location.distanceBetween(
                                                        GlobalVars.getInstance().latitude,
                                                        GlobalVars.getInstance().longitude,
                                                        result.getJSONObject("coords").getJSONArray("coordinates").getDouble(1),
                                                        result.getJSONObject("coords").getJSONArray("coordinates").getDouble(0),
                                                        results);
                                                if (result.has("city")) {
                                                    secondLine.setText(result.getString("city") + ", " + roundOff(results[0] * 0.001) + " km away");
                                                } else {
                                                    secondLine.setText("Unknown, " + roundOff(results[0] * 0.001) + " km away");
                                                }

                                                ApiService call = ApiService.getInstance();
                                                call.getActivity(result.getString("id"), new ApiServiceListenerP() {
                                                    @Override
                                                    public void onSuccess(JSONArray result) {

                                                        // set activity icon
                                                        if (result.length() > 0) {
                                                            for (int k = 0; k < result.length(); k++) {
                                                                try {
                                                                    if (result.getJSONObject(k).getBoolean("valid")) {
                                                                        for (int j = 0; j < GlobalVars.getInstance().activityList.length; j++) {
                                                                            if (result.getJSONObject(k).getString("_type").equals(GlobalVars.getInstance().activityList[j])) {
                                                                                icon.setImageResource(GlobalVars.getInstance().activityRIcons[j]);
                                                                            }
                                                                        }
                                                                    }
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                    }
                                                });

                                                // profile pic
                                                final DisplayImageOptions options;
                                                options = new DisplayImageOptions.Builder()
                                                        .displayer(new FadeInBitmapDisplayer(600))
                                                        .cacheInMemory(true)
                                                        .cacheOnDisc(true)
                                                        .build();
                                                imageLoader.displayImage("https://graph.facebook.com/" + result.getString("id") + "/picture?type=normal", face, options);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            // Add click listener on item;
                                            row.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    // Show person detail
                                                    GlobalVars.getInstance().personDetail = result;

                                                    // Start personView
                                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                                    startActivityForResult(i, 0);
                                                }
                                            });
                                            row.setOnTouchListener(new View.OnTouchListener() {
                                                @Override
                                                public boolean onTouch(View v, MotionEvent event) {
                                                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                                        v.setBackgroundColor(getResources().getColor(R.color.u_lgrey));
                                                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                                                        v.setBackgroundColor(0);
                                                    }
                                                    return false;
                                                }
                                            });
                                            tl.addView(row);
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {

                        // show no notifications message
                    }
                    swipeLayout.setRefreshing(false);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    double roundOff(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    public void onRefresh() {

        // refresh user and populate connections
        connectionFetch(getView());
    }

}
