package com.example.connecta666620de;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.connecta666620de.utills.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;

import java.security.SecureRandom;

public class RegisterActivity extends AppCompatActivity {

    // Initialization Views
    EditText emailInput, passwordInput;
    Button registerBtn;
    ProgressDialog progressDialog;
    TextView loginText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);


        emailInput = findViewById(R.id.email_Etxt);
        registerBtn = findViewById(R.id.Register_btn);
        loginText = findViewById(R.id.Login_txt);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We're creating your account");

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        registerBtn.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailInput.setError("Invalid Email");
                emailInput.requestFocus();
            } else{
                registerUser(email);
            }
        });

    }

    private void registerUser(String email) {
        progressDialog.show();

        // Check if email is already registered
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    if (result != null && !result.getSignInMethods().isEmpty()) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Email is already registered!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with account creation
                        String passwd = generatepass();
                        mAuth.createUserWithEmailAndPassword(email, passwd)
                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            mAuth.sendPasswordResetEmail(email)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if(task.isSuccessful()){
                                                                AndroidUtil.showToast(getApplicationContext(), "Email sent to " + email);



                                                                Intent emailAddress = new Intent(RegisterActivity.this, LoginActivity.class);
                                                                startActivity(emailAddress);

                                                            } else {
                                                                AndroidUtil.showToast(getApplicationContext(), "Something went wrong");
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                            AndroidUtil.showToast(getApplicationContext(), " " + e.getMessage());
                                                        }
                                                    });

                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Failed to Register", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
//                                Toast.makeText(RegisterActivity.this, "Failed to Register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        emailInput.setError("Email is already registered !");
                                        emailInput.requestFocus();
                                    }
                                });
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private String generatepass() {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@";
        final int PASSWORD_LENGTH = 8;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}