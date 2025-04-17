package com.example.connecta666620de;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connecta666620de.adapters.CommentAdapter;
import com.example.connecta666620de.model.Comment;
import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.NotificationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CommentFragment extends Fragment {

    private String postId;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private EditText commentInput;
    private Button postCommentButton;
    private TextView emptyStateText;

    public CommentFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentInput = view.findViewById(R.id.comment_input);
        postCommentButton = view.findViewById(R.id.post_comment_button);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentAdapter);

        fetchComments();

        postCommentButton.setOnClickListener(v -> postComment());

        return view;
    }

    private void fetchComments() {
        FirebaseDatabase.getInstance().getReference("Connecta/Comments/" + postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Comment comment = ds.getValue(Comment.class);
                            if (comment != null) {
                                commentList.add(comment);
                            }
                        }
                        Collections.sort(commentList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                        commentAdapter.notifyDataSetChanged();
                        emptyStateText.setVisibility(commentList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        AndroidUtil.showToast(getContext(), "Failed to load comments: " + error.getMessage());
                    }
                });
    }

    private void postComment() {
        String commentText = commentInput.getText().toString().trim();
        if (commentText.isEmpty()) {
            AndroidUtil.showToast(getContext(), "Please enter a comment");
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Connecta/ConnectaUsers/" + currentUserId)
                .child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String username = snapshot.getValue(String.class);
                        String commentId = UUID.randomUUID().toString();
                        Comment comment = new Comment(commentId, postId, currentUserId, username, commentText, System.currentTimeMillis());

                        // Get post owner’s userId to send notification
                        FirebaseDatabase.getInstance().getReference("Connecta/Posts/" + postId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                                        String postOwnerId = postSnapshot.child("userId").getValue(String.class);
                                        FirebaseDatabase.getInstance().getReference("Connecta/Comments/" + postId + "/" + commentId)
                                                .setValue(comment)
                                                .addOnSuccessListener(aVoid -> {
                                                    commentInput.setText("");
                                                    // Increment comment count in both paths
                                                    FirebaseDatabase.getInstance().getReference("Connecta/Posts/" + postId)
                                                            .child("commentCount").setValue(ServerValue.increment(1));
                                                    FirebaseDatabase.getInstance().getReference("Connecta/UserPosts/" + postOwnerId + "/" + postId)
                                                            .child("commentCount").setValue(ServerValue.increment(1));
                                                    // Send comment notification if not commenting on own post
                                                    if (!currentUserId.equals(postOwnerId)) {
                                                        NotificationUtil.sendPostInteractionNotification(
                                                                currentUserId,
                                                                postOwnerId,
                                                                postId,
                                                                "comment"
                                                        );
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    AndroidUtil.showToast(getContext(), "Failed to post comment: " + e.getMessage());
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        AndroidUtil.showToast(getContext(), "Failed to fetch post data: " + error.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        AndroidUtil.showToast(getContext(), "Failed to fetch username: " + error.getMessage());
                    }
                });
    }
}