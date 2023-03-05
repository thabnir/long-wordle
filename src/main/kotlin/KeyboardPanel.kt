import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*

class KeyboardPanel(val graphicsPanel: GraphicsPanel) : JPanel() {
    val qwertyKeyboard = arrayOf(
        arrayOf(
            KeyButton('q'),
            KeyButton('w'),
            KeyButton('e'),
            KeyButton('r'),
            KeyButton('t'),
            KeyButton('y'),
            KeyButton('u'),
            KeyButton('i'),
            KeyButton('o'),
            KeyButton('p')
        ), arrayOf(
            KeyButton('a'),
            KeyButton('s'),
            KeyButton('d'),
            KeyButton('f'),
            KeyButton('g'),
            KeyButton('h'),
            KeyButton('j'),
            KeyButton('k'),
            KeyButton('l'),
        ), arrayOf(
            KeyButton('z'),
            KeyButton('x'),
            KeyButton('c'),
            KeyButton('v'),
            KeyButton('b'),
            KeyButton('n'),
            KeyButton('m')
        )
    )
    val keyMap = qwertyKeyboard.flatten().associateBy { it.label }
    val keyList = qwertyKeyboard.flatten().toList()
    val guess = graphicsPanel.guess
    val wordle = graphicsPanel.wordle

    init {
        val f = GridBagConstraints.BOTH
        layout = GridBagLayout()
        qwertyKeyboard[0].forEachIndexed { index, key ->
            add(key, GridBagConstraints().apply {
                gridx = index
                gridy = 0
                weightx = 1.0
                weighty = 1.0
                fill = f
            }, index)
        }
        qwertyKeyboard[1].forEachIndexed { index, key ->
            add(key, GridBagConstraints().apply {
                gridx = index + 1
                gridy = 1
                weightx = 1.0
                weighty = 1.0
                fill = f
            }, index)
        }
        qwertyKeyboard[2].forEachIndexed { index, key ->
            add(key, GridBagConstraints().apply {
                gridx = index + 2
                gridy = 2
                weightx = 1.0
                weighty = 1.0
                fill = f
            }, index)
        }
        preferredSize = Dimension(
            qwertyKeyboard[0].size * keyList[0].preferredSize.width,
            qwertyKeyboard.size * keyList[0].preferredSize.height
        )
        maximumSize = preferredSize
        minimumSize = preferredSize
        isFocusable = true
        requestFocus()
    }

    fun updateHintKeys() {
        for (key in keyList) {
            if (key.label in wordle.lettersCorrect) {
                key.background = graphicsPanel.correctColor
            } else if (key.label in wordle.lettersMismatched) {
                key.background = graphicsPanel.mismatchedColor
            } else if (key.label in wordle.lettersIncorrect) {
                key.background = graphicsPanel.incorrectColor
            }
        }
    }

    inner class KeyButton(val label: Char) : JButton(label.uppercase()) {
        init {
            preferredSize = Dimension(50, 50)
            font = graphicsPanel.mainFont.deriveFont(preferredSize.width.toFloat() * .5f)
            isBorderPainted = false
            // isFocusPainted = true
            isRolloverEnabled = true
            // isContentAreaFilled = false
            // isOpaque = true
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent) {
                    graphicsPanel.type(label)
                    requestFocus(true)
                }
            })
        }
    }

    fun type(c: Char) {
        graphicsPanel.type(c)
    }
}