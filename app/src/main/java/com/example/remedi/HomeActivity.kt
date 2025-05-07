package com.example.remedi

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

// HomeActivity manages the main UI with a bottom navigation bar and a camera button
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize bottom navigation and fragments for each tab
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        val firstFragment = FirstFragment()     // Daily to-do list
        val secondFragment = SecondFragment()   // Prescription manager
        val thirdFragment = ThirdFragment()     // Firestore viewer/debugger
        val fourthFragment = FourthFragment()   // Account settings

        val cameraButton = findViewById<ImageButton>(R.id.camera)

        // Set the initial fragment to the home screen
        setCurrentFragment(firstFragment)

        // Set up navigation bar listeners to switch between fragments
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(firstFragment)
                R.id.prescription -> setCurrentFragment(secondFragment)
                R.id.view_database -> setCurrentFragment(thirdFragment)
                R.id.view_account -> setCurrentFragment(fourthFragment)
            }
            true
        }

        // Set up animation for the floating camera button
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            cameraButton,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.85f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.85f, 1f)
        ).apply {
            duration = 150
            interpolator = OvershootInterpolator()
        }

        // Camera button launches CameraActivity and exits HomeActivity
        cameraButton.setOnClickListener {
            animator.start()
            startActivity(Intent(this, CameraActivity::class.java))
            finish() // Assumes you want to prevent returning to HomeActivity from CameraActivity
        }
    }

    // Helper method to replace the current fragment in the fragment container
    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}

