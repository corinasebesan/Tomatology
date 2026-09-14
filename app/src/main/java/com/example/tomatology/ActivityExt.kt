package com.example.tomatology

import android.app.Activity
import android.content.Intent

fun Activity.goToMain() {
    startActivity(Intent(this, MainActivity::class.java))
}
