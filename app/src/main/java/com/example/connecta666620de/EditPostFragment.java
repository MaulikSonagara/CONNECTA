package com.example.connecta666620de;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.connecta666620de.model.Post;
import com.example.connecta666620de.utills.AndroidUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditPostFragment extends Fragment {

    private LinearLayout generalInputs, doubtInputs, quizInputs;
    private EditText captionInput, doubtQuestionInput, quizQuestionInput;
    private EditText option1Input, option2Input, option3Input, option4Input;
    private Spinner correctAnswerSpinner;
    private Button updatePostBtn;
    private Post post;

    public EditPostFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post = (Post) getArguments().getSerializable("post");
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);

        generalInputs = view.findViewById(R.id.general_inputs);
        doubtInputs = view.findViewById(R.id.doubt_inputs);
        quizInputs = view.findViewById(R.id.quiz_inputs);
        captionInput = view.findViewById(R.id.caption_input);
        doubtQuestionInput = view.findViewById(R.id.doubt_question_input);
        quizQuestionInput = view.findViewById(R.id.quiz_question_input);
        option1Input = view.findViewById(R.id.option1_input);
        option2Input = view.findViewById(R.id.option2_input);
        option3Input = view.findViewById(R.id.option3_input);
        option4Input = view.findViewById(R.id.option4_input);
        correctAnswerSpinner = view.findViewById(R.id.correct_answer_spinner);
        updatePostBtn = view.findViewById(R.id.update_post_btn);

        // Setup UI based on post type
        setupUI();

        updatePostBtn.setOnClickListener(v -> validateAndUpdatePost());

        return view;
    }

    private void setupUI() {
        generalInputs.setVisibility(View.GONE);
        doubtInputs.setVisibility(View.GONE);
        quizInputs.setVisibility(View.GONE);

        switch (post.getType()) {
            case "General":
                generalInputs.setVisibility(View.VISIBLE);
                captionInput.setText(post.getCaption());
                break;
            case "Doubt":
                doubtInputs.setVisibility(View.VISIBLE);
                doubtQuestionInput.setText(post.getQuestion());
                break;
            case "Quiz":
                quizInputs.setVisibility(View.VISIBLE);
                quizQuestionInput.setText(post.getQuestion());
                List<String> options = post.getOptions();
                if (options != null && options.size() >= 4) {
                    option1Input.setText(options.get(0));
                    option2Input.setText(options.get(1));
                    option3Input.setText(options.get(2));
                    option4Input.setText(options.get(3));
                }
                setupCorrectAnswerSpinner(post.getCorrectAnswer());
                break;
        }
    }

    private void setupCorrectAnswerSpinner(String currentAnswer) {
        List<String> answerOptions = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        ArrayAdapter<String> answerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, answerOptions);
        answerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        correctAnswerSpinner.setAdapter(answerAdapter);
        int position = answerOptions.indexOf(currentAnswer);
        if (position >= 0) {
            correctAnswerSpinner.setSelection(position);
        }
    }

    private void validateAndUpdatePost() {
        Map<String, Object> updates = new HashMap<>();
        String userId = post.getUserId();

        switch (post.getType()) {
            case "General":
                String caption = captionInput.getText().toString().trim();
                if (caption.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a caption", Toast.LENGTH_SHORT).show();
                    return;
                }
                post.setCaption(caption);
                break;
            case "Doubt":
                String question = doubtQuestionInput.getText().toString().trim();
                if (question.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a question", Toast.LENGTH_SHORT).show();
                    return;
                }
                post.setQuestion(question);
                break;
            case "Quiz":
                String quizQuestion = quizQuestionInput.getText().toString().trim();
                String option1 = option1Input.getText().toString().trim();
                String option2 = option2Input.getText().toString().trim();
                String option3 = option3Input.getText().toString().trim();
                String option4 = option4Input.getText().toString().trim();
                String correctAnswer = correctAnswerSpinner.getSelectedItem().toString();

                if (quizQuestion.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                post.setQuestion(quizQuestion);
                post.setOptions(Arrays.asList(option1, option2, option3, option4));
                post.setCorrectAnswer(correctAnswer);
                break;
        }

        // Update Firebase
        updates.put("Posts/" + post.getPostId(), post);
        updates.put("UserPosts/" + userId + "/" + post.getPostId(), post);

        FirebaseDatabase.getInstance().getReference("Connecta")
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    AndroidUtil.showToast(getContext(), "Post updated successfully");
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    AndroidUtil.showToast(getContext(), "Failed to update post: " + e.getMessage());
                });
    }
}