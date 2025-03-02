package com.example.connecta666620de;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.R;
import com.example.connecta666620de.model.EditSkillsAdapter;
import com.example.connecta666620de.model.Skill;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditSkillsFragment extends Fragment {

    private RecyclerView skillsRecyclerView;
    private EditSkillsAdapter editSkillsAdapter;
    private List<Skill> skillsList;
    private Button addSkillButton, saveSkillsButton;
    private DatabaseReference skillsRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_skills, container, false);

        // Initialize views
        skillsRecyclerView = view.findViewById(R.id.skills_recyclerView);
        addSkillButton = view.findViewById(R.id.add_skill_button);
        saveSkillsButton = view.findViewById(R.id.save_skills_button);

        // Initialize RecyclerView
        skillsList = new ArrayList<>();
        editSkillsAdapter = new EditSkillsAdapter(getContext(), skillsList);
        skillsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        skillsRecyclerView.setAdapter(editSkillsAdapter);

        // Get current user and Firebase reference
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            AndroidUtil.showToast(getContext(), "User not logged in.");
            return view;
        }
        skillsRef = FirebaseDatabase.getInstance().getReference("Connecta").child("ConnectaUserSkills").child(currentUser.getUid());

        // Load existing skills
        loadSkills();

        // Add Skill Button Click Listener
        addSkillButton.setOnClickListener(v -> addNewSkill());

        // Save Skills Button Click Listener
        saveSkillsButton.setOnClickListener(v -> saveSkills());

        return view;
    }

    private void loadSkills() {
        skillsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                skillsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Skill skill = ds.getValue(Skill.class);
                    if (skill != null) {
                        skillsList.add(skill);
                    }
                }
                editSkillsAdapter.notifyDataSetChanged(); // Refresh the RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditSkillsFragment", "Failed to load skills: " + error.getMessage());
                AndroidUtil.showToast(getContext(), "Failed to load skills.");
            }
        });
    }

    private void addNewSkill() {
        // Add a new empty skill to the list
        Skill newSkill = new Skill("", "", "Beginner", "Less than 1 year"); // Default values for skillLevel and experienceLevel
        skillsList.add(newSkill);
        editSkillsAdapter.notifyDataSetChanged(); // Refresh the RecyclerView
    }

    private void saveSkills() {
        // Save the updated skills list to Firebase
        skillsRef.setValue(skillsList).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AndroidUtil.showToast(getContext(), "Skills saved successfully!");
            } else {
                AndroidUtil.showToast(getContext(), "Failed to save skills: " + task.getException().getMessage());
            }
        });
    }
}