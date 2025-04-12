package com.example.connecta666620de;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.adapters.MessageAdapter;
import com.example.connecta666620de.model.Chat;
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

public class MessageActivity extends AppCompatActivity {

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

        // Initialize views
        avatarIv = findViewById(R.id.profileimage);
        userNameTv = findViewById(R.id.uname_profile);
        sendMsgbtn = findViewById(R.id.sendMessageButton);
        msgInputEt = findViewById(R.id.messageInput);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance(); // FIXED: Initialize FirebaseDatabase
        user = firebaseAuth.getCurrentUser();
        databaseReference = firebaseDatabase.getReference("Connecta").child("ConnectaUsers");



        Intent intent = getIntent();
        searchedUserUid = intent.getStringExtra("userID");

        if (searchedUserUid != null) {
            fetchUserProfileData();
        } else {
            Toast.makeText(getApplicationContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no user ID is provided
        }

        sendMsgbtn.setOnClickListener(v -> {
            String msg = msgInputEt.getText().toString();
            if(!msg.equals("")){
                sendMessage(user.getUid(),searchedUserUid,msg);
            } else {
                AndroidUtil.showToast(getApplicationContext(),"Message is empty");
            }
            msgInputEt.setText("");
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
                    profilePicUrl = ds.child("image").getValue(String.class); // Receiver image

                    if (username != null) {
                        userNameTv.setText(username);
                    }

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        loadProfileImage(profilePicUrl);
                    } else {
                        avatarIv.setImageResource(R.drawable.person_icon);
                    }
                }

                // Get current (sender) user image
                DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers").child(user.getUid());
                String finalProfilePicUrl = profilePicUrl;
                senderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUserImage[0] = snapshot.child("image").getValue(String.class);

                        // Now call readMessage with both images
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
                Glide.with(MessageActivity.this) // FIXED: Use correct context
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
            Glide.with(MessageActivity.this) // FIXED: Use correct context
                    .load(profilePicUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.person_icon)
                    .error(R.drawable.person_icon)
                    .into(avatarIv);
        }
    }

    public void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        long timestamp = System.currentTimeMillis(); // Get the current time in milliseconds

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp); // Store the timestamp

        String chatId = sender.compareTo(receiver) < 0 ? sender + "_" + receiver : receiver + "_" + sender;

        reference.child("Connecta").child("Chat").child(chatId).push().setValue(hashMap);
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
                        mChat.add(chat);

                        String dateKey = formatDate(chat.getTimestamp());
                        if (!groupedMessages.containsKey(dateKey)) {
                            groupedMessages.put(dateKey, new ArrayList<>());
                        }
                        groupedMessages.get(dateKey).add(chat);
                    }
                }

                messageAdapter = new MessageAdapter(MessageActivity.this, groupedMessages, senderImageUrl, receiverImageUrl);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }
}