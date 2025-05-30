package com.example.connecta666620de;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.adapters.AdapterChatlist;
import com.example.connecta666620de.model.Chat;
import com.example.connecta666620de.model.Chatlist;
import com.example.connecta666620de.model.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<UserModel> userList;
    private List<Chatlist> chatlistList;
    private AdapterChatlist adapterChatlist;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private TextInputEditText searchEt;
    private TextView emptyTv;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.chatsRv);
        searchEt = view.findViewById(R.id.searchEt);
        emptyTv = view.findViewById(R.id.emptyTv);

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatlistList = new ArrayList<>();
        userList = new ArrayList<>();

        // Get chat list
        reference = FirebaseDatabase.getInstance().getReference("Connecta").child("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chatlist chatlist = ds.getValue(Chatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Search functionality
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchChats(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadChats() {
        userList.clear();
        reference = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserModel user = ds.getValue(UserModel.class);
                    for (Chatlist chatlist : chatlistList) {
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())) {
                            // Add chatlist info to the user model
                            user.setLastMessage(chatlist.getLastMessage());
                            user.setTimestamp(chatlist.getTimestamp());
                            userList.add(user);
                            break;
                        }
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(userList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                // Update UI
                if (userList.isEmpty()) {
                    emptyTv.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyTv.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapterChatlist = new AdapterChatlist(getContext(), userList);
                    recyclerView.setAdapter(adapterChatlist);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void searchChats(String query) {
        if (adapterChatlist != null) {
            adapterChatlist.getFilter().filter(query);
        }
    }
}