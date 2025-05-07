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

    // Declare EditText fields for prescription data input
    private lateinit var nameField: EditText
    private lateinit var dosageField: EditText
    private lateinit var frequencyField: EditText
    private lateinit var startDateField: EditText
    private lateinit var endDateField: EditText
    private lateinit var doctorField: EditText
    private lateinit var notesField: EditText

    // Firestore and Firebase Authentication instances
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prescription_form)

        // Initialize EditText fields
        nameField = findViewById(R.id.etPrescriptionName)
        dosageField = findViewById(R.id.etDosage)
        frequencyField = findViewById(R.id.etFrequency)
        startDateField = findViewById(R.id.etStartDate)
        endDateField = findViewById(R.id.etEndDate)
        doctorField = findViewById(R.id.etDoctor)
        notesField = findViewById(R.id.etNotes)

        // Set up the save button and its action
        val saveButton: Button = findViewById(R.id.btnSave)
        saveButton.setOnClickListener {
            savePrescriptionData()  // Save prescription when clicked
        }

        // Check if an image Uri is passed via the intent (for OCR processing)
        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            processImageFromUri(imageUri)  // Process the image to auto-fill the form
        }
    }

    // Function to save the entered prescription data to Firestore
    private fun savePrescriptionData() {
        // Extract data from input fields
        val prescriptionName = nameField.text.toString().trim()
        val dosage = dosageField.text.toString().trim()
        val frequency = frequencyField.text.toString().trim()
        val startDate = startDateField.text.toString().trim()
        val endDate = endDateField.text.toString().trim()
        val doctor = doctorField.text.toString().trim()
        val notes = notesField.text.toString().trim()

        // Convert start and end dates to Firestore Timestamp format
        val startTimestamp = convertToTimestamp(startDate)
        val endTimestamp = convertToTimestamp(endDate)
        val userId = auth.currentUser?.uid ?: "anonymous_user"  // Get the current user's ID

        // Create a Prescription object with the entered data
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

        // Add the prescription to Firestore under the current user's collection
        db.collection("users")
            .document(userId)
            .collection("prescriptions")
            .add(prescription)
            .addOnSuccessListener {
                Toast.makeText(this, "Prescription saved successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))  // Redirect to home
                finish()  // Close this activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving prescription: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Convert a date string to Firestore Timestamp format
    private fun convertToTimestamp(dateString: String): Timestamp? {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            date?.let { Timestamp(date) }
        } catch (e: Exception) {
            null  // Return null if the date format is incorrect
        }
    }

    // Function to process an image URI (OCR) to extract text and auto-fill the form
    private fun processImageFromUri(uri: Uri) {
        try {
            // Open the input stream from the URI and convert it to a bitmap
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val image = InputImage.fromBitmap(bitmap, 0)

            // Set up the text recognizer for OCR
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Log.d("OCR", "Detected text: ${visionText.text}")
                    val text = visionText.text
                    autoFillForm(text)  // Attempt to auto-fill form fields based on detected text
                }
                .addOnFailureListener {
                    it.printStackTrace()  // Handle OCR failure
                }
        } catch (e: Exception) {
            e.printStackTrace()  // Handle any exceptions during image processing
        }
    }

    // Function to load a predefined list of known prescription names from a text file
    private fun loadPrescriptionList(): List<String> {
        return assets.open("prescriptions.txt").bufferedReader().useLines { lines ->
            lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
    }

    // Function to auto-fill the form based on extracted text from the image (OCR)
    private fun autoFillForm(text: String) {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val knownPrescriptions = loadPrescriptionList()

        // Declare variables for prescription fields
        var name: String? = null
        var dosage: String? = null
        var frequency: String? = null
        var startDate: String? = null
        var endDate: String? = null
        var doctor: String? = null

        // Define regular expressions for extracting specific data (dates, dosage, frequency, etc.)
        val dateRegex = Regex("""\b(0?[1-9]|1[0-2])[-/](0?[1-9]|[12][0-9]|3[01])[-/](\d{2,4})\b""")
        val dosageRegex = Regex("""\b\d+\s?(mg|ml|mcg|g)\b""", RegexOption.IGNORE_CASE)
        val frequencyRegex = Regex("""(?i)(take\s)?(one|two|three|four|five|\d+)\s(\w+\s)?(times|x)\s?(a|per)?\s?(day|daily)""")
        val doctorRegex = Regex("""(Dr\.?\s?[A-Za-z]+)""", RegexOption.IGNORE_CASE)

        // Loop through each line of extracted text and attempt to fill the fields
        for (line in lines) {
            if (name == null) {
                for (prescription in knownPrescriptions) {
                    if (line.contains(prescription, ignoreCase = true)) {
                        name = prescription
                        break
                    }
                }
            }

            // Match and extract dosage, frequency, start/end dates, and doctor
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

        // Set the extracted data to the form fields
        nameField.setText(name)
        dosageField.setText(dosage)
        frequencyField.setText(frequency)
        startDateField.setText(startDate)
        endDateField.setText(endDate)
        doctorField.setText(doctor)
    }
}
