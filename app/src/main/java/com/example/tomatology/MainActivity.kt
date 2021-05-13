package com.example.tomatology

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val STORAGE_PERMISSION_CODE = 3
        private const val STORAGE_REQUEST_CODE = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnBrowse = findViewById<Button>(R.id.btn_browse)
        val btnArchive = findViewById<Button>(R.id.btn_archive)
        val btnGuide = findViewById<Button>(R.id.btn_guide)

        btnCamera.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

        btnBrowse.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ){
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, STORAGE_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }

        btnArchive.setOnClickListener {
            val intent = Intent(this, ArchiveActivity::class.java)
            startActivity(intent)
        }

        btnGuide.setOnClickListener {
            val intent = Intent(this, GuideActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }else{
                Toast.makeText(
                    this,
                    "You denied the permission for camera. Go to settings to allow it.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, STORAGE_REQUEST_CODE)
            }else{
                Toast.makeText(
                    this,
                    "You denied the permission for storage access. Go to settings to allow it.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            val intent = Intent(this, ResultActivity::class.java)
            if(requestCode == CAMERA_REQUEST_CODE){
                val thumbnail= data!!.extras!!.get("data") as Bitmap
                intent.putExtra("picture", thumbnail)
                startActivity(intent)
                // viewResult.setImageBitmap(thumbnail)
            }
            if(requestCode == STORAGE_REQUEST_CODE && data != null){
//                    val uri= data?.data
//                    val source =
//                    ImageDecoder.createSource(this.contentResolver, uri!!)
//                    val thumbnail= ImageDecoder.decodeBitmap(source)
                val selectedPhotoUri = data.data
                try {
                    selectedPhotoUri?.let {
//                            val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
//                            var thumbnail = ImageDecoder.decodeBitmap(source)
//                            thumbnail = thumbnail.copy(Bitmap.Config.ARGB_8888, true)
                            val thumbnail = getThumbnail(selectedPhotoUri)
                            intent.putExtra("picture", thumbnail)
                            startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getThumbnail(uri: Uri?): Bitmap? {
        var input: InputStream? = this.contentResolver.openInputStream(uri!!)
        val onlyBoundsOptions = BitmapFactory.Options()
        onlyBoundsOptions.inJustDecodeBounds = true
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
        input?.close()
        if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
            return null
        }
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
        input = this.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
        input?.close()
        return bitmap
    }
}