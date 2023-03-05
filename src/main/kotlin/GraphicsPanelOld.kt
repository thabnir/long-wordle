import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import javax.swing.JPanel

class GraphicsPanelOld internal constructor(size: Dimension?) : JPanel(), KeyListener {
    var guessesLeft: Int = WordleOld.Companion.maxGuesses
    var guess = ArrayList<Char>()
    var fontSize = 0.0
    var w: WordleOld
    var guessFont: Font? = null
    var width = 0.0
    var height = 0.0
    var keyTyped = false
    var wordEntered = false
    var validWordEntered = false
    var invalidWordEntered = false

    // STYLE ELEMENTS:
    var hasAntiAliasing = true
    var roundness = .3 // how round the boxes are, the higher the rounder
    val centerCovered = .7 // higher = more screen covered
    val boxCovered = .85 // percentage of the letter's box taken up by the letter
    var boxSize = 0.0
    var vertGapPercent = .25 // as a percentage of box size
    var horizGapPercent = .15 // as a percentage of box size
    var vertGap = 0.0
    var horizGap = 0.0
    var leftOffsetPercent = .4 // as a percentage of screen size
    var topOffsetPercent = .05 // as a percentage of screen size
    var leftOffset = 0.0
    var topOffset = 0.0
    var boxColor = Color(220, 220, 220) // Color boxColor = new Color(245, 235, 125);
    var greenBox = Color(41, 171, 56)
    var yellowBox = Color(212, 178, 42)
    var blackBox = Color(69, 69, 69)
    var wrongWord = Color(170, 50, 50)
    var prevGuessColor = Color(241, 241, 241)
    var currentGuessColor = Color(50, 50, 50)

    // END STYLE ELEMENTS
    init {
        this.preferredSize = size
        background = Color.gray
        this.isFocusable = true
        addKeyListener(this)
        w = WordleOld()
        // wIt(w.correctWord); System.out.println();
        resize()
        System.setProperty("awt.useSystemAAFontSettings", "on")
    }

    fun resize() {
        if (width != getWidth().toDouble() || height != getHeight().toDouble()) {
            width = getWidth().toDouble()
            height = getHeight().toDouble()
            boxSize = centerCovered * width / w.numLetters // perhaps make something for vertical size? or not tbh
            fontSize = boxCovered * boxSize // need to convert this from pixels to font size
            vertGap = boxSize * vertGapPercent
            horizGap = boxSize * horizGapPercent
            leftOffset = width / 2 - (boxSize + horizGap) * w.correctWord.size / 2 + horizGap / 2 // wrong
            topOffset = height / 2 - (boxSize + vertGap) * WordleOld.Companion.maxGuesses / 2 + vertGap / 2
            guessFont = Font("Arial", Font.BOLD, fontSize.toInt())
        }
    }

    fun refresh() {
        repaint()
    }

