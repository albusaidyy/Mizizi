package com.example.mizizi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mizizi.authentication.LoginActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddAlertActivity extends AppCompatActivity {

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
            email = user.getEmail();
            uid = user.getUid();
        } else {
            startActivity(new Intent(AddAlertActivity.this, LoginActivity.class));
            Toast.makeText(this, "You need to log in to view Profile", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    ActionBar actionBar;

    //views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    //user info
    String name, email, uid, dp, slat, slong;


    //info of alert to be edited
    String editTitle, editDescription, editImage;

    //image picked will be saved in this uri
    Uri mImageUri =null;

    //progress bar
    ProgressDialog pd;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alert);


        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Add New Alert");
        //enable back button in action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //init views
        titleEt = findViewById(R.id.aTitle);
        descriptionEt = findViewById(R.id.aDescription);
        imageIv = findViewById(R.id.aImageIv);
        uploadBtn = findViewById(R.id.aUploadBtn);

        pd = new ProgressDialog(this);


        //get data through intent from previous activitie's adapter
        Intent intent= getIntent();
        final String isUpdateKey= ""+ intent.getStringExtra("key");
        final String editAlertId= ""+ intent.getStringExtra("editAlertId");
        //validate if we came here to update alert i.e came from AdapterAlert
        if (isUpdateKey.equals("editAlert")){
            //update
            actionBar.setTitle("Update Alert");
            uploadBtn.setText("Update");
            loadAlertData(editAlertId);

        }else {
            actionBar.setTitle("Add new Alert");
            uploadBtn.setText("Upload");

        }

        //get some info of current user to include in alert
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //checking until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    email = "" + ds.child("email").getValue();
                    name = "" + ds.child("name").getValue();
                    dp = "" + ds.child("image").getValue();
                    slat="" + ds.child("lat").getValue();
                    slong="" +ds.child("long").getValue();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(AddAlertActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();

            }
        });

        //Upload button click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data(title, description from EditTexts
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddAlertActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddAlertActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                }

                else if (isUpdateKey.equals("editAlert")) {
                    beginUpdate(title, description, editAlertId);

                } else {
                    uploadData(title, description);

                }


            }
        });

    }

    private void beginUpdate(String title, String description, String editAlertId) {
        pd.setMessage("Updating Alert...");
        pd.show();
            //with now image
            UpdateWithImage(title,description,editAlertId);

    }



    private void UpdateWithImage(final String title, final String description, final String editAlertId) {
        //alert is with image, delete previous image first
        StorageReference mPictureRef=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, upload new image
                        //for alert-image name, alert-id,publish-time
                        String timeStamp= String.valueOf(System.currentTimeMillis());
                        String filePathAndName= "Alerts/"+ "alert_"+timeStamp;
                        //get image from imageView
                        Bitmap bitmap =((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos= new ByteArrayOutputStream();
                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data= baos.toByteArray();

                        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                      //image uploaded gets its url
                                      Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                                      while (!uriTask.isSuccessful());

                                      String downloadUri= uriTask.getResult().toString();
                                      if (uriTask.isSuccessful()){
                                          //url is received, upload to firebase database

                                          HashMap<String, Object> hashMap= new HashMap<>();
                                          hashMap.put("uid",uid);
                                          hashMap.put("uName",name);
                                          hashMap.put("uEmail",email);
                                          hashMap.put("uDp",dp);
                                          hashMap.put("aTitle",title);
                                          hashMap.put("aDescr",description);
                                          hashMap.put("aImage",downloadUri);

                                          DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Alerts");
                                          ref.child(editAlertId)
                                                  .updateChildren(hashMap)
                                                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                      @Override
                                                      public void onSuccess(Void aVoid) {
                                                          pd.dismiss();
                                                          Toast.makeText(AddAlertActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                                          finish();

                                                      }
                                                  })
                                                  .addOnFailureListener(new OnFailureListener() {
                                                      @Override
                                                      public void onFailure(@NonNull Exception e) {
                                                          pd.dismiss();
                                                          Toast.makeText(AddAlertActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                                      }
                                                  });

                                      }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //image not uploaded
                                        pd.dismiss();
                                        Toast.makeText(AddAlertActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddAlertActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadAlertData(String editAlertId) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Alerts");
        //get detail of alert using id of alert
        Query fqeury= reference.orderByChild("aId").equalTo(editAlertId);
        fqeury.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //get data
                    editTitle="" + ds.child("aTitle").getValue();
                    editDescription="" + ds.child("aDescr").getValue();
                    editImage="" + ds.child("aImage").getValue();

                    //set data to views
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //set image
                    if (!editImage.equals("noImage")){
                        try{
                            Picasso.get().load(editImage).into(imageIv);

                        } catch (Exception e){

                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadData(final String title, final String description) {
        if (mImageUri==null ){
            Toast.makeText(AddAlertActivity.this, "Image Required...", Toast.LENGTH_SHORT).show();
        } else {


            pd.setMessage("Publishing alert...");
            pd.show();

            //for alert-image, name, alert-id, alert-publish-time
            final String timeStamp = String.valueOf(System.currentTimeMillis());

            String filePathAndName = "Alerts/" + "alert_" + timeStamp;

            //get image from imageView
            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Image is uploaded to firebase storage, now get its url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                //url is received to firebase database

                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put alert info
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("aId", timeStamp);
                                hashMap.put("aTitle", title);
                                hashMap.put("aDescr", description);
                                hashMap.put("aImage", downloadUri);
                                hashMap.put("uLat", slat);
                                hashMap.put("uLong", slong);
                                hashMap.put("aTime", timeStamp);

                                //path to store alert data
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in database
                                                pd.dismiss();
                                                Toast.makeText(AddAlertActivity.this, "Alert published", Toast.LENGTH_SHORT).show();
                                                //reset views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding alert in database
                                                pd.dismiss();
                                                Toast.makeText(AddAlertActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }
                        }

                    })
                    .addOnFailureListener(e -> {
                        //failed uploading image
                        Toast.makeText(AddAlertActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    });
        }
        }



    //passing selected Image to ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            assert data != null;
            mImageUri =data.getData();
                    // Use Uri object instead of File to avoid storage permissions
                    imageIv.setImageURI(mImageUri);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        //hide this icons in this activity
        menu.findItem(R.id.add_alert).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logOut) {
            logout();
        }
        return (super.onOptionsItemSelected(item));
    }

    //Logout method
    private void logout() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddAlertActivity.this);

        alertDialog.setTitle("Log Out");
        alertDialog.setIcon(R.drawable.ic_logout_black); //set icon

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to Log out?");
        alertDialog.setCancelable(false);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                startActivity(new Intent(AddAlertActivity.this, MainActivity.class));
                Toast.makeText(AddAlertActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                finish();
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

}
