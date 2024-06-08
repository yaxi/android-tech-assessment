package com.pelagohealth.codingchallenge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pelagohealth.codingchallenge.data.repository.FactRepository
import com.pelagohealth.codingchallenge.domain.CoroutineDispatcherModule
import com.pelagohealth.codingchallenge.domain.CoroutineDispatchers
import com.pelagohealth.codingchallenge.domain.model.Fact
import com.pelagohealth.codingchallenge.presentation.ui.viewstate.MainViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FactRepository,
    private val dispatchers: CoroutineDispatchers
): ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _viewState.asStateFlow()

    fun fetchNewFact() {
        viewModelScope.launch(dispatchers.io) {
            setState { copy(isLoading = true) }
            val fact = repository.getFact()
            setState {
                val list = ArrayDeque(_viewState.value.facts)
                val current = _viewState.value.fact
                list.add(current)
                trimList(list)
                copy(
                    fact = fact,
                    facts = list,
                    isLoading = false
                )
            }
        }
    }

    fun removeFact(id: String) {
        val facts = _viewState.value.facts.toMutableList().filterNot {
            it.id == id
        }
        setState { copy(facts = facts) }
    }

    private fun trimList(list: ArrayDeque<Fact>) {
        if (list.size >= 3) {
            for (i in 3 until list.size) {
                list.removeLast()
            }
        }
    }

    private fun setState(reducer: MainViewState.() -> MainViewState) {
        viewModelScope.launch(dispatchers.main) {
            _viewState.value = reducer(_viewState.value)
        }
    }
}