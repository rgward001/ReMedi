package com.example.remedi

data class ToDoDose(
    val prescriptionId: String,
    val prescriptionName: String,
    val scheduledTime: String,
    var isTaken: Boolean = false
)
