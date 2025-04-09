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

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this);

        firebaseAuth = Firebase.auth;

        val loginButton = findViewById<Button>(R.id.login_button);

        loginButton.setOnClickListener{
            val emailEditText = findViewById<EditText>(R.id.email_edit_text);
            val passwordEditText = findViewById<EditText>(R.id.password_edit_text);

            val email = emailEditText.text.toString();
            val password = passwordEditText.text.toString();

            // check if email or password is empty

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("AUTH", "signInWithEmail:success")
                        Toast.makeText(
                            baseContext,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Redirect to HomeActivity (which uses activity_main.xml)
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish() // optional: closes login activity so user can't go back to it
                    } else {
                        Log.w("AUTH", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Invalid email or password. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        };
    }
}