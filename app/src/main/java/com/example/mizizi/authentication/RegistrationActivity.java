package com.example.mizizi.authentication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mizizi.LocationHandler;
import com.example.mizizi.LocationResultListener;
import com.example.mizizi.MainActivity;
import com.example.mizizi.R;
import com.example.mizizi.getitnglocation.AppLocationService;
import com.example.mizizi.getitnglocation.LocationAddress;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RegistrationActivity extends AppCompatActivity implements LocationResultListener {


    //Views
    private EditText EdEmail;
    private EditText EdPassword;
    private EditText EdPassword2;
    private TextView Tvsignin;
    private Button btnSignup;

    private TextView tvLocation, tvLocation2;
    private ProgressBar progressBar;
    private Button btn_loc;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private final int LOCATION_ACTIVITY_REQUEST_CODE = 1000;
    private LocationHandler locationHandler;



    private ProgressDialog mDiag;


    // Declare an instance of firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        locationHandler = new LocationHandler(this,this,
                LOCATION_ACTIVITY_REQUEST_CODE, LOCATION_PERMISSION_REQUEST_CODE);

        //Actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Registration");
        //enable back Button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //Initialization
        mAuth = FirebaseAuth.getInstance();
        mDiag = new ProgressDialog(this);
        mDiag.setMessage("Creating Account..");
        mDiag.setCancelable(false);

        EdEmail = findViewById(R.id.email_reg);
        EdPassword = findViewById(R.id.password_reg);
        EdPassword2 = findViewById(R.id.ConfirmPassword_reg);
        Tvsignin = findViewById(R.id.Tv_signin);
        btnSignup = findViewById(R.id.btn_signup);

        tvLocation=findViewById(R.id.userLocation);
        tvLocation2=findViewById(R.id.userLocation2);
        progressBar=findViewById(R.id.progressBar);
        btn_loc=findViewById(R.id.getLoc);


        btn_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                locationHandler.getUserLocation();

            }
        });



        //handling signUp button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = EdEmail.getText().toString().trim();
                String password = EdPassword.getText().toString().trim();
                String password2 = EdPassword2.getText().toString().trim();
                String mlocation= tvLocation.getText().toString().trim();



                //data input validation
                if (email.isEmpty()) {
                    //set Error and focus to email editText
                    EdEmail.setError("Required Field..");
                    EdEmail.setFocusable(true);

                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set Error and focus to email editText
                    EdEmail.setError("Please enter a valid Email");
                    EdEmail.setFocusable(true);

                }
                if (password.isEmpty()) {
                    //set Error and focus to password editText
                    EdPassword.setError("Required Field..");
                    EdPassword.setFocusable(true);

                }


                if (!password.equals(password2)) {
                    //set Error and focus to confirmPassword editText
                    EdPassword2.setError("Password do not match..");
                    EdPassword2.setFocusable(true);


                }
                if (password.length() < 6) {
                    //set Error and focus to password editText
                    EdPassword.setError("Password length at least 6 characters");
                    EdPassword.setFocusable(true);

                } if (mlocation.contentEquals("Location")){
                    Toast.makeText(RegistrationActivity.this, "Location  is required", Toast.LENGTH_SHORT).show();
                }

                else {
                    registerUser(email, password, mlocation);

                }
            }
        });


        //sign in button
        Tvsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                finish();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            showProgressBar();
            locationHandler.getUserLocation();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                showProgressBar();
                locationHandler.getUserLocation();
            } else if (resultCode == RESULT_CANCELED) {
                showEnableLocationDialog();
            }
        }
    }

    double latitude;
    double longitude;

    @Override
    public void getLocation(Location location) {
        hideProgressBar();
         latitude=location.getLatitude();
         longitude=location.getLongitude();

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            tvLocation.setText(state);
            btn_loc.setVisibility(View.GONE);
            tvLocation2.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Latitude: " + latitude + ",Longitude: " +longitude, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
    }
    private void showEnableLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("You must enable location in order to proceed")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressBar();
                        locationHandler.getUserLocation();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })

                .setCancelable(false)
                .create()
                .show();
    }


    private void registerUser(String email, String password, final String mLocation) {
        mDiag.show();
        //creating user
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //sign in success
                    mDiag.dismiss();

                    FirebaseUser user =mAuth.getCurrentUser();
                    //Fet user email and uid from auth
                    String email = user.getEmail();
                    String uid = user.getUid();
                    String mlat=Double.toString(latitude);
                    String mlong =Double.toString(longitude);
                    //generate random user
                    Random rand = new Random();

                    // Generate random integers in range 0 to 999
                    int rand_int = rand.nextInt(1000);
                    String rand_string=Integer.toString(rand_int);

                    //when user is registered store user info in Firebase realtime database
                    //using HashMap
                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("email",email);
                    hashMap.put("uid",uid);
                    hashMap.put("name","User"+ rand_string);
                    hashMap.put("phone","+254"); // will be added later in profile editing
                    hashMap.put("address",mLocation);
                    hashMap.put("lat", mlat);
                    hashMap.put("long",mlong);
                    hashMap.put("image",""); // will be added later in profile editing

                    //Firebase database instance
                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                    //Path to store user data in "Users"
                    DatabaseReference reference = database.getReference("Users");

                    //Putting data within hashmap in database
                    reference.child(uid).setValue(hashMap);

                    Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                    finish();

                } else {
                    Toast.makeText(RegistrationActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error mess age
                mDiag.dismiss();
                Toast.makeText(RegistrationActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }





    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous Activity
        return super.onSupportNavigateUp();
    }
}
