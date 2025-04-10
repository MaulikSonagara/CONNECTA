package com.example.connecta666620de;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.connecta666620de.model.Skill;
import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.FireBaseUtill;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ProgressDialog pd;
    LinearLayout editSkillBtn, skillsContainer;

    ImageView avatarIv, settingBtn;
    TextView nameTv, usernameTv, bioTv, followersTv, followingTv, postsTv;
    LinearLayout followerArea, followingArea;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Connecta").child("ConnectaUsers");

        // Initialize views
        pd = new ProgressDialog(getContext());
        avatarIv = view.findViewById(R.id.profileimage_profile);
        nameTv = view.findViewById(R.id.name_profile);
        usernameTv = view.findViewById(R.id.username_profile);
        bioTv = view.findViewById(R.id.bio_profile);
        followersTv = view.findViewById(R.id.follower_data_profile);
        followingTv = view.findViewById(R.id.following_data_profile);
        postsTv = view.findViewById(R.id.post_data_profile);
        settingBtn = view.findViewById(R.id.settingBtn);
        editSkillBtn = view.findViewById(R.id.edit_skills_button);
        skillsContainer = view.findViewById(R.id.skills_container);

        followerArea = view.findViewById(R.id.followerAreaLayout);
        followingArea = view.findViewById(R.id.followingAreaLayout);

        // Fetch user profile data
        fetchUserProfileData();

        // Fetch user skills
        fetchUserSkills();

        // Set click listeners
        editSkillBtn.setOnClickListener(v -> openEditSkillsFragment());

        settingBtn.setOnClickListener(v -> openSettingFragment());

        // Add click listeners for followers and following
        followerArea.setOnClickListener(v -> openConnectionsList("followers"));
        followingArea.setOnClickListener(v -> openConnectionsList("following"));

        // Load profile picture
        FireBaseUtill.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri uri = task.getResult();
                AndroidUtil.setProfilePic(getContext(), uri, avatarIv);
            }
        });

        return view;
    }

    private void fetchUserProfileData() {
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String firstName = "" + ds.child("firstName").getValue();
                    String lastName = "" + ds.child("lastName").getValue();
                    String bio = "" + ds.child("bio").getValue();
                    String username = "" + ds.child("userName").getValue();
                    String followers = "" + ds.child("follower").getValue();
                    String following = "" + ds.child("following").getValue();
                    String posts = "" + ds.child("posts").getValue();

                    // Set profile data
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
                .child(user.getUid());

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

    private void openEditSkillsFragment() {
        // Create an instance of the EditSkillsFragment
        EditSkillsFragment editSkillsFragment = new EditSkillsFragment();

        // Replace the current fragment with the EditSkillsFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame_layout, editSkillsFragment); // Replace `main_frame_layout` with your container ID
        transaction.addToBackStack(null); // Add to back stack so the user can navigate back
        transaction.commit();
    }

    private void openSettingFragment() {
        Fragment settingFragment = new SettingFragment();
        Bundle bundle = new Bundle();

        // Pass profile data
        bundle.putString("name", nameTv.getText().toString());

        settingFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, settingFragment)
                .addToBackStack(null)
                .commit();
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

    private void openConnectionsList(String connectionType) {
        UserConnectionsFragment fragment = new UserConnectionsFragment();
        Bundle args = new Bundle();
        args.putString("userId", user.getUid());
        args.putString("connectionType", connectionType);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}