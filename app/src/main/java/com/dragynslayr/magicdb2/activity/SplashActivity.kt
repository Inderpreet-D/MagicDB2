package com.dragynslayr.magicdb2.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.User
import com.dragynslayr.magicdb2.helper.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*

class SplashActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        enableNightMode()

        database = Firebase.database.reference

        createNotificationChannel()

        if (savedInstanceState == null) {
            //checkForToken()
            checkForData()
        }
    }

    private fun checkForData() {
        val sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val lastDateString = sharedPreferences.getString(getString(R.string.bulk_date_key), null)

        val c = Calendar.getInstance()
        val now = c.time
        val nextDate = if (lastDateString == null) {
            now.format().parseDate()
        } else {
            c.time = lastDateString.parseDate()
            c.add(Calendar.DAY_OF_MONTH, 7)
            c.time.format().parseDate()
        }

        if (now >= nextDate) {
            "Should download".log()
            // Download both
            downloadBulkData {
//                with(sharedPreferences.edit()) {
//                    putString(getString(R.string.bulk_date_key), now.format())
//                    commit()
//                }
                "Finished".log()
            }

        } else {
            "Try to login".log()
//            checkForToken()
        }
    }

    private fun downloadBulkData(callback: () -> Unit) {
        Thread {
            val url = "https://api.scryfall.com/bulk-data"
            val bulkList = url.fetch()
            if (bulkList.has("data")) {
                bulkList.getJSONArray("data").toString().log()
                val dataArray = bulkList.getJSONArray("data")
                for (i in 0 until dataArray.length()) {
                    val obj = dataArray.getJSONObject(i)
                    if (obj.getString("type") == "oracle_cards") {
                        obj.toString().log()
                        val downloadUri = obj.getString("download_uri")
                        downloadUri.log()
//                                callback()
                        break
                    }
                }
                "Done".log()
            }
        }.start()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}