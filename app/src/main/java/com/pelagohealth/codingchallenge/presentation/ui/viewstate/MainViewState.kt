package com.pelagohealth.codingchallenge.presentation.ui.viewstate

import com.pelagohealth.codingchallenge.domain.model.Fact
import java.util.LinkedList

data class MainViewState(
    val fact: Fact = Fact(text = "", url = "", id = ""),
    val facts: List<Fact> = ArrayDeque(),
    val isLoading: Boolean = false,
    val error: Throwable? = null
)