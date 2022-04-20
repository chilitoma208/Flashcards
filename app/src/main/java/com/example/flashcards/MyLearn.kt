package com.example.flashcards

data class MyLearn (
    val flash_id: Int,
    var check: Boolean,
    val tag: Int,
    val flash_q: String,
    val flash_a: String,
    val flash_e: String,
    var isExpanded: Boolean = false
)