package com.akarin.hbina.akarinspending;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);

        mAuth = FirebaseAuth.getInstance();


        Button signInButton = findViewById(R.id.email_sign_in_button);
        Button createAccountButton = findViewById(R.id.email_create_account_button);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        } else {
            Log.d(this.toString(), "No user is logged in");
        }
    }

    private void createAccount(String email, String password) {
        Log.d(this.toString(), "creating account with email:" + email + " password:" + password);

        if (checkFormatOfEmail(email) && checkFormatOfPassword(password)) {
            Log.d(this.toString(), "Format validation of user email and password failed");
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(this.toString(), "creating of user account is successfull");
                                goToMainActivity();
                            } else {
                                Log.e(this.toString(), "creating of user account has failed due to" + task.getException());
                            }
                        }
                    });
        }
    }

    private void signIn(String email, String password) {
        Log.d(this.toString(), "signIn:" + email);

        if (checkFormatOfEmail(email) && checkFormatOfPassword(password)) {
            Log.d(this.toString(), "Format validation of user email and password failed");
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(LoginActivity.this.getClass().toString(), "signInWithEmail:success");
                                goToMainActivity();
                            } else {
                                Log.e(LoginActivity.this.getClass().toString(), "signInWithEmail:failure", task.getException());
                            }
                        }
                    });
        }
    }

    private boolean checkFormatOfEmail(String email) {
        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            Log.e(this.toString(), "The given email is an emptry string");
            mEmailField.setError("This field is required");
            valid = false;
        }
        return !valid;
    }

    private boolean checkFormatOfPassword(String password) {
        boolean valid = true;
        if (TextUtils.isEmpty(password)) {
            Log.e(this.toString(), "The given password is an emptry string");
            mEmailField.setError("This field is required");
            valid = false;
        }
        return !valid;
    }

    private void goToMainActivity() {
        Log.d(LoginActivity.this.getClass().toString(), "Redirecting user to MainActivity");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
    }
}

