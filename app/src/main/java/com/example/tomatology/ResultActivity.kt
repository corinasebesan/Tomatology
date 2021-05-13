package com.example.tomatology

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.perf.FirebasePerformance


class ResultActivity : AppCompatActivity() {

    private lateinit var listViewAdapter: ExpandableListViewAdapter
    private lateinit var titleList : List<String>
    private lateinit var contentList: HashMap<String, List<String>>

//    private val firebaseAnalytics = Firebase.analytics
    private var diseaseSelected = ""
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var firebasePerformance = FirebasePerformance.getInstance()
    private var tomatoDiseaseClassifier = TomatoDiseaseClassifier(this)
    private var thumbnail:Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val viewResult = findViewById<ImageView>(R.id.iv_result)
        val bundle = intent.extras
        if(bundle != null){
            thumbnail = this.intent?.getParcelableExtra<Parcelable>("picture") as Bitmap
            viewResult.setImageBitmap(thumbnail)
        }

        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val btnSelect = findViewById<Button>(R.id.btn_select)

        setupClassifier()

        showList()
        classify()
        listViewAdapter = ExpandableListViewAdapter(this,titleList,contentList)
        val elvResults = findViewById<ExpandableListView>(R.id.elv_results)
        elvResults.setAdapter(listViewAdapter)

        var lastPosition = -1
        elvResults.setOnGroupExpandListener { groupPosition ->
            if ((lastPosition != -1 && groupPosition != lastPosition)) {
                elvResults.collapseGroup(lastPosition)
                elvResults[lastPosition].setBackgroundColor(ContextCompat.getColor(
                            applicationContext,
                            R.color.gray
                        ))
            }
            lastPosition = groupPosition
        }


        elvResults.setOnGroupClickListener { parent, v, groupPosition, _ ->
            parent.smoothScrollToPosition(groupPosition)

            v.setBackgroundColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.red
                            )
                        )
            diseaseSelected = titleList.elementAt(groupPosition)
            false
        }

        btnSelect.setOnClickListener {
            //logAnalyticsEvent(diseaseSelected)
            if(titleList.indexOf(diseaseSelected) == 0) {
                Firebase.analytics.logEvent("correct_inference", null)
            }
            val intent = Intent(this, DetailsActivity::class.java)
            startActivity(intent)
        }

        btnCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showList() {
        titleList = ArrayList()
        contentList = HashMap()

        (titleList as ArrayList<String>).add("Title 1")
        (titleList as ArrayList<String>).add("Title 2")
        (titleList as ArrayList<String>).add("Title 3")

        val content1 : MutableList<String> = ArrayList()
        content1.add("Content 1")
        content1.add("Content 2")
        content1.add("Content 3")

        val content2 : MutableList<String> = ArrayList()
        content2.add("Content 1")
        content2.add("Content 2")
        content2.add("Content 3")

        val content3 : MutableList<String> = ArrayList()
        content3.add("Content 1")
        content3.add("Content 2")
        content3.add("Content 3")

        contentList[titleList[0]] = content1
        contentList[titleList[1]] = content2
        contentList[titleList[2]] = content3
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

    private fun downloadModel(modelName: String): Task<Void> {
        val remoteModel = FirebaseCustomRemoteModel.Builder(modelName).build()
        val firebaseModelManager = FirebaseModelManager.getInstance()
        return firebaseModelManager
            .isModelDownloaded(remoteModel)
            .continueWithTask { task ->
                // Create update condition if model is already downloaded, otherwise create download
                // condition.
                val conditions = if (task.result != null && task.result == true) {
                    FirebaseModelDownloadConditions.Builder()
                        .requireWifi()
                        .build() // Update condition that requires wifi.
                } else {
                    FirebaseModelDownloadConditions.Builder().build() // Download condition.
                }
                firebaseModelManager.download(remoteModel, conditions)
            }
            .addOnSuccessListener {
                firebaseModelManager.getLatestModelFile(remoteModel)
                    .addOnCompleteListener {
                        val model = it.result
                        if (model == null) {
                            showToast("Failed to get model file.")
                        } else {
                            showToast("Downloaded remote model: $modelName")
                            tomatoDiseaseClassifier.initialize(model)
                        }
                    }
            }
            .addOnFailureListener {
                showToast("Model download failed for $modelName, please check your connection.")
            }
    }

    override fun onDestroy() {
        tomatoDiseaseClassifier.close()
        super.onDestroy()
    }

    private fun classify() {
        val bitmap = thumbnail

        if ((bitmap != null) && (tomatoDiseaseClassifier.isInitialized)) {
            val classifyTrace = firebasePerformance.newTrace("classify")
            classifyTrace.start()

            tomatoDiseaseClassifier
                .classifyAsync(bitmap)
                .addOnSuccessListener { resultText ->
                    classifyTrace.stop()
                    (titleList as ArrayList<String>)[0] = resultText
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error classifying drawing.", e)
                }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}