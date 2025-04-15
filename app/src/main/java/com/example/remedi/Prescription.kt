package com.example.remedi

import com.google.firebase.Timestamp

data class Prescription(
    var id: String? = null,
    var name: String? = null,
    var dosage: String? = null,
    var frequency: String? = null,
    var startDate: Timestamp? = null,
    var endDate: Timestamp? = null,
    var prescribingDoctor: String? = null,
    var notes: String? = null,
    var isActive: Boolean = true

)

