package com.example.connecta666620de.model;

import android.content.Context;
import android.util.Log;
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
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<UserModel> userList;

    // Constructor
    public AdapterUsers(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout (row_users.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get data
        UserModel user = userList.get(position);
        String fullName = user.getFirstName() + " " + user.getLastName();
        String userName = user.getUserName();
        String profilePicUrl = user.getImage(); // This is a gs:// URL

        // Set text data
        holder.fName.setText(fullName);
        holder.uName.setText(userName);

        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            // Check if the URL is a Firebase Storage gs:// URL
            if (profilePicUrl.startsWith("gs://")) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(profilePicUrl);

                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("GlideDebug", "Converted URL: " + uri.toString());
                    Glide.with(context)
                            .load(uri.toString()) // Load HTTP URL
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.person_icon)
                            .error(R.drawable.person_icon)
                            .into(holder.mAvatarIv);
                }).addOnFailureListener(e -> {
                    Log.e("GlideDebug", "Failed to get download URL: " + e.getMessage());
                    holder.mAvatarIv.setImageResource(R.drawable.person_icon);
                });
            } else {
                // If it's already an HTTP URL, load it directly
                Glide.with(context)
                        .load(profilePicUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.person_icon)
                        .error(R.drawable.person_icon)
                        .into(holder.mAvatarIv);
                Log.d("GlideDebug", "Loading image: " + profilePicUrl);
            }
        } else {
            holder.mAvatarIv.setImageResource(R.drawable.person_icon);
            Log.e("GlideDebug", "No image URL provided for user: " + userName);
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> AndroidUtil.showToast(context, userName));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Method to update the list dynamically
    public void updateList(List<UserModel> newList) {
        userList = newList; // Update the list
        notifyDataSetChanged(); // Notify adapter of data changes
    }

    // ViewHolder class
    class MyHolder extends RecyclerView.ViewHolder {
        ImageView mAvatarIv;
        TextView fName, uName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            fName = itemView.findViewById(R.id.fullNameTv);
            uName = itemView.findViewById(R.id.uNameTv);
        }
    }
}