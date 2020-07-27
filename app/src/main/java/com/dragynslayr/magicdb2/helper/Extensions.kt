package com.dragynslayr.magicdb2.helper

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.InputFilter
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.activity.LoginActivity
import com.dragynslayr.magicdb2.activity.MainActivity
import com.dragynslayr.magicdb2.data.User
import com.google.android.material.textfield.TextInputLayout
import java.security.MessageDigest

fun EditText.setNameFilter() {
    val filter =
        InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!Character.isLetterOrDigit(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
    this.filters = arrayOf(filter)
}

fun String.hash(salt: String): String {
    val bytes = (salt + this + salt).toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02X".format(it) })
}

fun String.log() {
    Log.d(APP_TAG, this)
}

fun TextInputLayout.resetError() {
    this.error = null
}

fun TextInputLayout.getText(): String {
    return this.editText!!.text.toString()
}

fun AppCompatActivity.startMain(user: User) {
    val intent =
        Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    intent.putExtra(getString(R.string.user_object_key), user)
    createToken(user)
    startActivity(intent)
    finish()
}

private fun AppCompatActivity.createToken(user: User) {
    val sharedPreferences =
        getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString(getString(R.string.user_token_key), user.createToken())
        commit()
    }
}

fun AppCompatActivity.startLogin() {
    val intent =
        Intent(applicationContext, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    startActivity(intent)
    finish()
}

private fun makeToast(context: Context, text: String, duration: Int) {
    Toast.makeText(context, text, duration).show()
}

fun Fragment.toastShort(text: String) {
    makeToast(requireContext(), text, Toast.LENGTH_SHORT)
}

fun Fragment.toastLong(text: String) {
    makeToast(requireContext(), text, Toast.LENGTH_LONG)
}

fun AppCompatActivity.toastShort(text: String) {
    makeToast(this, text, Toast.LENGTH_SHORT)
}

fun AppCompatActivity.toastLong(text: String) {
    makeToast(this, text, Toast.LENGTH_LONG)
}

fun AlertDialog.spaceButtons() {
    val layout = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    layout.setMargins(7, 0, 7, 0)
    arrayOf(
        DialogInterface.BUTTON_NEGATIVE,
        DialogInterface.BUTTON_POSITIVE
    ).forEach {
        this.getButton(it).layoutParams = layout
    }
}

fun String.removeChars(): String {
    var s = this
    val toRemove = arrayOf('(', ')', ':', '|', '\n', '\r', '\t')
    toRemove.forEach { c ->
        s = s.replace(c.toString(), "")
    }
    return s
}