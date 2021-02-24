package com.example.mizizi.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mizizi.AlertActivity;
import com.example.mizizi.MainActivity;
import com.example.mizizi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdpterUser adpterUser;
    List<ModelUser> userList;
    ProgressBar pb;


    private FirebaseAuth mAuth;
    private DatabaseReference userCountRef,alertCountRef;
    private TextView nUsers, nAlerts;
    private int countUsers=0;
    private int countAlerts=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Users");


        mAuth = FirebaseAuth.getInstance();
        nUsers=findViewById(R.id.users);
        nAlerts=findViewById(R.id.alerts);
        pb=findViewById(R.id.loading_users_pb);

        userCountRef=FirebaseDatabase.getInstance().getReference().child("Users");
        alertCountRef=FirebaseDatabase.getInstance().getReference().child("Alerts");

        userCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countUsers=(int) dataSnapshot.getChildrenCount();
                   int ncountUser=countUsers-1;
                    nUsers.setText(Integer.toString(ncountUser));

                }else {
                    nUsers.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        alertCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countAlerts=(int) dataSnapshot.getChildrenCount();
                    nAlerts.setText(Integer.toString(countAlerts));

                }else {
                    nAlerts.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //init user list
        userList = new ArrayList<>();

        //getAll users
        pb.setVisibility(View.VISIBLE);
       getAllUsers();

    }

    private void getAllUsers() {

        recyclerView = findViewById(R.id.users_recyclerview);
        //set it's properties
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //get current users
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all user except currently signed in user
                    if (!modelUser.getUid().equals(fuser.getUid())) {
                        userList.add(modelUser);

                        //adapter
                        adpterUser = new AdpterUser(getApplicationContext(), userList);
                        //set adapter to recycler view
                        recyclerView.setAdapter(adpterUser);
                        pb.setVisibility(View.GONE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchMyAlerts(final String searchQuery) {

        //get current users
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all user except currently signed in user
                    if (!modelUser.getUid().equals(fuser.getUid())) {

                        if (modelUser.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(searchQuery.toLowerCase())){
                            userList.add(modelUser);
                        }

                        //adapter
                        adpterUser = new AdpterUser(getApplicationContext(), userList);
                        //refresh adapter
                        adpterUser.notifyDataSetChanged();
                        //set adapter to recycler view
                        recyclerView.setAdapter(adpterUser);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);

        //searchview to search alerts by alert title/description
        MenuItem item = menu.findItem(R.id.admin_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search icon
                if (!TextUtils.isEmpty(s)) {
                    searchMyAlerts(s);
                } else {
                    getAllUsers();
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchMyAlerts(s);
                } else {
                    getAllUsers();
                }
                return false;
            }
        });
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.admin_home:
                startActivity(new Intent(AdminActivity.this, AdminActivity.class));
                return true;

            case R.id.admin_alerts:
                startActivity(new Intent(AdminActivity.this, AlertActivity.class));
                return true;


            case R.id.admin_log_out:
                mAuth.signOut();
                startActivity(new Intent(AdminActivity.this, MainActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
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
