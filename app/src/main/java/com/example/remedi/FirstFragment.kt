package com.example.remedi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class FirstFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PrescriptionAdapter
    private val prescriptions = mutableListOf<Prescription>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.prescriptions)
        progressBar = view.findViewById(R.id.idProgressBar)

        adapter = PrescriptionAdapter(prescriptions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        getPrescriptions()
    }

    private fun getPrescriptions() {
        val currentUser = Firebase.auth.currentUser
        val uid = currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("prescriptions")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                prescriptions.clear()
                for (document in querySnapshot) {
                    val prescription = document.toObject(Prescription::class.java)
                    prescriptions.add(prescription)
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
