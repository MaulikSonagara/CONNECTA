package com.example.connecta666620de;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connecta666620de.model.UserModel;
import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.FireBaseUtill;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginProfileActivity extends AppCompatActivity {

    EditText firstName, lastName, userName, emailEtext;
    Button nextBtn;
    ImageView profilePic;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    String image, bio;
    long posts = 0, follower = 0, following = 0; // Ensuring default values

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_profile);

        firstName = findViewById(R.id.first_name_Etxt);
        lastName = findViewById(R.id.last_name_Etxt);
        userName = findViewById(R.id.username_Etxt);
        nextBtn = findViewById(R.id.next_btn);
        emailEtext = findViewById(R.id.Email_Etxt);
        profilePic = findViewById(R.id.avatarIv);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if (user != null) {
            emailEtext.setText(user.getEmail());
            getUserDetails();
        }

        // Getting Profile Pic
        FireBaseUtill.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri uri = task.getResult();
                AndroidUtil.setProfilePic(getApplicationContext(), uri, profilePic);
            }
        });

        nextBtn.setOnClickListener(v -> updateProfile());
    }

    private void getUserDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers");
        Query query = reference.orderByChild("email").equalTo(user.getEmail());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    firstName.setText(ds.child("firstName").getValue(String.class));
                    lastName.setText(ds.child("lastName").getValue(String.class));
                    userName.setText(ds.child("userName").getValue(String.class));
                    image = ds.child("image").getValue(String.class);
                    bio = ds.child("bio").getValue(String.class);

                    posts = ds.child("posts").getValue(Long.class) != null ? ds.child("posts").getValue(Long.class) : 0;
                    follower = ds.child("follower").getValue(Long.class) != null ? ds.child("follower").getValue(Long.class) : 0;
                    following = ds.child("following").getValue(Long.class) != null ? ds.child("following").getValue(Long.class) : 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AndroidUtil.showToast(getApplicationContext(), "Failed to fetch user details");
            }
        });
    }

    private void updateProfile() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String uName = userName.getText().toString().trim();

        if (validateInput(fName, lName, uName)) {
            checkIfUsernameExists(fName, lName, uName);
        }
    }

    private boolean validateInput(String fName, String lName, String uName) {
        if (fName.isEmpty()) {
            firstName.setError("Enter First Name");
            firstName.requestFocus();
            return false;
        } else if (lName.isEmpty()) {
            lastName.setError("Enter Last Name");
            lastName.requestFocus();
            return false;
        } else if (uName.isEmpty()) {
            userName.setError("Enter User Name");
            userName.requestFocus();
            return false;
        } else if (uName.length() < 4) {
            userName.setError("User Name must be at least 4 characters");
            userName.requestFocus();
            return false;
        } else if (!Character.isLetter(uName.charAt(0))) {
            userName.setError("Username must start with a letter");
            userName.requestFocus();
            return false;
        } else if (!uName.matches("^[a-zA-Z][a-zA-Z0-9._]*$")) {
            userName.setError("Only dot and underscore are allowed ( . ), ( _ )");
            userName.requestFocus();
            return false;
        }
        return true;
    }

    private void checkIfUsernameExists(String fName, String lName, String uName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers");
        Query query = reference.orderByChild("userName").equalTo(uName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isUsernameTaken = false;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String existingUserId = ds.getKey();
                    if (!existingUserId.equals(FireBaseUtill.currentUserId())) {
                        isUsernameTaken = true;
                        break;
                    }
                }

                if (isUsernameTaken) {
                    userName.setError("Username is already taken");
                    userName.requestFocus();
                } else {
                    saveProfileData(fName, lName, uName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AndroidUtil.showToast(getApplicationContext(), "Database error occurred");
            }
        });
    }

    private void saveProfileData(String fName, String lName, String uName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uId", user.getUid());
        hashMap.put("firstName", fName);
        hashMap.put("lastName", lName);
        hashMap.put("userName", uName);
        hashMap.put("bio", bio != null ? bio : "");
        hashMap.put("image", image != null ? image : "");
        hashMap.put("email", user.getEmail());
        hashMap.put("posts", posts);
        hashMap.put("follower", follower);
        hashMap.put("following", following);

        reference.child(FireBaseUtill.currentUserId()).setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AndroidUtil.showToast(getApplicationContext(), "Successfully logged in");
                startActivity(new Intent(LoginProfileActivity.this, MainActivity.class));
                finish();
            } else {
                AndroidUtil.showToast(getApplicationContext(), "Failed to store data");
            }
        });
    }
}
