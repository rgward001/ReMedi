package com.example.remedi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth  // Declare FirebaseAuth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)  // Set the layout for this activity

        auth = FirebaseAuth.getInstance()  // Initialize FirebaseAuth instance

        // Set up the "Submit" button click listener
        val submitButton = findViewById<Button>(R.id.submit_button)
        submitButton.setOnClickListener {
            // Retrieve the email and password entered by the user
            val email_edit_text = findViewById<EditText>(R.id.email_edit_text)
            val password_edit_text = findViewById<EditText>(R.id.password_edit_text)
            val email = email_edit_text.text.toString()
            val password = password_edit_text.text.toString()

            // Check if both fields are filled
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Attempt to create a new user with the provided email and password
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // If registration is successful, show a success message
                            Toast.makeText(this, "User registered!", Toast.LENGTH_SHORT).show()

                            // Redirect to the main activity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()  // Close the registration activity
                        } else {
                            // If registration fails, show an error message with the exception details
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // If fields are empty, prompt the user to fill them
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
