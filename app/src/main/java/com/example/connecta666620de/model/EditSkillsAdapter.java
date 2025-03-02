package com.example.connecta666620de.model;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connecta666620de.R;
import com.example.connecta666620de.model.Skill;

import java.util.List;

public class EditSkillsAdapter extends RecyclerView.Adapter<EditSkillsAdapter.MyHolder> {

    private Context context;
    private List<Skill> skillsList;

    // Constructor
    public EditSkillsAdapter(Context context, List<Skill> skillsList) {
        this.context = context;
        this.skillsList = skillsList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each skill item
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_skill, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get the skill at the current position
        Skill skill = skillsList.get(position);

        // Set skill data to the views
        holder.skillTitle.setText(skill.getTitle());
        holder.skillDescription.setText(skill.getDescription());
        holder.skillLevel.setText(skill.getLevel());

        // Handle changes in the Skill Title field
        holder.skillTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the skill title in the list
                skill.setTitle(s.toString());
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
                // Update the skill description in the list
                skill.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle changes in the Skill Level field
        holder.skillLevel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the skill level in the list
                skill.setLevel(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle Remove Skill Button Click
        holder.removeSkillButton.setOnClickListener(v -> {
            // Remove the skill from the list
            skillsList.remove(position);
            // Notify the adapter that the data has changed
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return skillsList.size();
    }

    // ViewHolder class
    class MyHolder extends RecyclerView.ViewHolder {
        EditText skillTitle, skillDescription, skillLevel;
        Button removeSkillButton;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            skillTitle = itemView.findViewById(R.id.edit_skill_title);
            skillDescription = itemView.findViewById(R.id.edit_skill_description);
            skillLevel = itemView.findViewById(R.id.edit_skill_level);
            removeSkillButton = itemView.findViewById(R.id.remove_skill_button);
        }
    }
}