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

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        val firstFragment = FirstFragment()
        val secondFragment = SecondFragment()
        val thirdFragment = ThirdFragment()
        val fourthFragment = FourthFragment()

        val cameraButton = findViewById<ImageButton>(R.id.camera);

        setCurrentFragment(firstFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(firstFragment)
                R.id.prescription -> setCurrentFragment(secondFragment)
                R.id.view_database -> setCurrentFragment(thirdFragment)
                R.id.view_account -> setCurrentFragment(fourthFragment)

            }
            true
        }

        val animator = ObjectAnimator.ofPropertyValuesHolder(
            cameraButton,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.85f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.85f, 1f)
        ).apply {
            duration = 150
            interpolator = OvershootInterpolator()
        }

        cameraButton.setOnClickListener {
            animator.start()
            startActivity(Intent(this, CameraActivity::class.java))
            finish()
        }

    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

}
