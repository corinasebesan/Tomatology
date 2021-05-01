package com.example.tomatology

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase


class ResultActivity : AppCompatActivity() {

    private lateinit var listViewAdapter: ExpandableListViewAdapter
    private lateinit var titleList : List<String>
    private lateinit var contentList: HashMap<String, List<String>>

    private val firebaseAnalytics = Firebase.analytics
    private var diseaseSelected = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val viewResult = findViewById<ImageView>(R.id.iv_result)
        val bundle = intent.extras
        if(bundle != null){
            val thumbnail = this.intent?.getParcelableExtra<Parcelable>("picture") as Bitmap
            viewResult.setImageBitmap(thumbnail)
        }

        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val btnSelect = findViewById<Button>(R.id.btn_select)

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
            logAnalyticsEvent(diseaseSelected)
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

    private fun logAnalyticsEvent(id: String){
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, id)
        }
    }
}