package com.example.connecta666620de.utills;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FireBaseUtill {

    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static Query getUserByUserName(String userName) {
        return FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("userName", userName);
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("Users").document(currentUserId());
    }

    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic").child(currentUserId());
    };
}
