package com.example.tomatology

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ArchiveActivity : AppCompatActivity() {

    private lateinit var dref : DatabaseReference
    private var titlesList = mutableListOf<String>()
    private var dateList = mutableListOf<String>()
    private var idList = mutableListOf<Long>()
    private var information:ArrayList<Information> = ArrayList()
    // private var imageList = mutableListOf<String>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        val bundle = intent.extras
        if(bundle != null){
            information = this.intent?.getParcelableArrayListExtra<Information>("information") as ArrayList<Information>
        }

        information.add(2, Information("","Healthy","","","",""))

        recyclerView = findViewById(R.id.rv_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        getUserData()
    }

    private fun getUserData(){
        dref = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid)

        dref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapshot in snapshot.children){
                        val title = userSnapshot.child("disease").value as String
                        val date = userSnapshot.child("date").value as String
                        val id = userSnapshot.child("diseaseID").value as Long
                        // val imageName = userSnapshot.child("diseasePicture").value as String

                        addToList(title, date, id)

                    }

                    recyclerView.adapter = RecyclerAdapter(titlesList, dateList, information[6])
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun addToList(title: String, date: String, id: Long){
        titlesList.add(title)
        dateList.add(date)
        idList.add(id)
        // imageList.add(imageName)
    }
}