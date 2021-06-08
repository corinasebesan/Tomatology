package com.example.tomatology

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.TextView

class DetailsActivity : AppCompatActivity() {

    private lateinit var listViewAdapter: ExpandableListViewAdapter
    private lateinit var titleList : List<String>
    private lateinit var contentList: HashMap<String, List<String>>


    // private var thumbnail:Bitmap? = null
    // private var name = ""
    private var information:Information = Information("","","","","","")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        // val viewResult = findViewById<ImageView>(R.id.iv_result2)

        val btnMenu = findViewById<Button>(R.id.btn_menu2)
        val tvDetails = findViewById<TextView>(R.id.tv_details_title)

        val bundle = intent.extras
        if(bundle != null){
            information = this.intent?.getParcelableExtra<Information>("information") as Information
            // name = this.intent?.getStringArrayExtra("name") as String
            // thumbnail = decodeFromFirebaseBase64(thumbnailStr)
            // viewResult.setImageBitmap(thumbnail)
            // getPicture(name, viewResult)
            tvDetails.text = information.diseaseName
        }

        showList()
        listViewAdapter = ExpandableListViewAdapter(this,titleList,contentList)
        val elvResults = findViewById<ExpandableListView>(R.id.elv_results2)
        elvResults.setAdapter(listViewAdapter)

        btnMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showList() {
        titleList = ArrayList()
        contentList = HashMap()

        //var sortedList = prediction.sortedWith(compareBy { it.percentage }).reversed()


        (titleList as ArrayList<String>).add("Symptoms")
        (titleList as ArrayList<String>).add("Causes")
        (titleList as ArrayList<String>).add("Treatment")
        (titleList as ArrayList<String>).add("Prevention")

        val content1 : MutableList<String> = ArrayList()
        content1.add(information.symptoms)

        val content2 : MutableList<String> = ArrayList()
        content2.add(information.causes)

        val content3 : MutableList<String> = ArrayList()
        content3.add(information.treatment)

        val content4 : MutableList<String> = ArrayList()
        content3.add(information.prevention)

        contentList[titleList[0]] = content1
        contentList[titleList[1]] = content2
        contentList[titleList[2]] = content3
        contentList[titleList[3]] = content4
    }

//    private fun decodeFromFirebaseBase64(image: String): Bitmap {
//        val decodedByteArray = Base64.decode(image, Base64.DEFAULT)
//        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
//    }

//    private fun getPicture(name: String, imageV: ImageView){
//        val tomatoRef = storageRef.child(name)
//        GlideApp.with(this)
//            .load(tomatoRef)
//            .into(imageV)
//    }
}