package com.example.connecta666620de.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connecta666620de.R;
import com.example.connecta666620de.model.UserModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ShareUserAdapter extends RecyclerView.Adapter<ShareUserAdapter.ViewHolder> {

    private final Context context;
    private final List<UserModel> userList;
    private final List<UserModel> selectedUsers;
    private final Consumer<List<UserModel>> onSelectionChanged;

    public ShareUserAdapter(Context context, List<UserModel> userList, Consumer<List<UserModel>> onSelectionChanged) {
        this.context = context;
        this.userList = userList;
        this.selectedUsers = new ArrayList<>();
        this.onSelectionChanged = onSelectionChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_share_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.usernameTv.setText("@" + user.getUserName());
        loadProfileImage(holder.profileIv, user.getImage());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedUsers.contains(user));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUsers.add(user);
            } else {
                selectedUsers.remove(user);
            }
            onSelectionChanged.accept(selectedUsers);
        });

        holder.itemView.setOnClickListener(v -> holder.checkBox.setChecked(!holder.checkBox.isChecked()));
    }

    private void loadProfileImage(ImageView imageView, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.person_icon);
            return;
        }

        if (imageUrl.startsWith("gs://")) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context)
                        .load(uri)
                        .circleCrop()
                        .placeholder(R.drawable.person_icon)
                        .error(R.drawable.person_icon)
                        .into(imageView);
            }).addOnFailureListener(e -> {
                imageView.setImageResource(R.drawable.person_icon);
            });
        } else {
            Glide.with(context)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.person_icon)
                    .error(R.drawable.person_icon)
                    .into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<UserModel> getSelectedUsers() {
        return selectedUsers;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIv;
        TextView usernameTv;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profile_image);
            usernameTv = itemView.findViewById(R.id.username);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}