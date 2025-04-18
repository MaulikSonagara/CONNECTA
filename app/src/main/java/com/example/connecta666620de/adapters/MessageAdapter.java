package com.example.connecta666620de.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.MainActivity;
import com.example.connecta666620de.R;
import com.example.connecta666620de.ShowPostFragment;
import com.example.connecta666620de.model.Chat;
import com.example.connecta666620de.utills.AndroidUtil;
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
        void onMessageReact(String messageId, String emoji);
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
        List<Chat> allChats = new ArrayList<>();
        for (List<Chat> chats : groupedMessages.values()) {
            allChats.addAll(chats);
        }
        Collections.sort(allChats, Comparator.comparingLong(Chat::getTimestamp));

        Map<String, List<Chat>> sortedGroupedMessages = new HashMap<>();
        for (Chat chat : allChats) {
            String dateKey = formatDate(chat.getTimestamp());
            if (!sortedGroupedMessages.containsKey(dateKey)) {
                sortedGroupedMessages.put(dateKey, new ArrayList<>());
            }
            sortedGroupedMessages.get(dateKey).add(chat);
        }

        List<String> sortedDates = new ArrayList<>(sortedGroupedMessages.keySet());
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

            String messageText = chat.getMessage();
            // Handle shared post messages
            if (messageText != null && messageText.contains("Post by @")) {
                String[] parts = messageText.split(": ");
                if (parts.length == 2) {
                    String prefix = parts[0];
                    String postId = parts[1].trim();
                    SpannableString spannable = new SpannableString(messageText);
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            try {
                                Intent intent = new Intent(mContext, MainActivity.class);
                                intent.putExtra("action", "show_post");
                                intent.putExtra("post_id", postId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                mContext.startActivity(intent);
                                Log.d("MessageAdapter", "Starting MainActivity to show postId: " + postId);
                            } catch (Exception e) {
                                AndroidUtil.showToast(mContext, "Failed to open post");
                                Log.e("MessageAdapter", "Error starting MainActivity: " + e.getMessage());
                            }
                        }
                    };
                    int start = messageText.indexOf(postId);
                    int end = start + postId.length();
                    spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    msgHolder.messageText.setText(spannable);
                    msgHolder.messageText.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    msgHolder.messageText.setText(messageText);
                }
            } else {
                msgHolder.messageText.setText(messageText);
            }

            msgHolder.timeText.setText(formatTime(chat.getTimestamp()));

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            boolean isSender = chat.getSender().equals(currentUserId);

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

            if (msgHolder.reactBtn != null) {
                msgHolder.reactBtn.setOnClickListener(v -> showReactionPopup(msgHolder.reactBtn, chat.getMessageId()));
            }

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
                    tv.setPadding(8, 4, 8, 4);
                    tv.setBackgroundResource(R.drawable.reaction_background);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(4, 0, 4, 0);
                    tv.setLayoutParams(params);
                    msgHolder.reactionsLayout.addView(tv);
                }
            } else {
                msgHolder.reactionsLayout.removeAllViews();
            }
        }
    }

    private void showReactionPopup(View anchorView, String messageId) {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.reaction_popup, null);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        RecyclerView recyclerView = popupView.findViewById(R.id.reaction_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        ReactionAdapter reactionAdapter = new ReactionAdapter(mContext, emoji -> {
            if (reactListener != null && messageId != null) {
                reactListener.onMessageReact(messageId, emoji);
            }
            popupWindow.dismiss();
        });
        recyclerView.setAdapter(reactionAdapter);

        // Position the popup above or below the anchor view
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1] - popupView.getHeight());
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