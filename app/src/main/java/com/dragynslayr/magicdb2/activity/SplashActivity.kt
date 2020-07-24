package com.dragynslayr.magicdb2.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.User
import com.dragynslayr.magicdb2.helper.DB_USERS
import com.dragynslayr.magicdb2.helper.enableNightMode
import com.dragynslayr.magicdb2.helper.startLogin
import com.dragynslayr.magicdb2.helper.startMain
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        enableNightMode()

        database = Firebase.database.reference

        if (savedInstanceState == null) {
            checkForToken()
        }
    }

    private fun checkForToken() {
        val sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userToken = sharedPreferences.getString(getString(R.string.user_token_key), null)
        if (userToken != null) {
            val split = userToken.split(",")
            verifyToken(split[0], split[1])
        } else {
            startLogin()
        }
    }

    private fun verifyToken(username: String, password: String) {
        database.child(DB_USERS).child(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue<User>()
                        if (user != null) {
                            if (user.password == password) {
                                startMain(user)
                                return
                            }
                        }
                    }
                    startLogin()
                }
            })
    }
}