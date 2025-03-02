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

import android.os.Handler;
import android.view.*;
import android.widget.*;

import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.FireBaseUtill;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.database.Query;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    SettingFragment settingFragment;
    ProgressDialog pd;


    ImageView avatarIv, settingBtn;
    TextView nameTv,usernameTv, bioTv, followersTv, followingTv, postsTv;

    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri selectedImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    public ProfileFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //init firebase

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Connecta").child("ConnectaUsers");


        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check untill required data get
                for (DataSnapshot ds : snapshot.getChildren()){
                    String firstName = "" + ds.child("firstName").getValue();
                    String lastName = "" + ds.child("lastName").getValue();
                    String bio = "" + ds.child("bio").getValue();
                    String email = "" + ds.child("email").getValue();
                    String username = "" + ds.child("userName").getValue();
                    String image = "" + ds.child("image").getValue();

                    // set data
                    nameTv.setText(firstName + " " + lastName);
                    usernameTv.setText("@" + username);
                    bioTv.setText(bio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pd = new ProgressDialog(getContext());
        avatarIv = view.findViewById(R.id.profileimage_profile);
        nameTv = view.findViewById(R.id.name_profile);
        usernameTv = view.findViewById(R.id.username_profile);
        bioTv = view.findViewById(R.id.bio_profile);
        followersTv = view.findViewById(R.id.follower_data_profile);
        followingTv = view.findViewById(R.id.following_data_profile);
        postsTv = view.findViewById(R.id.post_data_profile);
        settingBtn = view.findViewById(R.id.settingBtn);

        settingFragment = new SettingFragment();

        // Getting Profile Pic
        FireBaseUtill.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri uri = task.getResult();
                AndroidUtil.setProfilePic(getContext(), uri, avatarIv);
            }
        });

        settingBtn.setOnClickListener(v -> {
            Fragment settingFragment = new SettingFragment();
            Bundle bundle = new Bundle();

            // Pass profile data
            bundle.putString("name", nameTv.getText().toString());

            settingFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_layout, settingFragment)
                    .addToBackStack(null)
                    .commit();
        });

    }
}