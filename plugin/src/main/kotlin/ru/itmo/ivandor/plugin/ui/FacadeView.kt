package ru.itmo.ivandor.plugin.ui

import com.intellij.ide.util.TreeClassChooser
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import ru.itmo.ivandor.plugin.service.BusinessLogicHolder
import ru.itmo.ivandor.plugin.service.Microservice
import ru.itmo.ivandor.plugin.service.ViewHolder
import ru.itmo.ivandor.plugin.dto.MicroserviceDto
import ru.itmo.ivandor.plugin.service.code_modify.CodeModifyServiceImpl

fun createFacadeView(businessLogicHolder: BusinessLogicHolder, viewHolder: ViewHolder, father: JPanel, addFacadeButton: JButton, ms: MicroserviceDto?): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

    val headerField = JTextField(ms?.name ?: "MicroserviceFacade")
    headerField.maximumSize = Dimension(Int.MAX_VALUE, headerField.preferredSize.height)

    val infoLabel1 = JLabel("ℹ")
    infoLabel1.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    infoLabel1.toolTipText = ms?.description ?: "Custom facade"

    val headerPanel = JPanel()
    headerPanel.layout = BoxLayout(headerPanel, BoxLayout.X_AXIS)


    headerField.maximumSize = Dimension(headerField.maximumSize.width, headerField.preferredSize.height)
    headerPanel.add(headerField)
    headerPanel.add(infoLabel1)
    headerField.alignmentX = Component.LEFT_ALIGNMENT
    infoLabel1.alignmentX = Component.RIGHT_ALIGNMENT

    headerPanel.maximumSize = Dimension(Int.MAX_VALUE, headerField.preferredSize.height)

    panel.add(headerPanel)


    val listPanel = JPanel()
    listPanel.layout = BoxLayout(listPanel, BoxLayout.Y_AXIS)

    val scrollPane = JScrollPane(listPanel)
    scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
    scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    scrollPane.preferredSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
    panel.add(scrollPane)

    val microserviceId = UUID.randomUUID().toString()
    businessLogicHolder.microservices[microserviceId] = Microservice(headerField.text, classes = emptyList())

    fun addClass(clazz: PsiClass?, generateButton: JButton) {
        val itemPanel = JPanel()
        itemPanel.alignmentX = Component.LEFT_ALIGNMENT
        itemPanel.layout = BoxLayout(itemPanel, BoxLayout.X_AXIS)

        var result = listOf<PsiClass>()

        var text : String?
        var textFull : String?
        if (clazz == null) {
            val chooserFactory = TreeClassChooserFactory.getInstance(businessLogicHolder.project)
            val chooser: TreeClassChooser = chooserFactory.createNoInnerClassesScopeChooser(
                "Select a Class",
                GlobalSearchScope.projectScope(businessLogicHolder.project),
                null,
                null,
            )

            chooser.showDialog()
            result = chooser.selected?.let { listOf(it) } ?: emptyList()
        } else {
            result = listOf(clazz)
        }

        val isRemote = ms != null

        if (result.isNotEmpty()){
            generateButton.isEnabled = true
            generateButton.revalidate()
            generateButton.repaint()
        }


        if (isRemote || result.isNotEmpty()){
            result.forEach { selectedClass ->
                businessLogicHolder.microservices[microserviceId]!!.classes += selectedClass
                if (isRemote){
                    selectedClass.name?.let { businessLogicHolder.classesFromRemote += it }
                }
                text = selectedClass.name
                textFull = selectedClass.qualifiedName
                val itemLabel = JLabel(text)

                val deleteLabel = JLabel("🗑")
                deleteLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                deleteLabel.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        businessLogicHolder.microservices[microserviceId]!!.classes -= selectedClass
                        if (businessLogicHolder.microservices[microserviceId]!!.classes.isEmpty()){
                            generateButton.isEnabled = false
                            generateButton.revalidate()
                            generateButton.repaint()
                        }
                        listPanel.remove(itemPanel)
                        listPanel.revalidate()
                        listPanel.repaint()
                    }
                })

                val infoLabel = JLabel("ℹ")
                infoLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                infoLabel.toolTipText = textFull

                itemPanel.add(deleteLabel)
                itemPanel.add(Box.createRigidArea(Dimension(5, 0)))
                itemPanel.add(infoLabel)
                itemPanel.add(Box.createRigidArea(Dimension(5, 0)))
                itemPanel.add(itemLabel)

                listPanel.add(itemPanel)
                listPanel.revalidate()
            }
        }


    }

    val downPanel = JPanel(BorderLayout())

    val buttonsPanel = JPanel()
    buttonsPanel.layout = GridLayout(3, 1)

    val generateButton = JButton("Show code")
    generateButton.isEnabled = false
    val addButton = JButton("Add class")
    addButton.addActionListener {
        addClass(null, generateButton)
    }
    buttonsPanel.add(addButton)

    val deleteButton = JButton("Remove facade")
    deleteButton.addActionListener {
        addFacadeButton.isEnabled = true
        addFacadeButton.repaint()
        businessLogicHolder.microservices.remove(microserviceId)
        father.remove(panel)
        father.add(JLabel())
    }
    buttonsPanel.add(deleteButton)

    generateButton.addActionListener {

        val text = CodeModifyServiceImpl(businessLogicHolder).generateCode(microserviceId)
        when(businessLogicHolder.microservices[microserviceId]?.useKotlin){
            false -> {
                viewHolder.updateWithJava(text)
            }
            true -> {
                viewHolder.updateWithKotlin(text)
            }
            null -> {}
        }
    }
    buttonsPanel.add(generateButton)

    val flagsPanel = JPanel()
    flagsPanel.layout = GridLayout(3, 1)

    val checkboxUseKotlin = JCheckBox("Use Kotlin")
    checkboxUseKotlin.addActionListener { _ ->
        businessLogicHolder.microservices[microserviceId]!!.useKotlin = checkboxUseKotlin.isSelected
    }
    flagsPanel.add(checkboxUseKotlin)

    val checkboxDeprecateClasses = JCheckBox("Deprecate classes")
    checkboxDeprecateClasses.addActionListener { _ ->
        businessLogicHolder.microservices[microserviceId]!!.deprecateClasses = checkboxDeprecateClasses.isSelected
    }
    flagsPanel.add(checkboxDeprecateClasses)

    val checkboxAddHttpClient = JCheckBox("Add HTTP client")
    checkboxAddHttpClient.addActionListener { _ ->
        businessLogicHolder.microservices[microserviceId]!!.addHttpClient = checkboxAddHttpClient.isSelected
    }
    flagsPanel.add(checkboxAddHttpClient)

    downPanel.add(buttonsPanel, BorderLayout.WEST)
    downPanel.add(flagsPanel, BorderLayout.EAST)
    downPanel.repaint()
    panel.add(downPanel)

    ms?.classes?.forEach { classStr ->
        businessLogicHolder.requestedClasses.firstOrNull { it.name == classStr }?.let {
            addClass(it, generateButton)
        }

    }

    return panel
}