package com.example.connecta666620de;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.fragment.app.FragmentTransaction;

public class HomeFragment extends Fragment {

    ImageButton chatBtn;
    ChatFragment chatFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize chat button and fragment
        chatBtn = view.findViewById(R.id.chat_Btn);
        chatFragment = new ChatFragment();

        // Set click listener to open ChatFragment
        chatBtn.setOnClickListener(v -> {
            // open chatListActivity
            Intent intent = new Intent(getActivity(), ChatListActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
