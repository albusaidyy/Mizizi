package com.example.mizizi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class LearnActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Learn More");
        setContentView(R.layout.activity_learn);
        LinearLayout more = findViewById(R.id.mMore);
        LinearLayout mRem = findViewById(R.id.rem);
        Button mgo = findViewById(R.id.mGo);
        TextView mRemedies = findViewById(R.id.TvRem);

        TextView result = findViewById(R.id.mresults);
        ImageView imageView = findViewById(R.id.mImageView);


        Intent intent = getIntent();
        String mResult = intent.getStringExtra("key");
        Bitmap bitmap = intent.getParcelableExtra("image");
        if (mResult != null) {
            mRem.setVisibility(View.VISIBLE);
            result.setText(mResult);
            imageView.setImageBitmap(bitmap);

            switch (mResult) {
                case "apple apple scab":
                    mRemedies.setText(getResources().getString(R.string.apple_apple_scab));
                    break;


                case "apple black rot":
                    mRemedies.setText(getResources().getString(R.string.apple_black_rot));
                    break;

                case "apple cedar apple rust":
                    mRemedies.setText(getResources().getString(R.string.apple_cedar_apple_rust));
                    break;

                case "apple healthy":
                    mRemedies.setText(getResources().getString(R.string.apple_healthy));
                    break;

                case "blueberry healthy":
                    mRemedies.setText(getResources().getString(R.string.blueberry_healthy));
                    break;

                case "cherry including sour powdery mildew":
                    mRemedies.setText(getResources().getString(R.string.cherry_including_sour_powdery_mildew));
                    break;

                case "cherry including sour healthy":
                    mRemedies.setText(getResources().getString(R.string.cherry_including_sour_healthy));
                    break;

                case "corn maize cercospora leaf spot gray leaf spot":
                    mRemedies.setText(getResources().getString(R.string.corn_maize_cercospora_leaf_spot_gray_leaf_spot));
                    break;

                case "corn maize common rust":
                    mRemedies.setText(getResources().getString(R.string.corn_maize_common_rust));
                    break;

                case "corn maize northern leaf blight":
                    mRemedies.setText(getResources().getString(R.string.corn_maize_northern_leaf_blight));
                    break;

                case "corn maize healthy":
                    mRemedies.setText(getResources().getString(R.string.corn_maize_healthy));
                    break;

                case "grape black rot":
                    mRemedies.setText(getResources().getString(R.string.grape_black_rot));
                    break;

                case "grape esca black measles":
                    mRemedies.setText(getResources().getString(R.string.grape_esca_black_measles));
                    break;

                case "grape leaf blight isariopsis leaf spot":
                    mRemedies.setText(getResources().getString(R.string.grape_leaf_blight_isariopsis_leaf_spot));
                    break;

                case "grape healthy":
                    mRemedies.setText(getResources().getString(R.string.grape_healthy));

                case "orange haunglongbing citrus greening":
                    mRemedies.setText(getResources().getString(R.string.orange_haunglongbing_citrus_greening));
                    break;

                case "peach bacterial spot":
                    mRemedies.setText(getResources().getString(R.string.peach_bacterial_spot));
                    break;

                case "peach healthy":
                    mRemedies.setText(getResources().getString(R.string.peach_healthy));
                    break;

                case "pepper bell bacterial spot":
                    mRemedies.setText(getResources().getString(R.string.pepper_bell_bacterial_spot));
                    break;

                case "pepper bell healthy":
                    mRemedies.setText(getResources().getString(R.string.pepper_bell_healthy));
                    break;

                case "potato early blight":
                    mRemedies.setText(getResources().getString(R.string.potato_early_blight));
                    break;

                case "potato late blight":
                    mRemedies.setText(getResources().getString(R.string.potato_late_blight));
                    break;

                case "potato healthy":
                    mRemedies.setText(getResources().getString(R.string.potato_healthy));
                    break;

                case "raspberry healthy":
                    mRemedies.setText(getResources().getString(R.string.raspberry_healthy));
                    break;

                case "soybean healthy":
                    mRemedies.setText(getResources().getString(R.string.soybean_healthy));
                    break;

                case "squash powdery mildew":
                    mRemedies.setText(getResources().getString(R.string.squash_powdery_mildew));
                    break;

                case "strawberry leaf scorch":
                    mRemedies.setText(getResources().getString(R.string.strawberry_leaf_scorch));
                    break;

                case "strawberry healthy":
                    mRemedies.setText(getResources().getString(R.string.strawberry_healthy));
                    break;

                case "tomato bacterial spot":
                    mRemedies.setText(getResources().getString(R.string.tomato_bacterial_spot));
                    break;

                case "tomato early blight":
                    mRemedies.setText(getResources().getString(R.string.tomato_early_blight));
                    break;

                case "tomato late blight":
                    mRemedies.setText(getResources().getString(R.string.tomato_late_blight));
                    break;

                case "tomato leaf mold":
                    mRemedies.setText(getResources().getString(R.string.tomato_leaf_mold));
                    break;

                case "tomato septoria leaf spot":
                    mRemedies.setText(getResources().getString(R.string.tomato_septoria_leaf_spot));
                    break;

                case "tomato spider mites two spotted spider mite":
                    mRemedies.setText(getResources().getString(R.string.tomato_spider_mites_two_spotted_spider_mite));
                    break;

                case "tomato target spot":
                    mRemedies.setText(getResources().getString(R.string.tomato_target_spot));
                    break;

                case "tomato yellow leaf curl virus":
                    mRemedies.setText(getResources().getString(R.string.tomato_yellow_leaf_curl_virus));
                    break;

                case "tomato mosaic virus":
                    mRemedies.setText(getResources().getString(R.string.tomato_mosaic_virus));
                    break;

                case "tomato healthy":
                    mRemedies.setText(getResources().getString(R.string.tomato_healthy));
                    break;

                case "Unidentified":
                    mRemedies.setText(getResources().getString(R.string.Unidentified));
                    break;

                default:
                    break;

            }


        } else {
            more.setVisibility(View.VISIBLE);


        }


        mgo.setOnClickListener(v -> {
            startActivity(new Intent(LearnActivity.this, ProcessActivity.class));
            finish();
        });


    }

}
