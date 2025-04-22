package com.example.remedi

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.InputStream

class PrescriptionFormActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var dosageField: EditText
    private lateinit var frequencyField: EditText
    private lateinit var startDateField: EditText
    private lateinit var endDateField: EditText
    private lateinit var doctorField: EditText
    private lateinit var notesField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prescription_form)

        nameField = findViewById(R.id.prescription_name)
        dosageField = findViewById(R.id.dosage)
        frequencyField = findViewById(R.id.frequency)
        startDateField = findViewById(R.id.etStartDate)
        endDateField = findViewById(R.id.etEndDate)
        doctorField = findViewById(R.id.doctor)
        notesField = findViewById(R.id.notes)

        val saveButton: Button = findViewById(R.id.btnSave)
        saveButton.setOnClickListener {
            // Save logic here (e.g., upload to Firestore)
        }

        val imageUri = intent.getParcelableExtra<Uri>("imageUri")
        if (imageUri != null) {
            processImageFromUri(imageUri)
        }
    }

    private fun processImageFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val image = InputImage.fromBitmap(bitmap, 0)

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    autoFillForm(text)
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun autoFillForm(text: String) {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        lines.forEach { line ->
            when {
                line.contains("name", true) || line.contains("prescription", true) -> nameField.setText(line)
                line.contains("mg", true) || line.contains("dosage", true) -> dosageField.setText(line)
                line.contains("daily", true) || line.contains("times", true) -> frequencyField.setText(line)
                line.contains("start", true) -> startDateField.setText(line)
                line.contains("end", true) -> endDateField.setText(line)
                line.contains("dr", true) || line.contains("doctor", true) -> doctorField.setText(line)
            }
        }
    }
}