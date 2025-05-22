package ru.itmo.ivandor.plugin.ui

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.ui.util.preferredWidth
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import ru.itmo.ivandor.plugin.service.BusinessLogicHolder
import ru.itmo.ivandor.plugin.service.ViewHolder
import ru.itmo.ivandor.plugin.dto.MicroserviceDto
import ru.itmo.ivandor.plugin.idea_file.MicroservicesConfigVirtualFile
import ru.itmo.ivandor.plugin.remote.HttpProcessRequestWorker
import ru.itmo.ivandor.plugin.service.code_analyse.CodeAnalyseService
import ru.itmo.ivandor.plugin.service.code_analyse.CodeAnalyseServiceImpl
import ru.itmo.ivandor.plugin.service.code_modify.CodeModifyServiceImpl

fun createFacadesPanel(businessLogicHolder: BusinessLogicHolder, viewHolder: ViewHolder, file: MicroservicesConfigVirtualFile): JPanel {
    val addFacadeButton = JButton("Add facade")
    val generateButton = JButton("Generate code")

    generateButton.isEnabled = false
    generateButton.repaint()

    val codeModifyService = CodeModifyServiceImpl(businessLogicHolder)

    generateButton.addActionListener {
        when(Messages.showYesNoCancelDialog(businessLogicHolder.project, "Close page on generation?", "", Messages.getQuestionIcon())){
            Messages.YES -> {
                addFacadeButton.isEnabled = false
                generateButton.isEnabled = false
                codeModifyService.saveCode().let {
                    if (it) FileEditorManager.getInstance(businessLogicHolder.project).closeFile(file)
                }
            }
            Messages.NO -> {
                addFacadeButton.isEnabled = false
                generateButton.isEnabled = false
                codeModifyService.saveCode()
            }
        }
    }

    val panel = JPanel(GridLayout(3, 3, 5, 5))
    panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    val addFacade: (ms: MicroserviceDto?) -> Unit = { ms ->
        ms?.let {
            if(!it.name.endsWith("Facade")){
                it.name += "Facade"
            }
        }
        if (panel.componentCount == 0){
            panel.add(createFacadeView(businessLogicHolder, viewHolder, panel, addFacadeButton, ms))
            panel.add(JLabel())
            panel.add(JLabel())
            panel.add(JLabel())
            panel.add(JLabel())
            panel.add(JLabel())
            panel.add(JLabel())
            panel.add(JLabel())
            panel.add(JLabel())
        } else {

            when (businessLogicHolder.microservices.size) {
                in 0..8 -> {
                    panel.remove(8)
                    panel.add(createFacadeView(businessLogicHolder, viewHolder, panel, addFacadeButton, ms), businessLogicHolder.microservices.size - 1)
                }
                else -> {
                    println("invalid state")
                }
            }
        }
        generateButton.isEnabled = true
        generateButton.repaint()
        panel.revalidate()
        panel.repaint()
    }

    addFacadeButton.addActionListener {
        addFacade(null)
        if (businessLogicHolder.microservices.size == 9) {
            addFacadeButton.isEnabled = false
            addFacadeButton.repaint()
        }

    }

    val codeAnalyseService : CodeAnalyseService = CodeAnalyseServiceImpl(file.projectFiles, businessLogicHolder.project)

    HttpProcessRequestWorker(
        file = file,
        businessLogicHolder = businessLogicHolder,
        classes = codeAnalyseService.buildAnalyseData(),
    ) { msList ->
        msList.forEach { addFacade(it) }
        panel.revalidate()
        panel.repaint()
    }.execute()

    panel.revalidate()
    panel.repaint()

    val mainPanel = JPanel(BorderLayout())
    mainPanel.add(panel, BorderLayout.CENTER)

    val bottomPanel = JPanel()
    bottomPanel.layout = BoxLayout(bottomPanel, BoxLayout.Y_AXIS)
    bottomPanel.add(addFacadeButton)
    bottomPanel.add(generateButton)
    addFacadeButton.alignmentX = Component.CENTER_ALIGNMENT
    generateButton.alignmentX = Component.CENTER_ALIGNMENT
    addFacadeButton.preferredWidth = Int.MAX_VALUE
    generateButton.preferredWidth = Int.MAX_VALUE

    addFacadeButton.maximumSize = Dimension(Int.MAX_VALUE, addFacadeButton.preferredSize.height)
    generateButton.maximumSize = Dimension(Int.MAX_VALUE, generateButton.preferredSize.height)


    mainPanel.add(bottomPanel, BorderLayout.SOUTH)

    return mainPanel
}