import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

class WordleOld internal constructor() {
    var dictionary = ArrayList<Array<Char?>>()
    var words = ArrayList<Array<Char?>>()
    var legalWords = ArrayList<Array<Char?>>()
    var guesses = ArrayList<Array<Char?>>()
    var prevWords = ArrayList<Array<Char?>>()
    var lettersLeft: MutableSet<Char?> = HashSet() // sets are like lists but contain no duplicates
    var lettersUsed: MutableSet<Char?> = HashSet()
    var lettersWrong: MutableSet<Char?> = HashSet()
    var lettersMisplaced: MutableSet<IndexLetter> = HashSet() // but for some reason they don't work on these ones
    var lettersCorrect: MutableSet<IndexLetter> = HashSet() // something to do with the fact it's my own data type
    var correctWord: Array<Char?>
    var hint // - means incorrect, ~ means wrong location, + means correct
            : Array<Char?>
    var isWon = false
    var isLost = false
    var numGuesses = 0
    var numLetters: Int

    init {
        var input: Scanner? = null
        try {
            input = Scanner(textFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        while (input!!.hasNextLine()) dictionary.add(stringToCharArray(input.nextLine().lowercase(Locale.getDefault())))

        // System.out.println(dictionary.size());
        correctWord = dictionary[(Math.random() * dictionary.size).toInt()] // random word
        // correctWord = dictionary.get(455941);i
        while (input.hasNextLine()) dictionary.add(stringToCharArray(input.nextLine().lowercase(Locale.getDefault())))
        numLetters = correctWord.size

        // printIt(correctWord);
        input = null
        try {
            input = Scanner(dictFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        var next: String
        while (input!!.hasNextLine()) {
            next = input.nextLine()
            if (next.length == correctWord.size) words.add(stringToCharArray(next.lowercase(Locale.getDefault())))
        }
        for (c in words) legalWords.add(c)
        hint = arrayOfNulls(correctWord.size)
        for (i in hint.indices) hint[i] = '-'
    }

    fun loadWordsFrom(fileName: String) {
        val inputStream = javaClass.classLoader.getResourceAsStream(fileName)
        if (inputStream == null) {
            throw FileNotFoundException("File not found: $fileName")
        }
        loadWordsFrom(inputStream)
    }

    fun loadWordsFrom(inStream: InputStream) {
        inStream.reader().use { reader ->
            reader.forEachLine { line ->
                words.add(stringToCharArray(line.lowercase(Locale.getDefault())))
            }
        }
    }

    fun isValidWord(unconvertedWord: String): Boolean {
        var unconvertedWord = unconvertedWord
        unconvertedWord = unconvertedWord.lowercase(Locale.getDefault())
        val input = stringToCharArray(unconvertedWord)
        for (word in words) {
            if (Arrays.equals(input, word)) return true
        }
        return false
    }

    fun isValidWord(unconvertedWord: ArrayList<Char>): Boolean {
        val convertedWord = toLowerCase(arrayListToArray(unconvertedWord))
        for (word in words) {
            if (Arrays.equals(convertedWord, word)) return true
        }
        return false
    }

    fun checkCorrects(theWord: Array<Char?>): Boolean {
        for (c in lettersCorrect) {
            if (theWord[c.index] !== c.letter) {
                return false
            }
        }
        return true
    }

    fun checkWrongs(theWord: Array<Char?>?): Boolean {
        for (c in lettersWrong) {
            if (charArrayToString(theWord)!!.indexOf(c!!) != -1) {
                return false
            }
        }
        return true
    }

    fun checkMisplaceds(theWord: Array<Char?>): Boolean {
        for (c in lettersMisplaced) {
            if (theWord[c.index] === c.letter) {
                return false
            }
            if (charArrayToString(theWord)!!.indexOf(c.letter!!) == -1) {
                return false
            }
        }
        return true
    }

    fun pruneWords() {
        val itr = legalWords.iterator()
        while (itr.hasNext()) {
            val theWord = itr.next()
            if (!checkCorrects(theWord)) itr.remove() else if (!checkWrongs(theWord)) itr.remove() else if (!checkMisplaceds(
                    theWord
                )
            ) itr.remove()
        }
    }

    fun makeGuess(g: Array<Char?>) {
        var g = g
        prevWords.add(g)
        g = toLowerCase(g)
        if (Arrays.equals(correctWord, g)) isWon = true
        val lets = ArrayList<Char?>()
        for (i in correctWord.indices) {
            lets.add(correctWord[i])
        }
        val thisHint = arrayOfNulls<Char>(g.size)
        Arrays.fill(thisHint, 'x')

        // x means incorrect, ~ means wrong location, + means correct
        for (i in g.indices) {
            if (lettersLeft.contains(g[i])) {
                lettersLeft.remove(g[i])
                lettersUsed.add(g[i])
            }
            if (g[i] === correctWord[i]) {
                hint[i] = g[i]
                thisHint[i] = '+'
                lettersCorrect.add(IndexLetter(i, g[i]))
                lets.remove(g[i])
            }
        }
        for (i in g.indices) {
            if (g[i] !== correctWord[i] && lets.contains(g[i])) {
                thisHint[i] = '~'
                lettersMisplaced.add(IndexLetter(i, g[i]))
                lets.remove(g[i])
            } else {
                lettersWrong.add(g[i])
            }
        }
        numGuesses++
        if (numGuesses >= maxGuesses && !isWon) isLost = true
        guesses.add(thisHint)
    }

    fun getHint(): String? {
        return charArrayToString(hint)
    }

    fun getCorrectWord(): String? {
        return charArrayToString(correctWord)
    }

    fun printIt(word: Array<Char?>?) {
        for (letter in word!!) print(letter)
    }

    inner class IndexLetter internal constructor(var index: Int, var letter: Char?)
    companion object {
        // make ai that guesses words based on the list (only after everything else
        // works, though)
        // find out if the creation of the dictionary is working correctly; it takes
        // forever
        // make a "new word" method so that you don't have to close and re-open every
        // time
        // should probably make a match check algorithm that's not associated with the
        // guess method
        // actually i should 100% have that, the guess method should just call my
        // matchcheck/comparewords method
        var textFile = File("F:\\Books\\wordles.txt")
        var dictFile = File("F:\\Books\\lexicon.txt")
        const val maxGuesses = 6
        fun arrayListToArray(unconvertedWord: ArrayList<Char>): Array<Char?> {
            val convertedWord = arrayOfNulls<Char>(unconvertedWord.size)
            for (i in unconvertedWord.indices) {
                convertedWord[i] = unconvertedWord[i]
            }
            return convertedWord
        }

        fun toLowerCase(g: Array<Char?>): Array<Char?> {
            var g = g
            g = stringToCharArray(charArrayToString(g)!!.lowercase(Locale.getDefault()))
            return g
        }

        fun stringToCharArray(s: String): Array<Char?> {
            val c = arrayOfNulls<Char>(s.length)
            for (i in c.indices) c[i] = s[i]
            return c
        }

        fun charArrayToString(c: Array<Char?>?): String? {
            var s: String? = ""
            for (i in c!!.indices) s += c[i]
            return s
        }
    }
}