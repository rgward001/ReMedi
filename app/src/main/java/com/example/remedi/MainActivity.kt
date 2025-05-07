package com.example.remedi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// MainActivity handles user login and navigation to registration or home screen
class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable drawing behind system bars for modern UI
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Adjust view padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase before using its services
        FirebaseApp.initializeApp(this)

        firebaseAuth = Firebase.auth

        // Initialize login and register button references
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button)

        // Navigate to registration activity if user taps "Register"
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Attempt login when user taps "Login"
        loginButton.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.email_edit_text)
            val passwordEditText = findViewById<EditText>(R.id.password_edit_text)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Authenticate user using Firebase Auth
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("AUTH", "signInWithEmail:success")
                        Toast.makeText(
                            baseContext,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Redirect to main app (HomeActivity)
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish() // Prevents user from returning to login screen via back button
                    } else {
                        Log.w("AUTH", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Invalid email or password. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
