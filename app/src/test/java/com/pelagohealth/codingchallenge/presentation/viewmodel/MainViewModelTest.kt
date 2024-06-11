package com.pelagohealth.codingchallenge.presentation.viewmodel

import app.cash.turbine.test
import com.pelagohealth.codingchallenge.CoroutineTestRule
import com.pelagohealth.codingchallenge.data.repository.FactRepository
import com.pelagohealth.codingchallenge.data.repository.Resource
import com.pelagohealth.codingchallenge.domain.CoroutineDispatchers
import com.pelagohealth.codingchallenge.domain.model.Fact
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: MainViewModel
    private val repository = mockk<FactRepository>(relaxed = true)
    private val dispatchers by lazy {
        CoroutineDispatchers(
            io = coroutineTestRule.dispatcher,
            default = coroutineTestRule.dispatcher,
            main = coroutineTestRule.dispatcher
        )
    }

    @Before
    fun setup() {
        val dummyFact = Fact("", "", "")
        coEvery { repository.getFact() }.returns(Resource.Success(dummyFact))
        viewModel = MainViewModel(
            repository = repository,
            dispatchers = dispatchers
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `getFact with Exception expect error being set to ViewState`() {
        val dummyError = Throwable("Dummy Network Error")
        runTest {
            coEvery { repository.getFact() }.throws(dummyError)
            viewModel.fetchNewFact()
            viewModel.viewState.test {
                val actual = awaitItem().error
                assertEquals(dummyError, actual)
            }
        }
    }

    @Test
    fun `when initialised expect getFact got called`() {
        runTest {
            coVerify { repository.getFact() }
        }
    }

    @Test
    fun `when getFact is Success expect view state is updated`() {
        val dummyFact = Fact("test text", "http://foobar.com", "id-3456")
        runTest {
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact))
            viewModel.fetchNewFact()
            viewModel.viewState.test {
                val state = expectMostRecentItem()
                val actual = state.fact
                assertEquals(dummyFact.id, actual.id)
                assertEquals(dummyFact.url, actual.url)
                assertEquals(dummyFact.text, actual.text)
                assertEquals(
                    0,
                    state.facts.size
                ) // dummyFact is the current fact, no items are in the list
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `getFact is Success and called more than 3 times expect view state only keep the previous last 3 result`() {
        val dummyFact1 = Fact("test text1", "http://foobar.com/1", "id-1")
        val dummyFact2 = Fact("test text2", "http://foobar.com/2", "id-2")
        val dummyFact3 = Fact("test text3", "http://foobar.com/3", "id-3")
        val dummyFact4 = Fact("test text4", "http://foobar.com/4", "id-4")
        val dummyFact5 = Fact("test text5", "http://foobar.com/5", "id-5")

        runTest {
            coEvery { repository.getFact() }
                .returns(Resource.Success(dummyFact1)) // initial fact, will be removed
                .andThen(Resource.Success(dummyFact2)) // 3rd in the list
                .andThen(Resource.Success(dummyFact3)) // 2nd in the list
                .andThen(Resource.Success(dummyFact4)) // first in the list
                .andThen(Resource.Success(dummyFact5)) // shown as current Fact
            viewModel = MainViewModel(repository, dispatchers)
            viewModel.fetchNewFact()
            viewModel.fetchNewFact()
            viewModel.fetchNewFact()
            viewModel.fetchNewFact()

            viewModel.viewState.test {
                val state = expectMostRecentItem()

                assertEquals(3, state.facts.size)

                assertEquals(dummyFact4, state.facts[0])
                assertEquals(dummyFact3, state.facts[1])
                assertEquals(dummyFact2, state.facts[2])

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `getFact is Success with empty id in Fact expect it not added to facts in viewState `() {
        val dummyFact1 = Fact("test text1", "http://foobar.com/1", "")

        runTest {
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact1))
            viewModel.fetchNewFact()
            viewModel.viewState.test {
                val state = awaitItem()
                assertNull(state.facts.find { it.text == dummyFact1.text })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `getFact is Error expect error being set in ViewState and isLoading is False`() {
        val dummyError = Throwable("dummy exception")
        runTest {
            coEvery { repository.getFact() }.returns(Resource.Error(dummyError))
            viewModel.fetchNewFact()
            viewModel.viewState.test {
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertEquals(dummyError, state.error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `removeFact expect the Fact being removed from Facts in ViewState`() {
        val dummyFact1 = Fact("test text1", "http://foobar.com/1", "id-1")
        val dummyFact2 = Fact("test text2", "http://foobar.com/2", "id-2")
        val dummyFact3 = Fact("test text3", "http://foobar.com/3", "id-3")

        runTest {
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact1))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact2))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact3))
            viewModel.fetchNewFact()

            viewModel.removeFact(dummyFact2.id)

            viewModel.viewState.test {
                assertNull(awaitItem().facts.find { it.id == dummyFact2.id })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `confirmRemoval expect undoRemove not changing Facts in ViewState`() {
        val dummyFact1 = Fact("test text1", "http://foobar.com/1", "id-1")
        val dummyFact2 = Fact("test text2", "http://foobar.com/2", "id-2")
        val dummyFact3 = Fact("test text3", "http://foobar.com/3", "id-3")

        runTest {
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact1))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact2))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact3))
            viewModel.fetchNewFact()

            viewModel.removeFact(dummyFact2.id)
            viewModel.confirmRemoval()

            viewModel.undoRemove(dummyFact2.id, 1)

            viewModel.viewState.test {
                val facts = awaitItem().facts
                assertNull(facts.find { it.id == dummyFact2.id })
            }
        }

    }

    @Test
    fun `undoRemove with id and index, expect Fact with id being inserted into index in Facts of ViewState`() {
        val dummyFact1 = Fact("test text1", "http://foobar.com/1", "id-1")
        val dummyFact2 = Fact("test text2", "http://foobar.com/2", "id-2")
        val dummyFact3 = Fact("test text3", "http://foobar.com/3", "id-3")

        runTest {
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact1))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact2))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact3))
            viewModel.fetchNewFact()

            viewModel.removeFact(dummyFact2.id)
            viewModel.undoRemove(dummyFact2.id, 1)

            viewModel.viewState.test {
                val facts = awaitItem().facts
                assertNotNull(facts.find { it.id == dummyFact2.id })
                assertEquals(dummyFact2, facts[1])
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `undoRemove with id and index, with current Facts has 3 items, expect Fact last inserted in Facts of ViewState got removed`() {
        val dummyFact1 = Fact("test text1", "http://foobar.com/1", "id-1")
        val dummyFact2 = Fact("test text2", "http://foobar.com/2", "id-2")
        val dummyFact3 = Fact("test text3", "http://foobar.com/3", "id-3")
        val dummyFact4 = Fact("test text4", "http://foobar.com/4", "id-4")

        runTest {
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact1))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact2))
            viewModel.fetchNewFact()
            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact3))
            viewModel.fetchNewFact()

            viewModel.removeFact(dummyFact2.id)

            coEvery { repository.getFact() }.returns(Resource.Success(dummyFact4))
            viewModel.fetchNewFact()

            viewModel.undoRemove(dummyFact2.id, 1)

            viewModel.viewState.test {
                val facts = awaitItem().facts
                assertNull(facts.find { it.id == dummyFact4.id })
                assertNotNull(facts.find { it.id == dummyFact2.id })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}