package com.unlazeapp.unlaze;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.unlazeapp.R;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListener;
import com.unlazeapp.utils.GlobalVars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static String name;
    private static String email;

    private SharedPreferences shared;
    static final String PREF = "unlazePreferences";

    private Drawer result;
    private Toolbar toolbar;

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {

            // init variable and profile image
            name = GlobalVars.getInstance().userDetail.getString("name");
            email = GlobalVars.getInstance().userDetail.getString("email");

            // attach toolbar
            toolbar = (Toolbar) findViewById(R.id.u_toolbar);
            setSupportActionBar(toolbar);

            // navigation menu
            result = new DrawerBuilder()
                    .withActivity(this)
                    .withSliderBackgroundColorRes(R.color.u_blue)
                    .withDisplayBelowStatusBar(true)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            new DividerDrawerItem(),
                            new PrimaryDrawerItem()
                                    .withName(R.string.drawer_item_search)
                                    .withIcon(FontAwesome.Icon.faw_search)
                                    .withTextColorRes(R.color.u_white)
                                    .withIconColorRes(R.color.u_white)
                                    .withIdentifier(2),
                            new PrimaryDrawerItem()
                                    .withName(R.string.drawer_item_conversations)
                                    .withIcon(FontAwesome.Icon.faw_comment_o)
                                    .withTextColorRes(R.color.u_white)
                                    .withIconColorRes(R.color.u_white)
                                    .withIdentifier(3),
                            new PrimaryDrawerItem()
                                    .withName(R.string.drawer_item_notifications)
                                    .withIcon(FontAwesome.Icon.faw_bell)
                                    .withTextColorRes(R.color.u_white)
                                    .withIconColorRes(R.color.u_white)
                                    .withIdentifier(4),
                            new PrimaryDrawerItem()
                                    .withName(R.string.drawer_item_invite)
                                    .withIcon(FontAwesome.Icon.faw_plus)
                                    .withTextColorRes(R.color.u_white)
                                    .withIconColorRes(R.color.u_white)
                                    .withIdentifier(5),
                            new PrimaryDrawerItem()
                                    .withName(R.string.drawer_item_feedback)
                                    .withIcon(FontAwesome.Icon.faw_mail_reply)
                                    .withTextColorRes(R.color.u_white)
                                    .withIconColorRes(R.color.u_white)
                                    .withIdentifier(6),
                            new DividerDrawerItem(),
                            new SecondaryDrawerItem()
                                    .withName(R.string.drawer_item_logout)
                                    .withIcon(FontAwesome.Icon.faw_sign_out)
                                    .withTextColorRes(R.color.u_grey)
                                    .withIconColorRes(R.color.u_grey)
                                    .withIdentifier(7),
                            new SecondaryDrawerItem()
                                    .withName(R.string.drawer_item_delete)
                                    .withIcon(FontAwesome.Icon.faw_trash_o)
                                    .withTextColorRes(R.color.u_grey)
                                    .withIconColorRes(R.color.u_grey)
                                    .withIdentifier(8)
                    )
                    .withSavedInstance(savedInstanceState)
                    .build();
            result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

            imageLoader.loadImage("https://graph.facebook.com/" + GlobalVars.getInstance().userDetail.getString("id") + "/picture?width=150&height=150", new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    result.addItem(new ProfileDrawerItem()
                                    .withIcon(loadedImage)
                                    .withName(name)
                                    .withNameShown(true)
                                    .withEmail(email)
                                    .withTextColorRes(R.color.u_white)
                                    .withIdentifier(1), 0
                    );
                    result.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                            if (iDrawerItem.getIdentifier() == 1) {

                                // profile
                                final Intent j = new Intent(MainActivity.this, ProfileActivity.class);
                                startActivityForResult(j, 0);
                            } else if (iDrawerItem.getIdentifier() == 2) {
                                getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                                // search
                                Fragment f = SearchFragment.newInstance(getResources().getString(((Nameable) iDrawerItem).getNameRes()));
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
                            } else if (iDrawerItem.getIdentifier() == 3) {
                                getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                                // conversations
                                Fragment f = ConnectionFragment.newInstance(getResources().getString(((Nameable) iDrawerItem).getNameRes()));
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
                            } else if (iDrawerItem.getIdentifier() == 4) {
                                getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                                // notifications
                                Fragment f = NotificationFragment.newInstance(getResources().getString(((Nameable) iDrawerItem).getNameRes()));
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
                            } else if (iDrawerItem.getIdentifier() == 5) {
                                getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                                // invite friends
                            } else if (iDrawerItem.getIdentifier() == 6) {
                                getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                                // feedback
                            } else if (iDrawerItem.getIdentifier() == 7) {
                                getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                                // logout
                            } else if (iDrawerItem.getIdentifier() == 7) {
                                getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                                // delete
                            }
                            return false;
                        }
                    });
                    result.setSelection(2);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // update user on the API -- on regular app start
        try {
            GlobalVars.getInstance().userDetail.put("gcm", GlobalVars.getInstance().gcm);
            JSONArray coordinates = new JSONArray();
            coordinates.put(0, GlobalVars.getInstance().longitude);
            coordinates.put(1, GlobalVars.getInstance().latitude);
            JSONObject coords = new JSONObject();
            coords.put("coordinates", coordinates);
            coords.put("type", "Point");
            GlobalVars.getInstance().userDetail.put("coords", coords);
            GlobalVars.getInstance().userDetail.put("city", GlobalVars.getInstance().city);
            ApiService call = ApiService.getInstance();
            call.updateDetail(GlobalVars.getInstance().userDetail.getString("id"), GlobalVars.getInstance().userDetail, new ApiServiceListener() {
                @Override
                public void onSuccess(JSONObject result) {
                    GlobalVars.getInstance().userDetail = result;

                    // user updated
                    Toast.makeText(getApplicationContext(), "User Updated.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // save off state
        shared = getSharedPreferences(PREF, 0);
        shared.edit().putBoolean("active", false).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
