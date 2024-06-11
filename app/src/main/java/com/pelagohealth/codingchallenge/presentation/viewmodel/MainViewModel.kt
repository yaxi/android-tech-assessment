package com.pelagohealth.codingchallenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pelagohealth.codingchallenge.data.repository.Resource
import com.pelagohealth.codingchallenge.data.repository.FactRepository
import com.pelagohealth.codingchallenge.domain.CoroutineDispatchers
import com.pelagohealth.codingchallenge.domain.model.Fact
import com.pelagohealth.codingchallenge.presentation.ui.viewstate.MainViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FactRepository,
    private val dispatchers: CoroutineDispatchers
): ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _viewState.asStateFlow()
    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        setState { copy(error = throwable) }
    }

    private val undoStack by lazy {
        Stack<Fact>()
    }

    init {
        fetchNewFact()
    }

    fun fetchNewFact() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            setState { copy(isLoading = true, error = null) }
            val result = repository.getFact()
            if (result is Resource.Success) {
                val fact = result.data
                setState {
                    val list = ArrayDeque(_viewState.value.facts)
                    val current = _viewState.value.fact
                    if (current.id.isNotEmpty()) {
                        list.addFirst(current)
                    }
                    trimList(list)
                    copy(
                        fact = fact,
                        facts = list,
                        isLoading = false
                    )
                }
            } else {
                val error = (result as Resource.Error).error
                setState {
                    copy(isLoading = false, error = error)
                }
            }
        }
    }

    fun removeFact(id: String) {
        val undoFact = _viewState.value.facts.find { it.id == id }
        undoStack.push(undoFact)
        val facts = _viewState.value.facts.toMutableList().filterNot {
            it.id == id
        }

        setState { copy(facts = facts) }
    }

    fun confirmRemoval() {
        // clears the undo cache
        undoStack.clear()
    }

    fun undoRemove(id: String, index: Int) {
        if (undoStack.isNotEmpty()) {
            val uf = undoStack.pop()
            val list = ArrayDeque(_viewState.value.facts).also { l -> l.add(index, uf) }
            trimList(list)
            setState { copy(facts = list) }
        }
    }

    private fun trimList(list: ArrayDeque<Fact>) {
        if (list.size >= 3) {
            for (i in 3 until list.size) {
                list.removeLast()
            }
        }
    }

    private fun setState(reducer: MainViewState.() -> MainViewState) {
        viewModelScope.launch(dispatchers.main + exceptionHandler) {
            _viewState.value = reducer(_viewState.value)
        }
    }
}