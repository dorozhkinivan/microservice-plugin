package ru.itmo.ivandor.plugin.actions

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.TreeClassChooser
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.lang.java.JavaLanguage
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorTextField
import com.intellij.ui.util.preferredWidth
import com.intellij.util.io.HttpRequests
import com.jetbrains.rd.util.firstOrNull
import generateNewJavaClass
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.beans.PropertyChangeListener
import java.net.HttpURLConnection
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
import javax.swing.SwingWorker
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import removeUnusedImports
import ru.itmo.ivandor.plugin.auth.PluginOAuthService
import ru.itmo.ivandor.plugin.dto.ClassDto
import ru.itmo.ivandor.plugin.dto.MicroserviceDto
import ru.itmo.ivandor.plugin.dto.RequestDto
import ru.itmo.ivandor.plugin.dto.ResponseDto
import ru.itmo.ivandor.plugin.settings.PluginSettings


fun createFacadesPanel(businessLogicHolder: BusinessLogicHolder, viewHolder: ViewHolder, file: LightVirtualFile1): JPanel {
    val addFacadeButton = JButton("Add facade")
    val generateButton = JButton("Generate code")

    generateButton.isEnabled = false
    generateButton.repaint()

    generateButton.addActionListener {
        when(Messages.showYesNoCancelDialog(businessLogicHolder.project, "Close page on generation?", "", Messages.getQuestionIcon())){
            Messages.YES -> {
                FileEditorManager.getInstance(businessLogicHolder.project).closeFile(file)
            }
            Messages.NO -> {
                //
            }
            Messages.CANCEL -> {
                //
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

    HttpRequestWorker(file = file, businessLogicHolder = businessLogicHolder) { msList ->
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
    businessLogicHolder.microservices[microserviceId] = Microservice(ms?.name ?: "todo-add-name", classes = emptyList())

    fun addClass(clazz: PsiClass?) {
        val itemPanel = JPanel()
        itemPanel.alignmentX = Component.LEFT_ALIGNMENT
        itemPanel.layout = BoxLayout(itemPanel, BoxLayout.X_AXIS)

        var result = listOf<PsiClass>()

        var text : String? = null
        var textFull : String? = null
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


        if (isRemote || result.isNotEmpty()){
            result.forEach { selectedClass ->
                businessLogicHolder.microservices[microserviceId]!!.classes += selectedClass
                text = selectedClass.name
                textFull = selectedClass.qualifiedName
                val itemLabel = JLabel(text)

                val deleteLabel = JLabel("🗑")
                deleteLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                deleteLabel.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        businessLogicHolder.microservices[microserviceId]?.classes // fixme
                        // fixme add
                        //         generateButton.isEnabled = false on conditopn
                        //        generateButton.repaint()
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


    val addButton = JButton("Add class")
    addButton.addActionListener {
        addClass(null)
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

    val generateButton = JButton("Show code")
    generateButton.addActionListener {

        businessLogicHolder.microservices.firstOrNull()?.value?.classes?.firstOrNull()?.let {
            val text = generateNewJavaClass(it, businessLogicHolder.microservices[microserviceId]!!.name)
            //val tet = generateNewJavaClassFromPsiClass(it)
            when(it.language){
                JavaLanguage.INSTANCE -> {
                    viewHolder.updateWithJava(text)
                }
                KotlinLanguage.INSTANCE -> {
                    println("Kotlin!!!!!")
                    viewHolder.updateWithJava(removeUnusedImports(text, businessLogicHolder.project))
                }
            }
        } ?: "not found"

    }
    buttonsPanel.add(generateButton)

    val flagsPanel = JPanel()
    flagsPanel.layout = GridLayout(3, 1)

    val checkboxUseKotlin = JCheckBox("Use Kotlin")
    checkboxUseKotlin.addActionListener { _ ->
        businessLogicHolder.microservices[microserviceId]?.useKotlin = checkboxUseKotlin.isSelected
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
        addClass(businessLogicHolder.requestedClasses.first { it.name == classStr })
    }

    return panel
}


class HttpRequestWorker(
    private val file: LightVirtualFile1,
    private val businessLogicHolder: BusinessLogicHolder,
    private val block: (List<MicroserviceDto>) -> Unit,
) : SwingWorker<List<MicroserviceDto>, Unit>() {

    private fun getMicroservicesOrNullOn401(jwt: String) : List<MicroserviceDto>? {
        val classes = file.classes.map {
            ClassDto(name = it.name ?: "?", methods = it.methods.map { it.name })
        }
        val jsonBody = GsonBuilder().create().toJson(RequestDto(classes))
        return HttpRequests.post("${PluginSettings.HOST}/process", "application/json")
            .tuner {
                it.setRequestProperty("Authorization", "Bearer $jwt")
            }.throwStatusCodeException(false)
            .connect { request -> request.write(jsonBody)
                val responseCode = (request.connection as HttpURLConnection).responseCode
                when(responseCode){
                    HttpURLConnection.HTTP_OK -> Gson().fromJson(request.readString(), ResponseDto::class.java).let {
                        businessLogicHolder.requestId = it.requestId
                        it.microservices
                    }
                    HttpURLConnection.HTTP_UNAUTHORIZED -> null
                    else -> throw RuntimeException()
                }
            }
    }

    override fun doInBackground(): List<MicroserviceDto>? {

        val notification = Notification(
            "Microservices",
            "Microservices plugin enabled",
            "Files of the selected directories are being analyzed",
            NotificationType.INFORMATION
        )
        Notifications.Bus.notify(notification)

        val jwtFromSettings = PluginSettings.instance.state.JWT_TOKEN

        val res = if (jwtFromSettings.isEmpty()) null else getMicroservicesOrNullOn401(jwtFromSettings)

        return res
    }

    override fun done() {
        try {
            val result = get() ?: when(Messages.showYesNoCancelDialog(businessLogicHolder.project, "Authorize with github? Otherwise you can configure facades without LLM.", "Authorization", Messages.getQuestionIcon())){
                Messages.YES -> {
                    val token = PluginOAuthService.instance.authorize().get().accessToken
                    PluginSettings.instance.loadState(PluginSettings.instance.state.apply { this.JWT_TOKEN = token })
                    getMicroservicesOrNullOn401(token)!!
                }
                Messages.NO -> {
                    emptyList()
                }
                else -> {
                    FileEditorManager.getInstance(businessLogicHolder.project).closeFile(file)
                    emptyList()
                }
            }
            block(result)
        } catch (e: Exception) {
            e.printStackTrace()
            val notification = Notification(
                "Microservices",
                "Backend error",
                "Can not get facades from backend. You can configure custom facades🙃",
                NotificationType.WARNING
            )
            Notifications.Bus.notify(notification)
            block(listOf())
        }
    }
}


fun createMainComponent(businessLogicHolder: BusinessLogicHolder, file: LightVirtualFile1): JComponent {

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
  //  leftPanel.preferredSize = Dimension(600, screenBounds.height) // Это будет изменено компоновщиком
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
    var useKotlin: Boolean = true,
    var deprecateClasses: Boolean = true,
    var addHttpClient: Boolean = false,
)

data class BusinessLogicHolder(
    var microservices: HashMap<String,Microservice> = HashMap(),
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
        //if (javaEditor !in rightPanel.components)
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
        //if (kotlinEditor !in rightPanel.components)
            rightPanel.add(JScrollPane(kotlinEditor).apply {
                this.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                this.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            }, BorderLayout.CENTER)
        kotlinEditor.text = code
        rightPanel.add(kotlinEditor)
        kotlinEditor.repaint()
        rightPanel.revalidate()
        rightPanel.repaint()
    }
}

class LightVirtualFile1(
    val classes: List<PsiClass>,
) : LightVirtualFile("Microservices", MicroservicesFileType(), "")


class MicroservicesFacadesFileEditor(project: Project, private val file: LightVirtualFile1) : FileEditor {
    //private val panel = MyPanel(project, file)
    private val requestedClasses = file.classes
    private val businessLogicHolder = BusinessLogicHolder(project = project, requestedClasses = requestedClasses, requestId = null)

    private val panel = createMainComponent(businessLogicHolder, file)

    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent = panel

    override fun getName(): String = "Stubs"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getFile(): VirtualFile = file

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    override fun <T : Any?> getUserData(key: Key<T>): T? {
return null
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
    }

    override fun dispose() {}
}

