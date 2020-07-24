package com.dragynslayr.magicdb2.helper

import androidx.appcompat.app.AppCompatDelegate

const val APP_TAG = "MTG-APP"

const val DB_USERS = "users"

fun enableNightMode() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}