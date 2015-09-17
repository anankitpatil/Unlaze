package com.unlazeapp.unlaze;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.unlazeapp.R;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListener;
import com.unlazeapp.utils.ApiServiceListenerP;
import com.unlazeapp.utils.GlobalVars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by AP on 15/07/15.
 */
public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button button;
    private EditText write;
    private TableLayout tl;
    private ScrollView sv;

    ImageLoader imageLoader = ImageLoader.getInstance();

    private static ChatActivity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        // SETCONTENT
        setContentView(R.layout.activity_chat);

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

        // init content
        tl = (TableLayout) findViewById(R.id.u_chat);
        sv = (ScrollView) findViewById(R.id.chat_container);

        // get past messages from API
        try {
            imageLoader.loadImage("https://graph.facebook.com/" + GlobalVars.getInstance().personDetail.getString("id") + "/picture", new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    getSupportActionBar().setIcon(new BitmapDrawable(getResources(), loadedImage));
                }
            });
            getSupportActionBar().setTitle(" " + GlobalVars.getInstance().personDetail.getString("name"));
            ApiService call = ApiService.getInstance();
            call.getChatWith(GlobalVars.getInstance().userDetail.getString("id"), GlobalVars.getInstance().personDetail.getString("id"), new ApiServiceListenerP() {
                @SuppressLint("NewApi")
                @Override
                public void onSuccess(JSONArray result) {
                    try {

                        // make chat global and sort
                        ArrayList<JSONObject> array = new ArrayList<>();
                        if (result.length() > 0) {
                            GlobalVars.getInstance().userChat = result.getJSONArray(0);
                            for (int i = 0; i < GlobalVars.getInstance().userChat.length(); i++) {
                                try {
                                    GlobalVars.getInstance().userChat.getJSONObject(i).put("user", true);
                                    array.add(GlobalVars.getInstance().userChat.getJSONObject(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (result.length() > 1) {
                            GlobalVars.getInstance().personChat = result.getJSONArray(1);
                            for (int i = 0; i < GlobalVars.getInstance().personChat.length(); i++) {
                                try {
                                    GlobalVars.getInstance().personChat.getJSONObject(i).put("user", false);
                                    array.add(GlobalVars.getInstance().personChat.getJSONObject(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // sort
                        if ((GlobalVars.getInstance().personChat != null && GlobalVars.getInstance().personChat.length() > 0) || (GlobalVars.getInstance().userChat != null && GlobalVars.getInstance().userChat.length() > 0)) {
                            Collections.sort(array, new Comparator<JSONObject>() {
                                @Override
                                public int compare(JSONObject lhs, JSONObject rhs) {
                                    try {
                                        return (lhs.getString("created").compareTo(rhs.getString("created")));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        return 0;
                                    }
                                }
                            });
                        }

                        // print
                        if (array != null && array.size() > 0) {
                            for (int i = 0; i < array.size(); i++) {
                                if (array.get(i).getBoolean("user")) {
                                    final LayoutInflater inflater = getLayoutInflater();
                                    final TableRow row = (TableRow) inflater.inflate(R.layout.item_chat_user, null, true);
                                    final TextView line = (TextView) row.findViewById(R.id.content);
                                    line.setText(array.get(i).getString("text"));
                                    tl.addView(row);
                                } else {
                                    final LayoutInflater inflater = getLayoutInflater();
                                    final TableRow row = (TableRow) inflater.inflate(R.layout.item_chat_person, null, true);
                                    final TextView line = (TextView) row.findViewById(R.id.content);
                                    line.setText(array.get(i).getString("text"));
                                    tl.addView(row);
                                }
                            }
                        }
                        sv.post(new Runnable() {
                            @Override
                            public void run() {
                                sv.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {

                }
            });
        } catch(JSONException e) {
            e.printStackTrace();
        }

        // chat sender
        button = (Button) findViewById(R.id.enter);
        write = (EditText) findViewById(R.id.write);
        /* enter sends message -- disabled
        write.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){
                if (s.length() >= 1) {
                    if (s.charAt(s.length() - 1) == '\n') {

                        // when enter pressed
                        final JSONObject message = new JSONObject();
                        try {
                            message.put("text", write.getText().toString().substring(0, write.getText().toString().length() - 1));
                            sendMessage(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        write.setText("");
                        write.requestFocus();
                    }
                }
            }
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){
            }
            @Override
            public void afterTextChanged (Editable s){
            }
        });*/
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!write.getText().equals("")) {
                    final JSONObject message = new JSONObject();
                    try {
                        message.put("text", write.getText());
                        sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    write.setText("");
                    write.requestFocus();
                }
            }
        });

        // tell intentservice app open
        GlobalVars.getInstance().U_APP_STATE = "chat";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        final LayoutInflater inflater = getLayoutInflater();
        final TableRow row = (TableRow) inflater.inflate(R.layout.item_chat_person, null, true);
        final TextView line = (TextView) row.findViewById(R.id.content);
        line.setText(intent.getStringExtra("message"));
        tl.addView(row);
        sv.post(new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void sendMessage (final JSONObject message) {
        ApiService call = ApiService.getInstance();
        call.sendChatMessage(message, new ApiServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {

                // add content to screen
                final LayoutInflater inflater = getLayoutInflater();
                final TableRow row = (TableRow) inflater.inflate(R.layout.item_chat_user, null, true);
                final TextView line = (TextView) row.findViewById(R.id.content);
                try {
                    line.setText(message.getString("text"));
                    tl.addView(row);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }

            @Override
            public void onFailure() {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static ChatActivity getInstance() {
        return instance;
    }
}
