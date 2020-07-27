package com.dragynslayr.magicdb2.data

import com.dragynslayr.magicdb2.helper.removeChars
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class Card(val name: String) : Serializable {

    companion object {
        fun isValid(name: String): Boolean {
            return name.length >= 3
        }

        fun clean(name: String): String {
            return name.removeChars().replace("[^\\x00-\\x7F]", "").trim()
        }
    }
}