package com.example.tomatology

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Base64
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ResultActivity : AppCompatActivity() {

    private lateinit var listViewAdapter: ExpandableListViewAdapter
    private lateinit var titleList : List<String>
    private lateinit var contentList: HashMap<String, List<String>>

    private var database = Firebase.database
    private var myRef = database.reference

//    private val firebaseAnalytics = Firebase.analytics
    private var diseaseSelected = ""

    private var thumbnail:Bitmap? = null
    private var prediction:ArrayList<Prediction> = ArrayList()
    private var sortedList:ArrayList<Prediction> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val viewResult = findViewById<ImageView>(R.id.iv_result)
        val bundle = intent.extras
        if(bundle != null){
            thumbnail = this.intent?.getParcelableExtra<Parcelable>("picture") as Bitmap
            prediction = this.intent?.getParcelableArrayListExtra<Prediction>("prediction") as ArrayList<Prediction>
            viewResult.setImageBitmap(thumbnail)
        }

        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val btnSelect = findViewById<Button>(R.id.btn_select)

        sortedList = prediction.sortedWith(compareBy { it.percentage }).reversed() as ArrayList<Prediction>
        showList()
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
            val userID = FirebaseAuth.getInstance().currentUser!!.uid
            val currentDateTime = LocalDateTime.now()
            myRef.child("users")
                .child(userID)
                .child(currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .child("diseaseID")
                .setValue(sortedList[titleList.indexOf(diseaseSelected)].idLabel)
            myRef.child("users")
                .child(userID)
                .child(currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .child("disease")
                .setValue(sortedList[titleList.indexOf(diseaseSelected)].label)
            myRef.child("users")
                .child(userID)
                .child(currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .child("diseasePicture")
                .setValue(getImageData(thumbnail!!))
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

        (titleList as ArrayList<String>).add(sortedList[0].label+" - %.2f%%".format(sortedList[0].percentage*100))
        (titleList as ArrayList<String>).add(sortedList[1].label+" - %.2f%%".format(sortedList[1].percentage*100))
        (titleList as ArrayList<String>).add(sortedList[2].label+" - %.2f%%".format(sortedList[2].percentage*100))

        if(sortedList[0].label!="Healthy"){
            val content1 : MutableList<String> = ArrayList()
            content1.add("Content 1")
            contentList[titleList[0]] = content1
        }
        if(sortedList[1].label!="Healthy"){
            val content2 : MutableList<String> = ArrayList()
            content2.add("Content 1")
            contentList[titleList[1]] = content2
        }
        if(sortedList[2].label!="Healthy"){
            val content3 : MutableList<String> = ArrayList()
            content3.add("Content 1")
            contentList[titleList[2]] = content3
        }
    }

    private fun getImageData(bmp: Bitmap): String {
        val bao = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao) // bmp is bitmap from user image file

        bmp.recycle()
        val byteArray: ByteArray = bao.toByteArray()
        return Base64.encodeToString(byteArray, Base64.URL_SAFE)
    }
}