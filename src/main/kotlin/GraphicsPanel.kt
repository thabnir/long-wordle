import com.formdev.flatlaf.util.SystemInfo
import java.awt.*
import java.awt.event.*
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane


class GraphicsPanel : JPanel() {
    val boxColor = Color(220, 220, 220)
    var correctColor = Color(41, 171, 56)
    var incorrectColor = Color(170, 50, 50)
    var mismatchedColor = Color(212, 178, 42)

    var lightFontColor = Color(241, 241, 241)
    var darkFontColor = Color(50, 50, 50)


    // TODO: figure out how to make the key bindings work
    // rn it's just a keyListener on the keyboard panel, but I want pressing said key to cause the button to be pressed
    // that way the button visually indicates that it is pressed
    // also it doesn't work when out of focus which might be a problem

    val mainFont = Font("Lucida", Font.PLAIN, 24)

    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    val panelSize: Dimension = Dimension(screenSize.width / 2, screenSize.height / 2)
    val frame = JFrame()
    val wordle = Wordle(Wordle.ALL_WORDS)
    val guess = ArrayList<Char>(wordle.wordLength)
    val keyboard = KeyboardPanel(this)
    val guessPanel = GuessPanel(this)

    val scrollPane =
        JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

    var yPad = 5
    var xPad = 5

    init {
        val pad = 50
        macStuff()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        font = mainFont
        //border = BorderFactory.createEmptyBorder(yPad, xPad + scrollPane.verticalScrollBar.width, yPad, xPad)
        maximumSize = Dimension(screenSize.width, Int.MAX_VALUE)
        frame.minimumSize = Dimension(
            guessPanel.preferredSize.width + scrollPane.verticalScrollBar.width + pad,
            keyboard.minimumSize.height + guessPanel.minimumSize.height + scrollPane.horizontalScrollBar.height
        )

        val isScrolly = true

        val jp = JPanel()
        jp.preferredSize = Dimension(this.preferredSize.width, pad)
        jp.isVisible = true
        add(jp)

        add(guessPanel)
        add(keyboard)
        if (isScrolly) {
            frame.add(scrollPane)
        } else {
            frame.add(this)
        }

        frame.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {}

            override fun keyPressed(e: KeyEvent) {
                if (e.keyChar in Wordle.alphabet) type(e.keyChar)
                else if (e.keyCode == KeyEvent.VK_ENTER) {
                    enter()
                } else if (e.keyCode == KeyEvent.VK_BACK_SPACE) {
                    backspace()
                }
                // println("guess: ${String(guess.toCharArray())}")
            }

            override fun keyReleased(e: KeyEvent) {}
        })

        frame.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                val s = this@GraphicsPanel.size
//                this@GraphicsPanel.border = BorderFactory.createEmptyBorder(
//                    yPad, xPad + scrollPane.verticalScrollBar.width, yPad, xPad
//                )
                repaint()
            }
        })
        boilPlates()
        frame.pack()
        frame.validate()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
        frame.requestFocus()
    }

    fun boilPlates() {
        scrollPane.border = BorderFactory.createEmptyBorder(28, 0, 0, 0)
        scrollPane.verticalScrollBar.unitIncrement = 16
        scrollPane.verticalScrollBar.isVisible = false

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isResizable = true
    }

    fun scrollToBottom() {
        scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
    }

    fun macStuff() {
        if (SystemInfo.isMacOS) {
            frame.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            frame.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
            frame.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
        }
    }

    fun type(letter: Char) {
        if (guess.size < wordle.wordLength && !wordle.isWon && letter in Wordle.alphabet) {
            guess.add(letter)
            guessPanel.updateCurrentWord()
        }
    }

    fun backspace() {
        if (guess.isNotEmpty()) {
            guess.removeLast()
            guessPanel.updateCurrentWord()
        }
    }

    fun enter() {
        val g = String(guess.toCharArray())
        if (wordle.isValidWord(g)) {
            guess.clear()
            wordle.makeGuess(g)
            keyboard.updateHintKeys()
            guessPanel.updateAll()
        }
    }
}