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

    // Declare variables for the RecyclerView, ProgressBar, TextView, Adapter, and data list
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PrescriptionAdapter
    private lateinit var prescriptionText: TextView
    private val prescriptions = mutableListOf<Prescription>()

    // Inflate the fragment layout when the view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    // Called after the view has been created; set up UI elements and load data
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements from the layout
        recyclerView = view.findViewById(R.id.prescriptions)
        progressBar = view.findViewById(R.id.idProgressBar)
        prescriptionText = view.findViewById(R.id.no_prescriptions_text)

        // Hide the "no prescriptions" text initially
        prescriptionText.visibility = View.GONE

        // Get the current user from FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: return  // Exit if the user is not authenticated

        // Initialize the adapter for the RecyclerView with the empty list of prescriptions
        adapter = PrescriptionAdapter(prescriptions, uid)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())  // Set a vertical layout manager
        recyclerView.adapter = adapter  // Set the adapter for the RecyclerView

        // Fetch the active prescriptions from Firestore
        getPrescriptions(uid)
    }

    // Function to retrieve active prescriptions from Firestore
    private fun getPrescriptions(userId: String) {
        progressBar.visibility = View.VISIBLE  // Show the progress bar while loading data

        // Query the Firestore database for active prescriptions for the current user
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("prescriptions")
            .whereEqualTo("active", true)  // Filter for active prescriptions only
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Clear the current list of prescriptions
                prescriptions.clear()

                // Loop through the query results and add each prescription to the list
                for (document in querySnapshot) {
                    val prescription = document.toObject(Prescription::class.java)
                    prescription.id = document.id  // Store the Firestore document ID
                    prescriptions.add(prescription)  // Add to the list of prescriptions
                }

                // Show or hide the "no prescriptions" message depending on whether prescriptions were found
                if (prescriptions.isEmpty()) {
                    prescriptionText.visibility = View.VISIBLE  // Show the message if no prescriptions
                } else {
                    prescriptionText.visibility = View.GONE  // Hide the message if prescriptions exist
                }

                // Notify the adapter that the data has changed so the RecyclerView can update
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE  // Hide the progress bar after data has loaded
            }
            .addOnFailureListener {
                // Handle any errors that occurred during the data fetch
                progressBar.visibility = View.GONE  // Hide the progress bar in case of an error
            }
    }
}
