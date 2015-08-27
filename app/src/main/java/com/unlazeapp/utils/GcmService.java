package com.unlazeapp.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.unlazeapp.unlaze.SplashActivity;

public class GcmService extends IntentService {

    public static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    static final String TAG = "Unlaze";

    private SharedPreferences shared;
    static final String PREF = "unlazePreferences";

    private String content;

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
                sendNotification(null, "Message send error", null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification(null, "Message Deleted", null);
                Log.i(TAG, "GCM // Deleted message");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                // Init notif content
                if(!imageLoader.isInited()) ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
                DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc()
                        .imageScaleType(ImageScaleType.NONE)
                        .handler(new Handler())
                        .build();
                final Bitmap bmp = imageLoader.loadImageSync("https://graph.facebook.com/" + extras.getString("user") + "/picture?width=150&height=150", options);
                content = "You have an new request from " + extras.getString("name") + " to join in for some " + extras.getString("activity") + ".";
                sendNotification(bmp, content, extras.getString("user"));
                Log.i(TAG, "GCM Sent to notification //" + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bitmap bmp, String msg, String id) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.unlaze_notification)
                .setLargeIcon(bmp)
                .setContentTitle("New unlaze request!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setAutoCancel(true);

        final Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.putExtra("mode", "request");
        intent.putExtra("person", id);

        // start new application
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pi);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
