package com.pelagohealth.codingchallenge.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pelagohealth.codingchallenge.presentation.MainViewModel
import com.pelagohealth.codingchallenge.ui.theme.PelagoCodingChallengeTheme

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello!",
            modifier = modifier
        )
        Button(onClick = { viewModel.fetchNewFact() }) {
            Text("More facts!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PelagoCodingChallengeTheme {
        MainScreen()
    }
}