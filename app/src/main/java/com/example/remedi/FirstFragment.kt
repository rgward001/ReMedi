package com.example.remedi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class FirstFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    private fun getPrescriptions(user : String){

    }
}
