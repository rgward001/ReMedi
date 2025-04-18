package com.example.remedi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PrescriptionFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prescription_form)

        // Get recognized text from CameraActivity
        val recognizedText = intent.getStringExtra("recognizedText") ?: ""

        val editTextName = findViewById<EditText>(R.id.editTextMedicineName)
        val editTextDosage = findViewById<EditText>(R.id.editTextDosage)
        val editTextFrequency = findViewById<EditText>(R.id.editTextFrequency)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Naive parsing: assumes first 3 lines of OCR match name, dosage, frequency
        val lines = recognizedText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size >= 3) {
            editTextName.setText(lines[0])
            editTextDosage.setText(lines[1])
            editTextFrequency.setText(lines[2])
        } else {
            editTextName.setText(lines.getOrNull(0) ?: "")
            editTextDosage.setText(lines.getOrNull(1) ?: "")
            editTextFrequency.setText(lines.getOrNull(2) ?: "")
        }

        btnSave.setOnClickListener {
            val name = editTextName.text.toString()
            val dosage = editTextDosage.text.toString()
            val frequency = editTextFrequency.text.toString()

            // TODO: Save this data to Firestore or your local DB
            // For now, just show a confirmation
            Toast.makeText(this, "Saved: $name, $dosage, $frequency", Toast.LENGTH_SHORT).show()

            // Return to previous screen (or wherever you want)
            finish()
        }
    }
}
    