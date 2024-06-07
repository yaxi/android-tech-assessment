package com.pelagohealth.codingchallenge.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel : ViewModel() {
    fun fetchNewFact() {
        // TODO: get a new fact from the REST API and display it to the user
    }
}