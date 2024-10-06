package com.example.guessinggame

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val MAX_INCORRECT_GUESSES = 6

class GameViewModel : ViewModel() {

    data class GameState(
        val wordToGuess: String = "",
        val userGuess: String = "_".repeat(wordToGuess.length),
        val incorrectGuesses: Int = 0,
        val hintsUsed: Int = 0,
        val lettersUsed: Set<Char> = emptySet(),
        val fruitHint: String = "Fruit",
    )

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    enum class GameStatus { WON, LOST, IN_PROGRESS }

    val gameStatus: GameStatus
        get() = when {
            gameState.value.userGuess == gameState.value.wordToGuess -> GameStatus.WON
            gameState.value.incorrectGuesses >= MAX_INCORRECT_GUESSES -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

    fun startGame() {
        val newWord = words.random()
        _gameState.value = GameState(wordToGuess = newWord, userGuess = "_".repeat(newWord.length))
    }

    fun onLetterClick(letter: Char) : Boolean {
        val currentGameState = gameState.value
        return if (letter in currentGameState.wordToGuess) {
            val newUserGuess =
                currentGameState.wordToGuess.mapIndexed { index, w ->
                    when (w) {
                        letter -> letter
                        else -> currentGameState.userGuess[index]
                    }
                }.joinToString("")
            _gameState.value = currentGameState.copy(
                userGuess = newUserGuess,
                lettersUsed = currentGameState.lettersUsed + letter
            )
            true
        } else {
            _gameState.value = currentGameState.copy(
                incorrectGuesses = currentGameState.incorrectGuesses + 1,
                lettersUsed = currentGameState.lettersUsed + letter
            )
            false
        }
    }

    fun onHintClick(): Boolean {
        val currentGameState = gameState.value
        return if (currentGameState.hintsUsed < 3 && currentGameState.incorrectGuesses < MAX_INCORRECT_GUESSES - 1) {
            _gameState.value = currentGameState.copy(hintsUsed = currentGameState.hintsUsed + 1)

            if (currentGameState.hintsUsed == 1) {
                disableLetters(currentGameState.lettersUsed, currentGameState.wordToGuess)
            }
            onHintPenalty()
            false
        } else {
            true
        }
    }

    fun showVowels(): String {
        val wordToGuess = gameState.value.wordToGuess
        val seenVowels = mutableSetOf<Char>()
        return wordToGuess.filter { it in "AEIOU" && it !in seenVowels && seenVowels.add(it) }
    }

    private fun onHintPenalty() {
        _gameState.value = gameState.value.copy(incorrectGuesses = gameState.value.incorrectGuesses + 1)
    }

    private fun disableLetters(lettersUsed: Set<Char>, wordToGuess: String){
        val lettersToDisable = ('A'..'Z').toSet() - lettersUsed - wordToGuess.toSet()
        val numToDisable = lettersToDisable.size / 2
        val disabledLetters = lettersToDisable.shuffled().take(numToDisable)
        _gameState.value = gameState.value.copy(lettersUsed = lettersUsed + disabledLetters)
    }
}

// Consider moving this to a separate file or object if you have more game data
// fruits
val words = listOf(
    "APPLE",
    "BANANA",
    "CHERRY",
    "DATE",
    "ELDERBERRY",
    "FIG",
    "GRAPE",
    "HONEYDEW",
    "IMPERIAL",
    "JASMINE",
    "KIWI",
    "LEMON",
    "MELON",
    "Nectarine",
    "ORANGE",
    "PEAR",
    "RASPBERRY",
    "STRAWBERRY",
    "TANGERINE"
)