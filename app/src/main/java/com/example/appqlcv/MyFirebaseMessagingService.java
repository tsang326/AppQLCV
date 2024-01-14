package com.example.appqlcv;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.

        if (remoteMessage.getNotification()!=null){
            // Show the notification
            String notificationBody = remoteMessage.getNotification().getBody();
            String notificationTitle = remoteMessage.getNotification().getTitle();

            sendNotification(notificationTitle, notificationBody);

        }
    }


    private void sendNotification(String title, String body) {
        int notificationId = generateUniqueNotificationId();
        NotificationUtils.sendNotification(this, title, body, notificationId);
    }


    private int generateUniqueNotificationId() {
        return (int) System.currentTimeMillis();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM Token", token);
        // Xử lý làm mới token nếu cần
    }

    // Thêm một phương thức để gửi thông báo dựa trên thay đổi trong Realtime Database



}

