package com.example.connecta666620de.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.CommentFragment;
import com.example.connecta666620de.R;
import com.example.connecta666620de.SharePostFragment;
import com.example.connecta666620de.model.Post;
import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.NotificationUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_GENERAL = 0;
    private static final int TYPE_DOUBT = 1;
    private static final int TYPE_QUIZ = 2;

    private Context context;
    private List<Post> postList;
    private OnPostOptionsListener optionsListener;
    private SharedPreferences preferences;

    public interface OnPostOptionsListener {
        void onEditPost(Post post);
        void onDeletePost(Post post);
    }

    public PostAdapter(Context context, List<Post> postList, OnPostOptionsListener listener) {
        this.context = context;
        this.postList = postList;
        this.optionsListener = listener;
        this.preferences = context.getSharedPreferences("QuizSelections", Context.MODE_PRIVATE);
        Log.d("PostAdapter", "Initialized with " + postList.size() + " posts");
    }

    @Override
    public int getItemViewType(int position) {
        Post post = postList.get(position);
        String type = post.getType();
        Log.d("PostAdapter", "Position " + position + ": Type = " + type);
        if (type == null) {
            Log.w("PostAdapter", "Null type at position " + position + ", defaulting to General");
            return TYPE_GENERAL;
        }
        switch (type) {
            case "General":
                return TYPE_GENERAL;
            case "Doubt":
                return TYPE_DOUBT;
            case "Quiz":
                return TYPE_QUIZ;
            default:
                Log.w("PostAdapter", "Unknown type '" + type + "' at position " + position + ", defaulting to General");
                return TYPE_GENERAL;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        Log.d("PostAdapter", "Creating ViewHolder for viewType " + viewType);
        if (viewType == TYPE_GENERAL) {
            View view = inflater.inflate(R.layout.post_general_item, parent, false);
            Log.d("PostAdapter", "Inflated post_general_item");
            return new GeneralViewHolder(view);
        } else if (viewType == TYPE_DOUBT) {
            View view = inflater.inflate(R.layout.post_doubt_item, parent, false);
            Log.d("PostAdapter", "Inflated post_doubt_item");
            return new DoubtViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.post_quiz_item, parent, false);
            Log.d("PostAdapter", "Inflated post_quiz_item");
            return new QuizViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Post post = postList.get(position);
        String timestamp = formatTimestamp(post.getTimestamp());
        Log.d("PostAdapter", "Binding position " + position + ": ID = " + post.getPostId() + ", Type = " + post.getType() +
                ", Caption/Question = " + (post.getCaption() != null ? post.getCaption() : post.getQuestion()));

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        boolean isLiked = post.getLikedBy() != null && post.getLikedBy().contains(currentUserId);

        // Fetch and display user details
        FirebaseDatabase.getInstance().getReference("Connecta/ConnectaUsers/" + post.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String username = snapshot.child("userName").getValue(String.class);
                        String profilePicUrl = snapshot.child("image").getValue(String.class);
                        if (holder instanceof BaseViewHolder) {
                            BaseViewHolder baseHolder = (BaseViewHolder) holder;
                            baseHolder.username.setText(username != null ? "@" + username : "Unknown");
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                if (profilePicUrl.startsWith("gs://")) {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReferenceFromUrl(profilePicUrl);
                                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        Log.d("GlideDebug", "Converted URL: " + uri.toString());
                                        Glide.with(context)
                                                .load(uri.toString())
                                                .apply(RequestOptions.circleCropTransform())
                                                .placeholder(R.drawable.person_icon)
                                                .error(R.drawable.person_icon)
                                                .into(baseHolder.avatar);
                                    }).addOnFailureListener(e -> {
                                        Log.e("GlideDebug", "Failed to get download URL: " + e.getMessage());
                                        baseHolder.avatar.setImageResource(R.drawable.person_icon);
                                    });
                                } else {
                                    Glide.with(context)
                                            .load(profilePicUrl)
                                            .apply(RequestOptions.circleCropTransform())
                                            .placeholder(R.drawable.person_icon)
                                            .error(R.drawable.person_icon)
                                            .into(baseHolder.avatar);
                                    Log.d("GlideDebug", "Loading image: " + profilePicUrl);
                                }
                            } else {
                                baseHolder.avatar.setImageResource(R.drawable.person_icon);
                            }

                            // Bind share button with username
                            setupInteractionButtons(baseHolder, post, isLiked, username);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PostAdapter", "Failed to fetch user data for post " + post.getPostId() + ": " + error.getMessage());
                    }
                });

        // Bind skill tags
        if (holder instanceof BaseViewHolder) {
            BaseViewHolder baseHolder = (BaseViewHolder) holder;
            baseHolder.skillTagsChipGroup.removeAllViews();
            if (post.getSkillTags() != null && !post.getSkillTags().isEmpty()) {
                for (String tag : post.getSkillTags()) {
                    Chip chip = new Chip(context);
                    chip.setText(tag);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    baseHolder.skillTagsChipGroup.addView(chip);
                }
            }
        }

        if (holder instanceof GeneralViewHolder) {
            GeneralViewHolder generalHolder = (GeneralViewHolder) holder;
            generalHolder.caption.setText(post.getCaption() != null ? post.getCaption() : "");
            generalHolder.timestamp.setText(timestamp);
            if (post.getImageUrl() != null) {
                Log.d("PostAdapter", "Loading image: " + post.getImageUrl());
                Glide.with(context)
                        .load(post.getImageUrl())
                        .fitCenter()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(generalHolder.image);
            } else {
                Log.w("PostAdapter", "No image URL at position " + position);
                generalHolder.image.setImageDrawable(null);
            }
            // Show options menu only for own posts
            if (currentUserId.equals(post.getUserId())) {
                generalHolder.optionsMenu.setVisibility(View.VISIBLE);
                generalHolder.optionsMenu.setOnClickListener(v -> showOptionsMenu(v, post));
            } else {
                generalHolder.optionsMenu.setVisibility(View.GONE);
            }
        } else if (holder instanceof DoubtViewHolder) {
            DoubtViewHolder doubtHolder = (DoubtViewHolder) holder;
            doubtHolder.question.setText(post.getQuestion() != null ? post.getQuestion() : "");
            doubtHolder.timestamp.setText(timestamp);
            // Show options menu only for own posts
            if (currentUserId.equals(post.getUserId())) {
                doubtHolder.optionsMenu.setVisibility(View.VISIBLE);
                doubtHolder.optionsMenu.setOnClickListener(v -> showOptionsMenu(v, post));
            } else {
                doubtHolder.optionsMenu.setVisibility(View.GONE);
            }
        } else if (holder instanceof QuizViewHolder) {
            QuizViewHolder quizHolder = (QuizViewHolder) holder;
            quizHolder.question.setText(post.getQuestion() != null ? post.getQuestion() : "");
            List<String> options = post.getOptions();
            if (options != null && options.size() >= 4) {
                quizHolder.option1.setText("1. " + options.get(0));
                quizHolder.option2.setText("2. " + options.get(1));
                quizHolder.option3.setText("3. " + options.get(2));
                quizHolder.option4.setText("4. " + options.get(3));
                Log.d("PostAdapter", "Bound Quiz post with options: " + options);
            } else {
                Log.w("PostAdapter", "Invalid options at position " + position);
                quizHolder.option1.setText("");
                quizHolder.option2.setText("");
                quizHolder.option3.setText("");
                quizHolder.option4.setText("");
            }
            quizHolder.timestamp.setText(timestamp);
            // Show options menu only for own posts
            if (currentUserId.equals(post.getUserId())) {
                quizHolder.optionsMenu.setVisibility(View.VISIBLE);
                quizHolder.optionsMenu.setOnClickListener(v -> showOptionsMenu(v, post));
            } else {
                quizHolder.optionsMenu.setVisibility(View.GONE);
            }

            // Handle quiz interactions
            String postId = post.getPostId();
            String selectedOption = preferences.getString(postId, null);
            if (selectedOption != null) {
                updateOptionBorders(quizHolder, options, post.getCorrectAnswer(), selectedOption);
                disableOptions(quizHolder);
            } else {
                quizHolder.option1.setOnClickListener(v -> handleOptionClick(quizHolder, post, options, 0));
                quizHolder.option2.setOnClickListener(v -> handleOptionClick(quizHolder, post, options, 1));
                quizHolder.option3.setOnClickListener(v -> handleOptionClick(quizHolder, post, options, 2));
                quizHolder.option4.setOnClickListener(v -> handleOptionClick(quizHolder, post, options, 3));
            }
        }
        holder.itemView.setVisibility(View.VISIBLE);
        Log.d("PostAdapter", "Set visibility VISIBLE for position " + position);
    }

    private void setupInteractionButtons(BaseViewHolder holder, Post post, boolean isLiked, String username) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        holder.likeButton.setSelected(isLiked);
        holder.likeCount.setText(String.valueOf(post.getLikeCount()));
        holder.commentCount.setText(String.valueOf(post.getCommentCount()));

        // Like button click
        holder.likeButton.setOnClickListener(v -> {
            boolean newLikeState = !isLiked;
            holder.likeButton.setSelected(newLikeState);
            List<String> likedBy = post.getLikedBy() != null ? post.getLikedBy() : new ArrayList<>();

            if (newLikeState) {
                if (!likedBy.contains(currentUserId)) {
                    likedBy.add(currentUserId);
                    post.setLikeCount(post.getLikeCount() + 1);
                    // Send like notification if not liking own post
                    if (!currentUserId.equals(post.getUserId())) {
                        NotificationUtil.sendPostInteractionNotification(
                                currentUserId,
                                post.getUserId(),
                                post.getPostId(),
                                "like"
                        );
                    }
                }
            } else {
                likedBy.remove(currentUserId);
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            }
            post.setLikedBy(likedBy);
            holder.likeCount.setText(String.valueOf(post.getLikeCount()));

            // Update Firebase (both Posts and UserPosts)
            FirebaseDatabase.getInstance().getReference("Connecta/Posts/" + post.getPostId())
                    .child("likedBy").setValue(likedBy);
            FirebaseDatabase.getInstance().getReference("Connecta/Posts/" + post.getPostId())
                    .child("likeCount").setValue(post.getLikeCount());
            FirebaseDatabase.getInstance().getReference("Connecta/UserPosts/" + post.getUserId() + "/" + post.getPostId())
                    .child("likedBy").setValue(likedBy);
            FirebaseDatabase.getInstance().getReference("Connecta/UserPosts/" + post.getUserId() + "/" + post.getPostId())
                    .child("likeCount").setValue(post.getLikeCount());
        });

        // Comment button click
        holder.commentButton.setOnClickListener(v -> {
            CommentFragment commentFragment = new CommentFragment();
            Bundle args = new Bundle();
            args.putString("postId", post.getPostId());
            commentFragment.setArguments(args);
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_layout, commentFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Share button click
        holder.shareButton.setOnClickListener(v -> {
            SharePostFragment fragment = SharePostFragment.newInstance(post.getPostId(), post.getUserId(), username != null ? username : "Unknown");
            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "SharePostFragment");
        });
    }

    private void handleOptionClick(QuizViewHolder holder, Post post, List<String> options, int selectedIndex) {
        String selectedOption = "Option " + (selectedIndex + 1);
        String correctAnswer = post.getCorrectAnswer();
        String postId = post.getPostId();
        preferences.edit().putString(postId, selectedOption).apply();
        updateOptionBorders(holder, options, correctAnswer, selectedOption);
        disableOptions(holder);
    }

    private void updateOptionBorders(QuizViewHolder holder, List<String> options, String correctAnswer, String selectedOption) {
        TextView[] optionViews = {holder.option1, holder.option2, holder.option3, holder.option4};
        for (int i = 0; i < optionViews.length; i++) {
            String optionLabel = "Option " + (i + 1);
            if (optionLabel.equals(selectedOption)) {
                if (selectedOption.equals(correctAnswer)) {
                    optionViews[i].setBackgroundResource(R.drawable.option_border_correct);
                } else {
                    optionViews[i].setBackgroundResource(R.drawable.option_border_incorrect);
                }
            } else if (optionLabel.equals(correctAnswer)) {
                optionViews[i].setBackgroundResource(R.drawable.option_border_correct);
            } else {
                optionViews[i].setBackgroundResource(R.drawable.option_border_default);
            }
        }
    }

    private void disableOptions(QuizViewHolder holder) {
        holder.option1.setClickable(false);
        holder.option2.setClickable(false);
        holder.option3.setClickable(false);
        holder.option4.setClickable(false);
    }

    private void showOptionsMenu(View view, Post post) {
        // Only show menu for posts owned by the current user
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!currentUserId.equals(post.getUserId())) {
            return;
        }
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.post_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                optionsListener.onEditPost(post);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                optionsListener.onDeletePost(post);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        int count = postList.size();
        Log.d("PostAdapter", "getItemCount: " + count);
        return count;
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        ImageView optionsMenu, likeButton, commentButton, shareButton;
        ShapeableImageView avatar;
        TextView timestamp, likeCount, commentCount, username;
        ChipGroup skillTagsChipGroup;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            optionsMenu = itemView.findViewById(R.id.options_menu);
            likeButton = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            shareButton = itemView.findViewById(R.id.share_button);
            likeCount = itemView.findViewById(R.id.like_count);
            commentCount = itemView.findViewById(R.id.comment_count);
            timestamp = itemView.findViewById(R.id.post_timestamp);
            avatar = itemView.findViewById(R.id.post_user_avatar);
            username = itemView.findViewById(R.id.post_username);
            skillTagsChipGroup = itemView.findViewById(R.id.skill_tags_chip_group);
        }
    }

    static class GeneralViewHolder extends BaseViewHolder {
        ImageView image;
        TextView caption;

        public GeneralViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.post_image);
            caption = itemView.findViewById(R.id.post_caption);
        }
    }

    static class DoubtViewHolder extends BaseViewHolder {
        TextView question;

        public DoubtViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.post_question);
        }
    }

    static class QuizViewHolder extends BaseViewHolder {
        TextView question, option1, option2, option3, option4;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.post_question);
            option1 = itemView.findViewById(R.id.option1);
            option2 = itemView.findViewById(R.id.option2);
            option3 = itemView.findViewById(R.id.option3);
            option4 = itemView.findViewById(R.id.option4);
        }
    }
}