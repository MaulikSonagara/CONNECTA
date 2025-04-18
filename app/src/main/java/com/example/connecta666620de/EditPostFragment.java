package com.example.connecta666620de;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.example.connecta666620de.model.Post;
import com.example.connecta666620de.model.Skill;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import java.util.ArrayList;
import java.util.List;

public class EditPostFragment extends Fragment {

    private TextView postTypeText;
    private EditText postCaption, postQuestion, option1, option2, option3, option4;
    private Button imagePickerButton, savePostButton, addSkillTagButton;
    private Spinner correctAnswerSpinner;
    private ChipGroup skillTagsChipGroup;
    private TextInputEditText skillTagInput;
    private Post post;
    private Uri imageUri;
    private List<String> skillTags;
    private List<Skill> userSkills;

    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    AndroidUtil.showToast(getContext(), "Image selected");
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);

        // Initialize views
        postTypeText = view.findViewById(R.id.post_type_text);
        postCaption = view.findViewById(R.id.post_caption);
        imagePickerButton = view.findViewById(R.id.image_picker_button);
        postQuestion = view.findViewById(R.id.post_question);
        option1 = view.findViewById(R.id.option1);
        option2 = view.findViewById(R.id.option2);
        option3 = view.findViewById(R.id.option3);
        option4 = view.findViewById(R.id.option4);
        correctAnswerSpinner = view.findViewById(R.id.correct_answer_spinner);
        savePostButton = view.findViewById(R.id.save_post_button);
        skillTagsChipGroup = view.findViewById(R.id.skill_tags_chip_group);
        skillTagInput = view.findViewById(R.id.skill_tag_input);
        addSkillTagButton = view.findViewById(R.id.add_skill_tag_button);

        // Initialize skill tags
        skillTags = new ArrayList<>();
        userSkills = new ArrayList<>();

        // Get post from arguments
        Bundle args = getArguments();
        if (args != null) {
            post = (Post) args.getSerializable("post");
            if (post != null) {
                populateFields();
            }
        }

        // Fetch user skills
        fetchUserSkills();

        // Add skill tag
        addSkillTagButton.setOnClickListener(v -> {
            String skillTitle = skillTagInput.getText().toString().trim();
            if (!skillTitle.isEmpty() && !skillTags.contains(skillTitle)) {
                skillTags.add(skillTitle);
                addChip(skillTitle);
                skillTagInput.setText("");
                // Optionally add to user skills
                addSkillToUser(skillTitle);
            } else {
                AndroidUtil.showToast(getContext(), "Enter a unique skill title");
            }
        });

        // Set up save button
        savePostButton.setOnClickListener(v -> savePost());

        return view;
    }

    private void populateFields() {
        postTypeText.setText(post.getType());
        if (post.getType().equals("General")) {
            postCaption.setVisibility(View.VISIBLE);
            postCaption.setText(post.getCaption());
            imagePickerButton.setVisibility(View.VISIBLE);
        } else if (post.getType().equals("Doubt")) {
            postQuestion.setVisibility(View.VISIBLE);
            postQuestion.setText(post.getQuestion());
        } else if (post.getType().equals("Quiz")) {
            postQuestion.setVisibility(View.VISIBLE);
            postQuestion.setText(post.getQuestion());
            option1.setVisibility(View.VISIBLE);
            option2.setVisibility(View.VISIBLE);
            option3.setVisibility(View.VISIBLE);
            option4.setVisibility(View.VISIBLE);
            correctAnswerSpinner.setVisibility(View.VISIBLE);
            if (post.getOptions() != null && post.getOptions().size() >= 4) {
                option1.setText(post.getOptions().get(0));
                option2.setText(post.getOptions().get(1));
                option3.setText(post.getOptions().get(2));
                option4.setText(post.getOptions().get(3));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item,
                    new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            correctAnswerSpinner.setAdapter(adapter);
            if (post.getCorrectAnswer() != null) {
                correctAnswerSpinner.setSelection(Integer.parseInt(post.getCorrectAnswer().replace("Option ", "")) - 1);
            }
        }
        // Populate skill tags
        skillTags.addAll(post.getSkillTags());
        for (String tag : skillTags) {
            addChip(tag);
        }
    }

    private void fetchUserSkills() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Connecta/ConnectaUsers/" + userId + "/skills")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        userSkills.clear();
                        for (DataSnapshot skillSnapshot : snapshot.getChildren()) {
                            Skill skill = skillSnapshot.getValue(Skill.class);
                            if (skill != null && !skillTags.contains(skill.getTitle())) {
                                userSkills.add(skill);
                                addChip(skill.getTitle());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("EditPostFragment", "Failed to fetch user skills: " + error.getMessage());
                    }
                });
    }

    private void addChip(String skillTitle) {
        Chip chip = new Chip(getContext());
        chip.setText(skillTitle);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            skillTags.remove(skillTitle);
            skillTagsChipGroup.removeView(chip);
        });
        skillTagsChipGroup.addView(chip);
    }

    private void addSkillToUser(String skillTitle) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Connecta/ConnectaUsers/" + userId + "/skills")
                .push().setValue(new Skill(skillTitle, "", "", ""));
    }

    private void savePost() {
        String caption = postCaption.getText().toString().trim();
        String question = postQuestion.getText().toString().trim();
        List<String> options = new ArrayList<>();
        String correctAnswer = null;

        if (post.getType().equals("Quiz")) {
            if (option1.getText().toString().trim().isEmpty() || option2.getText().toString().trim().isEmpty() ||
                    option3.getText().toString().trim().isEmpty() || option4.getText().toString().trim().isEmpty()) {
                AndroidUtil.showToast(getContext(), "All quiz options are required");
                return;
            }
            options.add(option1.getText().toString().trim());
            options.add(option2.getText().toString().trim());
            options.add(option3.getText().toString().trim());
            options.add(option4.getText().toString().trim());
            correctAnswer = "Option " + (correctAnswerSpinner.getSelectedItemPosition() + 1);
        }

        if ((post.getType().equals("General") && caption.isEmpty() && imageUri == null && post.getImageUrl() == null) ||
                (post.getType().equals("Doubt") && question.isEmpty()) ||
                (post.getType().equals("Quiz") && question.isEmpty())) {
            AndroidUtil.showToast(getContext(), "Please fill in required fields");
            return;
        }

        post.setCaption(caption);
        post.setQuestion(question);
        post.setOptions(options);
        post.setCorrectAnswer(correctAnswer);
        post.setSkillTags(skillTags);

        if (imageUri != null && post.getType().equals("General")) {
            FirebaseStorage.getInstance().getReference("post_images/" + post.getPostId() + ".jpg")
                    .putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            post.setImageUrl(uri.toString());
                            updatePost(post);
                        });
                    })
                    .addOnFailureListener(e -> {
                        AndroidUtil.showToast(getContext(), "Failed to upload image");
                    });
        } else {
            updatePost(post);
        }
    }

    private void updatePost(Post post) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Connecta")
                .updateChildren(new java.util.HashMap<String, Object>() {{
                    put("Posts/" + post.getPostId(), post);
                    put("UserPosts/" + userId + "/" + post.getPostId(), post);
                }})
                .addOnSuccessListener(aVoid -> {
                    AndroidUtil.showToast(getContext(), "Post updated successfully");
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    AndroidUtil.showToast(getContext(), "Failed to update post: " + e.getMessage());
                });
    }
}