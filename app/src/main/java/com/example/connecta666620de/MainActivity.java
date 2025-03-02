package com.example.connecta666620de;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment;
    SearchFragment searchFragment;
    CreateFragment createFragment;
    ActivityFragment activityFragment;
    ProfileFragment profileFragment;


    ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View bottomNav = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        createFragment = new CreateFragment();
        activityFragment = new ActivityFragment();
        profileFragment = new ProfileFragment();
        chatFragment = new ChatFragment();


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.bottom_menu_home){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout,homeFragment).commit();
                }

                if (item.getItemId() == R.id.bottom_menu_search){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout,searchFragment).commit();
                }

                if (item.getItemId() == R.id.bottom_menu_add){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout,createFragment).commit();
                }

                if (item.getItemId() == R.id.bottom_menu_profile){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout,profileFragment).commit();
                }

                if (item.getItemId() == R.id.bottom_menu_activity){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout,activityFragment).commit();
                }

                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.bottom_menu_home);
    }


}
