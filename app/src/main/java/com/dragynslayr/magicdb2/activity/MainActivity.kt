package com.dragynslayr.magicdb2.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.helper.log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        "Loaded correctly".log()
    }
}