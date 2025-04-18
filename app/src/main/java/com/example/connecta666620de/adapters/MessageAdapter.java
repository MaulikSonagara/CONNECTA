package com.example.connecta666620de.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.R;
import com.example.connecta666620de.model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_RIGHT = 2;

    private Context mContext;
    private String senderImageUrl;
    private String receiverImageUrl;
    private List<Object> combinedList = new ArrayList<>();
    private OnMessageDeleteListener deleteListener;
    private OnMessageReactListener reactListener;

    public interface OnMessageDeleteListener {
        void onMessageDelete(String messageId);
    }

    public interface OnMessageReactListener {
        void onMessageReact(String messageId);
    }

    public MessageAdapter(Context mContext, Map<String, List<Chat>> groupedMessages, String senderImageUrl, String receiverImageUrl, OnMessageDeleteListener deleteListener, OnMessageReactListener reactListener) {
        this.mContext = mContext;
        this.senderImageUrl = senderImageUrl;
        this.receiverImageUrl = receiverImageUrl;
        this.deleteListener = deleteListener;
        this.reactListener = reactListener;
        buildCombinedList(groupedMessages);
    }

    private void buildCombinedList(Map<String, List<Chat>> groupedMessages) {
        combinedList.clear();
        // Create a flat list of all chats to sort by timestamp
        List<Chat> allChats = new ArrayList<>();
        for (List<Chat> chats : groupedMessages.values()) {
            allChats.addAll(chats);
        }
        // Sort chats by timestamp (ascending)
        Collections.sort(allChats, Comparator.comparingLong(Chat::getTimestamp));

        // Group sorted chats by date
        Map<String, List<Chat>> sortedGroupedMessages = new HashMap<>();
        for (Chat chat : allChats) {
            String dateKey = formatDate(chat.getTimestamp());
            if (!sortedGroupedMessages.containsKey(dateKey)) {
                sortedGroupedMessages.put(dateKey, new ArrayList<>());
            }
            sortedGroupedMessages.get(dateKey).add(chat);
        }

        // Build combinedList in chronological order
        List<String> sortedDates = new ArrayList<>(sortedGroupedMessages.keySet());
        // Sort dates to ensure chronological order
        Collections.sort(sortedDates, (d1, d2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
                Date date1 = sdf.parse(d1.equals("Today") ? sdf.format(new Date()) : d1.equals("Yesterday") ? sdf.format(getYesterday()) : d1);
                Date date2 = sdf.parse(d2.equals("Today") ? sdf.format(new Date()) : d2.equals("Yesterday") ? sdf.format(getYesterday()) : d2);
                return date1.compareTo(date2);
            } catch (Exception e) {
                return 0;
            }
        });

        for (String date : sortedDates) {
            List<Chat> chats = sortedGroupedMessages.get(date);
            if (chats != null && !chats.isEmpty()) {
                combinedList.add(date);
                combinedList.addAll(chats);
            }
        }
    }

    private Date getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return cal.getTime();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = combinedList.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_DATE;
        } else {
            Chat chat = (Chat) item;
            String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            return chat.getSender().equals(currentUserId) ? VIEW_TYPE_RIGHT : VIEW_TYPE_LEFT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == VIEW_TYPE_DATE) {
            View view = inflater.inflate(R.layout.item_date, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == VIEW_TYPE_LEFT) {
            View view = inflater.inflate(R.layout.chat_item_left, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.chat_item_right, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = combinedList.get(position);

        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).dateText.setText((String) item);
        } else if (holder instanceof MessageViewHolder) {
            Chat chat = (Chat) item;
            MessageViewHolder msgHolder = (MessageViewHolder) holder;

            msgHolder.messageText.setText(chat.getMessage());
            msgHolder.timeText.setText(formatTime(chat.getTimestamp()));

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            boolean isSender = chat.getSender().equals(currentUserId);

            // Show "Seen/Delivered" only for the sender's last message
            if (isSender && position == combinedList.size() - 1 && combinedList.get(position) instanceof Chat) {
                msgHolder.seenTv.setVisibility(View.VISIBLE);
                msgHolder.seenTv.setText(chat.isIsseen() ? " • Seen" : " • Delivered");
            } else {
                msgHolder.seenTv.setVisibility(View.GONE);
            }

            String profileUrlToUse = isSender ? senderImageUrl : receiverImageUrl;

            if (profileUrlToUse == null || profileUrlToUse.equals("default")) {
                msgHolder.profileImage.setImageResource(R.drawable.person_icon);
            } else {
                if (profileUrlToUse.startsWith("gs://")) {
                    FirebaseStorage.getInstance().getReferenceFromUrl(profileUrlToUse)
                            .getDownloadUrl()
                            .addOnSuccessListener(uri -> Glide.with(mContext)
                                    .load(uri.toString())
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(R.drawable.person_icon)
                                    .error(R.drawable.person_icon)
                                    .into(msgHolder.profileImage))
                            .addOnFailureListener(e -> msgHolder.profileImage.setImageResource(R.drawable.person_icon));
                } else {
                    Glide.with(mContext)
                            .load(profileUrlToUse)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.person_icon)
                            .error(R.drawable.person_icon)
                            .into(msgHolder.profileImage);
                }
            }

            // Handle delete button
            if (msgHolder.deleteBtn != null && isSender) {
                msgHolder.deleteBtn.setVisibility(View.VISIBLE);
                msgHolder.deleteBtn.setOnClickListener(v -> {
                    if (deleteListener != null && chat.getMessageId() != null) {
                        deleteListener.onMessageDelete(chat.getMessageId());
                    }
                });
            } else if (msgHolder.deleteBtn != null) {
                msgHolder.deleteBtn.setVisibility(View.GONE);
            }

            // Handle reaction button
            if (msgHolder.reactBtn != null) {
                msgHolder.reactBtn.setOnClickListener(v -> {
                    if (reactListener != null && chat.getMessageId() != null) {
                        reactListener.onMessageReact(chat.getMessageId());
                    }
                });
            }

            // Display reactions
            if (chat.getReactions() != null && !chat.getReactions().isEmpty()) {
                Map<String, Integer> reactionCounts = new HashMap<>();
                for (String emoji : chat.getReactions().values()) {
                    reactionCounts.put(emoji, reactionCounts.getOrDefault(emoji, 0) + 1);
                }
                msgHolder.reactionsLayout.removeAllViews();
                for (Map.Entry<String, Integer> entry : reactionCounts.entrySet()) {
                    String emoji = entry.getKey();
                    int count = entry.getValue();
                    TextView tv = new TextView(mContext);
                    tv.setText(emoji + (count > 1 ? " " + count : ""));
                    tv.setTextSize(14);
                    msgHolder.reactionsLayout.addView(tv);
                }
            } else {
                msgHolder.reactionsLayout.removeAllViews();
            }
        }
    }

    @Override
    public int getItemCount() {
        return combinedList.size();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatDate(long timestamp) {
        Date messageDate = new Date(timestamp);
        Calendar msgCal = Calendar.getInstance();
        msgCal.setTime(messageDate);

        Calendar today = Calendar.getInstance();

        if (msgCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && msgCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        today.add(Calendar.DAY_OF_YEAR, -1);
        if (msgCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && msgCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        return sdf.format(messageDate);
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, seenTv;
        ImageView profileImage;
        ImageButton deleteBtn, reactBtn;
        LinearLayout reactionsLayout;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.show_message);
            timeText = itemView.findViewById(R.id.msgTime);
            profileImage = itemView.findViewById(R.id.profile_image);
            seenTv = itemView.findViewById(R.id.seen_status);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            reactBtn = itemView.findViewById(R.id.reactBtn);
            reactionsLayout = itemView.findViewById(R.id.reactionsLayout);
        }
    }
}