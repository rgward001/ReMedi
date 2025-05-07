package com.example.remedi

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// A DialogFragment that allows users to edit an existing prescription
class EditPrescriptionDialogFragment(
    private val userId: String,
    private val prescription: Prescription,
    private val onUpdated: () -> Unit
) : DialogFragment() {

    // Input fields and button
    private lateinit var nameInput: EditText
    private lateinit var dosageInput: EditText
    private lateinit var frequencyInput: EditText
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var doctorInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var activeCheckbox: CheckBox
    private lateinit var saveButton: Button

    // Date formatter for parsing and displaying dates
    private val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    // Optional interface for notifying listeners of updates (not used here directly)
    interface OnPrescriptionUpdatedListener {
        fun onPrescriptionUpdated(updatedPrescription: Prescription)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        // Initialize all input views
        nameInput = view.findViewById(R.id.prescriptionName)
        dosageInput = view.findViewById(R.id.dosage)
        frequencyInput = view.findViewById(R.id.frequency)
        startDateInput = view.findViewById(R.id.startDate)
        endDateInput = view.findViewById(R.id.endDate)
        doctorInput = view.findViewById(R.id.prescribingDoctor)
        notesInput = view.findViewById(R.id.notes)
        activeCheckbox = view.findViewById(R.id.isActiveCheckbox)
        saveButton = view.findViewById(R.id.saveButton)

        // Pre-fill the form with existing prescription data
        nameInput.setText(prescription.name)
        dosageInput.setText(prescription.dosage)
        frequencyInput.setText(prescription.frequency)
        startDateInput.setText(prescription.startDate?.toDate()?.let { formatter.format(it) })
        endDateInput.setText(prescription.endDate?.toDate()?.let { formatter.format(it) })
        doctorInput.setText(prescription.prescribingDoctor)
        notesInput.setText(prescription.notes)
        activeCheckbox.isChecked = prescription.isActive

        // Add date pickers for start and end date inputs
        setupDatePicker(startDateInput)
        setupDatePicker(endDateInput)

        // Save button click updates the prescription
        saveButton.setOnClickListener {
            updatePrescription()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        // Set the dialog to use full width
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // Helper function to show a DatePickerDialog when an EditText is clicked
    private fun setupDatePicker(field: EditText) {
        field.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                field.setText(formatter.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // Push updated prescription data to Firestore
    private fun updatePrescription() {
        val updated = hashMapOf<String, Any>(
            "name" to nameInput.text.toString(),
            "dosage" to dosageInput.text.toString(),
            "frequency" to frequencyInput.text.toString(),
            "startDate" to Timestamp(formatter.parse(startDateInput.text.toString())!!),
            "endDate" to Timestamp(formatter.parse(endDateInput.text.toString())!!),
            "prescribingDoctor" to doctorInput.text.toString(),
            "notes" to notesInput.text.toString(),
            "isActive" to activeCheckbox.isChecked
        )

        val docId = prescription.id ?: return

        // Update the Firestore document with new values
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("prescriptions")
            .document(docId)
            .update(updated)
            .addOnSuccessListener {
                onUpdated() // Notify parent or refresh UI
                dismiss()   // Close the dialog
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
    }
}
