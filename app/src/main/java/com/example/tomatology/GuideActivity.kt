package com.example.tomatology

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.TextView

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        val btnMenu = findViewById<Button>(R.id.btn_menu)
        val tvGuide = findViewById<TextView>(R.id.tv_guide_text)
        val bundle = intent.extras
        if(bundle != null){
            val text:String = this.intent?.getStringExtra("text") as String
            tvGuide.text = text
        }

        btnMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}