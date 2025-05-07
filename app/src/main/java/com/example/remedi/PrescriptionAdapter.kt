package com.example.remedi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// RecyclerView Adapter to display and manage a list of prescriptions
class PrescriptionAdapter(
    private val prescriptions: MutableList<Prescription>,  // Mutable list allows dynamic updates (edit/delete)
    private val userId: String                             // Needed for Firestore queries
) : RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder>() {

    // ViewHolder holds references to UI components for each prescription item
    inner class PrescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.prescriptionName)
        val dosage: TextView = itemView.findViewById(R.id.dosage)
        val dates: TextView = itemView.findViewById(R.id.prescriptionDates)
        val frequency: TextView = itemView.findViewById(R.id.frequency)
        val prescribingDoctor: TextView = itemView.findViewById(R.id.doctor)
        val notes: TextView = itemView.findViewById(R.id.notes)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
    }

    // Inflate the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.prescription_item, parent, false)
        return PrescriptionViewHolder(view)
    }

    // Bind data from a Prescription object to each ViewHolder
    override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
        val prescription = prescriptions[position]

        // Set text fields, using fallback values if data is missing
        holder.name.text = prescription.name ?: "Unnamed"
        holder.dosage.text = prescription.dosage ?: "No dosage info"
        holder.frequency.text = prescription.frequency ?: "No frequency info"
        holder.prescribingDoctor.text = prescription.prescribingDoctor ?: "No prescribing doctor info"
        holder.notes.text = prescription.notes ?: "No notes"

        // Format and display start and end dates
        val start = formatTimestamp(prescription.startDate)
        val end = formatTimestamp(prescription.endDate)
        holder.dates.text = "$start - $end"

        // Delete button removes the prescription from Firestore and UI
        holder.btnDelete.setOnClickListener {
            val id = prescription.id
            if (!id.isNullOrEmpty()) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("prescriptions")
                    .document(id)
                    .delete()
                    .addOnSuccessListener {
                        prescriptions.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener {
                        // Optional: Handle failure (e.g., show error)
                    }
            }
        }

        // Edit button opens a dialog to edit the prescription
        holder.btnEdit.setOnClickListener {
            val fragmentManager = (holder.itemView.context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager
            fragmentManager?.let {
                // Show the EditPrescriptionDialogFragment
                EditPrescriptionDialogFragment(userId, prescription) {
                    // Refresh this item in the RecyclerView after editing
                    notifyItemChanged(position)
                }.show(it, "EditPrescriptionDialog")
            }
        }
    }

    // Return the total number of items
    override fun getItemCount(): Int = prescriptions.size

    // Convert a Firestore Timestamp to a readable date string
    private fun formatTimestamp(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let {
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(it)
        } ?: "Unknown"
    }
}

