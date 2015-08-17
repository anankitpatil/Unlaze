package com.unlazeapp.unlaze;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by AP on 15/07/15.
 */
public class NotificationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String KEY_TITLE = "Notifications";

    ImageLoader imageLoader = ImageLoader.getInstance();

    SwipeRefreshLayout swipeLayout;

    public NotificationFragment() {
        // Required empty public constructor
    }

    public static NotificationFragment newInstance(String title) {
        NotificationFragment f = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        f.setArguments(args);
        return (f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_notification, container, false);

        // pull to refresh
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        // first search
        notificationFetch(v);
        return v;
    }

    public void notificationFetch (final View v) {

        // get requests
        try {
            ApiService call = ApiService.getInstance();
            call.getRequest(GlobalVars.getInstance().userDetail.getString("id"), new ApiServiceListenerP() {
                @Override
                public void onSuccess(JSONArray result) {

                    // clear
                    final TableLayout tl = (TableLayout) v.findViewById(R.id.notification_results);
                    tl.removeAllViews();

                    // make requests global
                    GlobalVars.getInstance().userRequest = result;

                    if (GlobalVars.getInstance().userRequest.length() > 0) {
                        for (int k = 0; k < GlobalVars.getInstance().userRequest.length(); k++) {
                            try {
                                if (GlobalVars.getInstance().userRequest.getJSONObject(k).getBoolean("valid")) {
                                    ApiService call = ApiService.getInstance();
                                    final int _k = k;
                                    call.getDetail(GlobalVars.getInstance().userRequest.getJSONObject(k).getString("_with"), new ApiServiceListener() {
                                        @Override
                                        public void onSuccess(final JSONObject result) {

                                            // Create search results view
                                            LayoutInflater li = getActivity().getLayoutInflater();
                                            TableRow row = (TableRow) li.inflate(R.layout.item_notification, null, true);
                                            ImageView face = (ImageView) row.findViewById(R.id.icon);
                                            TextView line = (TextView) row.findViewById(R.id.line);
                                            TextView time = (TextView) row.findViewById(R.id.time);
                                            try {

                                                // profile pic and text
                                                final DisplayImageOptions options;
                                                options = new DisplayImageOptions.Builder()
                                                        .displayer(new FadeInBitmapDisplayer(600))
                                                        .cacheInMemory(true)
                                                        .cacheOnDisc(true)
                                                        .build();
                                                imageLoader.displayImage("https://graph.facebook.com/" + GlobalVars.getInstance().userRequest.getJSONObject(_k).getString("_with") + "/picture?type=normal", face, options);
                                                line.setText(GlobalVars.getInstance().userRequest.getJSONObject(_k).getString("name") + " has pinged you!");

                                                // get time lapsed
                                                final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
                                                SimpleDateFormat sf = new SimpleDateFormat(FORMAT, Locale.ENGLISH);
                                                sf.setTimeZone(TimeZone.getTimeZone("gmt"));
                                                long _time = sf.parse(GlobalVars.getInstance().userRequest.getJSONObject(_k).getString("created")).getTime();
                                                long now = System.currentTimeMillis();
                                                CharSequence relativeTimeStr = DateUtils.getRelativeTimeSpanString(_time, now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
                                                time.setText(relativeTimeStr);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            // Add click listener on item
                                            row.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    // make person global
                                                    GlobalVars.getInstance().personDetail = result;

                                                    // person loaded -- show request activity
                                                    Intent i = new Intent(getActivity(), RequestActivity.class);
                                                    startActivityForResult(i, 0);
                                                }
                                            });
                                            row.setOnTouchListener(new View.OnTouchListener() {
                                                @Override
                                                public boolean onTouch(View v, MotionEvent event) {
                                                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                                                        v.setBackgroundColor(getResources().getColor(R.color.u_lgrey));
                                                    } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
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

    @Override
    public void onRefresh() {

        // refresh user and populate connections
        notificationFetch(getView());
    }
}
