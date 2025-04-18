package com.example.connecta666620de;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.adapters.PostAdapter;
import com.example.connecta666620de.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowPostFragment extends Fragment implements PostAdapter.OnPostOptionsListener {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private String postId;

    public static ShowPostFragment newInstance(String postId) {
        ShowPostFragment fragment = new ShowPostFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_post, container, false);

        recyclerView = view.findViewById(R.id.post_recycler_view);
        postList = new ArrayList<>();
        adapter = new PostAdapter(getContext(), postList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fetchPost();

        return view;
    }

    private void fetchPost() {
        FirebaseDatabase.getInstance()
                .getReference("Connecta/Posts")
                .child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        if (post != null) {
                            post.setPostId(postId);
                            postList.add(post);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void onEditPost(Post post) {
        // Handle edit if needed (likely disabled for non-owners in PostAdapter)
    }

    @Override
    public void onDeletePost(Post post) {
        // Handle delete if needed (likely disabled for non-owners in PostAdapter)
    }
}