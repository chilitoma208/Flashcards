package com.example.flashcards

import java.io.Serializable

data class TestData (
    val id: Int,
    var check: Boolean,
    val question: String,
    val answer: String,
    val explain: String,
    val selectionA: String,
    val selectionB: String,
    val selectionC: String,
    val selectionD: String,
    val answerA: String,
    val answerB: String,
    val answerC: String,
    val answerD: String
    ): Serializable