package com.dragynslayr.magicdb2.activity


import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
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
import kotlinx.android.synthetic.main.activity_login.*
import java.security.SecureRandom

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var isRegistering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        enableNightMode()

        database = Firebase.database.reference

        username_layout.editText!!.setNameFilter()

        updateTypeText()
        switch_type_text.setOnClickListener {
            isRegistering = !isRegistering
            updateTypeText()
        }

        login_button.setOnClickListener {
            clearErrors()
            if (!checkFields()) {
                return@setOnClickListener
            }
            if (isRegistering) {
                processRegister()
            } else {
                processLogin()
            }
        }
    }

    private fun updateTypeText() {
        if (isRegistering) {
            switch_type_text.text = getHighlight(R.string.login_prompt)
            login_button.text = getString(R.string.register_button)
        } else {
            switch_type_text.text = getHighlight(R.string.register_prompt)
            login_button.text = getString(R.string.login_button)
        }
    }

    private fun getHighlight(resID: Int): SpannableString {
        val string = getString(resID)
        val idx = string.indexOf('?')

        val options = arrayOf(ForegroundColorSpan(Color.CYAN), UnderlineSpan())
        val spannable = SpannableString(string)
        for (option in options) {
            spannable.setSpan(
                option,
                idx + 2,
                idx + 12,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }

    private fun processRegister() {
        val username = username_layout.getText()
        val salt = SecureRandom().nextInt(Int.MAX_VALUE).toString()
        val password = password_layout.getText().hash(salt)

        database.child(DB_USERS).child(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        username_layout.error = getString(R.string.user_exists)
                        return
                    }

                    val user = User(username, password, salt)
                    database.child(DB_USERS).child(username).setValue(user)
                    startMain(user)
                }
            })
    }

    private fun processLogin() {
        val username = username_layout.getText()
        val password = password_layout.getText()

        database.child(DB_USERS).child(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        showLoginErrors()
                        return
                    }

                    val user = snapshot.getValue<User>()!!
                    if (user.verify(password)) {
                        startMain(user)
                    } else {
                        showLoginErrors()
                    }
                }
            })
    }

    private fun showLoginErrors() {
        with(getString(R.string.failed_login)) {
            username_layout.error = this
            password_layout.error = this
        }
    }

    private fun checkFields(): Boolean {
        var fieldsAcceptable = true

        with(getString(R.string.required_field)) {
            if (TextUtils.isEmpty(username_layout.getText())) {
                username_layout.error = this
                fieldsAcceptable = false
            }

            if (TextUtils.isEmpty((password_layout.getText()))) {
                password_layout.error = this
                fieldsAcceptable = false
            }
        }

        return fieldsAcceptable
    }

    private fun clearErrors() {
        username_layout.resetError()
        password_layout.resetError()
    }
}