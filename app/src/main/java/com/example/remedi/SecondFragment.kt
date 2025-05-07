package com.example.remedi

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SecondFragment : Fragment() {

    private lateinit var db: FirebaseFirestore  // Firebase Firestore instance
    private lateinit var auth: FirebaseAuth  // Firebase Authentication instance

    // onCreateView is called to set up the fragment's layout and logic
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Find the EditText and CheckBox elements in the layout
        val name = view.findViewById<EditText>(R.id.prescriptionName)
        val dosage = view.findViewById<EditText>(R.id.dosage)
        val frequency = view.findViewById<EditText>(R.id.frequency)
        val startDate = view.findViewById<EditText>(R.id.startDate)
        val endDate = view.findViewById<EditText>(R.id.endDate)
        val doctor = view.findViewById<EditText>(R.id.prescribingDoctor)
        val notes = view.findViewById<EditText>(R.id.notes)
        val isActive = view.findViewById<CheckBox>(R.id.isActiveCheckbox)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        // Set up date pickers for start and end dates
        val datePicker = { editText: EditText ->
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val selected = String.format("%04d-%02d-%02d", y, m + 1, d)
                editText.setText(selected)  // Set the selected date to the EditText
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Set onClickListeners for the startDate and endDate fields to show date picker
        startDate.setOnClickListener { datePicker(startDate) }
        endDate.setOnClickListener { datePicker(endDate) }

        // Set onClickListener for the saveButton
        saveButton.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener  // Get user ID from FirebaseAuth, or return if not authenticated

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // Date format for start and end dates

            // Create a Prescription object from the form input
            val prescription = Prescription(
                name = name.text.toString(),
                dosage = dosage.text.toString(),
                frequency = frequency.text.toString(),
                startDate = startDate.text.toString().let { sdf.parse(it)?.let { d -> Timestamp(d) } },
                endDate = endDate.text.toString().let { sdf.parse(it)?.let { d -> Timestamp(d) } },
                prescribingDoctor = doctor.text.toString(),
                notes = notes.text.toString(),
                isActive = isActive.isChecked
            )

            // Save the prescription to Firestore under the current user's prescriptions
            db.collection("users")
                .document(uid)
                .collection("prescriptions")
                .add(prescription)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Prescription saved", Toast.LENGTH_SHORT).show()

                    // Clear all fields after saving
                    name.setText("")
                    dosage.setText("")
                    frequency.setText("")
                    startDate.setText("")
                    endDate.setText("")
                    doctor.setText("")
                    notes.setText("")
                    isActive.setChecked(false)  // Reset the isActive checkbox
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error saving", Toast.LENGTH_SHORT).show()  // Show an error if saving fails
                }
        }

        return view  // Return the fragment's view
    }
}
