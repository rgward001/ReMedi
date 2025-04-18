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

class EditPrescriptionDialogFragment(
    private val userId: String,
    private val prescription: Prescription,
    private val onUpdated: () -> Unit
) : DialogFragment() {

    private lateinit var nameInput: EditText
    private lateinit var dosageInput: EditText
    private lateinit var frequencyInput: EditText
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var doctorInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var activeCheckbox: CheckBox
    private lateinit var saveButton: Button

    private val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    interface OnPrescriptionUpdatedListener {
        fun onPrescriptionUpdated(updatedPrescription: Prescription)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        nameInput = view.findViewById(R.id.prescriptionName)
        dosageInput = view.findViewById(R.id.dosage)
        frequencyInput = view.findViewById(R.id.frequency)
        startDateInput = view.findViewById(R.id.startDate)
        endDateInput = view.findViewById(R.id.endDate)
        doctorInput = view.findViewById(R.id.prescribingDoctor)
        notesInput = view.findViewById(R.id.notes)
        activeCheckbox = view.findViewById(R.id.isActiveCheckbox)
        saveButton = view.findViewById(R.id.saveButton)

        // Pre-fill values
        nameInput.setText(prescription.name)
        dosageInput.setText(prescription.dosage)
        frequencyInput.setText(prescription.frequency)
        startDateInput.setText(prescription.startDate?.toDate()?.let { formatter.format(it) })
        endDateInput.setText(prescription.endDate?.toDate()?.let { formatter.format(it) })
        doctorInput.setText(prescription.prescribingDoctor)
        notesInput.setText(prescription.notes)
        activeCheckbox.isChecked = prescription.isActive

        // Date pickers
        setupDatePicker(startDateInput)
        setupDatePicker(endDateInput)

        saveButton.setOnClickListener {
            updatePrescription()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setupDatePicker(field: EditText) {
        field.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                field.setText(formatter.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

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
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("prescriptions")
            .document(docId)
            .update(updated)
            .addOnSuccessListener {
                onUpdated()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
    }
}
