package com.dragynslayr.magicdb2.helper

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.InputFilter
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

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

fun TextInputLayout.setText(msg: String) {
    this.editText!!.setText(msg, TextView.BufferType.EDITABLE)
}

fun TextInputLayout.resetText() {
    this.setText("")
}

fun LinearLayout.hide() {
    this.visibility = View.GONE
}

fun LinearLayout.show() {
    this.visibility = View.VISIBLE
}

fun TextInputLayout.isValidEmail(): Boolean {
    return this.getText().isValidEmail()
}

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun TextInputLayout.focus(context: Context) {
    this.editText!!.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this.editText!!, InputMethodManager.SHOW_IMPLICIT)
}

fun TextInputLayout.moveCursorToEnd() {
    this.editText!!.setSelection(this.getText().length)
}

/*
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
*/

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

fun Int.formatTime(): String {
    val secondsLong = this.toLong()
    val hours = TimeUnit.SECONDS.toHours(secondsLong)
    val minutes = TimeUnit.SECONDS.toMinutes(secondsLong) - TimeUnit.HOURS.toMinutes(hours)
    val seconds =
        TimeUnit.SECONDS.toSeconds(secondsLong) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(
            hours
        )
    return "${hours.asTime()}:${minutes.asTime()}:${seconds.asTime()}"
}

private fun Long.asTime(): String {
    return if (this < 10) {
        "0$this"
    } else {
        "$this"
    }
}

fun Uri.getFilename(context: Context): String {
    var result = ""
    if (this.scheme == "content" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val cursor = context.contentResolver.query(this, null, null, null)
        cursor.use {
            if (it != null && it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    if (result == "") {
        result = this.path!!.split("/").last()
    }
    return result
}