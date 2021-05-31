package com.example.tomatology

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView

class DetailsActivity : AppCompatActivity() {

    private lateinit var listViewAdapter: ExpandableListViewAdapter
    private lateinit var titleList : List<String>
    private lateinit var contentList: HashMap<String, List<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val viewResult = findViewById<ImageView>(R.id.iv_result)

        val btnMenu = findViewById<Button>(R.id.btn_menu2)

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
        content1.add("Content 1")

        val content2 : MutableList<String> = ArrayList()
        content2.add("Content 1")

        val content3 : MutableList<String> = ArrayList()
        content3.add("Content 1")

        val content4 : MutableList<String> = ArrayList()
        content3.add("Content 1")

        contentList[titleList[0]] = content1
        contentList[titleList[1]] = content2
        contentList[titleList[2]] = content3
        contentList[titleList[3]] = content4
    }

    private fun decodeFromFirebaseBase64(image: String): Bitmap {
        val decodedByteArray = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }
}