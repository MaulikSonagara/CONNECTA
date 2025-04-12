package com.example.connecta666620de.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.MessageActivity;
import com.example.connecta666620de.R;
import com.example.connecta666620de.model.UserModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> implements Filterable {

    private Context context;
    private List<UserModel> userList;
    private List<UserModel> filteredList;

    public AdapterChatlist(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        UserModel user = filteredList.get(position);

        String imageUrl = user.getImage();

        // Get last message and timestamp
        String lastMessage = user.getLastMessage();
        long timestamp = user.getTimestamp();

        // Set user data
        holder.nameTv.setText(user.getUserName());
        holder.lastMsgTv.setText(lastMessage);
        holder.timeTv.setText(formatTime(timestamp));



        // Load profile image
        if (imageUrl.startsWith("gs://")) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context) // FIXED: Use correct context
                        .load(uri.toString())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.person_icon)
                        .error(R.drawable.person_icon)
                        .into(holder.profileIv);
            }).addOnFailureListener(e -> {
                Log.e("GlideDebug", "Failed to get download URL: " + e.getMessage());
                holder.profileIv.setImageResource(R.drawable.person_icon);
            });
        } else {
            Glide.with(context) // FIXED: Use correct context
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.person_icon)
                    .error(R.drawable.person_icon)
                    .into(holder.profileIv);
        }


        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            // Open chat activity with this user
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userID", user.getUid());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add this line
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<UserModel> filteredResults = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredResults.addAll(userList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (UserModel user : userList) {
                        if (user.getUserName().toLowerCase().contains(filterPattern) ||
                                user.getEmail().toLowerCase().contains(filterPattern)) {
                            filteredResults.add(user);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                filteredList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        ImageView profileIv;
        TextView nameTv, lastMsgTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMsgTv = itemView.findViewById(R.id.lastMessageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}