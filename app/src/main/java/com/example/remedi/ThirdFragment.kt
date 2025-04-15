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

class ThirdFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PrescriptionAdapter
    private lateinit var prescriptionText: TextView
    private val prescriptions = mutableListOf<Prescription>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.prescriptions)
        progressBar = view.findViewById(R.id.idProgressBar)
        prescriptionText = view.findViewById(R.id.no_prescriptions_text)
        prescriptionText.visibility = View.GONE

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: return

        adapter = PrescriptionAdapter(prescriptions, uid)
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
                prescriptions.clear()
                for (document in querySnapshot) {
                    val prescription = document.toObject(Prescription::class.java)
                    prescription.id = document.id
                    prescriptions.add(prescription)
                }

                if(prescriptions.isEmpty()){
                    prescriptionText.visibility = View.VISIBLE
                } else {
                    prescriptionText.visibility = View.GONE
                }

                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                // Handle error
                progressBar.visibility = View.GONE
            }
    }
}
