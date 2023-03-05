import java.awt.*
import javax.swing.JPanel

class GuessPanel(val graphicsPanel: GraphicsPanel) : JPanel() {
    val wordle
        get() = graphicsPanel.wordle
    val prevGuesses
        get() = graphicsPanel.wordle.guessedWords
    val hints
        get() = graphicsPanel.wordle.hints
    val currentGuess
        get() = graphicsPanel.guess

    val grid = ArrayList<LetterBox>(Wordle.maxGuesses * wordle.wordLength)

    init {
        layout = GridLayout(Wordle.maxGuesses, wordle.wordLength)
        //border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        for (i in 0 until Wordle.maxGuesses) {
            for (j in 0 until wordle.wordLength) {
                val letterBox = LetterBox(i, j)
                grid.add(letterBox)
                add(letterBox)
            }
        }
        preferredSize = Dimension(
            wordle.wordLength * grid[0].preferredSize.width,
            Wordle.maxGuesses * grid[0].preferredSize.height
        )
        minimumSize = preferredSize
        maximumSize = preferredSize
    }

    fun getLetterBox(row: Int, col: Int): LetterBox {
        if (row < 0 || row >= Wordle.maxGuesses || col < 0 || col >= wordle.wordLength) {
            throw IllegalArgumentException("Invalid row or column of LetterBox")
        }
        return grid[row * wordle.wordLength + col]
    }

    inner class LetterBox(val row: Int, val column: Int) : JPanel() {
        var letter: Char = ' '
        var bgColor: Color = graphicsPanel.boxColor
        var fontColor: Color = graphicsPanel.darkFontColor
        var isGuessed = false

        init {
            preferredSize = Dimension(
                graphicsPanel.panelSize.width / graphicsPanel.wordle.wordLength,
                graphicsPanel.panelSize.width / graphicsPanel.wordle.wordLength
            )
            font = graphicsPanel.mainFont.deriveFont(Font.BOLD, preferredSize.width.toFloat() * 0.8f)
            background = graphicsPanel.boxColor
            isOpaque = true
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)

            g?.color = bgColor
            g?.fillRect(0, 0, width, height)

            g?.color = fontColor
            g?.drawString(
                letter.uppercase(),
                (width - g.fontMetrics.stringWidth(letter.toString())) / 2,
                (height - g.fontMetrics.descent)
            )
        }

        fun setGuessed(state: Int) {
            if (isGuessed) return
            bgColor = when (state) {
                Wordle.CORRECT -> graphicsPanel.correctColor
                Wordle.INCORRECT -> graphicsPanel.incorrectColor
                Wordle.MISMATCHED -> graphicsPanel.mismatchedColor
                else -> throw IllegalStateException("Unknown hint type")
            }
            fontColor = graphicsPanel.lightFontColor
            isGuessed = true
            repaint()
        }
    }

    fun updateCurrentWord() {
        if (hints.size == Wordle.maxGuesses) return
        for (j in currentGuess.indices) {
            val letterBox = getLetterBox(hints.size, j)
            letterBox.letter = currentGuess[j]
            letterBox.repaint()
        }
        for (j in currentGuess.size until wordle.wordLength) {
            val letterBox = getLetterBox(hints.size, j)
            letterBox.letter = ' '
            letterBox.repaint()
        }
    }

    fun updateAll() {
        updateCurrentWord()
        for (i in hints.indices) {
            for (j in 0 until wordle.wordLength) {
                val letterBox = getLetterBox(i, j)
                letterBox.setGuessed(hints[i][j])
            }
        }
    }
}