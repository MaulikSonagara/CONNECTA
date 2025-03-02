package com.example.connecta666620de;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connecta666620de.model.AdapterUsers;
import com.example.connecta666620de.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    RecyclerView userRecyclerView;
    AdapterUsers adapterUsers;
    List<UserModel> userList;
    EditText searchUserEt;
    ImageView searchBtn;
    FirebaseUser fUser;
    DatabaseReference ref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        userRecyclerView = view.findViewById(R.id.user_recyclerView);
        searchUserEt = view.findViewById(R.id.searchUserEt);


        // Setup RecyclerView
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize User List and Adapter
        userList = new ArrayList<>();
        adapterUsers = new AdapterUsers(getContext(), userList);
        userRecyclerView.setAdapter(adapterUsers);

        // Get current user and Firebase reference
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers");

        // Fetch all users
        getAllUser();

        // Add TextWatcher for real-time search
        searchUserEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter users as the user types
                searchUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });


        return view;
    }

    private void getAllUser() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserModel model = ds.getValue(UserModel.class);
                    if (model != null && !model.getEmail().equals(fUser.getEmail())) {
                        userList.add(model);
                    }
                }
                adapterUsers.notifyDataSetChanged(); // Update RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Database Error: " + error.getMessage());
            }
        });
    }

    private void searchUsers(String query) {
        List<UserModel> filteredList = new ArrayList<>();
        for (UserModel user : userList) {
            // Check if the query matches the username, first name, or last name
            if (user.getUserName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getLastName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        // Update the adapter with the filtered list
        adapterUsers.updateList(filteredList);
    }
}