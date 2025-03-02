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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    UserModel userModel;
    String emailAddress;
    FirebaseAuth firebaseAuth;
    String image;
    FirebaseUser user;
    String bio;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_profile);

        firstName= findViewById(R.id.first_name_Etxt);
        lastName= findViewById(R.id.last_name_Etxt);
        userName= findViewById(R.id.username_Etxt);
        nextBtn = findViewById(R.id.next_btn);
        emailEtext = findViewById(R.id.Email_Etxt);
        profilePic = findViewById(R.id.avatarIv);

//        emailAddress = getIntent().getStringExtra("emailAddress");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        emailEtext.setText(email);

        // Getting Profile Pic
        FireBaseUtill.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri uri = task.getResult();
                AndroidUtil.setProfilePic(getApplicationContext(), uri, profilePic);
            }
        });
        getUserDetails();

        nextBtn.setOnClickListener(v -> {
            String fName = firstName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String uName = userName.getText().toString().trim();

            if (fName.isEmpty()) {
                firstName.setError("Enter First Name");
                firstName.requestFocus();
            } else if (lName.isEmpty()) {
                lastName.setError("Enter Last Name");
                lastName.requestFocus();
            } else if (uName.isEmpty()) {
                userName.setError("Enter User Name");
                userName.requestFocus();
            } else if (uName.length() < 4) {
                userName.setError("User Name must be at least 4 characters");
                userName.requestFocus();
            } else if (!Character.isLetter(uName.charAt(0))) {
                userName.setError("Username must start with a letter");
                userName.requestFocus();
            } else if (!uName.matches("^[a-zA-Z][a-zA-Z0-9._]*$")) {
                userName.setError("Only dot and underscore are allowed ( . ), ( _ )");
                userName.requestFocus();
            } else {
                // 🔹 Check if username is already taken
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUsers");
                Query query = reference.orderByChild("userName").equalTo(uName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isUsernameTaken = false;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String existingUserId = ds.getKey();  // Get the user ID of the existing username
                            if (!existingUserId.equals(FireBaseUtill.currentUserId())) {
                                isUsernameTaken = true;
                                break;
                            }
                        }

                        if (isUsernameTaken) {
                            userName.setError("Username is already taken");
                            userName.requestFocus();
                        } else {
                            // 🔹 Username is unique OR already belongs to the current user
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("firstName", fName);
                            hashMap.put("lastName", lName);
                            hashMap.put("userName", uName);
                            hashMap.put("bio", bio);
                            hashMap.put("image", image);
                            hashMap.put("email", firebaseAuth.getCurrentUser().getEmail());

                            reference.child(FireBaseUtill.currentUserId()).setValue(hashMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    AndroidUtil.showToast(getApplicationContext(), "Successfully logged in");
                                    Intent intent = new Intent(LoginProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    AndroidUtil.showToast(getApplicationContext(), "Failed to store data");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        AndroidUtil.showToast(getApplicationContext(), "Database error occurred");
                    }
                });
            }
        });

    }

    private void getUserDetails() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Connecta").child("ConnectaUsers");

        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            // check untill required data get
                for (DataSnapshot ds : snapshot.getChildren()){
                    String fName = "" + ds.child("firstName").getValue();
                    String lName = "" + ds.child("lastName").getValue();
                    String uname = "" + ds.child("userName").getValue();
                    image = "" + ds.child("image").getValue();
                    bio = "" + ds.child("bio").getValue();

                    //set data
                    firstName.setText(fName);
                    lastName.setText(lName);
                    userName.setText(uname);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}