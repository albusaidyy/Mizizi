package com.example.mizizi.admin;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mizizi.ProfileActivity;
import com.example.mizizi.R;
import com.example.mizizi.ThereProfileActivity;
import com.example.mizizi.profile.EditProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdpterUser extends RecyclerView.Adapter<AdpterUser.MyHolder> {

    Context context;
    List<ModelUser> userList;

    public AdpterUser(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout (row_user.xml)
        View view= LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        String userImage= userList.get(i).getImage();
        String userName= userList.get(i).getName();
        final String userEmail= userList.get(i).getEmail();
        final String uid= userList.get(i).getUid();

        //set data
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.userprofile)
                    .into(myHolder.mAvatarIv);

        }
        catch (Exception e){

        }
        myHolder.btn_EditProfile.setEnabled(true);

//handle  item click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*click to go to ThereProfileActivity withuid, this uid is of clicked user
                 *which will be used to show user specific data/alerts*/
                Intent intent= new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

        myHolder.btn_EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Edit post is clicked
                //Open activity to edit post
                Intent intent = new Intent(context, EditProfileActivity.class);
                intent.putExtra("key", "editUser");
                intent.putExtra("userId",uid);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
class MyHolder extends RecyclerView.ViewHolder{

    ImageView mAvatarIv;
    TextView mNameTv, mEmailTv;
        Button btn_EditProfile;



        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv= itemView.findViewById(R.id.avatarIv);
            mNameTv= itemView.findViewById(R.id.nameTv);
            mEmailTv= itemView.findViewById(R.id.emailTv);
            btn_EditProfile = itemView.findViewById(R.id.BtEditProfile);

        }
    }


}
