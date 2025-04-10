package com.example.connecta666620de;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.adapters.UserConnectionsAdapter;
import com.example.connecta666620de.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserConnectionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserConnectionsAdapter adapter;
    private List<UserModel> userList;
    private String userId;
    private String connectionType; // "followers" or "following"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_connections, container, false);

        // Get arguments
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            connectionType = getArguments().getString("connectionType");
        }

        // Set up toolbar with back button
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setTitle(
                connectionType.equals("followers") ? "Followers" : "Following"
        );

        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

//        TextView titleTv = view.findViewById(R.id.titleTv);
//        titleTv.setText(connectionType.equals("followers") ? "Followers" : "Following");

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new UserConnectionsAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);

        fetchConnections();

        return view;
    }

    private void fetchConnections() {
        DatabaseReference connectionsRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("UserConnections")
                .child(userId)
                .child(connectionType);

        connectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String connectedUserId = ds.getKey();
                    fetchUserDetails(connectedUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void fetchUserDetails(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("ConnectaUsers")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    user.setUid(userId); // Ensure UID is set
                    userList.add(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}