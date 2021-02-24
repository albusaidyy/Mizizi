package com.example.mizizi.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mizizi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    //view
    private Button resetSendEmailButton;
    private EditText restEmail;

    //firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Reset Password");
        setContentView(R.layout.activity_reset_password);

        //init firebase
        mAuth = FirebaseAuth.getInstance();

        //init views
        resetSendEmailButton = findViewById(R.id.send_email);
        restEmail = findViewById(R.id.EdEmail);

        resetSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = restEmail.getText().toString();
                //data input validation
                if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                    restEmail.setError("Please enter a valid Email");
                    restEmail.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(userEmail)){
                    restEmail.setError("Please enter a valid Email");
                    restEmail.requestFocus();
                    return;
                }else {
                    //sending email
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, "Please check your Email Account to reset your password", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                                finish();
                            }else {
                                //display error message
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, "Error Occurred" + message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });
    }

}
