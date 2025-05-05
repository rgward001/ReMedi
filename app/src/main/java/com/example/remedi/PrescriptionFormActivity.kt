package com.example.remedi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prescription_form)

        nameField = findViewById(R.id.etPrescriptionName)
        dosageField = findViewById(R.id.etDosage)
        frequencyField = findViewById(R.id.etFrequency)
        startDateField = findViewById(R.id.etStartDate)
        endDateField = findViewById(R.id.etEndDate)
        doctorField = findViewById(R.id.etDoctor)
        notesField = findViewById(R.id.etNotes)

        val saveButton: Button = findViewById(R.id.btnSave)
        saveButton.setOnClickListener {
            savePrescriptionData()
        }

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            processImageFromUri(imageUri)
        }
    }

    private fun savePrescriptionData() {
        val prescriptionName = nameField.text.toString().trim()
        val dosage = dosageField.text.toString().trim()
        val frequency = frequencyField.text.toString().trim()
        val startDate = startDateField.text.toString().trim()
        val endDate = endDateField.text.toString().trim()
        val doctor = doctorField.text.toString().trim()
        val notes = notesField.text.toString().trim()

        val startTimestamp = convertToTimestamp(startDate)
        val endTimestamp = convertToTimestamp(endDate)
        val userId = auth.currentUser?.uid ?: "anonymous_user"

        val prescription = Prescription(
            name = prescriptionName,
            dosage = dosage,
            frequency = frequency,
            startDate = startTimestamp,
            endDate = endTimestamp,
            prescribingDoctor = doctor,
            notes = notes,
            isActive = true
        )

        db.collection("users")
            .document(userId)
            .collection("prescriptions")
            .add(prescription)
            .addOnSuccessListener {
                Toast.makeText(this, "Prescription saved successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving prescription: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertToTimestamp(dateString: String): Timestamp? {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            date?.let { Timestamp(date) }
        } catch (e: Exception) {
            null
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
                    Log.d("OCR", "Detected text: ${visionText.text}")
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

    private fun loadPrescriptionList(): List<String> {
        return assets.open("prescriptions.txt").bufferedReader().useLines { lines ->
            lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
    }

    private fun autoFillForm(text: String) {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val knownPrescriptions = loadPrescriptionList()

        var name: String? = null
        var dosage: String? = null
        var frequency: String? = null
        var startDate: String? = null
        var endDate: String? = null
        var doctor: String? = null

        val dateRegex = Regex("""\b(0?[1-9]|1[0-2])[-/](0?[1-9]|[12][0-9]|3[01])[-/](\d{2,4})\b""")
        val dosageRegex = Regex("""\b\d+\s?(mg|ml|mcg|g)\b""", RegexOption.IGNORE_CASE)
        val frequencyRegex = Regex("""\b(\d+)\s?(x|times|per)?\s?(day|daily)\b""", RegexOption.IGNORE_CASE)
        val doctorRegex = Regex("""(Dr\.?\s?[A-Za-z]+)""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            if (name == null) {
                for (prescription in knownPrescriptions) {
                    if (line.contains(prescription, ignoreCase = true)) {
                        name = prescription
                        break
                    }
                }
            }

            if (dosage == null && dosageRegex.containsMatchIn(line)) dosage = dosageRegex.find(line)?.value
            if (frequency == null && frequencyRegex.containsMatchIn(line)) frequency = frequencyRegex.find(line)?.value
            if (doctor == null && doctorRegex.containsMatchIn(line)) doctor = doctorRegex.find(line)?.value

            if (startDate == null && line.lowercase().contains("start") && dateRegex.containsMatchIn(line)) {
                startDate = dateRegex.find(line)?.value
            }

            if (endDate == null && line.lowercase().contains("end") && dateRegex.containsMatchIn(line)) {
                endDate = dateRegex.find(line)?.value
            }
        }

        nameField.setText(name)
        dosageField.setText(dosage)
        frequencyField.setText(frequency)
        startDateField.setText(startDate)
        endDateField.setText(endDate)
        doctorField.setText(doctor)
    }
}
