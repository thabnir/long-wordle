import java.io.InputStream
import java.util.*
import kotlin.collections.HashSet

class Wordle(wordSource: Int = WORDLES, startWord: String? = null, numLetters: Int? = null) {
    val word = if (startWord == null && numLetters == null) {
        getRandomWord(wordSource)
    } else startWord ?: getRandomWord(wordSource, numLetters!!)
    val wordLength = word.length
    val legalWords = getLegalWords(wordLength)
    val hints = ArrayList<Array<Int>>(maxGuesses)
    val guessedWords = ArrayList<String>(maxGuesses)
    val numGuesses: Int
        get() = guessedWords.size
    val guessesLeft: Int
        get() = maxGuesses - guessedWords.size
    val indexLettersCorrect = HashSet<IndexChar>(40) // find a way to prevent duplicates
    val indexLettersMismatched = HashSet<IndexChar>(40) // or don't, IDK, it's just an optimization

    val lettersIncorrect = HashSet<Char>(40)
    val lettersCorrect: List<Char>
        get() = listOf(indexLettersCorrect).flatten().map { it.char }
    val lettersMismatched: List<Char>
        get() = indexLettersMismatched.map { it.char }

    val isWon: Boolean
        get() = word in guessedWords

    val lettersUnused = ArrayList(alphabet)

    val solver = Solver()

    fun makeGuess(str: String): Boolean {
        val guess = str.lowercase()
        val hint = Array(wordLength) { INCORRECT }
        val unhitCharList = ArrayList(word.toCharArray().toList())
        for (i in guess.indices) {
            if (guess[i] == word[i]) {
                hint[i] = CORRECT
                indexLettersCorrect.add(IndexChar(i, guess[i]))
                unhitCharList.remove(guess[i])
            }
        }
        for (i in guess.indices) {
            if (guess[i] != word[i] && unhitCharList.contains(guess[i])) {
                hint[i] = MISMATCHED
                indexLettersMismatched.add(IndexChar(i, guess[i]))
                unhitCharList.remove(guess[i])
            } else lettersIncorrect.add(guess[i])
        }
        hints.add(hint)
        guessedWords.add(guess)
        lettersUnused.removeAll(guess.toCharArray().toMutableList())

//        print("Corrects: ")
//        indexLettersCorrect.sorted().toTypedArray().forEach { print(it.char) }
//        println()
//
//        print("Mismatched: ")
//        indexLettersMismatched.sorted().toTypedArray().forEach { print(it.char) }
//        println()
//        print("Incorrect: ${lettersIncorrect.sorted().toTypedArray().contentDeepToString()}")
//        println()
//        print("Unused: ${lettersUnused.sorted().toTypedArray().contentDeepToString()}")
//        println()

        solver.lengthPrune()
        solver.pruneWords()

        if (numGuesses == maxGuesses && guess != word) {
            println("You lose! The word was $word") // TODO: make this a GUI thing
            return false
        }
        return guess == word
    }

    fun isValidWord(str: String): Boolean {
        val word = str.lowercase()
        if (word.length != wordLength) return false
        if (guessedWords.contains(word)) return false
        if (!legalWords.contains(word)) return false
        return true
    }

    companion object {
        val lexicon: Array<String> = parseFile("lexicon.txt")
        val wordles: Array<String> = parseFile("wordles.txt")
        val fiveLetterWords: Array<String> = parseFile("fiveLetterWords.txt")
        const val ALL_WORDS = 0 // all words in lexicon
        const val WORDLES = 1 // all words in wordles
        const val FIVE_LETTER_WORDS = 2 // all words in fiveLetterWords
        const val CORRECT = 1 // in word
        const val INCORRECT = 0 // not in word
        const val MISMATCHED = -1 // in word
        var maxGuesses = 6

        fun getRandomWord(wordSource: Int): String {
            when (wordSource) {
                ALL_WORDS -> return lexicon.random()
                WORDLES -> return wordles.random()
                FIVE_LETTER_WORDS -> return fiveLetterWords.random()
                else -> throw IllegalArgumentException("Invalid word source")
            }
        }

        fun getRandomWord(wordSource: Int, wordLength: Int): String {
            val source = when (wordSource) {
                ALL_WORDS -> lexicon
                WORDLES -> wordles
                FIVE_LETTER_WORDS -> fiveLetterWords
                else -> throw IllegalArgumentException("Invalid word source")
            }.copyOf()
            source.shuffle()
            source.forEach { if (it.length == wordLength) return it }
            throw IllegalArgumentException("Invalid word length")
        }

        fun getLegalWords(wordLength: Int): Array<String> {
            val legalWords = ArrayList<String>()
            lexicon.forEach { if (it.length == wordLength) legalWords.add(it) }
            return legalWords.toTypedArray()
        }

        fun parseFile(fileName: String): Array<String> {
            val inputStream = javaClass.classLoader.getResourceAsStream(fileName)
            return parseFile(inputStream ?: throw IllegalArgumentException("File is empty: $fileName"))
        }

        fun parseFile(inputStream: InputStream): Array<String> {
            val wordList = ArrayList<String>()
            inputStream.reader().readLines().forEach { wordList.add(it) }
            val wordArray = wordList.toTypedArray()
            for (i in wordArray.indices) {
                wordArray[i] = wordArray[i].lowercase(Locale.getDefault())
            }
            return wordArray
        }

        val alphabet = ('a'..'z').toMutableList() // add hyphens?
    }

    data class IndexChar(val index: Int, val char: Char) : Comparable<IndexChar> {
        override fun compareTo(other: IndexChar): Int {
            return index.compareTo(other.index)
        }
    }

    inner class Solver {

        // todo: add a predicate to filter out words that have the wrong number of a certain letter
        // e.g., if the word is "eaten" and the user has guessed "evens", then the predicate should remove all words that don't have at least 2 e's
        // additionally, the predicate should remove all words that have more than 2 e's if the user has guessed "eerie"

        var words = ArrayList<String>()
        var possibleLetters = ArrayList<IndexChar>()
        var impossibleLetters = ArrayList<IndexChar>()

        init {
            words = lexicon.toCollection(ArrayList())
            lengthPrune()
        }

        fun lengthPrune() {
            words.removeIf { it.length != wordLength }
        }

        fun pruneWords() {
            words.removeIf { word -> lettersIncorrect.any { word.contains(it) } }
            println("Pruned words: ${words.size}")
            if (!words.contains(word)) println("incorrect letters predicate failed")

            words.removeIf { word -> indexLettersCorrect.any { word[it.index] != it.char } }
            println("Pruned words: ${words.size}")
            if (!words.contains(word)) println("correct letters predicate failed")

            words.removeIf { indexLettersMismatched.any { word[it.index] == it.char } }
            println("Pruned words: ${words.size}")
            if (!words.contains(word)) println("mismatched letters predicate 1 failed")

            words.removeIf { indexLettersMismatched.any { !word.contains(it.char) } }
            println("Pruned words: ${words.size}")
            if (!words.contains(word)) println("mismatched letters predicate 2 failed")
        }

        // add a predicate to filter out words that have the wrong number of a certain letter; min and max repeats
        // e.g., if the word is "eaten" and the user has guessed "evens", then the predicate should remove all words that don't have at least 2 e's
        // additionally, the predicate should remove all words that have more than 2 e's if the user has guessed "eerie"
    }


}