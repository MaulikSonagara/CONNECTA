package com.example.connecta666620de.model;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.R;
import com.example.connecta666620de.model.UserModel;
import com.example.connecta666620de.model.Skill;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
        // Inflate layout (row_user.xml for skills display)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        UserModel user = userList.get(position);

        // Set user data
        holder.uNameTv.setText(user.getUserName());
        holder.fullNameTv.setText(user.getFirstName() + " " + user.getLastName());

        // Load profile picture
        String profilePicUrl = user.getImage(); // This is a gs:// URL
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
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
            Log.e("GlideDebug", "No image URL provided for user: " + user.getUserName());
        }

        // Fetch and display skills
        fetchUserSkills(user.getUid(), holder.skillsContainer);

        // Handle item click
        holder.itemView.setOnClickListener(v -> AndroidUtil.showToast(context, user.getUserName()));
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
        TextView uNameTv, fullNameTv;
        LinearLayout skillsContainer;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mAvatarIv = itemView.findViewById(R.id.avatarIv); // Ensure this ID exists in row_user.xml
            uNameTv = itemView.findViewById(R.id.uNameTv);
            fullNameTv = itemView.findViewById(R.id.fullNameTv);
            skillsContainer = itemView.findViewById(R.id.skills_container);
        }
    }

    // Fetch user skills from Firebase
    private void fetchUserSkills(String userId, LinearLayout skillsContainer) {
        DatabaseReference skillsRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("ConnectaUserSkills")
                .child(userId);

        skillsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                skillsContainer.removeAllViews(); // Clear existing skills

                int maxSkillsToShow = 4;
                int skillCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (skillCount >= maxSkillsToShow) break; // Show only 4 skills

                    Skill skill = ds.getValue(Skill.class);
                    if (skill != null) {
                        // Create a TextView for the skill
                        TextView skillTextView = new TextView(context);
                        skillTextView.setText(skill.getTitle());
                        skillTextView.setTextSize(12);
                        skillTextView.setPadding(8, 4, 8, 4);
                        skillTextView.setGravity(Gravity.CENTER);
                        skillTextView.setTextColor(Color.BLACK);
                        skillTextView.setBackgroundColor(getSkillBackgroundColor(skill.getSkillLevel())); // Set background color

                        // Set layout parameters
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(4, 4, 4, 4); // Add margins
                        skillTextView.setLayoutParams(params);

                        // Add the TextView to the skills container
                        skillsContainer.addView(skillTextView);

                        skillCount++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdapterUsers", "Failed to load skills: " + error.getMessage());
            }
        });
    }

    // Helper method to get background color based on skill level
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
