package com.example.remedi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

// Adapter to bind a list of ToDoDose items to a RecyclerView
class DoseAdapter(private val doses: List<ToDoDose>, private val userId: String) :
    RecyclerView.Adapter<DoseAdapter.DoseViewHolder>() {

    // ViewHolder holds references to the views for each item
    class DoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_taken)
        val name: TextView = itemView.findViewById(R.id.prescription_name)
        val time: TextView = itemView.findViewById(R.id.time_due)
    }

    // Inflates the item layout and returns a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dose_item, parent, false)
        return DoseViewHolder(view)
    }

    // Binds data to the views in the ViewHolder
    override fun onBindViewHolder(holder: DoseViewHolder, position: Int) {
        val dose = doses[position]
        holder.name.text = dose.prescriptionName
        holder.time.text = dose.scheduledTime

        // Detach any previous listener to prevent triggering it during recycling
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = dose.isTaken

        // Attach a new listener to update Firestore when checkbox is toggled
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            dose.isTaken = isChecked
            updateDoseStatus(dose)
        }
    }

    // Returns the total number of dose items
    override fun getItemCount(): Int = doses.size

    // Updates the Firestore document representing the dose status
    private fun updateDoseStatus(dose: ToDoDose) {
        val db = FirebaseFirestore.getInstance()

        // Construct a unique document ID based on prescription ID and time
        val docId = "${dose.prescriptionId}_${dose.scheduledTime.replace(":", "").replace(" ", "_")}"

        val doseData = hashMapOf(
            "prescriptionId" to dose.prescriptionId,
            "scheduledTime" to dose.scheduledTime,
            "isTaken" to dose.isTaken
        )

        // Store the updated dose status under the user's takenDoses collection
        db.collection("users")
            .document(userId)
            .collection("takenDoses")
            .document(docId)
            .set(doseData)
    }
}
