package com.pelagohealth.codingchallenge.presentation.ui.screen

import android.content.res.Resources.Theme
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pelagohealth.codingchallenge.R
import com.pelagohealth.codingchallenge.domain.model.Fact
import com.pelagohealth.codingchallenge.presentation.MainViewModel
import com.pelagohealth.codingchallenge.presentation.ui.viewstate.MainViewState
import com.pelagohealth.codingchallenge.ui.theme.PelagoCodingChallengeTheme

@Composable
fun MainScreen() {
    val viewModel: MainViewModel = viewModel()
    val state by viewModel.viewState.collectAsState()

    MainScreen(
        state = state,
        onFetchFact = { viewModel.fetchNewFact() },
        onRemove = { id -> viewModel.removeFact(id) }
    )
}

@Composable
private fun MainScreen(
    state: MainViewState,
    onFetchFact: () -> Unit,
    onRemove: (id: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.fact.text,
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onFetchFact) {
            Text(stringResource(R.string.action_more_facts))
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = state.facts,
                key = { it.id }
            ) { fact ->
                val dismissState = rememberDismissState()
                if (dismissState.isDismissed(DismissDirection.EndToStart) ||
                    dismissState.isDismissed(DismissDirection.StartToEnd)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        LocalView.current.performHapticFeedback((HapticFeedbackConstants.CONFIRM))
                    } else {
                        LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onRemove(fact.id)
                }

                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        val alpha by animateFloatAsState(
                            targetValue = when (dismissState.targetValue) {
                                DismissValue.Default -> 0f
                                else -> 1f
                            },
                            label = "alpha animation"
                        )

                        val color by animateColorAsState(
                            targetValue = when (dismissState.targetValue) {
                                DismissValue.Default -> MaterialTheme.colorScheme.background.copy(
                                    alpha = alpha
                                )

                                else -> MaterialTheme.colorScheme.error.copy(alpha = alpha)
                            },
                            label = "color animation"
                        )

                        val icon = Icons.Outlined.Delete
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .alpha(alpha),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Swipe to Delete Icon",
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }

                    },
                    directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                    dismissContent = {
                        val textAlpha by animateFloatAsState(
                            targetValue = when (dismissState.targetValue) {
                                DismissValue.Default -> 1f
                                else -> 0.5f
                            },
                            label = "text alpha animation"
                        )
                        val textColor by animateColorAsState(
                            targetValue = when (dismissState.targetValue) {
                                DismissValue.Default -> MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                                else -> MaterialTheme.colorScheme.onError.copy(alpha = textAlpha)
                            },
                            label = "text color animation"
                        )
                        Box {
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .requiredHeight(56.dp),
                                text = fact.text,
                                color = textColor,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview(@PreviewParameter(LoremIpsum::class) text: String) {
    PelagoCodingChallengeTheme {
        MainScreen(
            state = MainViewState(
                fact = Fact(text = text.take(200), "", "0"),
                facts = listOf(
                    Fact(text.take(100), "", "1"),
                    Fact(text.takeLast(100), "", "2"),
                    Fact(text.takeLast(150), "", "3")
                )
            ),
            onFetchFact = {},
            onRemove = {})
    }
}