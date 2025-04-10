package com.example.connecta666620de;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.connecta666620de.adapters.NotificationAdapter;
import com.example.connecta666620de.model.Notification;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;
    private ValueEventListener notificationsListener;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupEmptyState();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadNotifications();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeNotificationsListener();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.notifications_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyStateText = view.findViewById(R.id.empty_state_text);
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadNotifications();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupEmptyState() {
        emptyStateText.setText("No notifications yet");
        emptyStateText.setVisibility(View.GONE);
    }

    private void loadNotifications() {
        if (currentUser == null) {
            AndroidUtil.showToast(getContext(), "Please sign in to view notifications");
            return;
        }

        // Get current time minus 2 days in milliseconds
        long twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000);

        Query notificationsQuery = FirebaseDatabase.getInstance()
                .getReference("Connecta/Notifications")
                .child(currentUser.getUid())
                .orderByChild("timestamp");

        removeNotificationsListener();

        notificationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Notification notification = ds.getValue(Notification.class);
                    if (notification != null) {
                        notification.setNotificationId(ds.getKey());
                        notificationList.add(0, notification); // Newest first
                    }
                }

                // Delete old notifications in background
                deleteOldNotifications(twoDaysAgo);

                updateEmptyState();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AndroidUtil.showToast(getContext(), "Failed to load notifications");
                updateEmptyState();
            }
        };

        notificationsQuery.addValueEventListener(notificationsListener);
    }

    private void updateEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void removeNotificationsListener() {
        if (notificationsListener != null && currentUser != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Connecta/Notifications")
                    .child(currentUser.getUid())
                    .removeEventListener(notificationsListener);
        }
    }

    private void deleteOldNotifications(long cutoffTime) {
        if (currentUser == null) return;

        FirebaseDatabase.getInstance()
                .getReference("Connecta/Notifications")
                .child(currentUser.getUid())
                .orderByChild("timestamp")
                .endAt(cutoffTime - 1) // All notifications older than cutoff
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue(); // Delete old notification
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ActivityFragment", "Failed to delete old notifications: " + error.getMessage());
                    }
                });
    }
}