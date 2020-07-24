package com.dragynslayr.magicdb2.data

import com.dragynslayr.magicdb2.helper.hash
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class User(
    var username: String? = "",
    var password: String? = "",
    var salt: String? = ""
) : Serializable {
    @Exclude
    fun verify(otherPass: String): Boolean {
        return password == otherPass.hash(salt!!)
    }

    @Exclude
    fun createToken(): String {
        return "$username,$password"
    }
}