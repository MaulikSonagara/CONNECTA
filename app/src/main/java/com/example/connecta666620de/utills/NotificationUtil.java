package com.example.connecta666620de.utills;

import android.content.Context;
import android.widget.Toast;

import com.example.connecta666620de.model.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationUtil {
    private static final String NOTIFICATIONS_PATH = "Connecta/Notifications";

    public static void sendFollowNotification(String senderId, String receiverId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid().equals(receiverId)) return;

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                .getReference(NOTIFICATIONS_PATH)
                .child(receiverId);

        String notificationId = notificationsRef.push().getKey();
        String content = "connected with you!";

        Notification notification = new Notification(
                senderId,
                receiverId,
                "follow",
                content,
                null
        );

        notification.setIncoming(true);
        notification.setNotificationId(notificationId);

        notificationsRef.child(notificationId).setValue(notification);
    }

    public static void sendYouFollowedNotification(String senderId, String receiverId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                .getReference(NOTIFICATIONS_PATH)
                .child(senderId);

        String notificationId = notificationsRef.push().getKey();
        String content = "You connected with [USER]!"; // [USER] will be replaced in adapter

        Notification notification = new Notification(
                senderId,
                receiverId,
                "you_followed",
                content,
                null
        );
        notification.setNotificationId(notificationId);

        notificationsRef.child(notificationId).setValue(notification);
    }

    // Helper method to get username
    private static String getUsername(String userId) {
        // In a real app, you might want to cache usernames or use a different approach
        // This is a simplified version that blocks the main thread - consider using callbacks
        try {
            DataSnapshot snapshot = FirebaseDatabase.getInstance()
                    .getReference("Connecta/ConnectaUsers")
                    .child(userId)
                    .child("userName")
                    .get()
                    .getResult();

            return snapshot.getValue(String.class);
        } catch (Exception e) {
            return "a user";
        }
    }

    public static void sendPostInteractionNotification(String senderId, String receiverId, String postId, String interactionType) {
        if (senderId.equals(receiverId)) return;

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                .getReference(NOTIFICATIONS_PATH)
                .child(receiverId);

        String notificationId = notificationsRef.push().getKey();
        String content = interactionType + " your post";

        Notification notification = new Notification(
                senderId,
                receiverId,
                interactionType,
                content,
                postId
        );
        notification.setNotificationId(notificationId);

        notificationsRef.child(notificationId).setValue(notification);
    }

    public static String getTimeAgo(Timestamp timestamp) {
        long now = System.currentTimeMillis();
        long notificationTime = timestamp.toDate().getTime();
        long diff = now - notificationTime;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " min" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
}