package com.dragynslayr.magicdb2.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.User
import com.dragynslayr.magicdb2.fragment.CollectionFragment
import com.dragynslayr.magicdb2.fragment.ScanFragment
import com.dragynslayr.magicdb2.fragment.SearchFragment
import com.dragynslayr.magicdb2.helper.enableNightMode
import com.dragynslayr.magicdb2.helper.spaceButtons
import com.dragynslayr.magicdb2.helper.toastLong
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableNightMode()

        setSupportActionBar(toolbar)
        supportActionBar!!.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        nav_view.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<CollectionFragment>(R.id.content_frame, null, intent.extras)
            }
        }

        user = intent.extras?.getSerializable(getString(R.string.user_object_key)) as User
        nav_view.menu.getItem(0).title = user.username!!
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_item_scan -> moveTo<ScanFragment>()
            R.id.nav_item_search -> moveTo<SearchFragment>()
            R.id.nav_item_view -> moveTo<CollectionFragment>()
            R.id.nav_item_logout -> showLogoutDialog()
        }
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private inline fun <reified F : Fragment> moveTo() {
        supportFragmentManager.commit {
            replace<F>(R.id.content_frame, null, intent.extras)
            addToBackStack(null)
        }
    }

    private fun showLogoutDialog() {
        val dialog =
            AlertDialog.Builder(this).setMessage(getString(R.string.logout_prompt))
                .setNegativeButton(getString(R.string.dialog_cancel)) { _: DialogInterface, _: Int -> }
                .setPositiveButton(getString(R.string.nav_logout)) { _: DialogInterface, _: Int -> logout() }
                .create()
        dialog.setOnShowListener {
            dialog.spaceButtons()
        }
        dialog.show()
    }

    private fun logout() {
        clearToken()
        val intent = Intent(applicationContext, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        toastLong("Successfully logged out")
        startActivity(intent)
    }

    private fun clearToken() {
        val sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(getString(R.string.user_token_key))
            commit()
        }
    }
}
