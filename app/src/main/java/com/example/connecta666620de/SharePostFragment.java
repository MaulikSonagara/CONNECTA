package com.example.connecta666620de;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.adapters.ShareUserAdapter;
import com.example.connecta666620de.model.Notification;
import com.example.connecta666620de.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SharePostFragment extends DialogFragment {

    private RecyclerView recyclerView;
    private ShareUserAdapter adapter;
    private List<UserModel> followingList;
    private Button sendButton;
    private TextView emptyStateText;
    private String postId;
    private String postOwnerId;
    private String postOwnerUsername;
    private String currentUsername;

    public static SharePostFragment newInstance(String postId, String postOwnerId, String postOwnerUsername) {
        SharePostFragment fragment = new SharePostFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("postOwnerId", postOwnerId);
        args.putString("postOwnerUsername", postOwnerUsername);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme); // Set full-screen dialog theme
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            postOwnerId = getArguments().getString("postOwnerId");
            postOwnerUsername = getArguments().getString("postOwnerUsername");
        }
        fetchCurrentUsername();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_post, container, false);

        recyclerView = view.findViewById(R.id.share_users_recycler_view);
        sendButton = view.findViewById(R.id.send_button);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        followingList = new ArrayList<>();
        adapter = new ShareUserAdapter(getContext(), followingList, selectedUsers -> sendButton.setEnabled(!selectedUsers.isEmpty()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fetchFollowingUsers();

        sendButton.setOnClickListener(v -> {
            sharePostToSelectedUsers();
            dismiss();
        });

        return view;
    }

    private void fetchCurrentUsername() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference("Connecta/ConnectaUsers")
                .child(currentUserId)
                .child("userName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUsername = snapshot.getValue(String.class);
                        if (currentUsername == null) currentUsername = "Unknown";
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        currentUsername = "Unknown";
                    }
                });
    }

    private void fetchFollowingUsers() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference("Connecta/UserConnections")
                .child(currentUserId)
                .child("following")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followingList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String userId = ds.getKey();
                            fetchUserDetails(userId);
                        }
                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        updateEmptyState();
                    }
                });
    }

    private void fetchUserDetails(String userId) {
        FirebaseDatabase.getInstance()
                .getReference("Connecta/ConnectaUsers")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            user.setUid(userId);
                            followingList.add(user);
                            adapter.notifyDataSetChanged();
                        }
                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        updateEmptyState();
                    }
                });
    }

    private void updateEmptyState() {
        emptyStateText.setVisibility(followingList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(followingList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void sharePostToSelectedUsers() {
        List<UserModel> selectedUsers = adapter.getSelectedUsers();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        for (UserModel user : selectedUsers) {
            sendPostAsMessage(currentUserId, user.getUid(), postId);
            sendNotification(currentUserId, user.getUid());
        }
    }

    private void sendPostAsMessage(String senderId, String receiverId, String postId) {
        long timestamp = System.currentTimeMillis();
        String chatId = senderId.compareTo(receiverId) < 0 ? senderId + "_" + receiverId : receiverId + "_" + senderId;
        String message = "Post by @" + postOwnerUsername + ": " + postId;

        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("sender", senderId);
        messageData.put("receiver", receiverId);
        messageData.put("message", message);
        messageData.put("isseen", false);
        messageData.put("timestamp", timestamp);
        messageData.put("messageId", FirebaseDatabase.getInstance().getReference().push().getKey());

        FirebaseDatabase.getInstance()
                .getReference("Connecta/Chat")
                .child(chatId)
                .push()
                .setValue(messageData);

        // Update Chatlist
        HashMap<String, Object> chatlistData = new HashMap<>();
        chatlistData.put("id", receiverId);
        chatlistData.put("timestamp", timestamp);
        chatlistData.put("lastMessage", message);
        FirebaseDatabase.getInstance()
                .getReference("Connecta/Chatlist")
                .child(senderId)
                .child(receiverId)
                .setValue(chatlistData);

        chatlistData.put("id", senderId);
        FirebaseDatabase.getInstance()
                .getReference("Connecta/Chatlist")
                .child(receiverId)
                .child(senderId)
                .setValue(chatlistData);
    }

    private void sendNotification(String senderId, String receiverId) {
        String notificationId = FirebaseDatabase.getInstance().getReference().push().getKey();
        String content = (currentUsername != null ? currentUsername : "Unknown") + " shared a post by " + postOwnerUsername;
        Notification notification = new Notification(
                senderId,
                receiverId,
                "share",
                content,
                postId
        );
        notification.setNotificationId(notificationId);
        notification.setTimestamp(System.currentTimeMillis());

        FirebaseDatabase.getInstance()
                .getReference("Connecta/Notifications")
                .child(receiverId)
                .child(notificationId)
                .setValue(notification);
    }
}