    public override fun paintComponent(g: Graphics) {
        if (hasAntiAliasing) {
            val g2d = g as Graphics2D
            // RenderingHints rhints = g2d.getRenderingHints();
            // boolean antialiasOn =
            // rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        }
        super.paintComponent(g)
        g.font = guessFont
        for (i in 0 until WordleOld.Companion.maxGuesses) {
            for (j in 0 until w.numLetters) {

                // color of box
                if (w.guesses.size > i) {
                    if (w.guesses[i][j] == '+') {
                        g.color = greenBox
                    } else if (w.guesses[i][j] == '~') {
                        g.color = yellowBox
                    } else {
                        g.color = blackBox
                    }
                } else {
                    g.color = boxColor
                }
                // box
                g.fillRoundRect(
                    (leftOffset +
                            j * (boxSize + horizGap)).toInt(),
                    (topOffset +
                            i * (boxSize + vertGap)).toInt(),
                    boxSize.toInt(),
                    boxSize.toInt(),
                    (boxSize * roundness).toInt(),
                    (boxSize * roundness).toInt()
                )
                if (w.prevWords.size > i) {
                    val p = fontStats(w.prevWords[i][j].toString())
                    p.x = (boxSize / 2 - p.x / 2).toInt()
                    p.y = (boxSize / 2 - p.y / 2).toInt()
                    g.color = prevGuessColor // show the previous guesses
                    g.drawString(
                        w.prevWords[i][j].toString(),
                        (p.x + leftOffset + j * (boxSize + horizGap)).toInt(),
                        (p.y + topOffset + fontSize + i * (boxSize + vertGap)).toInt()
                    )
                }
                if (i == w.numGuesses && guess.size > j) {
                    val p = fontStats(guess[j].toString())
                    p.x = (boxSize / 2 - p.x / 2).toInt()
                    p.y = (boxSize / 2 - p.y / 2).toInt()
                    g.color = currentGuessColor // shows current/in progress guess
                    g.drawString(
                        guess[j].toString(),
                        (p.x + leftOffset + j * (boxSize + horizGap)).toInt(),
                        (p.y + topOffset + fontSize + i * (boxSize + vertGap)).toInt()
                    )
                }
            }
        }
        if (w.isWon) {
            g.color = greenBox
            g.fillRect(
                0,
                getHeight() / 2 - (fontSize * 1.5).toInt(),
                getWidth(), (fontSize * 1.4).toInt()
            )
            val winMessage = "Congratulations!"
            g.color = currentGuessColor
            g.font = guessFont
            g.drawString(
                winMessage,
                getWidth() / 2 - g.fontMetrics.stringWidth(winMessage) / 2, (getHeight() / 2 - fontSize / 2.4).toInt()
            )
        } else if (w.isLost) {
            g.color = wrongWord
            g.fillRect(
                0,
                getHeight() / 2 - (fontSize * 1.5).toInt(),
                getWidth(), (fontSize * 1.5).toInt()
            )
            val loseMessage = "You lose!"
            g.font = guessFont
            g.color = prevGuessColor
            g.drawString(
                loseMessage,
                getWidth() / 2 - g.fontMetrics.stringWidth(loseMessage) / 2, (getHeight() / 2 - fontSize / 4).toInt()
            )

            // String loseWord = Wordle.charArrayToString(w.correctWord).toUpperCase();
            // w.prevWords.remove(w.guesses.size()-1);
            // w.prevWords.add(w.correctWord);
            // g.setColor(wrongWord);
            // g.drawString(loseWord, this.getWidth() / 2 +
            // g.getFontMetrics().stringWidth(loseMessage) / 2, (int) (this.getHeight() / 2
            // + fontSize / 4));
        }
    }

    fun wordEntered() {
        if (w.isValidWord(guess)) {
            validWordEntered = true
            w.makeGuess(WordleOld.Companion.arrayListToArray(guess))
            guess.clear()
        } else {
            invalidWordEntered = true
        }
    }

    fun invalidAnimation() {
        // do some shit to indicate that the input is invalid
    }

    override fun keyTyped(e: KeyEvent) {}
    override fun keyPressed(e: KeyEvent) {
        // System.out.println(e.getKeyCode());
        if (e.keyCode == KeyEvent.VK_ENTER) {
            wordEntered = true
            wordEntered()
            keyTyped = true
        } else if (e.keyCode == 8) {
            if (guess.size > 0) {
                keyTyped = true
                guess.removeAt(guess.size - 1)
            }
        } else if (guess.size < w.correctWord.size && (e.keyCode >= 65 && e.keyCode <= 90
                    || e.keyCode == 45)
        ) {
            keyTyped = true
            guess.add(e.keyChar.uppercaseChar())
        }
        // System.out.println(guess);
    }

    override fun keyReleased(e: KeyEvent) {}
    fun fontStats(letter: String?): Point {
        val affinetransform = AffineTransform()
        val frc = FontRenderContext(affinetransform, true, true)
        val textWidth = guessFont!!.getStringBounds(letter, frc).width.toInt()
        val textHeight = guessFont!!.getStringBounds(letter, frc).height.toInt()
        return Point(textWidth, textHeight)
    }

    companion object {
        fun arrayListToString(al: ArrayList<Char?>): String? {
            var converted: String? = ""
            for (i in al.indices) converted += al[i]
            return converted
        }
    }
}