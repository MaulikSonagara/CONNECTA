package com.example.connecta666620de;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.connecta666620de.adapters.PostAdapter;
import com.example.connecta666620de.model.Post;
import com.example.connecta666620de.model.Skill;
import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.NotificationUtil;
import com.google.android.material.button.MaterialButton;
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
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowUserFragment extends Fragment implements PostAdapter.OnPostOptionsListener {

    String searchedUserUid;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    TextView nameTv, usernameTv, bioTv, followersTv, followingTv, postsTv;
    ImageView avatarIv;
    LinearLayout skillsContainer, followerArea, followingArea;
    private MaterialButton connectBtn, messageBtn;
    private boolean isFollowing = false;
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    public void onStart() {
        super.onStart();
        if (searchedUserUid != null && user != null) {
            checkFollowingStatus();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        followerArea = view.findViewById(R.id.followerAreaLayout);
        followingArea = view.findViewById(R.id.followingAreaLayout);
        connectBtn = view.findViewById(R.id.connect_btn);
        messageBtn = view.findViewById(R.id.message_btn);
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view);

        // Initialize post list and adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, this);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postsRecyclerView.setAdapter(postAdapter);

        messageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MessageActivity.class);
            intent.putExtra("userID", searchedUserUid);
            startActivity(intent);
        });

        // Get searched user ID
        if (getArguments() != null) {
            searchedUserUid = getArguments().getString("userId");
        }

        // Fetch user profile data and posts
        fetchUserProfileData();
        fetchUserSkills();
        fetchUserPosts();

        // Check if viewed profile is current user's profile
        if (user != null && user.getUid().equals(searchedUserUid)) {
            view.findViewById(R.id.connect_btn).setVisibility(View.GONE);
            view.findViewById(R.id.message_btn).setVisibility(View.GONE);
        } else {
            checkFollowingStatus();
            connectBtn.setOnClickListener(v -> {
                if (isFollowing) {
                    unfollowUser();
                } else {
                    followUser();
                }
            });
        }

        // Add click listeners for followers and following
        followerArea.setOnClickListener(v -> openConnectionsList(searchedUserUid, "followers"));
        followingArea.setOnClickListener(v -> openConnectionsList(searchedUserUid, "following"));

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
                    Long followersLong = ds.child("follower").getValue(Long.class);
                    Long followingLong = ds.child("following").getValue(Long.class);
                    Long postsLong = ds.child("posts").getValue(Long.class);
                    String followers = followersLong != null ? String.valueOf(followersLong) : "0";
                    String following = followingLong != null ? String.valueOf(followingLong) : "0";
                    String posts = postsLong != null ? String.valueOf(postsLong) : "0";

                    Log.d("ProfilePic", profilePicUrl);
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        if (profilePicUrl.startsWith("gs://")) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReferenceFromUrl(profilePicUrl);
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Log.d("GlideDebug", "Converted URL: " + uri.toString());
                                Glide.with(getContext())
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

                    usernameTv.setText("@" + username);
                    nameTv.setText(firstName + " " + lastName);
                    bioTv.setText(bio);
                    followersTv.setText(followers);
                    followingTv.setText(following);
                    postsTv.setText(posts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowUserFragment", "Failed to load profile data: " + error.getMessage());
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
                updateSkillsUI(skills);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowUserFragment", "Failed to load skills: " + error.getMessage());
            }
        });
    }

    private void fetchUserPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("UserPosts")
                .child(searchedUserUid);
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                Log.d("ShowUserFragment", "Fetching posts for user: " + searchedUserUid);
                Log.d("ShowUserFragment", "Snapshot exists: " + snapshot.exists() + ", children count: " + snapshot.getChildrenCount());
                int postCounter = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Post post = ds.getValue(Post.class);
                        if (post != null) {
                            Log.d("ShowUserFragment", "Post " + postCounter + " - ID: " + post.getPostId() + ", Type: " + post.getType() +
                                    ", Caption/Question: " + (post.getCaption() != null ? post.getCaption() : post.getQuestion()) +
                                    ", Timestamp: " + post.getTimestamp());
                            postList.add(post);
                            postCounter++;
                        } else {
                            Log.w("ShowUserFragment", "Null post at key: " + ds.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("ShowUserFragment", "Error deserializing post at key: " + ds.getKey() + ", Error: " + e.getMessage());
                    }
                }
                Collections.sort(postList, new Comparator<Post>() {
                    @Override
                    public int compare(Post p1, Post p2) {
                        return Long.compare(p2.getTimestamp(), p1.getTimestamp());
                    }
                });
                Log.d("ShowUserFragment", "Posts sorted by timestamp, total: " + postList.size());
                postAdapter.notifyDataSetChanged();
                Log.d("ShowUserFragment", "Adapter notified with " + postList.size() + " posts");
                if (postList.isEmpty()) {
                    AndroidUtil.showToast(getContext(), "No posts available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowUserFragment", "Post fetch error: " + error.getMessage());
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

    private void checkFollowingStatus() {
        if (user == null || searchedUserUid == null) return;
        DatabaseReference followingRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("UserConnections")
                .child(user.getUid())
                .child("following")
                .child(searchedUserUid);
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isFollowing = snapshot.exists();
                updateConnectButton();
                if (isFollowing) {
                    fetchUpdatedFollowerCount(searchedUserUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowUserFragment", "Error checking follow status: " + error.getMessage());
            }
        });
    }

    private void updateConnectButton() {
        if (isFollowing) {
            connectBtn.setText("Connected");
            connectBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.connected_color));
        } else {
            connectBtn.setText("Connect");
            connectBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.connect_color));
        }
    }

    private void followUser() {
        if (user == null || searchedUserUid == null) return;
        String currentUserId = user.getUid();
        String targetUserId = searchedUserUid;
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Connecta");
        Map<String, Object> updates = new HashMap<>();
        updates.put("UserConnections/" + currentUserId + "/following/" + targetUserId, true);
        updates.put("UserConnections/" + targetUserId + "/followers/" + currentUserId, true);
        updates.put("ConnectaUsers/" + currentUserId + "/following", ServerValue.increment(1));
        updates.put("ConnectaUsers/" + targetUserId + "/follower", ServerValue.increment(1));
        rootRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    isFollowing = true;
                    updateConnectButton();
                    fetchUpdatedFollowerCount(targetUserId);
                    NotificationUtil.sendFollowNotification(currentUserId, targetUserId);
                    NotificationUtil.sendYouFollowedNotification(currentUserId, targetUserId);
                })
                .addOnFailureListener(e -> {
                    Log.e("ShowUserFragment", "Follow failed: " + e.getMessage());
                });
    }

    private void unfollowUser() {
        if (user == null || searchedUserUid == null) return;
        String currentUserId = user.getUid();
        String targetUserId = searchedUserUid;
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Connecta");
        Map<String, Object> updates = new HashMap<>();
        updates.put("UserConnections/" + currentUserId + "/following/" + targetUserId, null);
        updates.put("UserConnections/" + targetUserId + "/followers/" + currentUserId, null);
        updates.put("ConnectaUsers/" + currentUserId + "/following", ServerValue.increment(-1));
        updates.put("ConnectaUsers/" + targetUserId + "/follower", ServerValue.increment(-1));
        rootRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    isFollowing = false;
                    updateConnectButton();
                    fetchUpdatedFollowerCount(targetUserId);
                })
                .addOnFailureListener(e -> {
                    Log.e("ShowUserFragment", "Unfollow failed: " + e.getMessage());
                });
    }

    private void fetchUpdatedFollowerCount(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("ConnectaUsers")
                .child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long followersCount = snapshot.child("follower").getValue(Long.class);
                Long followingCount = snapshot.child("following").getValue(Long.class);
                followersTv.setText(followersCount != null ? String.valueOf(followersCount) : "0");
                followingTv.setText(followingCount != null ? String.valueOf(followingCount) : "0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowUserFragment", "Error fetching updated counts: " + error.getMessage());
            }
        });
    }

    private void openConnectionsList(String userId, String connectionType) {
        UserConnectionsFragment fragment = new UserConnectionsFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("connectionType", connectionType);
        fragment.setArguments(args);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onEditPost(Post post) {
        // Only allow editing if the post belongs to the current user
        if (user != null && user.getUid().equals(post.getUserId())) {
            EditPostFragment editPostFragment = new EditPostFragment();
            Bundle args = new Bundle();
            args.putSerializable("post", post);
            editPostFragment.setArguments(args);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_layout, editPostFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDeletePost(Post post) {
        // Only allow deletion if the post belongs to the current user
        if (user != null && user.getUid().equals(post.getUserId())) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("Posts/" + post.getPostId(), null);
            updates.put("UserPosts/" + user.getUid() + "/" + post.getPostId(), null);
            updates.put("ConnectaUsers/" + user.getUid() + "/posts", ServerValue.increment(-1));
            FirebaseDatabase.getInstance().getReference("Connecta")
                    .updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        AndroidUtil.showToast(getContext(), "Post deleted successfully");
                    })
                    .addOnFailureListener(e -> {
                        AndroidUtil.showToast(getContext(), "Failed to delete post: " + e.getMessage());
                    });
            if ("General".equals(post.getType()) && post.getImageUrl() != null) {
                FirebaseStorage.getInstance().getReference("post_images")
                        .child(post.getPostId() + ".jpg")
                        .delete()
                        .addOnFailureListener(e -> {
                            Log.e("ShowUserFragment", "Failed to delete image: " + e.getMessage());
                        });
            }
        }
    }
}