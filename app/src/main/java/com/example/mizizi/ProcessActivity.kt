package com.example.mizizi

import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_process.*
import java.io.IOException

class ProcessActivity : AppCompatActivity() {
    private lateinit var mClassifier: Classifier
    private lateinit var mBitmap: Bitmap

    private val mCameraRequestCode = 1
    private val mGalleryRequestCode = 2

    private val mInputSize = 200 //224
    private val mModelPath = "model.tflite"
    private val mLabelPath = "labels.txt"
    private val mSamplePath = "soybean.JPG"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)
        mClassifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)

        resources.assets.open(mSamplePath).use {
            mBitmap = BitmapFactory.decodeStream(it)
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true)
            mPhotoImageView.setImageBitmap(mBitmap)
        }

        mCameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(callCameraIntent, mCameraRequestCode)
            mDetectButton.visibility=View.VISIBLE
            mInfo.visibility = View.GONE
            mResultTextView.text="Result"
            mConfidenceTextView.text=""



        }

        mGalleryButton.setOnClickListener {
            val callGalleryIntent = Intent(Intent.ACTION_PICK)
            callGalleryIntent.type = "image/*"
            startActivityForResult(callGalleryIntent, mGalleryRequestCode)
            mDetectButton.visibility=View.VISIBLE
            mInfo.visibility = View.GONE
            mResultTextView.text="Result"
            mConfidenceTextView.text=""

        }
        mDetectButton.setOnClickListener {
            val results = mClassifier.recognizeImage(scaleImage(mBitmap)).firstOrNull()
            mResultTextView.text = results?.title
            mConfidenceTextView.visibility=View.VISIBLE
            mConfidenceTextView.text=" Confidence:" + results?.confidence
            mDetectButton.visibility=View.GONE
            if (results?.confidence!! >= 0.7) {
                mInfo.visibility = View.VISIBLE


            } else {
                Toast.makeText(this, "Upload a clear image for better results..", Toast.LENGTH_LONG).show()
            }

        }
        mInfo.setOnClickListener {
            val intent= Intent(this,LearnActivity::class.java)
            val mresult=mResultTextView.text.toString();
            intent.putExtra("key",mresult)
            intent.putExtra("image",mBitmap)
            startActivity(intent)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCameraRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = data.extras!!.get("data") as Bitmap
                mBitmap = scaleImage(mBitmap)
                val toast = Toast.makeText(this, ("Image crop to: w= ${mBitmap.width} h= ${mBitmap.height}"), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.BOTTOM, 0, 20)
                toast.show()
                mPhotoImageView.setImageBitmap(mBitmap)
                mResultTextView.text = "Your photo image set now."
            } else {
                Toast.makeText(this, "Camera cancel..", Toast.LENGTH_LONG).show()


            }
        } else if (requestCode == mGalleryRequestCode) {
            if (data != null) {
                val uri = data.data
                try {
                    uri?.let {
                        if (Build.VERSION.SDK_INT < 28) {
                            mBitmap = MediaStore.Images.Media.getBitmap(
                                    this.contentResolver, uri)
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, uri)
                            mBitmap = ImageDecoder.decodeBitmap(source)
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

                println("Success!!!")
                mBitmap = scaleImage(mBitmap)
                mDetectButton.visibility=View.VISIBLE
                mPhotoImageView.setImageBitmap(mBitmap)

            }
        } else {
            Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show()

        }
    }


    fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }

}