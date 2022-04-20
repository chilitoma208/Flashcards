package com.example.flashcards

import androidx.lifecycle.ViewModel

class TestViewModel: ViewModel() {
    var counter: Int = 0
    var check: Boolean = false
    var question: String = ""
    var answer: String = ""
    var explain: String = ""
    var selectionA: String = ""
    var selectionB: String = ""
    var selectionC: String = ""
    var selectionD: String = ""
    var answerA: String = ""
    var answerB: String = ""
    var answerC: String = ""
    var answerD: String = ""

    override fun onCleared() {
        super.onCleared()
    }
}
