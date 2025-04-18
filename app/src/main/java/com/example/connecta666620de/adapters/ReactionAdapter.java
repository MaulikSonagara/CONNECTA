package com.example.connecta666620de.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.R;

import java.util.Arrays;
import java.util.List;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ReactionViewHolder> {

    private Context context;
    private List<String> emojis;
    private OnEmojiClickListener emojiClickListener;

    public interface OnEmojiClickListener {
        void onEmojiClick(String emoji);
    }

    public ReactionAdapter(Context context, OnEmojiClickListener emojiClickListener) {
        this.context = context;
        this.emojis = Arrays.asList("❤️", "😂", "😢", "👍", "😮");
        this.emojiClickListener = emojiClickListener;
    }

    @NonNull
    @Override
    public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reaction_item, parent, false);
        return new ReactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionViewHolder holder, int position) {
        String emoji = emojis.get(position);
        holder.emojiText.setText(emoji);
        holder.itemView.setOnClickListener(v -> {
            if (emojiClickListener != null) {
                emojiClickListener.onEmojiClick(emoji);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emojis.size();
    }

    static class ReactionViewHolder extends RecyclerView.ViewHolder {
        TextView emojiText;

        ReactionViewHolder(View itemView) {
            super(itemView);
            emojiText = itemView.findViewById(R.id.emoji_text);
        }
    }
}