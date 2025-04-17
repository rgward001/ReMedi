package com.example.remedi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class DoseAdapter(private val doses: List<ToDoDose>, private val userId: String) :
    RecyclerView.Adapter<DoseAdapter.DoseViewHolder>() {

    class DoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_taken)
        val name: TextView = itemView.findViewById(R.id.prescription_name)
        val time: TextView = itemView.findViewById(R.id.time_due)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dose_item, parent, false)
        return DoseViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoseViewHolder, position: Int) {
        val dose = doses[position]
        holder.name.text = dose.prescriptionName
        holder.time.text = dose.scheduledTime

        // Prevent checkbox from triggering listener on reuse
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = dose.isTaken

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            dose.isTaken = isChecked
            updateDoseStatus(dose)
        }
    }

    override fun getItemCount(): Int = doses.size

    private fun updateDoseStatus(dose: ToDoDose) {
        val db = FirebaseFirestore.getInstance()

        // Use a unique document ID like prescriptionId + time to identify each dose
        val docId = "${dose.prescriptionId}_${dose.scheduledTime.replace(":", "").replace(" ", "_")}"

        val doseData = hashMapOf(
            "prescriptionId" to dose.prescriptionId,
            "scheduledTime" to dose.scheduledTime,
            "isTaken" to dose.isTaken
        )

        db.collection("users")
            .document(userId)
            .collection("takenDoses")
            .document(docId)
            .set(doseData)
    }
}
