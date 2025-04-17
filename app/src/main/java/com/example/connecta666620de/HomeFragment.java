package com.example.connecta666620de;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.connecta666620de.adapters.PostAdapter;
import com.example.connecta666620de.model.Post;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment implements PostAdapter.OnPostOptionsListener {

    private RecyclerView feedRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private GestureDetector gestureDetector;
    private ImageButton chatBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        feedRecyclerView = view.findViewById(R.id.feed_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        chatBtn = view.findViewById(R.id.chat_Btn);

        // Initialize post list and adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, this);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedRecyclerView.setAdapter(postAdapter);

        // Set up chat button
        chatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatListActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        });

        // Set up pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchFeedPosts();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Set up swipe gesture detector
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                // Ensure the swipe is mostly horizontal
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX < 0) { // Right to left swipe
                        Intent intent = new Intent(getActivity(), ChatListActivity.class);
                        startActivity(intent);
                        requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                        return true;
                    } else { // Left to right swipe
                        fetchFeedPosts();
                        return true;
                    }
                }
                return false;
            }
        });

        // Attach gesture detector to RecyclerView
        feedRecyclerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // Allow RecyclerView to handle scrolling
        });

        // Fetch initial feed posts
        fetchFeedPosts();

        return view;
    }

    private void fetchFeedPosts() {
        FirebaseDatabase.getInstance().getReference("Connecta/Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        Log.d("HomeFragment", "Fetching feed posts, snapshot exists: " + snapshot.exists() + ", children count: " + snapshot.getChildrenCount());
                        int postCounter = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                Post post = ds.getValue(Post.class);
                                if (post != null) {
                                    Log.d("HomeFragment", "Post " + postCounter + " - ID: " + post.getPostId() + ", Type: " + post.getType() +
                                            ", Caption/Question: " + (post.getCaption() != null ? post.getCaption() : post.getQuestion()) +
                                            ", Timestamp: " + post.getTimestamp());
                                    postList.add(post);
                                    postCounter++;
                                } else {
                                    Log.w("HomeFragment", "Null post at key: " + ds.getKey());
                                }
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Error deserializing post at key: " + ds.getKey() + ", Error: " + e.getMessage());
                            }
                        }
                        // Sort posts by timestamp (newest first)
                        Collections.sort(postList, new Comparator<Post>() {
                            @Override
                            public int compare(Post p1, Post p2) {
                                return Long.compare(p2.getTimestamp(), p1.getTimestamp());
                            }
                        });
                        Log.d("HomeFragment", "Posts sorted by timestamp, total: " + postList.size());
                        postAdapter.notifyDataSetChanged();
                        Log.d("HomeFragment", "Adapter notified with " + postList.size() + " posts");
                        if (postList.isEmpty()) {
                            AndroidUtil.showToast(getContext(), "No posts available in the feed.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("HomeFragment", "Feed fetch error: " + error.getMessage());
                        AndroidUtil.showToast(getContext(), "Failed to load feed: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onEditPost(Post post) {
        // Only allow editing if the post belongs to the current user
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId.equals(post.getUserId())) {
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
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId.equals(post.getUserId())) {
            FirebaseDatabase.getInstance().getReference("Connecta")
                    .updateChildren(new java.util.HashMap<String, Object>() {{
                        put("Posts/" + post.getPostId(), null);
                        put("UserPosts/" + currentUserId + "/" + post.getPostId(), null);
                        put("ConnectaUsers/" + currentUserId + "/posts", com.google.firebase.database.ServerValue.increment(-1));
                    }})
                    .addOnSuccessListener(aVoid -> {
                        AndroidUtil.showToast(getContext(), "Post deleted successfully");
                    })
                    .addOnFailureListener(e -> {
                        AndroidUtil.showToast(getContext(), "Failed to delete post: " + e.getMessage());
                    });
            if ("General".equals(post.getType()) && post.getImageUrl() != null) {
                com.google.firebase.storage.FirebaseStorage.getInstance().getReference("post_images")
                        .child(post.getPostId() + ".jpg")
                        .delete()
                        .addOnFailureListener(e -> {
                            Log.e("HomeFragment", "Failed to delete image: " + e.getMessage());
                        });
            }
        }
    }
}