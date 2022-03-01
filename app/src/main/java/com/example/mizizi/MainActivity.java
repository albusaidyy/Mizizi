package com.example.mizizi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mizizi.admin.AdminActivity;
import com.example.mizizi.authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView hlogin, hlogout;


    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String uId;
    String AUid="RkOrZoxtV6bbOMFgqoxR023Y6y22";


    //if user has not logged in
    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
        //requestPermissions();


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            hlogin.setVisibility(View.GONE);
            hlogout.setVisibility(View.VISIBLE);

            uId = user.getUid();
            if (uId.equals(AUid))
            {
                startActivity(new Intent(MainActivity.this,AdminActivity.class));
            }
        } else {
            hlogin.setVisibility(View.VISIBLE);
            hlogout.setVisibility(View.GONE);


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init of firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Defining Cards
        CardView hProcess = findViewById(R.id.home_process);
        CardView hAlerts = findViewById(R.id.home_alerts);
        CardView hProfile = findViewById(R.id.home_profile);
        CardView hLearn = findViewById(R.id.home_learn);
        hlogin= findViewById(R.id.login);
        hlogout= findViewById(R.id.logout);



        //Add Click Listener to cards
        hProcess.setOnClickListener(this);
        hAlerts.setOnClickListener(this);
        hProfile.setOnClickListener(this);
        hLearn.setOnClickListener(this);
        hlogin.setOnClickListener(this);
        hlogout.setOnClickListener(this);
        //checkUserStatus();




    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
            case R.id.home_process:
                i = new Intent(this, ProcessActivity.class);
                startActivity(i);
                break;

            case R.id.home_alerts:
                i = new Intent(this, AlertActivity.class);
                startActivity(i);
                break;
            case R.id.home_profile:
                i = new Intent(this, ProfileActivity.class);
                startActivity(i);
                break;
            case R.id.home_learn:
                i = new Intent(this, LearnActivity.class);
                startActivity(i);
                break;
            case R.id.login:
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
            case R.id.logout:
               logout();
                break;

            default:
                break;

        }


    }

    //Logout method
    private void logout() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle("Log Out");
        alertDialog.setIcon(R.drawable.ic_logout_black); //set icon

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to Log out?");
        alertDialog.setCancelable(false);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                Toast.makeText(MainActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                hlogin.setVisibility(View.VISIBLE);
                hlogout.setVisibility(View.GONE);
            }

        });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });

        // Showing Alert Message
        alertDialog.show();


    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        //Display toast message when back button has been pressed
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press Back button again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);

    }


}
