package com.example.tomatology

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val STORAGE_PERMISSION_CODE = 3
        private const val STORAGE_REQUEST_CODE = 4
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var firebasePerformance = FirebasePerformance.getInstance()
    private var tomatoDiseaseClassifier = TomatoDiseaseClassifier(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnBrowse = findViewById<Button>(R.id.btn_browse)
        val btnArchive = findViewById<Button>(R.id.btn_archive)
        val btnGuide = findViewById<Button>(R.id.btn_guide)
        val btnSignOut = findViewById<Button>(R.id.btn_sign_out)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth

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

        btnSignOut.setOnClickListener {
            Firebase.auth.signOut()
            showToast("Signed out")
            signIn()
        }

        setupClassifier()
    }

    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser==null){
            signIn()
        }
        else{
            showToast("Welcome "+currentUser.displayName)
        }
        // updateUI(currentUser)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    showToast("Welcome "+user!!.displayName)
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // updateUI(null)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

//    private fun updateUI(user: FirebaseUser?) {
//        if(user != null){
//            val uid = user.uid
//        }
//    }

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
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE){
                val thumbnail= data!!.extras!!.get("data") as Bitmap
                classify(thumbnail)
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
                            classify(thumbnail!!)
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

    private fun setupClassifier() {
        configureRemoteConfig()
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val modelName = remoteConfig.getString("model_name")
                    val downloadTrace = firebasePerformance.newTrace("download_model")
                    downloadTrace.start()
                    downloadModel(modelName)
                        .addOnSuccessListener {
                            downloadTrace.stop()
                        }
                } else {
                    showToast("Failed to fetch model name.")
                }
            }
    }

    private fun configureRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun downloadModel(modelName: String): Task<CustomModel> {
//        val remoteModel = FirebaseCustomRemoteModel.Builder(modelName).build()
//        val firebaseModelManager = FirebaseModelManager.getInstance()
//        return firebaseModelManager
//            .isModelDownloaded(remoteModel)
//            .continueWithTask { task ->
//                // Create update condition if model is already downloaded, otherwise create download
//                // condition.
//                val conditions = if (task.result != null && task.result == true) {
//                    FirebaseModelDownloadConditions.Builder()
//                        .requireWifi()
//                        .build() // Update condition that requires wifi.
//                } else {
//                    FirebaseModelDownloadConditions.Builder().build() // Download condition.
//                }
//                firebaseModelManager.download(remoteModel, conditions)
//            }
//            .addOnSuccessListener {
//                firebaseModelManager.getLatestModelFile(remoteModel)
//                    .addOnCompleteListener {
//                        val model = it.result
//                        if (model == null) {
//                            showToast("Failed to get model file.")
//                        } else {
//                            showToast("Downloaded remote model: $modelName")
//                            tomatoDiseaseClassifier.initialize(model)
//                        }
//                    }
//            }
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
            .build()
        return FirebaseModelDownloader.getInstance()
            .getModel(modelName, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                conditions)
            .addOnSuccessListener { model: CustomModel? ->
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.

                // The CustomModel object contains the local path of the model file,
                // which you can use to instantiate a TensorFlow Lite interpreter.
                val modelFile = model?.file
                if (modelFile == null) {
                    showToast("Failed to get model file.")
                } else {
                    showToast("Downloaded remote model: $modelName")
                    tomatoDiseaseClassifier.initialize(modelFile)
                }
            }
            .addOnFailureListener {
                showToast("Model download failed for $modelName, please check your connection.")
            }
    }

    private fun showToast(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        tomatoDiseaseClassifier.close()
        super.onDestroy()
    }

    private fun classify(thumbnail:Bitmap) {

        if (tomatoDiseaseClassifier.isInitialized) {
            val classifyTrace = firebasePerformance.newTrace("classify")
            classifyTrace.start()

            tomatoDiseaseClassifier
                .classifyAsync(thumbnail)
                .addOnSuccessListener { resultText ->
                    classifyTrace.stop()
                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("picture", thumbnail)
                    intent.putExtra("prediction", resultText)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error classifying drawing.", e)
                }
        }
    }
}