package com.pelagohealth.codingchallenge.presentation.ui.screen

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.pelagohealth.codingchallenge.R
import com.pelagohealth.codingchallenge.domain.model.Fact
import com.pelagohealth.codingchallenge.presentation.viewmodel.MainViewModel
import com.pelagohealth.codingchallenge.presentation.ui.viewstate.MainViewState
import com.pelagohealth.codingchallenge.ui.theme.PelagoCodingChallengeTheme
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.viewState.collectAsState()

    MainScreen(
        state = state,
        onFetchFact = { viewModel.fetchNewFact() },
        onRemove = { id -> viewModel.removeFact(id) },
        onUndo = { id, index -> viewModel.undoRemove(id, index) },
        onConfirmRemoval = { viewModel.confirmRemoval() }
    )
}

@Composable
fun MainScreen(
    state: MainViewState,
    onFetchFact: () -> Unit,
    onRemove: (id: String) -> Unit,
    onUndo: (id: String, index: Int) -> Unit,
    onConfirmRemoval: () -> Unit
) {
    val lazyColumnState = rememberLazyListState()
    LaunchedEffect(state.facts) {
        lazyColumnState.animateScrollToItem(0)
    }
    var undoId by remember {
        mutableStateOf("")
    }
    var showUndo by remember {
        mutableStateOf(false)
    }
    var undoIndex by remember {
        mutableIntStateOf(-1)
    }
    var shouldUndo by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val actionLabel = stringResource(R.string.action_undo)
    val message = stringResource(R.string.fact_removed)

    LaunchedEffect(showUndo, undoId, undoIndex) {
        if (showUndo && undoIndex >= 0) {
            snackbarHostState.currentSnackbarData?.dismiss()
            onRemove(undoId)
            coroutineScope.launch {
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short
                )
                when (snackbarResult) {
                    SnackbarResult.ActionPerformed -> {
                        shouldUndo = true
                        onUndo(undoId, undoIndex)
                    }

                    SnackbarResult.Dismissed -> {
                        shouldUndo = false
                        onConfirmRemoval()
                    }
                }
            }
        }
    }

    val errorMessage = stringResource(R.string.error_message, state.error?.message ?: "")
    LaunchedEffect(key1 = state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
        }
    }

    val context = LocalContext.current
    var showTips by remember {
        mutableStateOf(false)
    }
    val tips = stringResource(id = R.string.text_tips)
    LaunchedEffect(key1 = showTips) {
        if (showTips) {
            snackbarHostState.showSnackbar(
                message = tips,
                duration = SnackbarDuration.Short
            )
            showTips = false
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { contentPadding ->
        Surface(
            modifier = Modifier.padding(paddingValues = contentPadding)
        ) {
            val configuration = LocalConfiguration.current
            ConstraintLayout(Modifier.fillMaxSize()) {
                val (currentRef, buttonRef, listRef, loadingRef) = createRefs()
                if (state.isLoading) {
                    LoadingIndicator(
                        Modifier.constrainAs(loadingRef) {
                            top.linkTo(parent.top, margin = 16.dp)
                            bottom.linkTo(buttonRef.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        })
                } else {
                    Text(
                        modifier = Modifier.constrainAs(currentRef) {
                            top.linkTo(parent.top, margin = 16.dp)
                            bottom.linkTo(buttonRef.top, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                Dimension.wrapContent
                            } else {
                                Dimension.fillToConstraints
                            }
                        }
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                            .combinedClickable(
                                onClick = {
                                    showTips = true
                                },
                                onLongClick = {
                                    val urlIntent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse(state.fact.url))
                                    context.startActivity(urlIntent)
                                }
                            ),
                        text = state.fact.text,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Button(
                    modifier = Modifier.constrainAs(buttonRef) {
                        bottom.linkTo(listRef.top, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.value(216.dp)
                    },
                    onClick = onFetchFact
                ) {
                    Text(stringResource(R.string.action_more_facts))
                }
                LazyColumn(
                    modifier = Modifier
                        .constrainAs(listRef) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom, margin = 16.dp)
                            height = Dimension.value(216.dp)
                        }
                        .testTag("bottom_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = lazyColumnState
                ) {
                    itemsIndexed(
                        items = state.facts,
                        key = { _: Int, t: Fact -> t.id }
                    ) { i: Int, fact: Fact ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            LaunchedEffect(key1 = shouldUndo) {
                                if (shouldUndo) {
                                    dismissState.reset()
                                }
                            }
                        }

                        DismissibleItem(
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    showTips = true
                                },
                                onLongClick = {
                                    val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fact.url))
                                    context.startActivity(urlIntent)
                                }
                            ),
                            dismissState = dismissState,
                            fact = fact,
                            onRemove = { id ->
                                undoId = id
                                showUndo = true
                                undoIndex = i
                            }
                        )
                        if (i < state.facts.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.loading_random_facts),
            textAlign = TextAlign.Center
        )
        LinearProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .testTag("loading_indicator")
        )
    }
}

@Composable
private fun DismissibleItem(
    modifier: Modifier = Modifier,
    dismissState: SwipeToDismissBoxState,
    fact: Fact,
    onRemove: (id: String) -> Unit
) {
    if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd ||
        dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            LocalView.current.performHapticFeedback((HapticFeedbackConstants.CONFIRM))
        } else {
            LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        onRemove(fact.id)
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            val alpha by animateFloatAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> 0f
                    else -> 1f
                },
                label = "alpha animation"
            )

            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.background.copy(
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
        content = {
            val textAlpha by animateFloatAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> 1f
                    else -> 0.5f
                },
                label = "text alpha animation"
            )
            val textColor by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                    else -> MaterialTheme.colorScheme.onError.copy(alpha = textAlpha)
                },
                label = "text color animation"
            )
            Box {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .requiredHeight(56.dp)
                        .wrapContentHeight(),
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
                fact = Fact(text = text.take(10), "", "0"),
                facts = listOf(
                    Fact(text.take(10), "", "1"),
                    Fact(text.takeLast(100), "", "2"),
                    Fact(text.takeLast(150), "", "3")
                )
            ),
            onFetchFact = {},
            onRemove = {},
            onUndo = { _, _ -> },
            onConfirmRemoval = {})
    }
}

@Preview(showBackground = true, device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun MainScreenPreviewLandscape(@PreviewParameter(LoremIpsum::class) text: String) {
    PelagoCodingChallengeTheme {
        MainScreen(
            state = MainViewState(
                fact = Fact(text = text.take(500), "", "0"),
                facts = listOf(
                    Fact(text.take(10), "", "1"),
                    Fact(text.takeLast(100), "", "2"),
                    Fact(text.takeLast(150), "", "3")
                )
            ),
            onFetchFact = {},
            onRemove = {},
            onUndo = { _, _ -> },
            onConfirmRemoval = {})
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewLoading(@PreviewParameter(LoremIpsum::class) text: String) {
    PelagoCodingChallengeTheme {
        MainScreen(
            state = MainViewState(
                isLoading = true
            ),
            onFetchFact = {},
            onRemove = {},
            onUndo = { _, _ -> },
            onConfirmRemoval = {})
    }
}