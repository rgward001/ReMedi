package com.example.remedi

import com.google.firebase.Timestamp

data class Prescription(
    val dosage: String? = null,
    val endDate: Timestamp? = null,
    val frequency: String? = null,
    val isActive: Boolean = false,
    val name: String? = null,
    val notes: String? = null,
    val prescribingDoctor: String? = null,
    val startDate: Timestamp? = null
)
