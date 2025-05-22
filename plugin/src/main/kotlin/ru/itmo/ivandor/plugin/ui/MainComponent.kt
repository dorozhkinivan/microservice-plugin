package ru.itmo.ivandor.plugin.ui

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ui.EditorTextField
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SwingConstants
import org.jetbrains.kotlin.idea.KotlinFileType
import ru.itmo.ivandor.plugin.service.BusinessLogicHolder
import ru.itmo.ivandor.plugin.service.ViewHolder
import ru.itmo.ivandor.plugin.idea_file.MicroservicesConfigVirtualFile

fun createMainComponent(businessLogicHolder: BusinessLogicHolder, file: MicroservicesConfigVirtualFile): JComponent {

    val mainPanel = JPanel(BorderLayout())

    val rightPanel = JPanel(BorderLayout())

    val text = """
        /*
            Generated code will be located here
        */
    """.trimIndent()

    val javaEditor = EditorTextField(null, businessLogicHolder.project, JavaFileType.INSTANCE, true, false).apply { this.text = text; isVisible = true; }
    val kotlinEditor = EditorTextField(null, businessLogicHolder.project, KotlinFileType.INSTANCE, true, false).apply {isVisible = true; }

    rightPanel.add(javaEditor, BorderLayout.CENTER)

    val viewHolder = ViewHolder(
        javaEditor = javaEditor,
        kotlinEditor = kotlinEditor,
        rightPanel = rightPanel,
    )



    rightPanel.isVisible = true
    mainPanel.add(rightPanel, BorderLayout.LINE_END)

    val leftPanel = JPanel(BorderLayout())
    mainPanel.add(leftPanel, BorderLayout.CENTER)

    val topLabel = JLabel("Текст сверху", SwingConstants.CENTER)
    leftPanel.add(topLabel, BorderLayout.NORTH)

    val centerPanel = JPanel()
    leftPanel.add(centerPanel, BorderLayout.CENTER)

    val buttonPanel = JPanel()
    buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.Y_AXIS)
    val button = JButton("Кнопка")
    button.alignmentX = Component.CENTER_ALIGNMENT

    buttonPanel.add(Box.createVerticalGlue())
    buttonPanel.add(button)
    buttonPanel.add(Box.createVerticalStrut(10))

    leftPanel.add(buttonPanel, BorderLayout.SOUTH)

    val splitPanel = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createFacadesPanel(businessLogicHolder, viewHolder, file), rightPanel)

    viewHolder.splitPanel = splitPanel
    splitPanel.setDividerLocation(850)
    splitPanel.revalidate()
    splitPanel.repaint()
    mainPanel.add(splitPanel, BorderLayout.CENTER)

    mainPanel.isVisible = true

    return mainPanel
}