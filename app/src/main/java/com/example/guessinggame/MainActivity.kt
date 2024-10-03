package com.example.guessinggame

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.guessinggame.ui.theme.GuessingGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuessingGameTheme {
                val viewModel = GameViewModel()
                    GameScreen(viewModel)
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState = viewModel.gameStatePublic.value
    val wideScreen = calculateCurrentWindowInfo().isWideScreen
    //display game state
    Scaffold { padding -> Modifier.padding(padding) }
    if (wideScreen) {
        //wide screen layout
    } else {
        //narrow screen layout
    }
}

@Composable
fun HintPanes(game: GameViewModel, modifier: Modifier = Modifier) {
    var maxHintUsed by remember { mutableStateOf(false) }
    val context = LocalContext.current // Get the current context to use toast

    ElevatedButton(onClick = { maxHintUsed = game.onHintClick() }) {
        Text("Hint")
    }
    //check if hint can be used
    if (maxHintUsed) {
        //show toast message
        Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
    } else {
        val numHints = game.gameStatePublic.value.hintsUsed
        val lettersUsed = game.gameStatePublic.value.lettersUsed
        val wordToGuess = game.gameStatePublic.value.wordToGuess

        when (numHints) {
            1 -> Toast.makeText(context, "Hint 1: This object is a fruit", Toast.LENGTH_SHORT).show()
            2 -> game.disableLetters(lettersUsed, wordToGuess)
            3 -> Text("Hint 3: the vowels are ${game.showVowels()}")
        }
        game.hintUsed()
    }
}

@Composable
fun LetterPanes(lettersUsed: Set<Char>, game: GameViewModel) {
    val letterList = ('A'..'Z').toList()
    //disable letters that have already been used
    LazyVerticalGrid(columns = GridCells.Fixed(6)) {
        items(letterList) { letter ->
            Button(onClick = { game.onLetterClick(letter) }, enabled = letter !in lettersUsed) {
                Text(letter.toString())
            }
        }
    }
}

@Composable
fun calculateCurrentWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Set a breakpoint for wide vs narrow screens (600dp is commonly used)
    val isWideScreen = screenWidth >= 600

    return WindowInfo(
        isWideScreen = isWideScreen
    )
}

data class WindowInfo(
    val isWideScreen: Boolean
)

