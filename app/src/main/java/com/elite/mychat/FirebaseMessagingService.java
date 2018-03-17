package com.elite.mychat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by evk29 on 26-01-2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {//remoteMessage is the payload found in index.js
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();
        String notification_click_action = remoteMessage.getNotification().getClickAction();
        String data_from_user_id = remoteMessage.getData().get("from_user_id_key");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "M_CH_ID")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_chat)
                        .setContentTitle(notification_title)
                        .setContentText(notification_body);

        Intent ProfileIntent = new Intent(notification_click_action);
        ProfileIntent.putExtra("uid", data_from_user_id);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        ProfileIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(pendingIntent);

        int notification_id = (int) System.currentTimeMillis();

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(notification_id, builder.build());

    }
}
