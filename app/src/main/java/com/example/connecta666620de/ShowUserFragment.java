package com.example.connecta666620de;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.model.Skill;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ShowUserFragment extends Fragment {


    String searchedUserUid;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    TextView nameTv, usernameTv, bioTv, followersTv, followingTv, postsTv;
    ImageView avatarIv;
    LinearLayout skillsContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_user, container, false);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Connecta").child("ConnectaUsers");

        // Initialize views
        avatarIv = view.findViewById(R.id.profileimage_profile);
        nameTv = view.findViewById(R.id.name_profile);
        usernameTv = view.findViewById(R.id.uname_searchedprofile);
        bioTv = view.findViewById(R.id.bio_profile);
        followersTv = view.findViewById(R.id.follower_data_profile);
        followingTv = view.findViewById(R.id.following_data_profile);
        postsTv = view.findViewById(R.id.post_data_profile);
        skillsContainer = view.findViewById(R.id.skills_container);

        if (getArguments() != null) {
            searchedUserUid = getArguments().getString("userId");
        }

        // Fetch user profile data
        fetchUserProfileData();

        // Fetch user skills
        fetchUserSkills();

        return view;
    }

    private void fetchUserProfileData() {
        Query query = databaseReference.orderByChild("uId").equalTo(searchedUserUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String firstName = "" + ds.child("firstName").getValue();
                    String lastName = "" + ds.child("lastName").getValue();
                    String bio = "" + ds.child("bio").getValue();
                    String username = "" + ds.child("userName").getValue();
                    String profilePicUrl = "" + ds.child("image").getValue();
                    String followers = "" + ds.child("follower").getValue();
                    String following = "" + ds.child("following").getValue();
                    String posts = "" + ds.child("posts").getValue();

                    Log.d("ProfilePic",profilePicUrl);

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        if (profilePicUrl.startsWith("gs://")) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReferenceFromUrl(profilePicUrl);

                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Log.d("GlideDebug", "Converted URL: " + uri.toString());
                                Glide.with(getContext())
                                        .load(uri.toString()) // Load HTTP URL
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.person_icon)
                                        .error(R.drawable.person_icon)
                                        .into(avatarIv);
                            }).addOnFailureListener(e -> {
                                Log.e("GlideDebug", "Failed to get download URL: " + e.getMessage());
                                avatarIv.setImageResource(R.drawable.person_icon);
                            });
                        } else {
                            Glide.with(getContext())
                                    .load(profilePicUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(R.drawable.person_icon)
                                    .error(R.drawable.person_icon)
                                    .into(avatarIv);
                            Log.d("GlideDebug", "Loading image: " + profilePicUrl);
                        }
                    } else {
                        avatarIv.setImageResource(R.drawable.person_icon);
                        Log.e("GlideDebug", "No image URL provided for user: " + searchedUserUid);
                    }

                    // set profile data
                    usernameTv.setText("@" + username);
                    nameTv.setText(firstName + " " + lastName);
                    usernameTv.setText("@" + username);
                    bioTv.setText(bio);
                    followersTv.setText(followers);
                    followingTv.setText(following);
                    postsTv.setText(posts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProfileFragment", "Failed to load profile data: " + error.getMessage());
            }
        });
    }

    private void fetchUserSkills() {
        DatabaseReference skillsRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("ConnectaUserSkills")
                .child(searchedUserUid);

        skillsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Skill> skills = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Skill skill = ds.getValue(Skill.class);
                    if (skill != null) {
                        skills.add(skill);
                    }
                }
                // Update the UI with skills
                updateSkillsUI(skills);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to load skills: " + error.getMessage());
            }
        });


    }

    private void updateSkillsUI(List<Skill> skills) {
        // Clear any existing views
        skillsContainer.removeAllViews();

        // Display up to 4 skills
        int maxSkillsToShow = 4;
        for (int i = 0; i < Math.min(skills.size(), maxSkillsToShow); i++) {
            Skill skill = skills.get(i);

            // Create a TextView for the skill
            TextView skillTextView = new TextView(getContext());
            skillTextView.setText(skill.getTitle());
            skillTextView.setTextSize(12);
            skillTextView.setPadding(16, 8, 16, 8);
            skillTextView.setGravity(Gravity.CENTER);
            skillTextView.setBackgroundColor(getSkillBackgroundColor(skill.getSkillLevel())); // Set background color

            // Set layout parameters
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Height
                    1 // Weight
            );
            params.setMargins(8, 8, 8, 8); // Add margins
            skillTextView.setLayoutParams(params);

            // Add the TextView to the skills container
            skillsContainer.addView(skillTextView);
        }
    }

    private int getSkillBackgroundColor(String skillLevel) {
        switch (skillLevel) {
            case "Beginner":
                return Color.parseColor("#ADD8E6"); // Light Blue
            case "Intermediate":
                return Color.parseColor("#90EE90"); // Light Green
            case "Advanced":
                return Color.parseColor("#FFFFE0"); // Light Yellow
            case "Expert":
                return Color.parseColor("#FFA07A"); // Light Orange
            case "Master":
                return Color.parseColor("#FFC0CB"); // Light Pink
            default:
                return Color.parseColor("#ADD8E6"); // Default to Light Blue
        }
    }


}