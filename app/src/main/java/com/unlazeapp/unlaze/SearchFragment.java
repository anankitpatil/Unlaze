package com.unlazeapp.unlaze;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.unlazeapp.R;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListenerP;
import com.unlazeapp.utils.GlobalVars;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by AP on 15/07/15.
 */
public class SearchFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String KEY_TITLE = "Search";

    ImageLoader imageLoader = ImageLoader.getInstance();

    SwipeRefreshLayout swipeLayout;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String title) {
        SearchFragment f = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        f.setArguments(args);
        return (f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_search, container, false);
        try {
            ApiService call = ApiService.getInstance();
            call.getActivity(GlobalVars.getInstance().userDetail.getString("id"), new ApiServiceListenerP() {
                @Override
                public void onSuccess(JSONArray result) {
                    try {

                        // make activity global
                        GlobalVars.getInstance().userActivity = result;

                        // Get selected activity
                        if (GlobalVars.getInstance().userActivity.length() > 0) {
                            for (int j = 0; j < GlobalVars.getInstance().userActivity.length(); j++) {
                                if (GlobalVars.getInstance().userActivity.getJSONObject(j).getBoolean("valid")) {
                                    GlobalVars.getInstance().selectedActivity = GlobalVars.getInstance().userActivity.getJSONObject(j).getString("_type");
                                }
                            }

                            // Set male female options
                            final CheckBox male = (CheckBox) v.findViewById(R.id.men_switch);
                            final CheckBox female = (CheckBox) v.findViewById(R.id.women_switch);

                            // load last selection
                            if (GlobalVars.getInstance().userDetail.getInt("selected_gender") == 2) {

                                // disable men
                                male.setChecked(false);
                            } else if (GlobalVars.getInstance().userDetail.getInt("selected_gender") == 1) {

                                // disable women
                                female.setChecked(false);
                            }

                            // male female listeners
                            male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                                    try {
                                        if (b && female.isChecked()) {
                                            GlobalVars.getInstance().userDetail.put("selected_gender", 0);
                                        } else if (!b && female.isChecked()) {
                                            GlobalVars.getInstance().userDetail.put("selected_gender", 2);
                                        } else if (!b && !female.isChecked()) {
                                            Toast.makeText(getActivity(), "You must select at least one.", Toast.LENGTH_LONG).show();
                                            buttonView.setChecked(true);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                                    try {
                                        if (b && male.isChecked()) {
                                            GlobalVars.getInstance().userDetail.put("selected_gender", 0);
                                        } else if (!b && male.isChecked()) {
                                            GlobalVars.getInstance().userDetail.put("selected_gender", 1);
                                        } else if (!b && !male.isChecked()) {
                                            Toast.makeText(getActivity(), "You must select at least one.", Toast.LENGTH_LONG).show();
                                            buttonView.setChecked(true);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            // Set radius slider
                            SeekBar slider = (SeekBar) v.findViewById(R.id.slider);
                            slider.setProgress(GlobalVars.getInstance().userDetail.getInt("selected_radius") / 1000);
                            final TextView radiusValue = (TextView) v.findViewById(R.id.sliderValue);
                            radiusValue.setText(roundOff(GlobalVars.getInstance().userDetail.getInt("selected_radius") / 1000) + " km");
                            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    try {
                                        GlobalVars.getInstance().userDetail.put("selected_radius", progress * 1000);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    radiusValue.setText(roundOff(progress) + " km");
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });

                            // pull to refresh
                            swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
                            swipeLayout.setOnRefreshListener(SearchFragment.this);

                            // People search
                            personSearch(v);
                        } else {

                            // If no activities selected
                            Toast.makeText(getActivity(), "Select an activity to get started!.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return v;
    }

    public void personSearch (final View v) {

        // get selection
        int gender = 0, radius = 0;
        try {
            gender = GlobalVars.getInstance().userDetail.getInt("selected_gender");
            radius = GlobalVars.getInstance().userDetail.getInt("selected_radius");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // get person search results
        ApiService call = ApiService.getInstance();
        call.getPeople(gender, GlobalVars.getInstance().selectedActivity.replaceAll(" ", "%20"), radius, new ApiServiceListenerP() {
            @Override
            public void onSuccess(final JSONArray result) {
                try {
                    TableLayout tl = (TableLayout) v.findViewById(R.id.person_search_results);
                    tl.removeAllViews();
                    if (result.getJSONArray(0).length() > 0) {
                        for (int i = 0; i < result.getJSONArray(0).length(); i++) {

                            // remove self
                            if (result.getJSONArray(0).getJSONObject(i).getString("id").equals(GlobalVars.getInstance().userDetail.getString("id"))) continue;

                            // check if request exists with user
                            Boolean requestExists = false;
                            if (result.getJSONArray(1).getJSONArray(i).length() > 0) {
                                for (int j = 0; j < result.getJSONArray(1).getJSONArray(i).length(); j++) {
                                    if (result.getJSONArray(1).getJSONArray(i).getJSONObject(j).getString("_with").equals(GlobalVars.getInstance().userDetail.getString("id"))) {

                                        // check if same activity
                                        if (result.getJSONArray(1).getJSONArray(i).getJSONObject(j).getString("_type").equals(GlobalVars.getInstance().selectedActivity)) {

                                            // request exists -- close loop
                                            requestExists = true;
                                        }
                                    }
                                }
                            }
                            if (requestExists) continue;

                            // Create search results view
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            TableRow row = (TableRow) inflater.inflate(R.layout.item_search, null, true);
                            ImageView face = (ImageView) row.findViewById(R.id.icon);
                            TextView firstLine = (TextView) row.findViewById(R.id.firstLine);
                            TextView secondLine = (TextView) row.findViewById(R.id.secondLine);
                            ImageView icon = (ImageView) row.findViewById(R.id.activity_icon);

                            // Calculate age
                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                            String dateInString = result.getJSONArray(0).getJSONObject(i).getString("birth");
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
                            firstLine.setText(result.getJSONArray(0).getJSONObject(i).getString("name") + ", " + age);

                            // Get distance
                            float results[] = new float[2];
                            Location.distanceBetween(
                                    GlobalVars.getInstance().latitude,
                                    GlobalVars.getInstance().longitude,
                                    result.getJSONArray(0).getJSONObject(i).getJSONObject("coords").getJSONArray("coordinates").getDouble(1),
                                    result.getJSONArray(0).getJSONObject(i).getJSONObject("coords").getJSONArray("coordinates").getDouble(0),
                                    results);
                            if (result.getJSONArray(0).getJSONObject(i).has("city")) {
                                secondLine.setText(result.getJSONArray(0).getJSONObject(i).getString("city") + ", " + roundOff(results[0] * 0.001) + " km away");
                            } else {
                                secondLine.setText("Unknown, " + roundOff(results[0] * 0.001) + " km away");
                            }

                            // Get selected activity icon
                            for (int j = 0; j < GlobalVars.getInstance().activityList.length; j++) {
                                if (GlobalVars.getInstance().activityList[j].equals(GlobalVars.getInstance().selectedActivity)) {
                                    icon.setImageResource(GlobalVars.getInstance().activityIcons[j]);
                                }
                            }

                            // profile pic
                            final DisplayImageOptions options;
                            options = new DisplayImageOptions.Builder()
                                    .displayer(new FadeInBitmapDisplayer(600))
                                    .cacheInMemory(true)
                                    .cacheOnDisk(true)
                                    .build();
                            imageLoader.displayImage("https://graph.facebook.com/" + result.getJSONArray(0).getJSONObject(i).getString("id") + "/picture?type=normal", face, options);

                            // Add click listener on item
                            final int _i = i;
                            row.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    // Show person detail
                                    try {
                                        GlobalVars.getInstance().personDetail = result.getJSONArray(0).getJSONObject(_i);

                                        // Start personView
                                        Intent i = new Intent(getActivity(), PersonActivity.class);
                                        startActivityForResult(i, 0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            tl.addView(row);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // remove refresh
                swipeLayout.setRefreshing(false);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    double roundOff(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    public void onRefresh() {

        // start search
        personSearch(getView());
    }

}