package com.example.mizizi.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizizi.AddAlertActivity;
import com.example.mizizi.R;
import com.example.mizizi.ThereProfileActivity;
import com.example.mizizi.models.ModelAlert;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterAlert extends RecyclerView.Adapter<AdapterAlert.MyHolder> {

    private Context context;
    private List<ModelAlert> alertList;

    private String myUid;


    public AdapterAlert(Context context, List<ModelAlert> alertList) {
        this.context = context;
        this.alertList = alertList;
        myUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_alert.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_alerts, viewGroup, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, int i) {
        //get data
        final String uid = alertList.get(i).getUid();
        String uEmail = alertList.get(i).getuEmail();
        String uName = alertList.get(i).getuName();
        String uDp = alertList.get(i).getuDp();
        final String aId = alertList.get(i).getaId();
        final String aTitle = alertList.get(i).getaTitle();
        final String aDescription = alertList.get(i).getaDescr();
        final String aImage = alertList.get(i).getaImage();
        String sLat = alertList.get(i).getuLat();
        String sLong = alertList.get(i).getuLong();
        String aTimeStamp = alertList.get(i).getaTime();
        double ulat = Double.parseDouble(sLat);
        double ulong = Double.parseDouble(sLong);


        //convert timestamp to dd//mm//yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(aTimeStamp));
        String aTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //set data
        myHolder.uNameTv.setText(uName);
        myHolder.aTimeTv.setText(aTime);
        myHolder.aTitleTv.setText(aTitle);
        myHolder.aDescriptionTv.setText(aDescription);
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(ulat, ulong, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            myHolder.aAddressTv.setText(address);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Picasso.get().load(uDp).placeholder(R.drawable.userprofile).into(myHolder.uPictureIv);


        } catch (Exception e) {

        }

        //set alert image
        //if there is no image i.e aImage.equals("noImage")
        if (aImage.equals("noImage")) {
            //hide imageView
            myHolder.aImageIv.setVisibility(View.GONE);
        } else {
            //show imageView
            myHolder.aImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(aImage).into(myHolder.aImageIv);


            } catch (Exception e) {

            }

        }


        //handle button clicks
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to be implemented
                showMoreOptions(myHolder.moreBtn, uid, myUid, aId, aImage);
            }
        });

        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getImage fromImageView
                BitmapDrawable bitmapDrawable = (BitmapDrawable) myHolder.aImageIv.getDrawable();
                //convert image to Bitmap
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImageAndText(aTitle, aDescription, bitmap);

            }
        });

        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*click to go to ThereProfileActivity withuid, this uid is of clicked user
                 *which will be used to show user specific data/alerts*/
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });


    }


    private void shareImageAndText(String aTitle, String aDescription, Bitmap bitmap) {
        //concatenate title and description to share
        String shareBody = aTitle + "\n" + aDescription;

        //first we will save image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);//text to share
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");//in case you share via an email
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via")); //message to show share dialog
    }

    private Uri saveImageToShare(Bitmap bitmap) {


        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdir(); //creates if not exists
            File file = new File(imageFolder, "share_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.mizizi.fileprovider",
                    file);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;

    }


    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String aId, final String aImage) {
        //Creating popup menu currently having options Delete
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only alert(s) of currently signed in user or admin

        String AUid = "RkOrZoxtV6bbOMFgqoxR023Y6y22";
        if (uid.equals(myUid) || (myUid.equals(AUid))) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");

        }

        //add items in menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menitem) {
                int id = menitem.getItemId();
                if (id == 0) {
                    //delete is clicked
                    beginDelete(aId, aImage);
                } else if (id == 1) {
                    //Edit post is clicked
                    //Open activity to edit post
                    Intent intent = new Intent(context, AddAlertActivity.class);
                    intent.putExtra("key", "editAlert");
                    intent.putExtra("editAlertId", aId);
                    context.startActivity(intent);

                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String aId, String aImage) {
        if (aImage.equals("noImage")) {
            //alert is without image
            deleteWithout(aId);
        } else {
            //alert is with image
            deleteWithImage(aId, aImage);
        }

    }

    private void deleteWithImage(final String aId, String aImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        //1)delete Image using url
        //2) delete from database using alert id
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(aImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //image deleted, now delete database

                Query fquery = FirebaseDatabase.getInstance().getReference("Alerts").orderByChild("aId").equalTo(aId);
                fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();// remove vales from firebase where aId matches

                        }
                        //deleted
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed can't go further
            }
        });
    }

    private void deleteWithout(String aId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Alerts").orderByChild("aId").equalTo(aId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();// remove vales from firebase where aId matches
                }
                //deleted
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //view from row_alert.xml
        ImageView uPictureIv, aImageIv;
        TextView uNameTv, aTimeTv, aTitleTv, aDescriptionTv, aAddressTv;
        ImageButton moreBtn;
        Button shareBtn;
        LinearLayout profileLayout;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            aImageIv = itemView.findViewById(R.id.aImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            aTimeTv = itemView.findViewById(R.id.aTimeTv);
            aTitleTv = itemView.findViewById(R.id.aTitleTv);
            aDescriptionTv = itemView.findViewById(R.id.aDescriptionTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            aAddressTv = itemView.findViewById(R.id.aAdressTv);
            profileLayout = itemView.findViewById(R.id.profile_layout);


        }
    }

}
