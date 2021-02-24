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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AlertActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;

    //recyclerview
    RecyclerView recyclerView;
    List<ModelAlert> alertList;
    AdapterAlert adapterAlert;
    ProgressBar pb;


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
            pb.setVisibility(View.VISIBLE);
            loadAlerts();
        } else {
            startActivity(new Intent(AlertActivity.this, LoginActivity.class));
            Toast.makeText(this, "You need to log in to view Alerts", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Alerts");
        setContentView(R.layout.activity_alert);

        //init of firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = firebaseDatabase.getInstance();
        pb=findViewById(R.id.loading_alerts_pb);

        //recyclerview and its properties
        recyclerView = findViewById(R.id.alertsRecyclerView);





    }

    private void loadAlerts() {
        //init alert list
        alertList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show   newest alert first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);
        //path of all alerts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                alertList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAlert modelAlert = ds.getValue(ModelAlert.class);

                    alertList.add(modelAlert);
                    //adapter
                    adapterAlert = new AdapterAlert(getApplicationContext(), alertList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterAlert);
                    pb.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //incase of error
                pb.setVisibility(View.GONE);
                //Toast.makeText(AlertActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAlerts(final String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show   newest alert first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);
        //path of all alerts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alerts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                alertList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAlert modelAlert = ds.getValue(ModelAlert.class);

                    if (modelAlert.getaTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelAlert.getaDescr().toLowerCase().contains(searchQuery.toLowerCase()))
                        alertList.add(modelAlert);
                    //adapter
                    adapterAlert = new AdapterAlert(getApplicationContext(), alertList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterAlert);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //incase of error
                Toast.makeText(AlertActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);

        //searchview to search alerts by alert title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search icon
                if (!TextUtils.isEmpty(s)) {
                    searchAlerts(s);
                } else {
                    loadAlerts();
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchAlerts(s);
                } else {
                    loadAlerts();
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

            case R.id.add_alert:
                startActivity(new Intent(AlertActivity.this, AddAlertActivity.class));
                break;

            default:
                break;


        }
        return (super.onOptionsItemSelected(item));
    }

    //Logout method
    private void logout() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AlertActivity.this);

        alertDialog.setTitle("Log Out");
        alertDialog.setIcon(R.drawable.ic_logout_black); //set icon

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to Log out?");
        alertDialog.setCancelable(false);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                startActivity(new Intent(AlertActivity.this, MainActivity.class));
                Toast.makeText(AlertActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
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
