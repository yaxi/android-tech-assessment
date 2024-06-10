package com.pelagohealth.codingchallenge.presentation.ui.screen

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
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
        onDismiss = { id -> viewModel.removeFact(id) }
    )
}

@Composable
private fun MainScreen(
    state: MainViewState,
    onFetchFact: () -> Unit,
    onDismiss: (id: String) -> Unit
) {
    val lazyColumnState = rememberLazyListState()
    LaunchedEffect(state.facts) {
        lazyColumnState.animateScrollToItem(0)
    }

    Surface {
        ConstraintLayout(Modifier.fillMaxSize()) {
            val (currentRef, buttonRef, listRef) = createRefs()
            Text(
                modifier = Modifier
                    .constrainAs(currentRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(buttonRef.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(16.dp),
                text = state.fact.text
            )
            Button(
                modifier = Modifier.constrainAs(buttonRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                onClick = onFetchFact
            ) {
                Text(stringResource(R.string.action_more_facts))
            }
            LazyColumn(
                modifier = Modifier.constrainAs(listRef) {
                    top.linkTo(buttonRef.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                },
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyColumnState
            ) {
                items(
                    items = state.facts,
                    key = { it.id }
                ) { fact ->
                    DismissibleItem(fact, onDismiss)
                }
            }
        }
    }
}

@Composable
private fun DismissibleItem(
    fact: Fact,
    onRemove: (id: String) -> Unit
) {
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
            onDismiss = {})
    }
}