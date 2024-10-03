package com.example.guessinggame

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

const val MAX_INCORRECT_GUESSES = 6

class GameViewModel: ViewModel() {
    data class GameState(
        val wordToGuess: String = "",
        val userGuess: String = "_".repeat(wordToGuess.length),
        val incorrectGuesses: Int = 0,
        val hintsUsed : Int = 0,
        val lettersUsed: Set<Char> = emptySet(),
        val hint1: String = "Fruit",
    )
    private val gameState = mutableStateOf(GameState())
    val gameStatePublic: State<GameState> = gameState

    val isGameWon: Boolean get() = gameState.value.userGuess == gameState.value.wordToGuess
    val isGameLost: Boolean get() = gameState.value.incorrectGuesses >= MAX_INCORRECT_GUESSES

    fun resetGame() {
        val newWord = words.random()
        gameState.value = GameState(wordToGuess = newWord, userGuess = "_".repeat(newWord.length))
    }

    fun onLetterClick(letter: Char) {
        var (wordToGuess, userGuess, incorrectGuesses) = gameState.value
        // check if letter is in word to be guessed
        if (letter in wordToGuess) { // iterate over the letters in the word to guess (gemini)
            userGuess = userGuess.mapIndexed { index, c ->
                if (wordToGuess[index] == letter) letter else c
            }.joinToString("")
            gameState.value = gameState.value.copy(userGuess = userGuess)
        } else {
            gameState.value = gameState.value.copy(incorrectGuesses = incorrectGuesses + 1)
        }
        gameState.value = gameState.value.copy(lettersUsed = gameState.value.lettersUsed + letter)
    }

    fun hintUsed() {
        val incorrectGuesses = gameState.value.incorrectGuesses
        gameState.value = gameState.value.copy(incorrectGuesses = incorrectGuesses + 1)
    }

    fun showVowels(): String {
        //return vowels of word to guess
        val wordToGuess = gameState.value.wordToGuess
        return wordToGuess.filter { it in "AEIOU" }
    }

    fun disableLetters(lettersUsed: Set<Char>, wordToGuess: String) {
        //disable letters that have already been used
        val letterList = ('A'..'Z').toList()
        letterList.filter { it !in lettersUsed && it !in wordToGuess }
        //disable half of the letters left
        for (i in letterList.indices) {
            if (i % 2 == 0) {
                gameState.value = gameState.value.copy(lettersUsed = gameState.value.lettersUsed + letterList[i])
            }
        }
    }

    fun onHintClick() : Boolean {
        val hintsUsed = gameState.value.hintsUsed
        val incorrectGuesses = gameState.value.incorrectGuesses
        return if (hintsUsed < 3 && incorrectGuesses < MAX_INCORRECT_GUESSES - 1) {
            gameState.value = gameState.value.copy(hintsUsed = hintsUsed + 1)
             true
        } else {
             false
        }
    }
}

val words = listOf("apple", "banana", "cherry", "date", "elderberry")