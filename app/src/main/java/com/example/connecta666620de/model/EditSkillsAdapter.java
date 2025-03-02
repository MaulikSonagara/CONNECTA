package com.example.connecta666620de.model;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.R;
import com.example.connecta666620de.model.Skill;

import java.util.List;

public class EditSkillsAdapter extends RecyclerView.Adapter<EditSkillsAdapter.MyHolder> {

    private Context context;
    private List<Skill> skillsList;

    // Skill Level and Experience Level options
    private String[] skillLevels = {"Beginner", "Intermediate", "Advanced", "Expert", "Master"};
    private String[] experienceLevels = {"Less than 1 year", "1-3 years", "3-5 years", "5-10 years", "More than 10 years"};

    // Constructor
    public EditSkillsAdapter(Context context, List<Skill> skillsList) {
        this.context = context;
        this.skillsList = skillsList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_skill, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Skill skill = skillsList.get(position);

        // Set skill data to views
        holder.skillTitle.setText(skill.getTitle());
        holder.skillDescription.setText(skill.getDescription());

        // Handle changes in the Skill Title field
        holder.skillTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                skill.setTitle(s.toString()); // Update the skill title
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle changes in the Skill Description field
        holder.skillDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                skill.setDescription(s.toString()); // Update the skill description
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set Skill Level Spinner
        holder.skillLevelSpinner.setSelection(getIndex(holder.skillLevelSpinner, skill.getSkillLevel()));
        holder.skillLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                skill.setSkillLevel(skillLevels[position]); // Update the skill level
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set Experience Level Spinner
        holder.experienceLevelSpinner.setSelection(getIndex(holder.experienceLevelSpinner, skill.getExperienceLevel()));
        holder.experienceLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                skill.setExperienceLevel(experienceLevels[position]); // Update the experience level
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle Remove Skill Button Click
        holder.removeSkillButton.setOnClickListener(v -> {
            skillsList.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return skillsList.size();
    }

    // Helper method to get the index of a value in a Spinner
    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // Default to the first item if not found
    }

    // ViewHolder class
    class MyHolder extends RecyclerView.ViewHolder {
        EditText skillTitle, skillDescription;
        Spinner skillLevelSpinner, experienceLevelSpinner;
        Button removeSkillButton;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            skillTitle = itemView.findViewById(R.id.edit_skill_title);
            skillDescription = itemView.findViewById(R.id.edit_skill_description);
            skillLevelSpinner = itemView.findViewById(R.id.skill_level_spinner);
            experienceLevelSpinner = itemView.findViewById(R.id.experience_level_spinner);
            removeSkillButton = itemView.findViewById(R.id.remove_skill_button);

            // Set adapters for Spinners
            skillLevelSpinner.setAdapter(new android.widget.ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, skillLevels));
            experienceLevelSpinner.setAdapter(new android.widget.ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, experienceLevels));
        }
    }
}