package com.unlazeapp.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.unlazeapp.R;
import com.unlazeapp.unlaze.ChatActivity;
import com.unlazeapp.unlaze.SplashActivity;

import org.json.JSONException;

public class GcmService extends IntentService {

    public static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    static final String TAG = "UNLAZE";

    ImageLoader imageLoader = ImageLoader.getInstance();

    public GcmService() {
        super("GcmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(TAG, "GCM // Send error");
                createNotification(null, "Message send error", null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                createNotification(null, "Message Deleted", null);
                Log.i(TAG, "GCM // Deleted message");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                // load image
                if(!imageLoader.isInited()) ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
                DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(true).cacheInMemory(true)
                        .imageScaleType(ImageScaleType.NONE)
                        .handler(new Handler())
                        .build();
                final Bitmap bmp = imageLoader.loadImageSync("https://graph.facebook.com/" + extras.getString("user") + "/picture?width=150&height=150", options);
                if (null != GlobalVars.getInstance().U_APP_STATE) {
                    if (extras.getString("form").equals("notification")) {

                        // app open -- create request notification anyway
                        final String content = "You have an new request from " + extras.getString("name") + " to join in for some " + extras.getString("activity") + ".";
                        createNotification(bmp, content, extras.getString("user"));
                        Log.i(TAG, "new REQUEST notif" + extras.toString());
                    } else if (extras.getString("form").equals("conversation")) {
                        if (GlobalVars.getInstance().U_APP_STATE == "main") {

                            // app in main -- create notification
                            createChatNotification(bmp, extras.getString("name"), extras.getString("message"), extras.getString("user"));
                            Log.i(TAG, "new CHAT notif" + extras.toString());
                        } else if (GlobalVars.getInstance().U_APP_STATE == "chat") {
                            try {
                                if (GlobalVars.getInstance().personDetail.getString("id").equals(extras.getString("user"))) {

                                    // app in chat with same person -- send chat message
                                    Intent chatIntent = new Intent(getBaseContext(), ChatActivity.class);
                                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    chatIntent.putExtra("message", extras.getString("message"));
                                    getApplication().startActivity(chatIntent);
                                } else {

                                    // different person -- create notification
                                    createChatNotification(bmp, extras.getString("name"), extras.getString("message"), extras.getString("user"));
                                    Log.i(TAG, "new CHAT notif" + extras.toString());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {

                    // app not active
                    if (extras.getString("form").equals("notification")) {

                        // new request notification
                        final String content = "You have an new request from " + extras.getString("name") + " to join in for some " + extras.getString("activity") + ".";
                        createNotification(bmp, content, extras.getString("user"));
                        Log.i(TAG, "new REQUEST notif" + extras.toString());
                    } else if (extras.getString("form").equals("conversation")) {

                        // new message notification
                        createChatNotification(bmp, extras.getString("name"), extras.getString("message"), extras.getString("user"));
                        Log.i(TAG, "new CHAT notif" + extras.toString());
                    }
                }
            }
        }
        GcmReceiver.completeWakefulIntent(intent);
    }

    private void createNotification(Bitmap bmp, String msg, String id) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.unlaze_notification)
                .setLargeIcon(bmp)
                .setContentTitle("New UNLAZE request!")
                .setContentText(msg)
                .setAutoCancel(true);
        final Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.putExtra("mode", "request");
        intent.putExtra("person", id);

        // start new application
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private  void createChatNotification(Bitmap bmp, String title, String msg, String id) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.unlaze_notification)
                .setLargeIcon(bmp)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true);
        final Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.putExtra("mode", "chat");
        intent.putExtra("person", id);

        // start new application
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
