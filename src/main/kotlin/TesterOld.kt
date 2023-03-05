import java.util.*

object TesterOld {
    @JvmStatic
    fun main(args: Array<String>) {
        val w = WordleOld()
        val input = Scanner(System.`in`)
        var guess: String

        //w.printIt(w.correctWord); System.out.println();
        println("Wordle\n")
        println("The word is " + w.correctWord.size + " letters long")
        println(
            """There are ${w.words.size} ${w.correctWord.size} letter words
"""
        )
        var guessesLeft: Int = WordleOld.Companion.maxGuesses
        while (true) {
            for (c in w.lettersLeft) {
                print(c)
            }
            println()
            guess = input.nextLine()
            guess = guess.lowercase(Locale.getDefault())
            if (w.isValidWord(guess)) {
                guessesLeft--
                w.makeGuess(WordleOld.Companion.stringToCharArray(guess))
                w.pruneWords()
                println("\t\t\t\t\twords left now: " + w.legalWords.size)
                w.printIt(w.guesses[w.numGuesses - 1])
                println("      " + w.getHint() + "      " + guessesLeft + " guesses left")
                if (w.legalWords.size <= 10) {
                    //w.printRemainingWords();
                }
                if (w.isWon) {
                    println("You won on guess " + w.numGuesses + "!")
                    System.exit(0)
                } else if (w.isLost) {
                    println("You lose! The word was " + WordleOld.Companion.charArrayToString(w.correctWord))
                    System.exit(0)
                }
            } else {
                println("Invalid input")
            }
        }
    }
}