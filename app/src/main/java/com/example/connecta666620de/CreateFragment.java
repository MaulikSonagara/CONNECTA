package com.example.connecta666620de;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.connecta666620de.model.Post;
import com.example.connecta666620de.model.Skill;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Spinner postTypeSpinner, correctAnswerSpinner;
    private LinearLayout generalInputs, doubtInputs, quizInputs;
    private ImageView imagePreview;
    private Button pickImageBtn, createPostBtn, addSkillTagButton;
    private EditText captionInput, doubtQuestionInput, quizQuestionInput;
    private EditText option1Input, option2Input, option3Input, option4Input;
    private ChipGroup skillTagsChipGroup;
    private TextInputEditText skillTagInput;
    private Uri selectedImageUri;
    private List<String> skillTags;
    private List<Skill> userSkills;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    public CreateFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Connecta");
        storageReference = FirebaseStorage.getInstance().getReference("post_images");
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Posting...");
        progressDialog.setCancelable(false);
        skillTags = new ArrayList<>();
        userSkills = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        // Initialize views
        postTypeSpinner = view.findViewById(R.id.post_type_spinner);
        generalInputs = view.findViewById(R.id.general_inputs);
        doubtInputs = view.findViewById(R.id.doubt_inputs);
        quizInputs = view.findViewById(R.id.quiz_inputs);
        imagePreview = view.findViewById(R.id.image_preview);
        pickImageBtn = view.findViewById(R.id.pick_image_btn);
        createPostBtn = view.findViewById(R.id.create_post_btn);
        captionInput = view.findViewById(R.id.caption_input);
        doubtQuestionInput = view.findViewById(R.id.doubt_question_input);
        quizQuestionInput = view.findViewById(R.id.quiz_question_input);
        option1Input = view.findViewById(R.id.option1_input);
        option2Input = view.findViewById(R.id.option2_input);
        option3Input = view.findViewById(R.id.option3_input);
        option4Input = view.findViewById(R.id.option4_input);
        correctAnswerSpinner = view.findViewById(R.id.correct_answer_spinner);
        skillTagsChipGroup = view.findViewById(R.id.skill_tags_chip_group);
        skillTagInput = view.findViewById(R.id.skill_tag_input);
        addSkillTagButton = view.findViewById(R.id.add_skill_tag_button);

        // Set up post type spinner
        List<String> postTypes = Arrays.asList("General Image Post", "Doubt Post", "Quiz Post");
        ArrayAdapter<String> postTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, postTypes);
        postTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postTypeSpinner.setAdapter(postTypeAdapter);

        postTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = postTypes.get(position);
                generalInputs.setVisibility(View.GONE);
                doubtInputs.setVisibility(View.GONE);
                quizInputs.setVisibility(View.GONE);
                imagePreview.setVisibility(View.GONE);

                switch (selectedType) {
                    case "General Image Post":
                        generalInputs.setVisibility(View.VISIBLE);
                        if (selectedImageUri != null) {
                            imagePreview.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "Doubt Post":
                        doubtInputs.setVisibility(View.VISIBLE);
                        break;
                    case "Quiz Post":
                        quizInputs.setVisibility(View.VISIBLE);
                        setupCorrectAnswerSpinner();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up image picker
        pickImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Set up add skill tag button
        addSkillTagButton.setOnClickListener(v -> {
            String skillTitle = skillTagInput.getText().toString().trim();
            if (!skillTitle.isEmpty() && !skillTags.contains(skillTitle)) {
                skillTags.add(skillTitle);
                addChip(skillTitle);
                skillTagInput.setText("");
                // Optionally add to user skills
                addSkillToUser(skillTitle);
            } else {
                AndroidUtil.showToast(requireContext(), "Enter a unique skill title");
            }
        });

        // Set up create post button
        createPostBtn.setOnClickListener(v -> validateAndSavePost());

        // Fetch user skills
        fetchUserSkills();

        return view;
    }

    private void setupCorrectAnswerSpinner() {
        List<String> answerOptions = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        ArrayAdapter<String> answerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, answerOptions);
        answerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        correctAnswerSpinner.setAdapter(answerAdapter);
    }

    private void fetchUserSkills() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference.child("ConnectaUsers").child(userId).child("skills")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userSkills.clear();
                        for (DataSnapshot skillSnapshot : snapshot.getChildren()) {
                            Skill skill = skillSnapshot.getValue(Skill.class);
                            if (skill != null && !skillTags.contains(skill.getTitle())) {
                                userSkills.add(skill);
                                skillTags.add(skill.getTitle());
                                addChip(skill.getTitle());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CreateFragment", "Failed to fetch user skills: " + error.getMessage());
                    }
                });
    }

    private void addChip(String skillTitle) {
        Chip chip = new Chip(requireContext());
        chip.setText(skillTitle);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            skillTags.remove(skillTitle);
            skillTagsChipGroup.removeView(chip);
        });
        skillTagsChipGroup.addView(chip);
    }

    private void addSkillToUser(String skillTitle) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        Skill newSkill = new Skill(skillTitle, "", "", "");
        databaseReference.child("ConnectaUsers").child(userId).child("skills").push().setValue(newSkill);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(selectedImageUri)
                    .fitCenter()
                    .into(imagePreview);
        }
    }

    private void validateAndSavePost() {
        String postType = postTypeSpinner.getSelectedItem().toString();
        String userId = firebaseAuth.getCurrentUser().getUid();
        String postId = databaseReference.child("Posts").push().getKey();
        long timestamp = System.currentTimeMillis();

        progressDialog.show();

        switch (postType) {
            case "General Image Post":
                if (selectedImageUri == null) {
                    progressDialog.dismiss();
                    AndroidUtil.showToast(requireContext(), "Please select an image");
                    return;
                }
                String caption = captionInput.getText().toString().trim();
                if (caption.isEmpty()) {
                    progressDialog.dismiss();
                    AndroidUtil.showToast(requireContext(), "Please enter a caption");
                    return;
                }
                uploadImageAndSaveGeneralPost(postId, userId, timestamp, caption);
                break;

            case "Doubt Post":
                String doubtQuestion = doubtQuestionInput.getText().toString().trim();
                if (doubtQuestion.isEmpty()) {
                    progressDialog.dismiss();
                    AndroidUtil.showToast(requireContext(), "Please enter a question");
                    return;
                }
                saveDoubtPost(postId, userId, timestamp, doubtQuestion);
                break;

            case "Quiz Post":
                String quizQuestion = quizQuestionInput.getText().toString().trim();
                String option1 = option1Input.getText().toString().trim();
                String option2 = option2Input.getText().toString().trim();
                String option3 = option3Input.getText().toString().trim();
                String option4 = option4Input.getText().toString().trim();
                String correctAnswer = correctAnswerSpinner.getSelectedItem().toString();

                if (quizQuestion.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty()) {
                    progressDialog.dismiss();
                    AndroidUtil.showToast(requireContext(), "Please fill all fields");
                    return;
                }
                saveQuizPost(postId, userId, timestamp, quizQuestion, Arrays.asList(option1, option2, option3, option4), correctAnswer);
                break;
        }
    }

    private void uploadImageAndSaveGeneralPost(String postId, String userId, long timestamp, String caption) {
        StorageReference imageRef = storageReference.child(postId + ".jpg");
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Post post = new Post(postId, userId, "General", caption, imageUrl, null, null, null, timestamp, 0, new ArrayList<>(), 0, skillTags);
                        savePostToFirebase(post, userId);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    AndroidUtil.showToast(requireContext(), "Failed to upload image: " + e.getMessage());
                });
    }

    private void saveDoubtPost(String postId, String userId, long timestamp, String question) {
        Post post = new Post(postId, userId, "Doubt", null, null, question, null, null, timestamp, 0, new ArrayList<>(), 0, skillTags);
        savePostToFirebase(post, userId);
    }

    private void saveQuizPost(String postId, String userId, long timestamp, String question, List<String> options, String correctAnswer) {
        Post post = new Post(postId, userId, "Quiz", null, null, question, options, correctAnswer, timestamp, 0, new ArrayList<>(), 0, skillTags);
        savePostToFirebase(post, userId);
    }

    private void savePostToFirebase(Post post, String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Posts/" + post.getPostId(), post);
        updates.put("UserPosts/" + userId + "/" + post.getPostId(), post);
        updates.put("ConnectaUsers/" + userId + "/posts", ServerValue.increment(1));

        databaseReference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    AndroidUtil.showToast(requireContext(), "Post created successfully");
                    clearInputs();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    AndroidUtil.showToast(requireContext(), "Failed to create post: " + e.getMessage());
                });
    }

    private void clearInputs() {
        captionInput.setText("");
        doubtQuestionInput.setText("");
        quizQuestionInput.setText("");
        option1Input.setText("");
        option2Input.setText("");
        option3Input.setText("");
        option4Input.setText("");
        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);
        selectedImageUri = null;
        skillTags.clear();
        skillTagsChipGroup.removeAllViews();
        skillTagInput.setText("");
        postTypeSpinner.setSelection(0);
        generalInputs.setVisibility(View.GONE);
        doubtInputs.setVisibility(View.GONE);
        quizInputs.setVisibility(View.GONE);
    }
}