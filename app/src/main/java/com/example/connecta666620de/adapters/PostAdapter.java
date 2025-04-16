package com.example.connecta666620de.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connecta666620de.R;
import com.example.connecta666620de.model.Post;

import java.text.SimpleDateFormat;
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

    public interface OnPostOptionsListener {
        void onEditPost(Post post);
        void onDeletePost(Post post);
    }

    public PostAdapter(Context context, List<Post> postList, OnPostOptionsListener listener) {
        this.context = context;
        this.postList = postList;
        this.optionsListener = listener;
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
            generalHolder.optionsMenu.setOnClickListener(v -> showOptionsMenu(v, post));
        } else if (holder instanceof DoubtViewHolder) {
            DoubtViewHolder doubtHolder = (DoubtViewHolder) holder;
            doubtHolder.question.setText(post.getQuestion() != null ? post.getQuestion() : "");
            doubtHolder.timestamp.setText(timestamp);
            doubtHolder.optionsMenu.setOnClickListener(v -> showOptionsMenu(v, post));
            Log.d("PostAdapter", "Bound Doubt post at position " + position);
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
            quizHolder.optionsMenu.setOnClickListener(v -> showOptionsMenu(v, post));
        }
        // Ensure view is visible
        holder.itemView.setVisibility(View.VISIBLE);
        Log.d("PostAdapter", "Set visibility VISIBLE for position " + position);
    }

    private void showOptionsMenu(View view, Post post) {
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

    static class GeneralViewHolder extends RecyclerView.ViewHolder {
        ImageView image, optionsMenu;
        TextView caption, timestamp;

        public GeneralViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.post_image);
            caption = itemView.findViewById(R.id.post_caption);
            timestamp = itemView.findViewById(R.id.post_timestamp);
            optionsMenu = itemView.findViewById(R.id.options_menu);
        }
    }

    static class DoubtViewHolder extends RecyclerView.ViewHolder {
        TextView question, timestamp;
        ImageView optionsMenu;

        public DoubtViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.post_question);
            timestamp = itemView.findViewById(R.id.post_timestamp);
            optionsMenu = itemView.findViewById(R.id.options_menu);
        }
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView question, option1, option2, option3, option4, timestamp;
        ImageView optionsMenu;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.post_question);
            option1 = itemView.findViewById(R.id.option1);
            option2 = itemView.findViewById(R.id.option2);
            option3 = itemView.findViewById(R.id.option3);
            option4 = itemView.findViewById(R.id.option4);
            timestamp = itemView.findViewById(R.id.post_timestamp);
            optionsMenu = itemView.findViewById(R.id.options_menu);
        }
    }
}