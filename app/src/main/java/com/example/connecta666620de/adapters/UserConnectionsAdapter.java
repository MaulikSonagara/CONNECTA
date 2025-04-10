package com.example.connecta666620de.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connecta666620de.R;
import com.example.connecta666620de.ShowUserFragment;
import com.example.connecta666620de.model.UserModel;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UserConnectionsAdapter extends RecyclerView.Adapter<UserConnectionsAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userList;

    public UserConnectionsAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_connection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.usernameTv.setText("@" + user.getUserName());
        holder.nameTv.setText(user.getFirstName() + " " + user.getLastName());

        // Load profile picture
        loadProfileImage(holder.profileIv, user.getImage());

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            // Open the user's profile
            ShowUserFragment fragment = new ShowUserFragment();
            Bundle args = new Bundle();
            args.putString("userId", user.getUid());
            fragment.setArguments(args);

            ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadProfileImage(ImageView imageView, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.person_icon);
            return;
        }

        if (imageUrl.startsWith("gs://")) {
            // Handle Firebase Storage reference
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context)
                        .load(uri)
                        .apply(AndroidUtil.getProfilePicOptions())
                        .into(imageView);
            }).addOnFailureListener(e -> {
                imageView.setImageResource(R.drawable.person_icon);
            });
        } else {
            // Handle regular URL
            Glide.with(context)
                    .load(imageUrl)
                    .apply(AndroidUtil.getProfilePicOptions())
                    .into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIv;
        TextView nameTv, usernameTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
        }
    }
}