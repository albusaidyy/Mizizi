package com.example.mizizi.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mizizi.MainActivity;
import com.example.mizizi.R;
import com.example.mizizi.admin.AdminActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText EdEmail;
    private EditText EdPassword;
    private Button btnSignin;
    private TextView Tvsignup;
    private TextView TvresetPassword;

    private FirebaseAuth mAuth;
    private ProgressDialog mDiag;
 /*
    //if user has already logged in
    @Override
   protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() !=null){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }

    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //declaration
        mAuth = FirebaseAuth.getInstance();
        mDiag = new ProgressDialog(this);


        EdEmail = findViewById(R.id.email_login);
        EdPassword = findViewById(R.id.password_login);
        btnSignin = findViewById(R.id.btn_signin);
        Tvsignup = findViewById(R.id.Tv_signup);
        TvresetPassword = findViewById(R.id.Tv_RestPass);

        //sign in process
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = EdEmail.getText().toString().trim();
                String password = EdPassword.getText().toString().trim();

                //input data validation
                if (email.isEmpty()) {
                    EdEmail.setError("Required Field..");
                    EdEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    EdPassword.setError("Required Field..");
                    EdPassword.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    EdEmail.setError("Please enter a valid Email");
                    EdEmail.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    EdPassword.setError("Minimum Length of password should be 6");
                    EdPassword.requestFocus();
                    return;
                }
                mDiag.setMessage("Processing..");
                mDiag.show();
                mDiag.setCancelable(false);

                //authenticating user
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDiag.dismiss();
                            if (Objects.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail(), "alphamizizi@gmail.com")) {
                                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                                Toast.makeText(LoginActivity.this, "Log In successful", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            mDiag.dismiss();
                            String message = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

        //for signup
        Tvsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        //reseting password
        TvresetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));

            }
        });


    }

}
