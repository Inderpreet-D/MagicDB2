package com.dragynslayr.magicdb2.helper

import androidx.appcompat.app.AppCompatDelegate
import java.text.SimpleDateFormat
import java.util.*

const val APP_TAG = "MTG-APP"

const val DB_USERS = "users"
const val DB_COLLECTION = "collection"

const val CHANNEL_ID = "MagicDBUpdates"

private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.CANADA)

fun enableNightMode() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}

fun getToday(): String {
    return dateFormat.format(getDateToday())
}

fun getDateToday(): Date {
    return Calendar.getInstance().time
}

fun parseDate(dateStr: String): Date {
    return dateFormat.parse(dateStr)!!
}