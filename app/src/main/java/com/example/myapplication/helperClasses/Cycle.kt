package com.example.myapplication.helperClasses


data class Cycle(
    val id: Long, // Изменили тип на Long
    val startDate: String,
    val endDate: String,
    val length: Int,
    val symptoms: String
)


data class Symptom(
    val name: String  // Название симптома
)
