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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.adapters.PostAdapter;
import com.example.connecta666620de.model.Post;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment implements PostAdapter.OnPostOptionsListener {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ProgressDialog pd;
    LinearLayout editSkillBtn, skillsContainer;

    ImageView avatarIv, settingBtn;
    TextView nameTv, usernameTv, bioTv, followersTv, followingTv, postsTv;
    LinearLayout followerArea, followingArea;
    RecyclerView postsRecyclerView;
    PostAdapter postAdapter;
    List<Post> postList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Connecta").child("ConnectaUsers");

        // Initialize views
        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading profile...");
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
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view);

        // Initialize post list and adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, this);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postsRecyclerView.setAdapter(postAdapter);
        Log.d("ProfileFragment", "RecyclerView initialized with LinearLayoutManager");

        // Fetch data
        pd.show();
        fetchUserProfileData();
        fetchUserSkills();
        fetchUserPosts();

        // Set click listeners
        editSkillBtn.setOnClickListener(v -> openEditSkillsFragment());

        settingBtn.setOnClickListener(v -> openSettingFragment());

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

                    nameTv.setText(firstName + " " + lastName);
                    usernameTv.setText("@" + username);
                    bioTv.setText(bio);
                    followersTv.setText(followers);
                    followingTv.setText(following);
                    postsTv.setText(posts);
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AndroidUtil.showToast(getContext(), "Failed to load profile data: " + error.getMessage());
                pd.dismiss();
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
                updateSkillsUI(skills);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AndroidUtil.showToast(getContext(), "Failed to load skills: " + error.getMessage());
            }
        });
    }

    private void fetchUserPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("UserPosts")
                .child(user.getUid());

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                Log.d("ProfileFragment", "Fetching posts for user: " + user.getUid());
                Log.d("ProfileFragment", "Snapshot exists: " + snapshot.exists() + ", children count: " + snapshot.getChildrenCount());
                int postCounter = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Post post = ds.getValue(Post.class);
                        if (post != null) {
                            Log.d("ProfileFragment", "Post " + postCounter + " - ID: " + post.getPostId() + ", Type: " + post.getType() +
                                    ", Caption/Question: " + (post.getCaption() != null ? post.getCaption() : post.getQuestion()) +
                                    ", Timestamp: " + post.getTimestamp());
                            postList.add(post);
                            postCounter++;
                        } else {
                            Log.w("ProfileFragment", "Null post at key: " + ds.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error deserializing post at key: " + ds.getKey() + ", Error: " + e.getMessage());
                    }
                }
                // Sort posts by timestamp in descending order (newest first)
                Collections.sort(postList, new Comparator<Post>() {
                    @Override
                    public int compare(Post p1, Post p2) {
                        return Long.compare(p2.getTimestamp(), p1.getTimestamp());
                    }
                });
                Log.d("ProfileFragment", "Posts sorted by timestamp, total: " + postList.size());
                postAdapter.notifyDataSetChanged();
                Log.d("ProfileFragment", "Adapter notified with " + postList.size() + " posts");
                if (postList.isEmpty()) {
                    AndroidUtil.showToast(getContext(), "No posts available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Post fetch error: " + error.getMessage());
                AndroidUtil.showToast(getContext(), "Failed to load posts: " + error.getMessage());
            }
        });
    }

    private void updateSkillsUI(List<Skill> skills) {
        skillsContainer.removeAllViews();
        int maxSkillsToShow = 4;
        for (int i = 0; i < Math.min(skills.size(), maxSkillsToShow); i++) {
            Skill skill = skills.get(i);
            TextView skillTextView = new TextView(getContext());
            skillTextView.setText(skill.getTitle());
            skillTextView.setTextSize(12);
            skillTextView.setPadding(16, 8, 16, 8);
            skillTextView.setGravity(Gravity.CENTER);
            skillTextView.setBackgroundColor(getSkillBackgroundColor(skill.getSkillLevel()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            params.setMargins(8, 8, 8, 8);
            skillTextView.setLayoutParams(params);
            skillsContainer.addView(skillTextView);
        }
    }

    private void openEditSkillsFragment() {
        EditSkillsFragment editSkillsFragment = new EditSkillsFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame_layout, editSkillsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openSettingFragment() {
        Fragment settingFragment = new SettingFragment();
        Bundle bundle = new Bundle();
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
                return Color.parseColor("#ADD8E6");
            case "Intermediate":
                return Color.parseColor("#90EE90");
            case "Advanced":
                return Color.parseColor("#FFFFE0");
            case "Expert":
                return Color.parseColor("#FFA07A");
            case "Master":
                return Color.parseColor("#FFC0CB");
            default:
                return Color.parseColor("#ADD8E6");
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

    @Override
    public void onEditPost(Post post) {
        EditPostFragment editPostFragment = new EditPostFragment();
        Bundle args = new Bundle();
        args.putSerializable("post", post);
        editPostFragment.setArguments(args);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, editPostFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeletePost(Post post) {
        // Delete post from Firebase
        Map<String, Object> updates = new HashMap<>();
        updates.put("Posts/" + post.getPostId(), null);
        updates.put("UserPosts/" + user.getUid() + "/" + post.getPostId(), null);
        updates.put("ConnectaUsers/" + user.getUid() + "/posts", ServerValue.increment(-1));

        FirebaseDatabase.getInstance().getReference("Connecta")
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    AndroidUtil.showToast(getContext(), "Post deleted successfully");
                    // Post list will auto-update via ValueEventListener
                })
                .addOnFailureListener(e -> {
                    AndroidUtil.showToast(getContext(), "Failed to delete post: " + e.getMessage());
                });

        // Delete image from Firebase Storage (for General posts)
        if ("General".equals(post.getType()) && post.getImageUrl() != null) {
            FirebaseStorage.getInstance().getReference("post_images")
                    .child(post.getPostId() + ".jpg")
                    .delete()
                    .addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "Failed to delete image: " + e.getMessage());
                    });
        }
    }
}