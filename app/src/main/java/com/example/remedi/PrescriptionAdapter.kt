package com.example.remedi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.remedi.Prescription
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp

class PrescriptionAdapter(private val prescriptions: List<Prescription>) :
    RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder>() {

    inner class PrescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.prescriptionName)
        val dosage: TextView = itemView.findViewById(R.id.dosage)
        val dates: TextView = itemView.findViewById(R.id.prescriptionDates)
        val frequency: TextView = itemView.findViewById(R.id.frequency)
        val prescribingDoctor: TextView = itemView.findViewById(R.id.doctor)
        val notes: TextView = itemView.findViewById(R.id.notes)
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
        holder.frequency.text = prescription.frequency?: "No frequency info"
        holder.prescribingDoctor.text = prescription.prescribingDoctor?: "No prescribing doctor info"
        holder.notes.text = prescription.notes?: "No notes"


        val start = formatTimestamp(prescription.startDate)
        val end = formatTimestamp(prescription.endDate)
        holder.dates.text = "$start - $end"
    }

    override fun getItemCount(): Int = prescriptions.size

    private fun formatTimestamp(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let {
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(it)
        } ?: "Unknown"
    }
}
