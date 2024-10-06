package com.example.guessinggame

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guessinggame.ui.theme.GuessingGameTheme
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuessingGameTheme {
                val viewModel = GameViewModel()
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun WideScreenLayout(game: GameViewModel, gameState: GameViewModel.GameState) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, // Spaced more evenly
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left section: letters and hint
        Column(
            modifier = Modifier
                .weight(0.5f) // Constrain width to 60% of the screen
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "CHOOSE A LETTER:", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            LetterPanes(gameState.lettersUsed, game)
            HintPane(game)
        }

        // Right section: Hangman figure and guessed word
        Column(
            modifier = Modifier
                .weight(0.4f) // Constrain width to 40% of the screen
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HangmanPane(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                wrongAttempts = gameState.incorrectGuesses
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = gameState.userGuess,
                fontSize = 24.sp,
                fontStyle = Italic,
            )
        }
    }
}


@Composable
fun NarrowScreenLayout(game: GameViewModel, gameState: GameViewModel.GameState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HangmanPane(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            wrongAttempts = gameState.incorrectGuesses
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = gameState.userGuess,
                modifier = Modifier.weight(1f),
                fontSize = 24.sp,
                fontStyle = Italic,
            )
            Spacer(modifier = Modifier.weight(1f))
            HintPane(game)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "CHOOSE A LETTER:")
        Spacer(modifier = Modifier.height(4.dp))
        LetterPanes(gameState.lettersUsed, game)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MainScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val wideScreen = calculateCurrentWindowInfo().isWideScreen

    // Start the game if it's not already started
    if (gameState.wordToGuess.isEmpty()) {
        viewModel.startGame()
    }
    if (viewModel.gameStatus == GameViewModel.GameStatus.IN_PROGRESS) {
        // Display game state
        if (wideScreen) WideScreenLayout(viewModel, gameState) else NarrowScreenLayout(
            viewModel,
            gameState
        )
    } else {
        // Display game over message
        val gameOverMessage = when (viewModel.gameStatus) {
            GameViewModel.GameStatus.WON -> "Congrats! You guessed the word: ${gameState.wordToGuess}! :D"
            GameViewModel.GameStatus.LOST -> "You lost! The word was: ${gameState.wordToGuess}. :("
            else -> "Game Over" // Handle other states if needed
        }
        //center content horizontally and vertically
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = gameOverMessage)
                Button(
                    onClick = { viewModel.startGame() },
                    enabled = viewModel.gameStatus != GameViewModel.GameStatus.IN_PROGRESS
                ) {
                    Text("Play Again")
                }
            }
        }
    }
}

@Composable
fun HintPane(game: GameViewModel) {
    var maxHintUsed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ElevatedButton(onClick = {
        val hintResult = game.onHintClick()
        maxHintUsed = hintResult
        if (!hintResult) {
            displayHint(game, context)
        }
    }) {
        Text("Hint")
    }

    if (maxHintUsed) {
        Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
    }
}

fun displayHint(game: GameViewModel, context: Context) {
    val numHints = game.gameState.value.hintsUsed
    when (numHints) {
        1 -> Toast.makeText(context, "Hint 1: This object is a fruit :P", Toast.LENGTH_LONG).show()
        2 -> Toast.makeText(
            context,
            "Hint 2: Letters not in the word have been disabled!",
            Toast.LENGTH_LONG
        ).show()

        3 -> Toast.makeText(
            context,
            "Hint 3: The vowels in this word are: ${game.showVowels()}",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun LetterPanes(lettersUsed: Set<Char>, game: GameViewModel) {
    val letterList = ('A'..'Z').toList()
    val context = LocalContext.current

    //disable letters that have already been used
    LazyVerticalGrid(columns = GridCells.Adaptive(60.dp), modifier = Modifier.fillMaxWidth()) {
        items(letterList) { letter ->
            Button(
                onClick = { displayLetter(context, game.onLetterClick(letter))},
                enabled = letter !in lettersUsed,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.padding(6.dp)
            ) {
                Text(letter.toString())
            }
        }
    }
}

//display if the letter is correct
fun displayLetter(context: Context, isLetterCorrect: Boolean) {
    Toast.makeText(
        context,
        if (isLetterCorrect) "Correct :D" else "Incorrect :T",
        Toast.LENGTH_SHORT
    ).show()
}

// Calculate the current window size from classroom code
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


// Hangman figure from chatgpt; prompt: how to draw hangman figure in android jetpack compose?
@Composable
fun HangmanPane(modifier: Modifier = Modifier, wrongAttempts: Int = 0) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = min(width, height) / 50 // Adjust stroke width based on size

        // Draw the stand
        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.1f, y = height * 0.9f),
            end = Offset(x = width * 0.1f, y = height * 0.1f),
            strokeWidth = strokeWidth
        ) // Base

        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.1f, y = height * 0.1f),
            end = Offset(x = width * 0.5f, y = height * 0.1f),
            strokeWidth = strokeWidth
        ) // Top Beam

        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.5f, y = height * 0.1f),
            end = Offset(x = width * 0.5f, y = height * 0.2f),
            strokeWidth = strokeWidth
        ) // Rope

        // Draw the Hangman parts: head, body, arms, legs
        drawFullHangman(wrongAttempts, width, height, strokeWidth)
    }
}

fun DrawScope.drawFullHangman(
    wrongAttempts: Int,
    width: Float,
    height: Float,
    strokeWidth: Float
) {
    // Remove parts of the hangman based on the number of wrong attempts
    // Each body part is visible until the incorrect guess count matches

    if (wrongAttempts > 5) {
        // Right Leg
        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.5f, y = height * 0.6f),
            end = Offset(x = width * 0.6f, y = height * 0.75f),
            strokeWidth = strokeWidth
        )
    }
    if (wrongAttempts > 4) {
        // Left Leg
        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.5f, y = height * 0.6f),
            end = Offset(x = width * 0.4f, y = height * 0.75f),
            strokeWidth = strokeWidth
        )
    }
    if (wrongAttempts > 3) {
        // Right Arm
        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.5f, y = height * 0.35f),
            end = Offset(x = width * 0.6f, y = height * 0.45f),
            strokeWidth = strokeWidth
        )
    }
    if (wrongAttempts > 2) {
        // Left Arm
        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.5f, y = height * 0.35f),
            end = Offset(x = width * 0.4f, y = height * 0.45f),
            strokeWidth = strokeWidth
        )
    }
    if (wrongAttempts > 1) {
        // Body
        drawLine(
            color = Color.Black,
            start = Offset(x = width * 0.5f, y = height * 0.3f),
            end = Offset(x = width * 0.5f, y = height * 0.6f),
            strokeWidth = strokeWidth
        )
    }
    if (wrongAttempts > 0) {
        // Head
        drawCircle(
            color = Color.Black,
            radius = width * 0.05f,
            center = Offset(x = width * 0.5f, y = height * 0.25f),
            style = Stroke(width = strokeWidth)
        )
    }
}

