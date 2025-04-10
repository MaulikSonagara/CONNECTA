package com.example.connecta666620de.utills;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.connecta666620de.R;


public class AndroidUtil {

    public static void showToast(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

    public static RequestOptions getProfilePicOptions() {
        return new RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.person_icon)
                .error(R.drawable.person_icon);
    }

}
