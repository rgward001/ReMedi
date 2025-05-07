package com.example.remedi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Fragment for user account settings, including logout and delete account options
class FourthFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and initialize Firebase instances
        val view = inflater.inflate(R.layout.fragment_fourth, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get references to logout and delete account buttons
        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_account_button)

        // Logout: Confirm with user, sign out, then return to home screen
        logoutButton.setOnClickListener {
            showConfirmationDialog("Logout") {
                auth.signOut()
                goToHomeScreen()
            }
        }

        // Delete account: Confirm with user, delete Firestore data, then auth account
        deleteButton.setOnClickListener {
            showConfirmationDialog("Delete Account") {
                val uid = auth.currentUser?.uid ?: return@showConfirmationDialog

                // Step 1: Delete main user document from Firestore
                db.collection("users").document(uid).delete().addOnSuccessListener {
                    // Step 2: Delete associated subcollections
                    deleteSubcollections(uid) {
                        // Step 3: Delete the Firebase Auth user
                        auth.currentUser?.delete()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                goToHomeScreen()
                            } else {
                                showError("Failed to delete account.")
                            }
                        }
                    }
                }.addOnFailureListener {
                    showError("Failed to delete user data from Firestore.")
                }
            }
        }

        return view
    }

    // Shows a confirmation dialog before logout or deletion
    private fun showConfirmationDialog(action: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("$action Confirmation")
            .setMessage("Are you sure you want to $action?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Navigates back to the main screen, clearing the activity stack
    private fun goToHomeScreen() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    // Displays an error alert dialog with a given message
    private fun showError(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    // Deletes 'prescriptions' and 'takenDoses' subcollections for the user
    private fun deleteSubcollections(uid: String, onComplete: () -> Unit) {
        val prescriptionsRef = db.collection("users").document(uid).collection("prescriptions")
        val takenDosesRef = db.collection("users").document(uid).collection("takenDoses")

        // Delete all prescription documents
        prescriptionsRef.get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (doc in snapshot) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnSuccessListener {
                onComplete()
            }.addOnFailureListener {
                showError("Failed to delete prescriptions.")
            }
        }.addOnFailureListener {
            showError("Failed to access prescriptions.")
        }

        // Delete all takenDose documents
        takenDosesRef.get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (doc in snapshot) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnSuccessListener {
                onComplete()
            }.addOnFailureListener {
                showError("Failed to delete prescriptions.")
            }
        }.addOnFailureListener {
            showError("Failed to access prescriptions.")
        }
    }
}

