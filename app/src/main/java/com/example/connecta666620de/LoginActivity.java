package com.example.connecta666620de;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.connecta666620de.utills.AndroidUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView signUpText, forgotPasswordTxt;
    ProgressDialog pd;
    SignInButton googleLoginBtn;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        signUpText = findViewById(R.id.SignUp_txt);
        emailInput = findViewById(R.id.email_Etxt);
        passwordInput = findViewById(R.id.password_Etxt);
        forgotPasswordTxt = findViewById(R.id.forgot_password_text);
        loginBtn = findViewById(R.id.Login_btn);
        googleLoginBtn = findViewById(R.id.google_login_btn);
        pd = new ProgressDialog(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient  = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();


        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailInput.setError("Invalid Email");
                emailInput.requestFocus();
            } else if (password.isEmpty()) {
                passwordInput.setError("Enter Password");
                passwordInput.requestFocus();
            } else {
                loginUser(email, password);
            }
        });

        googleLoginBtn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        forgotPasswordTxt.setOnClickListener(v -> {
            showRecoverPasswdDialog();
        });
    }

    private void loginUser(String email, String password) {

        pd.setTitle("Logging In");
        pd.setMessage("Please wait...");
        pd.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            pd.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent emailAddress = new Intent(LoginActivity.this, LoginProfileActivity.class);
                            emailAddress.putExtra("flag", false);
                            startActivity(emailAddress);
                            finish();
                        } else {
                            pd.dismiss();
                            emailInput.setError("Invalid Credentials");
                            emailInput.requestFocus();
                        }
                    }
                }).addOnFailureListener(this, e -> {
                    pd.dismiss();
                    emailInput.setError("Invalid Credentials");
                    emailInput.requestFocus();
                });

    }

    private void showRecoverPasswdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover password");
        LinearLayout linearLayout = new LinearLayout(this);

        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEt.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailInput.setError("Invalid Email");
                    emailInput.requestFocus();
                }
                else if(email.isEmpty()){
                    emailEt.setError("Please enter Email Address !");
                    emailEt.requestFocus();
                }
                else{
                    beginRecovery(email);
                }
            }
        });

        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecovery(String email) {
        pd.setTitle("Password Recovery");
        pd.setMessage("Sending Email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        if(task.isSuccessful()){
                            AndroidUtil.showToast(getApplicationContext(), "Email sent to " + email);
                        } else {
                            AndroidUtil.showToast(getApplicationContext(), "Something went wrong");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        AndroidUtil.showToast(getApplicationContext(), " " + e.getMessage());
                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                AndroidUtil.showToast(getApplicationContext(),"" + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent emailAddress = new Intent(LoginActivity.this, LoginProfileActivity.class);

                            emailAddress.putExtra("emailAddress", user.getEmail());
                            startActivity(emailAddress);
                            finish();

                        } else {
                            AndroidUtil.showToast(getApplicationContext(), "Something went wrong");

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AndroidUtil.showToast(getApplicationContext(), "" + e.getMessage());
                    }
                });
    }



}