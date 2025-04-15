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

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val name = view.findViewById<EditText>(R.id.prescriptionName)
        val dosage = view.findViewById<EditText>(R.id.dosage)
        val frequency = view.findViewById<EditText>(R.id.frequency)
        val startDate = view.findViewById<EditText>(R.id.startDate)
        val endDate = view.findViewById<EditText>(R.id.endDate)
        val doctor = view.findViewById<EditText>(R.id.prescribingDoctor)
        val notes = view.findViewById<EditText>(R.id.notes)
        val isActive = view.findViewById<CheckBox>(R.id.isActiveCheckbox)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        // Handle date pickers
        val datePicker = { editText: EditText ->
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val selected = String.format("%04d-%02d-%02d", y, m + 1, d)
                editText.setText(selected)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        startDate.setOnClickListener { datePicker(startDate) }
        endDate.setOnClickListener { datePicker(endDate) }

        saveButton.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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

            db.collection("users")
                .document(uid)
                .collection("prescriptions")
                .add(prescription)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Prescription saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error saving", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
