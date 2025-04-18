package com.example.connecta666620de;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private CreateFragment createFragment;
    private ActivityFragment activityFragment;
    private ProfileFragment profileFragment;
    private ChatFragment chatFragment;

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
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.bottom_menu_home) {
                    selectedFragment = homeFragment;
                } else if (itemId == R.id.bottom_menu_search) {
                    selectedFragment = searchFragment;
                } else if (itemId == R.id.bottom_menu_add) {
                    selectedFragment = createFragment;
                } else if (itemId == R.id.bottom_menu_activity) {
                    selectedFragment = activityFragment;
                } else if (itemId == R.id.bottom_menu_profile) {
                    selectedFragment = profileFragment;
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame_layout, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.bottom_menu_home);
        }

        // Handle intent to show post
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && "show_post".equals(intent.getStringExtra("action"))) {
            String postId = intent.getStringExtra("post_id");
            if (postId != null) {
                Log.d("MainActivity", "Handling show_post intent for postId: " + postId);
                ShowPostFragment fragment = ShowPostFragment.newInstance(postId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
                // Deselect bottom navigation to indicate a non-menu fragment
                bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
            }
        }
    }
}