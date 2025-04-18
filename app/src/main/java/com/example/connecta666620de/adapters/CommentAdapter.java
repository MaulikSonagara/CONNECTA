package com.example.connecta666620de.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.R;
import com.example.connecta666620de.model.Comment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.username.setText("@" + comment.getUsername());
        holder.commentText.setText(comment.getCommentText());
        holder.timestamp.setText(formatTimestamp(comment.getTimestamp()));
        String profileImageUrl = comment.getImageUrl();

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {

            if (profileImageUrl.startsWith("gs://")) {
                FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUrl)
                        .getDownloadUrl()
                        .addOnSuccessListener(uri -> Glide.with(context)
                                .load(uri.toString())
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.person_icon)
                                .error(R.drawable.person_icon)
                                .into(holder.imageView))
                        .addOnFailureListener(e -> holder.imageView.setImageResource(R.drawable.person_icon));
            } else {
                Glide.with(context)
                        .load(profileImageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.person_icon)
                        .error(R.drawable.person_icon)
                        .into(holder.imageView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, commentText, timestamp;
        ShapeableImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.comment_user_image);
            username = itemView.findViewById(R.id.comment_username);
            commentText = itemView.findViewById(R.id.comment_text);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
        }
    }
}