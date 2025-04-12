package com.example.connecta666620de.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_RIGHT = 2;

    private Context mContext;
    private String senderImageUrl;
    private String receiverImageUrl;
    private List<Object> combinedList = new ArrayList<>();

    public MessageAdapter(Context mContext, Map<String, List<Chat>> groupedMessages, String senderImageUrl, String receiverImageUrl) {
        this.mContext = mContext;
        this.senderImageUrl = senderImageUrl;
        this.receiverImageUrl = receiverImageUrl;
        buildCombinedList(groupedMessages);
    }

    private void buildCombinedList(Map<String, List<Chat>> groupedMessages) {
        for (Map.Entry<String, List<Chat>> entry : groupedMessages.entrySet()) {
            List<Chat> chats = entry.getValue();
            if (!chats.isEmpty()) {
                long timestamp = chats.get(0).getTimestamp();
                combinedList.add(formatDate(timestamp));
                combinedList.addAll(chats);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = combinedList.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_DATE;
        } else {
            Chat chat = (Chat) item;
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            return chat.getSender().equals(currentUserId) ? VIEW_TYPE_RIGHT : VIEW_TYPE_LEFT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = combinedList.get(position);

        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).dateText.setText((String) item);
        } else if (holder instanceof MessageViewHolder) {
            Chat chat = (Chat) item;
            MessageViewHolder msgHolder = (MessageViewHolder) holder;

            msgHolder.messageText.setText(chat.getMessage());
            msgHolder.timeText.setText(formatTime(chat.getTimestamp()));

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String profileUrlToUse = chat.getSender().equals(currentUserId) ? senderImageUrl : receiverImageUrl;

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

        // Check Today
        if (msgCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && msgCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        // Check Yesterday
        today.add(Calendar.DAY_OF_YEAR, -1);
        if (msgCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && msgCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        // Else show full date
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        return sdf.format(messageDate);
    }

    // --- View Holders ---
    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView profileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.show_message);
            timeText = itemView.findViewById(R.id.msgTime);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
