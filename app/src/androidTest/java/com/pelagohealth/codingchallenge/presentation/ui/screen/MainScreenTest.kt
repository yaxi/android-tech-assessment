package com.pelagohealth.codingchallenge.presentation.ui.screen

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pelagohealth.codingchallenge.R
import com.pelagohealth.codingchallenge.domain.model.Fact
import com.pelagohealth.codingchallenge.presentation.ui.viewstate.MainViewState
import com.pelagohealth.codingchallenge.presentation.viewmodel.MainViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coVerify
import io.mockk.mockk
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context

    @Before
    fun setup() {
        context = composeTestRule.activity
    }

    @Test
    fun when_launch_expect_fact_moreFactButton_and_emptyList() {
        val fact = Fact("Sample Fact", "", "")
        composeTestRule.setContent {
            MainScreen(
                state = MainViewState(fact = fact, facts = emptyList()),
                onFetchFact = {},
                onRemove = {},
                onUndo = { _, _ -> },
                onConfirmRemoval = {}
            )
        }
        composeTestRule.apply {
            onNodeWithText(fact.text)
                .assertIsDisplayed()
            onNodeWithText(context.getString(R.string.action_more_facts))
                .assertIsDisplayed()
            onNodeWithTag("bottom_list")
                .assertExists()
            onNodeWithTag("loading_indicator")
                .assertIsNotDisplayed()
        }
    }

    @Test
    fun when_launch_expect_loading_indicator() {
        val fact = Fact("Sample Fact", "", "")
        composeTestRule.setContent {
            MainScreen(
                state = MainViewState(fact = fact, facts = emptyList(), isLoading = true),
                onFetchFact = {},
                onRemove = {},
                onUndo = { _, _ -> },
                onConfirmRemoval = {}
            )
        }
        composeTestRule.apply {
            waitForIdle()
            mainClock.autoAdvance = false
            onNodeWithText(fact.text)
                .assertIsNotDisplayed()
            onNodeWithText(context.getString(R.string.action_more_facts))
                .assertIsDisplayed()
            onNodeWithTag("loading_indicator")
                .assertIsDisplayed()
            mainClock.advanceTimeBy(2500L)
            mainClock.autoAdvance = true
            onNodeWithText(context.getString(R.string.loading_random_facts))
                .assertIsDisplayed()
        }
    }

    @Ignore("Not be able to test it for now")
    @Test
    fun when_click_on_button_expect_new_fact_shown_and_previous_fact_added_to_list() {
        val fact1 = Fact(
            text = "splendide",
            url = "https://search.yahoo.com/search?p=massa",
            id = "eum"
        )
        val fact2 = Fact(
            text = "sodales",
            url = "https://search.yahoo.com/search?p=gravida",
            id = "audire"
        )

        val viewModel = mockk<MainViewModel>(relaxed = true)
        composeTestRule.apply {
            val state = MainViewState(fact = fact1, facts = emptyList())
            setContent {
                MainScreen(
                    state = state,
                    onFetchFact = { },
                    onRemove = {},
                    onUndo = { _, _ -> },
                    onConfirmRemoval = {}
                )
            }
            onNodeWithText(context.getString(R.string.action_more_facts))
                .performClick()
            coVerify { viewModel.fetchNewFact() }
            onNodeWithTag("bottom_list")
                .onChildAt(0)
                .assert(hasText(fact1.text))
        }
    }

    @Test
    fun when_swipeRight_on_item_expect_snackbar_shown() {

        val fact4 = Fact(
            text = "porttitor",
            url = "https://www.google.com/#q=mutat",
            id = "elit"
        )

        composeTestRule.apply {
            val state = MainViewState(fact = fact4, facts = provide4DummyItems())
            setContent {
                MainScreen(
                    state = state,
                    onFetchFact = { },
                    onRemove = {  },
                    onUndo = { _, _ -> },
                    onConfirmRemoval = {}
                )
            }

            mainClock.autoAdvance = false
            onNodeWithTag("bottom_list")
                .onChildAt(0)
                .performTouchInput { swipeRight() }
            mainClock.advanceTimeBy(1500L)
            mainClock.autoAdvance = true
            onNodeWithText(context.getString(R.string.action_undo))
                .assertIsDisplayed()
        }
    }

    @Test
    fun when_swipeLeft_on_item_expect_snackbar_shown() {
        val fact4 = Fact(
            text = "porttitor",
            url = "https://www.google.com/#q=mutat",
            id = "elit"
        )

        composeTestRule.apply {
            val state = MainViewState(fact = fact4, facts = provide4DummyItems())
            setContent {
                MainScreen(
                    state = state,
                    onFetchFact = { },
                    onRemove = { },
                    onUndo = { _, _ -> },
                    onConfirmRemoval = {}
                )
            }

            mainClock.autoAdvance = false
            onNodeWithTag("bottom_list")
                .onChildAt(0)
                .performTouchInput { swipeLeft() }
            mainClock.advanceTimeBy(1500L)
            mainClock.autoAdvance = true
            onNodeWithText(context.getString(R.string.action_undo))
                .assertIsDisplayed()
        }
    }

    @Test
    fun when_tap_on_item_expect_snackbar_with_tips_shown() {
        val fact4 = Fact(
            text = "porttitor",
            url = "https://www.google.com/#q=mutat",
            id = "elit"
        )

        composeTestRule.apply {
            val state = MainViewState(fact = fact4, facts = provide4DummyItems())
            setContent {
                MainScreen(
                    state = state,
                    onFetchFact = { },
                    onRemove = { },
                    onUndo = { _, _ -> },
                    onConfirmRemoval = { }
                )
            }

            onNodeWithTag("bottom_list")
                .onChildAt(0)
                .performClick()
            onNodeWithText(context.getString(R.string.text_tips))
                .assertIsDisplayed()
        }
    }

    @Test
    fun when_tap_on_text_expect_snackbar_with_tips_shown() {

        val fact4 = Fact(
            text = "porttitor",
            url = "https://www.google.com/#q=mutat",
            id = "elit"
        )

        composeTestRule.apply {
            val state = MainViewState(fact = fact4, facts = provide4DummyItems())
            setContent {
                MainScreen(
                    state = state,
                    onFetchFact = { },
                    onRemove = { },
                    onUndo = { _, _ -> },
                    onConfirmRemoval = { }
                )
            }

            onNodeWithText(fact4.text)
                .performClick()
            onNodeWithText(context.getString(R.string.text_tips))
                .assertIsDisplayed()
        }
    }

    @Test
    fun when_long_click_on_item_expect_launch_intent_with_url() {

        val fact4 = Fact(
            text = "porttitor",
            url = "https://www.google.com/#q=mutat",
            id = "elit"
        )

        val facts = provide4DummyItems()

        Intents.init()

        composeTestRule.apply {
            val state = MainViewState(fact = fact4, facts = facts)
            setContent {
                MainScreen(
                    state = state,
                    onFetchFact = { },
                    onRemove = { },
                    onUndo = { _, _ -> },
                    onConfirmRemoval = { }
                )
            }
            val expectedIntent = Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(facts[0].url)
            )
            onNodeWithTag("bottom_list")
                .onChildAt(0)
                .performTouchInput { longClick() }
            Intents.intended(expectedIntent)
            Intents.release()
        }
    }

    @Test
    fun when_long_click_on_text_expect_launch_intent_with_url() {

        val fact4 = Fact(
            text = "porttitor",
            url = "https://www.google.com/#q=mutat",
            id = "elit"
        )

        val facts = provide4DummyItems()

        Intents.init()

        composeTestRule.apply {
            val state = MainViewState(fact = fact4, facts = facts)
            setContent {
                MainScreen(
                    state = state,
                    onFetchFact = { },
                    onRemove = { },
                    onUndo = { _, _ -> },
                    onConfirmRemoval = { }
                )
            }
            val expectedIntent = Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(fact4.url)
            )
            onNodeWithText(fact4.text)
//                .performSemanticsAction(SemanticsActions.OnLongClick)
                .performTouchInput { longClick() }
            Intents.intended(expectedIntent)
            Intents.release()
        }
    }

    private fun provide4DummyItems(): List<Fact> {
        val fact1 = Fact(
            text = "splendide",
            url = "https://search.yahoo.com/search?p=massa",
            id = "eum"
        )
        val fact2 = Fact(
            text = "sodales",
            url = "https://search.yahoo.com/search?p=gravida",
            id = "audire"
        )
        val fact3 = Fact(
            text = "error",
            url = "https://search.yahoo.com/search?p=fermentum",
            id = "scripta"
        )

        return listOf(fact3, fact2, fact1)
    }
}