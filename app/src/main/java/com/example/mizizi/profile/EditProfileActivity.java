package com.example.mizizi.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mizizi.LocationHandler;
import com.example.mizizi.LocationResultListener;
import com.example.mizizi.R;
import com.example.mizizi.authentication.LoginActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity implements LocationResultListener {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //views
    ImageView profileEd;
    EditText nameEd, phoneEd;

    TextView tv_change, btn_changepass;
    Button btn_save;

    ProgressDialog pd;


    private Uri mImageUri;
    StorageReference storageRef;
    String uId;

    private TextView tvLocation, tvLocation2;
    private ProgressBar progressBar;
    private Button btn_loc;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private final int LOCATION_ACTIVITY_REQUEST_CODE = 1000;
    private final int IMAGE_ACTIVITY_REQUEST_CODE = 2000;

    private LocationHandler locationHandler;


    //if user has not logged in
    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();


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
            uId = user.getUid();
        } else {
            startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
            Toast.makeText(this, "You need to log in to view Profile", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Edit Profile");
        setContentView(R.layout.activity_edit_profile);
        locationHandler = new LocationHandler(this,this,
                LOCATION_ACTIVITY_REQUEST_CODE, LOCATION_PERMISSION_REQUEST_CODE);


        //init of firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");


        //Init views
        profileEd = findViewById(R.id.profile);
        tv_change = findViewById(R.id.tv_change);
        nameEd = findViewById(R.id.EdNameUpdate);
        phoneEd = findViewById(R.id.EdPhoneUpdate);
        btn_save = findViewById(R.id.btnsaveUpdate);
        btn_changepass = findViewById(R.id.TvchangePass);
        pd = new ProgressDialog(this);

        tvLocation = findViewById(R.id.userLocation);
        tvLocation2 = findViewById(R.id.userLocation2);
        progressBar = findViewById(R.id.progressBar);
        btn_loc = findViewById(R.id.getLoc);


        //get data through intent from previous activitie's adapter
        Intent intent = getIntent();
        final String isUpdateKey = "" + intent.getStringExtra("key");
        final String userId = "" + intent.getStringExtra("userId");
        //validate if we came here to update alert i.e came from AdapterAlert
        if (isUpdateKey.equals("editUser")) {
            //Load user data
            actionBar.setTitle("Update User");
            loadUsertData(userId);
            btn_changepass.setVisibility(View.GONE);
            profileEd.setVisibility(View.GONE);
            tv_change.setVisibility(View.GONE);
            btn_loc.setVisibility(View.GONE);
            tvLocation2.setVisibility(View.VISIBLE);
            tvLocation2.setText("is the user's Location");
            btn_save.setText("Save User Details");

        } else {
            loadDetails();

        }

        checkUserStatus();


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEd.getText().toString().trim();
                String phone = phoneEd.getText().toString().trim();
                String address = tvLocation.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    nameEd.setError("Required");
                    nameEd.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    phoneEd.setError("Required");
                    phoneEd.requestFocus();
                    return;
                }
                if (!Patterns.PHONE.matcher(phone).matches()) {
                    phoneEd.setError("Enter a valid Phone No.");
                    phoneEd.requestFocus();
                    return;

                }
                if (isUpdateKey.equals("editUser")) {
                    //update
                    btn_loc.setVisibility(View.GONE);
                    updateUser(name, phone, userId);


                } else {
                    updateProfile(name, phone, address);

                }


            }
        });

        btn_changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfileActivity.this, ChangePasswordActivity.class));
            }
        });

        //start image pick activity
        tv_change.setOnClickListener(v -> getImage());

        //start image pick activity
        profileEd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_loc.setOnClickListener(v -> {
            showProgressBar();
            locationHandler.getUserLocation();
        });


    }

    //get image
    private void getImage() {
        ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();

    }


    //user details load
    private void loadDetails() {
        databaseReference = firebaseDatabase.getReference("Users");
        storageRef = FirebaseStorage.getInstance().getReference("Uploads")
                .child("Images").child("Users").child("Profile pic").child(user.getUid());
        //Getting info of currently signed in user. Using user's email or uid.
        /*By using orderByChild query to show the detail from a node
        whose key named email has a value to currently signed in email.
        It will search all nodes, where the key matches and pull the details*/

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //checking until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String address = "" + ds.child("address").getValue();
                    String image = "" + ds.child("image").getValue();

                    //Set data
                    nameEd.setText(name);
                    phoneEd.setText(phone);
                    tvLocation.setText(address);
                    try {
                        //if Image is received
                        btn_save.setClickable(true);
                        btn_loc.setClickable(true);
                        Picasso.get().load(image).into(profileEd);

                    } catch (Exception e) {
                        //if there is any exception while getting image
                        Picasso.get().load(R.drawable.userprofile).into(profileEd);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //user profile updating
    private void updateProfile(final String name, String phone, String address) {

        pd.setMessage("Updating..");
        pd.show();
        pd.setCancelable(false);

        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("phone", phone);
        result.put("address", address);

        databaseReference.child(user.getUid()).updateChildren(result)
                .addOnSuccessListener(aVoid -> {
                    //update is successful
                    pd.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Profile details updated...", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    //error in update
                    pd.dismiss();
                    Toast.makeText(EditProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
        //update name on alerts too
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        Query query = ref.orderByChild("uid").equalTo(uId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    assert child != null;
                    dataSnapshot.getRef().child(child).child("uName").setValue(name);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //Admin load of users
    private void loadUsertData(String userId) {

        //Getting info of currently signed in user. Using user's email or uid.
        /*By using orderByChild query to show the detail from a node
        whose key named email has a value to currently signed in email.
        It will search all nodes, where the key matches and pull the details*/

        Query query = databaseReference.orderByChild("uid").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //checking until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String address = "" + ds.child("address").getValue();


                    //Set data
                    nameEd.setText(name);
                    phoneEd.setText(phone);
                    tvLocation.setText(address);
                    btn_save.setClickable(true);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //Admin updating user
    private void updateUser(final String name, String phone, String userId) {
        pd.setMessage("Updating..");
        pd.show();
        pd.setCancelable(false);

        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("phone", phone);


        databaseReference.child(userId).updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update is successful
                        pd.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Profile details updated...", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //error in update
                        pd.dismiss();
                        Toast.makeText(EditProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        //update name on alerts too
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        Query query = ref.orderByChild("uid").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    dataSnapshot.getRef().child(child).child("uName").setValue(name);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //getting Uri of image
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Uploading Image to firebase storage
    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        if (mImageUri != null) {
            final StorageReference filerefence = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            StorageTask uploadTask = filerefence.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filerefence.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        final String myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("image", "" + myUrl);

                        reference.updateChildren(hashMap);
                        pd.dismiss();
                        //update image on alerts too
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
                        Query query = ref.orderByChild("uid").equalTo(uId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    assert child != null;
                                    dataSnapshot.getRef().child(child).child("uDp").setValue(myUrl);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        Toast.makeText(EditProfileActivity.this, "Image Updated...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    //get image from activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            switch (requestCode) {
                case ImagePicker.REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        //Image Uri will not be null for RESULT_OK
                        assert data != null;
                        mImageUri =data.getData();
                        profileEd.setImageURI(mImageUri);
                        // Use Uri object instead of File to avoid storage permissions
                        uploadImage();
                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case LOCATION_ACTIVITY_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        showProgressBar();
                        locationHandler.getUserLocation();
                    } else if (resultCode == RESULT_CANCELED) {
                        showEnableLocationDialog();
                    }


            }

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int LOCATION_PERMISSION_REQUEST_CODE = 1000;
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            showProgressBar();
            locationHandler.getUserLocation();
        }
    }


    @Override
    public void getLocation(Location location) {
        hideProgressBar();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String state = addresses.get(0).getAdminArea();

            tvLocation.setText(state);
            btn_loc.setVisibility(View.GONE);
            tvLocation2.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Latitude: " + latitude + ",Longitude: " + longitude, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showEnableLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("You must enable location in order to proceed")
                .setPositiveButton("Enable", (dialog, which) -> {
                    showProgressBar();
                    locationHandler.getUserLocation();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                })

                .setCancelable(false)
                .create()
                .show();
    }


}
