package com.example.mizizi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_process.*
import java.io.IOException

class ProcessActivity : AppCompatActivity() {

    //if user has not logged in
    override fun onStart() {
        super.onStart()
        requestPermissions()
    }


    /**
     * Requesting multiple permissions (storage and location) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private fun requestPermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    report.areAllPermissionsGranted()

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@ProcessActivity)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton(
            "GOTO SETTINGS"
        ) { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }
        builder.show()
    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }


    private lateinit var mClassifier: Classifier
    private lateinit var mBitmap: Bitmap

    private val mCameraRequestCode = 1
    private val mGalleryRequestCode = 2

    private val mInputSize = 200 //224
    private val mModelPath = "model.tflite"
    private val mLabelPath = "labels.txt"
    private val mSamplePath = "soybean.JPG"


    @SuppressLint("SetTextI18n")
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



        }

        mGalleryButton.setOnClickListener {
            val callGalleryIntent = Intent(Intent.ACTION_PICK)
            callGalleryIntent.type = "image/*"
            startActivityForResult(callGalleryIntent, mGalleryRequestCode)

        }
        mDetectButton.setOnClickListener {
            val results = mClassifier.recognizeImage(scaleImage(mBitmap)).firstOrNull()
            mConfidenceTextView.visibility = View.VISIBLE

            if (results?.confidence!! >= 0.7) {
                mResultTextView.text = results?.title
                mConfidenceTextView.text = " Confidence:" + results?.confidence
                mDetectButton.visibility = View.GONE
                mInfo.visibility = View.VISIBLE


            } else {
                //mResultTextView.text = results?.title
                mResultTextView.visibility=View.GONE
                mConfidenceTextView.text = "Upload a clear image for better results."
                mDetectButton.visibility = View.GONE
                Toast.makeText(this, "Please Try Again..", Toast.LENGTH_LONG)
                    .show()
            }

        }
        mInfo.setOnClickListener {
            val intent = Intent(this, LearnActivity::class.java)
            val mResult = mResultTextView.text.toString()
            intent.putExtra("key", mResult)
            intent.putExtra("image", mBitmap)
            startActivity(intent)

        }

    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCameraRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = data.extras!!.get("data") as Bitmap
                mBitmap = scaleImage(mBitmap)
                val toast = Toast.makeText(
                    this,
                    ("Image crop to: w= ${mBitmap.width} h= ${mBitmap.height}"),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.BOTTOM, 0, 20)
                toast.show()
                val mPhotoImageView: ImageView = findViewById(R.id.mPhotoImageView)
                val mResultTextView: TextView = findViewById(R.id.mResultTextView)
                mPhotoImageView.setImageBitmap(mBitmap)
                mDetectButton.visibility = View.VISIBLE
                mInfo.visibility = View.GONE
                mConfidenceTextView.text = ""
                mResultTextView.text = "Your photo image set now."
            } else {
                Toast.makeText(this, "Camera cancel..", Toast.LENGTH_LONG).show()


            }
        } else if (requestCode == mGalleryRequestCode) {
            if (data != null) {
                val uri = data.data
                try {
                    uri?.let {
                        mBitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(
                                this.contentResolver, uri
                            )
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

                mBitmap = scaleImage(mBitmap)
                mDetectButton.visibility = View.VISIBLE
                mInfo.visibility = View.GONE
                //mResultTextView.text = "Results to show up here"
                mConfidenceTextView.text = ""
                mResultTextView.text = "Your photo image set now."
                mDetectButton.visibility = View.VISIBLE
                mPhotoImageView.setImageBitmap(mBitmap)

            }
        } else {
            Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show()

        }
    }


    private fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }

}