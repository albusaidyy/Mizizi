package com.example.mizizi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mizizi.adapters.AdapterAlert;
import com.example.mizizi.authentication.LoginActivity;
import com.example.mizizi.models.ModelAlert;
import com.example.mizizi.profile.EditProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //views
    ImageView profileIv;
    TextView emailTv, nameTv, phoneTv, addressTv;
    Button btn_EditProfile;
    TextView nAlerts;

    String uId;
    String uEmail;

    //recyclerview
    RecyclerView recyclerView;
    List<ModelAlert> myAlertList;
    AdapterAlert myAdapterAlert;


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
            uEmail = user.getEmail();
        } else {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            Toast.makeText(this, "You need to log in to view Profile", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        setContentView(R.layout.activity_profile);


        //init of firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = firebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        checkUserStatus();


        //Init views
        profileIv = findViewById(R.id.profile);
        emailTv = findViewById(R.id.user_mail);
        nameTv = findViewById(R.id.user_name);
        phoneTv = findViewById(R.id.user_phone);
        addressTv = findViewById(R.id.user_address);
        btn_EditProfile = findViewById(R.id.BtEditProfile);
        nAlerts=findViewById(R.id.noAlerts);
        recyclerView = findViewById(R.id.myAlertsRecyclerView);


        //Getting info of currently signed in user. Using user's email or uid.
        /*By using orderByChild query to show the detail from a node
        whose key named email has a value to currently signed in email.
        It will search all nodes, where the key matches and pull the details*/
            Query query = databaseReference.orderByChild("email").equalTo(uEmail);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //checking until required data get
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //get data
                        String email = "" + ds.child("email").getValue();
                        String name = "" + ds.child("name").getValue();
                        String phone = "" + ds.child("phone").getValue();
                        String address = "" + ds.child("address").getValue();
                        String image = "" + ds.child("image").getValue();

                        //Set data
                        emailTv.setText(email);
                        nameTv.setText(name);
                        phoneTv.setText(phone);
                        addressTv.setText(address);
                        try {
                            //if Image is received
                            Picasso.get().load(image).into(profileIv);
                        } catch (Exception e) {
                            //if there is any exception while getting image
                            Picasso.get().load(R.drawable.userprofile).into(profileIv);
                        }
                        btn_EditProfile.setEnabled(true);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    btn_EditProfile.setEnabled(false);


                }
            });





        //init alert list
        myAlertList = new ArrayList<>();
        if (user != null) {
            loadMyAlerts();
        }



        btn_EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });

    }


    private void loadMyAlerts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show   newest alert first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);


        //path of all alerts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        //query to load alerts from user specific uid created when an alert is added by a user
        Query query = ref.orderByChild("uid").equalTo(uId);
            //get all data from this ref
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    myAlertList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ModelAlert myAlerts = ds.getValue(ModelAlert.class);

                        myAlertList.add(myAlerts);

                        //adapter
                        myAdapterAlert = new AdapterAlert(getApplicationContext(), myAlertList);
                        //set adapter to recyclerview
                        recyclerView.setAdapter(myAdapterAlert);
                        if (myAdapterAlert.getItemCount()==0){
                            recyclerView.setVisibility(View.GONE);
                            nAlerts.setVisibility(View.VISIBLE);
                        }else{
                            nAlerts.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    //incase of error
                    //Toast.makeText(ProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


    }



    private void searchMyAlerts(final String searchQuery) {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show   newest alert first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);


        //path of all alerts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        //query to load alerts from user specific uid created when an alert is added by a user
        Query query = ref.orderByChild("uid").equalTo(uId);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myAlertList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAlert myAlerts = ds.getValue(ModelAlert.class);
                    if (myAlerts.getaTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    myAlerts.getaDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        myAlertList.add(myAlerts);
                    }

                    //adapter
                    myAdapterAlert = new AdapterAlert(getApplicationContext(), myAlertList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(myAdapterAlert);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //incase of error
                //Toast.makeText(ProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        //hide add alert con from this activity
        menu.findItem(R.id.add_alert).setVisible(false);

        //searchview to search alerts by alert title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search icon
                if (!TextUtils.isEmpty(s)) {
                    searchMyAlerts(s);
                } else {
                    loadMyAlerts();
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchMyAlerts(s);
                } else {
                    loadMyAlerts();
                }
                return false;
            }
        });


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOut:
                logout();
                break;

            default:
                break;


        }
        return (super.onOptionsItemSelected(item));
    }

    //Logout method
    private void logout() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);

        alertDialog.setTitle("Log Out");
        alertDialog.setIcon(R.drawable.ic_logout_black); //set icon

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to Log out?");
        alertDialog.setCancelable(false);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                Toast.makeText(ProfileActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
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



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous Activity
        finish();
        return super.onSupportNavigateUp();
    }

}
