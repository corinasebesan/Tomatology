package com.example.tomatology

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.TextView

class DetailsActivity : AppCompatActivity() {

    private lateinit var listViewAdapter: ExpandableListViewAdapter
    private lateinit var titleList : List<String>
    private lateinit var contentList: HashMap<String, List<String>>

    private var information:Information = Information("","","","","","")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val btnMenu = findViewById<Button>(R.id.btn_menu2)
        val tvDetails = findViewById<TextView>(R.id.tv_details_title)

        val bundle = intent.extras
        if(bundle != null){
            information = this.intent?.getParcelableExtra<Information>("information") as Information
            tvDetails.text = information.diseaseName
        }

        showList()
        listViewAdapter = ExpandableListViewAdapter(this,titleList,contentList)
        val elvResults = findViewById<ExpandableListView>(R.id.elv_results2)
        elvResults.setAdapter(listViewAdapter)

        btnMenu.setOnClickListener {
            goToMain()
        }
    }

    private fun showList() {
        titleList = ArrayList()
        contentList = HashMap()

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
        content4.add(information.prevention)

        contentList[titleList[0]] = content1
        contentList[titleList[1]] = content2
        contentList[titleList[2]] = content3
        contentList[titleList[3]] = content4
    }
}