package com.example.connecta666620de;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.connecta666620de.utills.AndroidUtil;
import com.example.connecta666620de.utills.FireBaseUtill;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingFragment extends Fragment {

    LinearLayout setting_logout;
    ShapeableImageView profilePic;
    TextView name_profile;
    BottomNavigationView bottomNavigationView;
    EditProfileFragment editProfileFragment;
    MaterialButton editProfileBtn;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();

    ProfileFragment profileFragment;

    private SwitchCompat nightModeSwitch;
    private SharedPreferences sharedPreferences;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        editProfileFragment = new EditProfileFragment();
        editProfileBtn = view.findViewById(R.id.edit_profile_btn);
        setting_logout = view.findViewById(R.id.setting_logout);
        nightModeSwitch = view.findViewById(R.id.night_mode_switch);
        sharedPreferences = requireContext().getSharedPreferences("Settings", requireContext().MODE_PRIVATE);

        profilePic = view.findViewById(R.id.profileAvatarIv);
        name_profile = view.findViewById(R.id.name_profile);

        profileFragment = new ProfileFragment();

        // Get arguments from ProfileFragment
        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("name", "");
            name_profile.setText(name);
        }

        // Getting Profile Pic
        FireBaseUtill.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri uri = task.getResult();
                AndroidUtil.setProfilePic(getContext(), uri, profilePic);
            }
        });

        // Check if user has already set a preference
        boolean isUserSetTheme = sharedPreferences.contains("NightMode");

        if (isUserSetTheme) {
            // Apply user preference
            boolean isNightMode = sharedPreferences.getBoolean("NightMode", false);
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            nightModeSwitch.setChecked(isNightMode);
        } else {
            // Default to system theme
            int currentNightMode = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isSystemDark = (currentNightMode == Configuration.UI_MODE_NIGHT_YES);
            AppCompatDelegate.setDefaultNightMode(isSystemDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            nightModeSwitch.setChecked(isSystemDark);
        }

        // Toggle switch listener
        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("NightMode", isChecked);
            editor.apply();

            // Change theme
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            

        });

        setting_logout.setOnClickListener(v -> {
            ProgressDialog pd = new ProgressDialog(getContext());
            pd.setTitle("Logging Out");
            pd.setMessage("Please wait...");
            pd.show();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

            googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
                firebaseAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                pd.dismiss();
            });
        });

        editProfileBtn.setOnClickListener(v -> {
            Fragment settingFragment = new SettingFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_layout, editProfileFragment) // Ensure this ID matches your container in XML
                    .addToBackStack(null) // Allows back navigation
                    .commit();
        });
    }
}