package com.example.connecta666620de;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.FireBaseUtill;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class EditProfileFragment extends Fragment {

    MaterialButton submitBtn;
    EditText firstName, lastName, userName, bio;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    ShapeableImageView profilePic;
    Uri selectedImageUri;
    String emailAddress;
    String image;
    String posts, follower, following;


    ActivityResultLauncher<Intent> imagePickLauncher;


    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getUserDetails();

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if (data != null && data.getData() != null){
                        selectedImageUri = data.getData();
                        AndroidUtil.setProfilePic(getContext(), selectedImageUri, profilePic);
                    }
                }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profilePic = view.findViewById(R.id.profileAvatarIv);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        submitBtn = view.findViewById(R.id.editProfileSubmitBtn);
        firstName = view.findViewById(R.id.first_Etxt);
        lastName = view.findViewById(R.id.last_Etxt);
        userName = view.findViewById(R.id.userName_Etxt);
        bio = view.findViewById(R.id.Bio_Etxt);

        submitBtn.setOnClickListener(v -> {
            if (selectedImageUri != null){
                FireBaseUtill.getCurrentProfilePicStorageRef().putFile(selectedImageUri).addOnCompleteListener(task -> {
                    setUserDetails();
                });
            } else {
                setUserDetails();
            }
        });

        profilePic.setOnClickListener(v -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

    }

    private void setUserDetails() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String uName = userName.getText().toString().trim();
        String bioData = bio.getText().toString().trim();

        if (fName.isEmpty()) {
            firstName.setError("Enter First Name");
            firstName.requestFocus();
        } else if (lName.isEmpty()) {
            lastName.setError("Enter Last Name");
            lastName.requestFocus();
        } else if (uName.isEmpty()) {
            userName.setError("Enter User Name");
            userName.requestFocus();
        } else if (bioData.isEmpty()) {
            bio.setError("Enter Bio");
            bio.requestFocus();
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
            ProgressDialog pd = new ProgressDialog(getContext());
            pd.setTitle("Updating Profile");
            pd.setMessage("Please wait...");
            pd.show();
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
                        emailAddress = user.getEmail();
                        // 🔹 Username is unique OR already belongs to the current user
                        HashMap<Object, String> hashMap = new HashMap<>();

                        // Preserve the existing image if no new image is selected
                        if (selectedImageUri != null) {
                            hashMap.put("image", "gs://blooddonation-bf35b.appspot.com/profile_pic/" + user.getUid());
                        } else if (image != null && !image.isEmpty()) {
                            hashMap.put("image", image); // Retain existing image URL
                        }

                        hashMap.put("uId", firebaseAuth.getCurrentUser().getUid().toString());
                        hashMap.put("firstName", fName);
                        hashMap.put("lastName", lName);
                        hashMap.put("userName", uName);
                        hashMap.put("email", emailAddress);
                        hashMap.put("bio", bioData);
                        hashMap.put("posts", posts);
                        hashMap.put("follower", follower);
                        hashMap.put("following", following);

                        reference.child(FireBaseUtill.currentUserId()).setValue(hashMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Data Updated Successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            } else {
                                Toast.makeText(getContext(), "Failed to store data", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    AndroidUtil.showToast(getContext(), "Database error occurred");
                }
            });
        }
    }

    private void getUserDetails() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // Getting Profile Pic
        FireBaseUtill.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               Uri uri = task.getResult();
               AndroidUtil.setProfilePic(getContext(), uri, profilePic);
           }
        });

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
                    String bioData = "" + ds.child("bio").getValue();
                    image = "" + ds.child("image").getValue();
                    posts = "" + ds.child("posts").getValue();
                    follower = "" + ds.child("follower").getValue();
                    following = "" + ds.child("following").getValue();

                    //set data
                    firstName.setText(fName);
                    lastName.setText(lName);
                    userName.setText(uname);

                    bio.setText(bioData);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}