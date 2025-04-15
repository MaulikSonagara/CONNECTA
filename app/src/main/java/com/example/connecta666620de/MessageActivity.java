package com.example.connecta666620de;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.adapters.MessageAdapter;
import com.example.connecta666620de.model.Chat;
import com.example.connecta666620de.model.Chatlist;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity implements MessageAdapter.OnMessageDeleteListener, MessageAdapter.OnMessageReactListener {

    ImageView avatarIv;
    TextView userNameTv;

    ImageButton sendMsgbtn;
    EditText msgInputEt;
    String searchedUserUid;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    String currentUserId;
    String chatId;
    TextView statusTextTv;
    private Handler typingHandler = new Handler();
    private static final long TYPING_TIMEOUT = 500;
    DatabaseReference seenMessageReference;
    ValueEventListener seenListener;
    LinearLayout openProfileFromChat;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);

        recyclerView = findViewById(R.id.messageList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        avatarIv = findViewById(R.id.profileimage);
        userNameTv = findViewById(R.id.uname_profile);
        sendMsgbtn = findViewById(R.id.sendMessageButton);
        msgInputEt = findViewById(R.id.messageInput);
        statusTextTv = findViewById(R.id.statusText);
        openProfileFromChat = findViewById(R.id.openProfileChatLayout);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = firebaseDatabase.getReference("Connecta").child("ConnectaUsers");

        Intent intent = getIntent();
        searchedUserUid = intent.getStringExtra("userID");

        seenMessage(searchedUserUid);

        if (searchedUserUid != null) {
            fetchUserProfileData();
        } else {
            Toast.makeText(getApplicationContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        sendMsgbtn.setOnClickListener(v -> {
            String msg = msgInputEt.getText().toString();
            if (!msg.equals("")) {
                sendMessage(user.getUid(), searchedUserUid, msg);
            } else {
                AndroidUtil.showToast(getApplicationContext(), "Message is empty");
            }
            msgInputEt.setText("");
        });

        msgInputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chatId = user.getUid().compareTo(searchedUserUid) < 0 ? user.getUid() + "_" + searchedUserUid : searchedUserUid + "_" + user.getUid();
                DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("Connecta").child("userChatStatus").child(chatId);
                statusRef.child(user.getUid()).setValue("Typing...");

                typingHandler.removeCallbacks(typingStoppedRunnable);
                typingHandler.postDelayed(typingStoppedRunnable, TYPING_TIMEOUT);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private Runnable typingStoppedRunnable = new Runnable() {
        @Override
        public void run() {
            chatId = user.getUid().compareTo(searchedUserUid) < 0 ? user.getUid() + "_" + searchedUserUid : searchedUserUid + "_" + user.getUid();
            DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("Connecta").child("userChatStatus").child(chatId);
            statusRef.child(user.getUid()).setValue("Online");
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        chatId = user.getUid().compareTo(searchedUserUid) < 0 ? user.getUid() + "_" + searchedUserUid : searchedUserUid + "_" + user.getUid();
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("Connecta").child("userChatStatus").child(chatId);
        statusRef.child(user.getUid()).setValue("offline");

        if (seenListener != null) {
            seenMessageReference.removeEventListener(seenListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatId = user.getUid().compareTo(searchedUserUid) < 0 ? user.getUid() + "_" + searchedUserUid : searchedUserUid + "_" + user.getUid();
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("Connecta").child("userChatStatus").child(chatId);
        statusRef.child(user.getUid()).setValue("online");

        statusRef.child(searchedUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status != null) {
                    statusTextTv.setVisibility(View.VISIBLE);

                    if ("Typing...".equals(status)) {
                        statusTextTv.setText("Typing...");
                    } else if ("online".equalsIgnoreCase(status)) {
                        statusTextTv.setText("Online");
                    } else {
                        statusTextTv.setVisibility(View.GONE);
                    }
                } else {
                    statusTextTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchUserProfileData() {
        Query query = databaseReference.orderByChild("uId").equalTo(searchedUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("Profile", "User not found in database");
                    Toast.makeText(MessageActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                final String[] currentUserImage = {null};
                String profilePicUrl = null;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String username = ds.child("userName").getValue(String.class);
                    profilePicUrl = ds.child("image").getValue(String.class);

                    if (username != null) {
                        userNameTv.setText(username);
                    }

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        loadProfileImage(profilePicUrl);
                    } else {
                        avatarIv.setImageResource(R.drawable.person_icon);
                    }
                }

                DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers").child(user.getUid());
                String finalProfilePicUrl = profilePicUrl;
                senderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUserImage[0] = snapshot.child("image").getValue(String.class);

                        readMessage(user.getUid(), searchedUserUid, currentUserImage[0], finalProfilePicUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        readMessage(user.getUid(), searchedUserUid, null, finalProfilePicUrl);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to load profile data: " + error.getMessage());
            }
        });
    }

    private void loadProfileImage(String profilePicUrl) {
        if (profilePicUrl.startsWith("gs://")) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(profilePicUrl);

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(MessageActivity.this)
                        .load(uri.toString())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.person_icon)
                        .error(R.drawable.person_icon)
                        .into(avatarIv);
            }).addOnFailureListener(e -> {
                Log.e("GlideDebug", "Failed to get download URL: " + e.getMessage());
                avatarIv.setImageResource(R.drawable.person_icon);
            });
        } else {
            Glide.with(MessageActivity.this)
                    .load(profilePicUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.person_icon)
                    .error(R.drawable.person_icon)
                    .into(avatarIv);
        }
    }

    private void seenMessage(String friendId){
        chatId = user.getUid().compareTo(friendId) < 0 ? user.getUid() + "_" + friendId : friendId + "_" + user.getUid();
        currentUserId = user.getUid();
        seenMessageReference = FirebaseDatabase.getInstance().getReference("Connecta").child("Chat").child(chatId);

        seenListener = seenMessageReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUserId) && chat.getSender().equals(friendId)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("timestamp", timestamp);

        String chatId = sender.compareTo(receiver) < 0 ? sender + "_" + receiver : receiver + "_" + sender;

        DatabaseReference newMessageRef = reference.child("Connecta").child("Chat").child(chatId).push();
        String messageId = newMessageRef.getKey();
        hashMap.put("messageId", messageId);

        newMessageRef.setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    updateChatlist(sender, receiver, message);
                });
    }

    private void readMessage(final String myid, final String userid, final String senderImageUrl, final String receiverImageUrl) {
        mChat = new ArrayList<>();

        String chatId = myid.compareTo(userid) < 0 ? myid + "_" + userid : userid + "_" + myid;

        databaseReference = FirebaseDatabase.getInstance().getReference("Connecta").child("Chat").child(chatId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                Map<String, List<Chat>> groupedMessages = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        chat.setMessageId(snapshot.getKey());
                        mChat.add(chat);

                        String dateKey = formatDate(chat.getTimestamp());
                        if (!groupedMessages.containsKey(dateKey)) {
                            groupedMessages.put(dateKey, new ArrayList<>());
                        }
                        groupedMessages.get(dateKey).add(chat);
                    }
                }

                messageAdapter = new MessageAdapter(MessageActivity.this, groupedMessages, senderImageUrl, receiverImageUrl, MessageActivity.this, MessageActivity.this);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateChatlist(String sender, String receiver, String message) {
        DatabaseReference chatlistRef = FirebaseDatabase.getInstance()
                .getReference("Connecta")
                .child("Chatlist");

        long timestamp = System.currentTimeMillis();

        Chatlist senderChatlist = new Chatlist(receiver, timestamp, message);
        chatlistRef.child(sender)
                .child(receiver)
                .setValue(senderChatlist);

        Chatlist receiverChatlist = new Chatlist(sender, timestamp, message);
        chatlistRef.child(receiver)
                .child(sender)
                .setValue(receiverChatlist);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    @Override
    public void onMessageDelete(String messageId) {
        String chatId = user.getUid().compareTo(searchedUserUid) < 0 ? user.getUid() + "_" + searchedUserUid : searchedUserUid + "_" + user.getUid();
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("Connecta").child("Chat").child(chatId).child(messageId);
        messageRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMessageReact(String messageId) {
        String chatId = user.getUid().compareTo(searchedUserUid) < 0 ? user.getUid() + "_" + searchedUserUid : searchedUserUid + "_" + user.getUid();
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("Connecta").child("Chat").child(chatId).child(messageId);

        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Chat chat = snapshot.getValue(Chat.class);
                if (chat != null) {
                    Map<String, String> reactions = chat.getReactions();
                    if (reactions == null) {
                        reactions = new HashMap<>();
                    }
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String emoji = reactions.get(currentUserId);
                    if (emoji == null || !emoji.equals("👍")) {
                        reactions.put(currentUserId, "👍");
                    } else {
                        reactions.remove(currentUserId);
                    }
                    chat.setReactions(reactions);
                    messageRef.setValue(chat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}