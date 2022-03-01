package com.example.mizizi;

import static androidx.core.view.MenuItemCompat.getActionView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mizizi.adapters.AdapterAlert;
import com.example.mizizi.authentication.LoginActivity;
import com.example.mizizi.models.ModelAlert;
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

public class ThereProfileActivity extends AppCompatActivity {

    //views
    ImageView profileIv;
    TextView emailTv, nameTv, phoneTv, addressTv;

    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;

    List<ModelAlert> myAlertList;
    AdapterAlert myAdapterAlert;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        ActionBar actionBar= getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Init views
        profileIv = findViewById(R.id.profile);
        emailTv = findViewById(R.id.user_mail);
        nameTv = findViewById(R.id.user_name);
        phoneTv = findViewById(R.id.user_phone);
        addressTv = findViewById(R.id.user_address);
        recyclerView = findViewById(R.id.myAlertsRecyclerView);
        firebaseAuth = FirebaseAuth.getInstance();

        //get uid of clicked user to retrieve his alerts
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
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


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

        myAlertList=new ArrayList<>();

        checkUserStatus();
        loadHistAlerts();

    }

    private void loadHistAlerts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show   newest alert first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);


        //path of all alerts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        //query to load alerts from user specific uid created when an alert is added by a user
        Query query = ref.orderByChild("uid").equalTo(uid);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //incase of error
                //Toast.makeText(ProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchHistAlerts(final String searchQuery){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show   newest alert first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);


        //path of all alerts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        //query to load alerts from user specific uid created when an alert is added by a user
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myAlertList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAlert myAlerts = ds.getValue(ModelAlert.class);
                    assert myAlerts != null;
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


    private void checkUserStatus() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }  //user is signed in stay here

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.profile_menu, menu);

        //hide add alert con from this activity
        menu.findItem(R.id.add_alert).setVisible(false);

        //searchview to search alerts by alert title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search icon
                if (!TextUtils.isEmpty(s)) {
                    searchHistAlerts(s);
                } else {
                    loadHistAlerts();
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchHistAlerts(s);
                } else {
                    loadHistAlerts();
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logOut) {
            logout();
        }
        return (super.onOptionsItemSelected(item));
    }

    //Logout method
    private void logout() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Log Out");
        alertDialog.setIcon(R.drawable.ic_logout_black); //set icon

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to Log out?");
        alertDialog.setCancelable(false);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", (dialog, which) -> {
            firebaseAuth.signOut();
            startActivity(new Intent(ThereProfileActivity.this, MainActivity.class));
            Toast.makeText(ThereProfileActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
            finish();
        });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", (dialog, which) -> dialog.cancel());

        // Showing Alert Message
        alertDialog.show();


    }
}
