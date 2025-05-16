package ru.itmo.ivandor.plugin.actions

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.TreeClassChooser
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorTextField
import com.intellij.ui.util.preferredWidth
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextField
import javax.swing.SwingConstants
import org.jetbrains.kotlin.idea.KotlinFileType
import ru.itmo.ivandor.plugin.dto.MicroserviceDto
import ru.itmo.ivandor.plugin.idea_file.MicroservicesFileType
import ru.itmo.ivandor.plugin.service.code_modify.CodeModifyServiceImpl
import ru.itmo.ivandor.plugin.remote.HttpProcessRequestWorker

fun createFacadesPanel(businessLogicHolder: BusinessLogicHolder, viewHolder: ViewHolder, file: MicroservicesConfigVirtualFile): JPanel {
    val addFacadeButton = JButton("Add facade")
    val generateButton = JButton("Generate code")

    generateButton.isEnabled = false
    generateButton.repaint()

    val codeModifyService = CodeModifyServiceImpl(businessLogicHolder)

    generateButton.addActionListener {
        when(Messages.showYesNoCancelDialog(businessLogicHolder.project, "Close page on generation?", "", Messages.getQuestionIcon())){
            Messages.YES -> {
                codeModifyService.saveCode().let {
                    if (it) FileEditorManager.getInstance(businessLogicHolder.project).closeFile(file)
                }
            }
            Messages.NO -> {
                codeModifyService.saveCode()
            }
        }
    }

    val panel = JPanel(GridLayout(3, 3, 5, 5))
    panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    val addFacade: (ms: MicroserviceDto?) -> Unit = { ms ->
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

    HttpProcessRequestWorker(file = file, businessLogicHolder = businessLogicHolder) { msList ->
        msList.forEach {
            addFacade(it)
        }
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
        businessLogicHolder.microservices[microserviceId]?.deprecateClasses = checkboxDeprecateClasses.isSelected
    }
    flagsPanel.add(checkboxDeprecateClasses)

    val checkboxAddHttpClient = JCheckBox("Add HTTP client")
    checkboxAddHttpClient.addActionListener { _ ->
        businessLogicHolder.microservices[microserviceId]?.addHttpClient = checkboxAddHttpClient.isSelected
    }
    flagsPanel.add(checkboxAddHttpClient)

    downPanel.add(buttonsPanel, BorderLayout.WEST)
    downPanel.add(flagsPanel, BorderLayout.EAST)
    downPanel.repaint()
    panel.add(downPanel)

    ms?.classes?.forEach { classStr ->
        addClass(businessLogicHolder.requestedClasses.first { it.name == classStr }, generateButton)
    }

    return panel
}

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

data class Microservice(
    var name: String,
    var classes: List<PsiClass>,
    var useKotlin: Boolean = false,
    var deprecateClasses: Boolean = false,
    var addHttpClient: Boolean = false,
)

data class BusinessLogicHolder(
    var microservices: HashMap<String,Microservice> = HashMap(),
    var classesFromRemote: List<String> = emptyList(),
    val project: Project,
    val requestedClasses: List<PsiClass>,
    var requestId : String?,
)

data class ViewHolder(
    val rightPanel: JPanel,
    val kotlinEditor: EditorTextField,
    val javaEditor: EditorTextField,
){
    lateinit var splitPanel: JSplitPane

    fun updateWithJava(code: String) {
        rightPanel.removeAll()
        rightPanel.add(javaEditor)

        javaEditor.text = code
        rightPanel.add(JScrollPane(javaEditor).apply {
            this.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            this.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }, BorderLayout.CENTER)
        javaEditor.repaint()
        rightPanel.revalidate()
        rightPanel.repaint()
    }

    fun updateWithKotlin(code: String) {
        rightPanel.removeAll()
        rightPanel.add(kotlinEditor)

        kotlinEditor.text = code
        rightPanel.add(JScrollPane(kotlinEditor).apply {
            this.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            this.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }, BorderLayout.CENTER)
        kotlinEditor.repaint()
        rightPanel.revalidate()
        rightPanel.repaint()
    }
}

class MicroservicesConfigVirtualFile(
    val classes: List<PsiClass>,
) : LightVirtualFile("Microservices", MicroservicesFileType(), "")
