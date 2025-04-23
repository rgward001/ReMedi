package com.example.remedi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirstFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: DoseAdapter
    private lateinit var prescriptionText: TextView
    private val doses = mutableListOf<ToDoDose>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.prescriptionsToTake)
        progressBar = view.findViewById(R.id.idProgressBar)
        prescriptionText = view.findViewById(R.id.no_prescriptions_text)
        prescriptionText.visibility = View.GONE

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: return

        adapter = DoseAdapter(doses, uid)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        getPrescriptions(uid)
    }

    private fun getPrescriptions(userId: String) {
        progressBar.visibility = View.VISIBLE

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("prescriptions")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                doses.clear()
                for (document in querySnapshot) {
                    val name = document.getString("name") ?: continue
                    val frequency = document.getString("frequency") ?: "Once"
                    val prescriptionId = document.id

                    val lowerFreq =  frequency.lowercase()
                    val times = when {
                        lowerFreq.contains("once") -> listOf("8:00 AM")
                        lowerFreq.contains("twice") -> listOf("8:00 AM", "8:00 PM")
                        lowerFreq.contains("three times") -> listOf("8:00 AM", "1:00 PM", "8:00 PM")
                        else -> listOf("8:00 AM")
                    }

                    for (time in times) {
                        doses.add(ToDoDose(prescriptionId, name, time, false))
                    }
                }

                prescriptionText.visibility = if (doses.isEmpty()) View.VISIBLE else View.GONE
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
            }
    }
}
