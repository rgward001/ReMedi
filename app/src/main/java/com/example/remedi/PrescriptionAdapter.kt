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

class PrescriptionAdapter(
    private val prescriptions: MutableList<Prescription>,  // MutableList to allow updates
    private val userId: String
) : RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.prescription_item, parent, false)
        return PrescriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
        val prescription = prescriptions[position]

        holder.name.text = prescription.name ?: "Unnamed"
        holder.dosage.text = prescription.dosage ?: "No dosage info"
        holder.frequency.text = prescription.frequency ?: "No frequency info"
        holder.prescribingDoctor.text = prescription.prescribingDoctor ?: "No prescribing doctor info"
        holder.notes.text = prescription.notes ?: "No notes"

        val start = formatTimestamp(prescription.startDate)
        val end = formatTimestamp(prescription.endDate)
        holder.dates.text = "$start - $end"

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
                        // Remove the item from the list
                        prescriptions.removeAt(position)
                        // Notify the adapter that an item has been removed
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener {
                        // Optionally handle errors
                    }
            }
        }

        holder.btnEdit.setOnClickListener {
            val fragmentManager = (holder.itemView.context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager
            fragmentManager?.let {
                EditPrescriptionDialogFragment(userId, prescription) {
                    notifyItemChanged(position)
                }.show(it, "EditPrescriptionDialog")
            }
        }

    }

    override fun getItemCount(): Int = prescriptions.size

    private fun formatTimestamp(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let {
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(it)
        } ?: "Unknown"
    }
}

