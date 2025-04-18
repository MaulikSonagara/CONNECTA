package com.example.connecta666620de.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.R;
import com.example.connecta666620de.ShowUserFragment;
import com.example.connecta666620de.model.Notification;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context context;
    private final List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Set time first
        holder.timeTv.setText(getTimeAgo(notification.getTimestamp()));

        String userIdToFetch = notification.getType().equals("you_followed")
                ? notification.getReceiverId()
                : notification.getSenderId();

        FirebaseDatabase.getInstance()
                .getReference("Connecta/ConnectaUsers")
                .child(userIdToFetch)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String userName = snapshot.child("userName").getValue(String.class);
                            String profilePicUrl = snapshot.child("image").getValue(String.class);

                            // Load profile image using your existing pattern
                            loadProfileImage(holder.avatarIv, profilePicUrl);

                            // Handle notification text
                            String notificationText;
                            switch (notification.getType()) {
                                case "follow":
                                    notificationText = userName + " connected with you!";
                                    break;
                                case "you_followed":
                                    notificationText = "You connected with " + userName + "!";
                                    SpannableString spannable = new SpannableString(notificationText);
                                    int start = notificationText.indexOf(userName);
                                    spannable.setSpan(
                                            new StyleSpan(Typeface.BOLD),
                                            start,
                                            start + userName.length(),
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    );
                                    holder.notificationTextTv.setText(spannable);
                                    return;
                                case "like":
                                    notificationText = userName + " liked your post";
                                    break;
                                case "comment":
                                    notificationText = userName + " commented on your post";
                                    break;
                                case "share":
                                    notificationText = notification.getContent(); // e.g., "UserA shared a post by UserB"
                                    break;
                                default:
                                    notificationText = notification.getContent();
                            }
                            holder.notificationTextTv.setText(notificationText);
                        } catch (Exception e) {
                            Log.e("NotificationAdapter", "Error processing notification", e);
                            holder.notificationTextTv.setText("New notification");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("NotificationAdapter", "Database error: " + error.getMessage());
                        holder.notificationTextTv.setText("New notification");
                    }
                });

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            // Determine which user's profile to open based on notification type
            String userIdToOpen;
            if (notification.getType().equals("you_followed")) {
                // For "You followed" notifications, open the profile of who you followed
                userIdToOpen = notification.getReceiverId();
            } else {
                // For all other notifications, open the profile of who sent it
                userIdToOpen = notification.getSenderId();
            }

            // Create and show the profile fragment
            Fragment showUserFragment = new ShowUserFragment();
            Bundle args = new Bundle();
            args.putString("userId", userIdToOpen);
            showUserFragment.setArguments(args);

            // Use the fragment's context to get the FragmentManager
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame_layout, showUserFragment)
                    .addToBackStack("notification_to_profile")
                    .commit();
        });
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

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

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatarIv;
        TextView notificationTextTv, timeTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.notification_user_avatar);
            notificationTextTv = itemView.findViewById(R.id.notification_text);
            timeTv = itemView.findViewById(R.id.notification_time);

            // Verify all views are found
            if (avatarIv == null || notificationTextTv == null || timeTv == null) {
                throw new IllegalStateException("Missing required views in item_notification.xml");
            }
        }
    }

    private void loadProfileImage(ImageView avatarIv, String profilePicUrl) {
        Log.d("ProfilePic", "Loading profile image: " + profilePicUrl);

        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            if (profilePicUrl.startsWith("gs://")) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(profilePicUrl);

                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("GlideDebug", "Converted URL: " + uri.toString());
                    Glide.with(context)
                            .load(uri.toString())
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.person_icon)
                            .error(R.drawable.person_icon)
                            .into(avatarIv);
                }).addOnFailureListener(e -> {
                    Log.e("GlideDebug", "Failed to get download URL: " + e.getMessage());
                    avatarIv.setImageResource(R.drawable.person_icon);
                });
            } else {
                Glide.with(context)
                        .load(profilePicUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.person_icon)
                        .error(R.drawable.person_icon)
                        .into(avatarIv);
                Log.d("GlideDebug", "Loading image: " + profilePicUrl);
            }
        } else {
            avatarIv.setImageResource(R.drawable.person_icon);
            Log.e("GlideDebug", "No image URL provided");
        }
    }
}