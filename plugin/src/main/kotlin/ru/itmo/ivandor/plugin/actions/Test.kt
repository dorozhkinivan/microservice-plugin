package ru.itmo.ivandor.plugin.actions

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.TreeClassChooser
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.diagnostic.logger
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
import com.intellij.ui.util.preferredHeight
import com.intellij.ui.util.preferredWidth
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.GridBagConstraints
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.beans.PropertyChangeListener
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextField
import javax.swing.SwingConstants
import kotlin.random.Random
import org.jetbrains.kotlin.idea.KotlinFileType


class MyPanel(private val project: Project, private val file: VirtualFile) : JPanel(BorderLayout()) {


    private val selectButton = JButton("Select Class")

    init {
        add(selectButton, BorderLayout.SOUTH)
        add(createPanel(project), BorderLayout.SOUTH)

        selectButton.addActionListener {
            val chooserFactory = TreeClassChooserFactory.getInstance(project)
            val chooser: TreeClassChooser = chooserFactory.createNoInnerClassesScopeChooser(
                "Select a Class",
                GlobalSearchScope.projectScope(project),
                null,
                null
            )

            chooser.showDialog()
            val selectedClass: PsiClass? = chooser.selected
            logger<AnAction>().warn("!!!! ${selectedClass?.qualifiedName}")
                        if (selectedClass != null) {
                            Messages.showMessageDialog(
                                project,
                                "Selected class: ${selectedClass.qualifiedName}",
                                "Class Selected",
                                Messages.getInformationIcon()
                            )
                        }

            FileEditorManager.getInstance(project).closeFile(file)

        }
        }
}

fun createPanelWithEditableTitle(project: Project): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

    // Заголовок, который пользователь может изменить
    val titleField = JTextField("Editable Title")
    panel.add(titleField)

    // Модель для списка элементов
    val listModel = DefaultListModel<String>()
    val list = JList(listModel)
    val scrollPane = JScrollPane(list)
    panel.add(scrollPane)

    // Обработчик удаления элементов
    list.cellRenderer = object : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?, value: Any?, index: Int,
            isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            return JPanel(BorderLayout()).apply {
                add(component, BorderLayout.CENTER)
                add(JButton("Удалить элемент").apply {
                    addActionListener {
                        listModel.remove(index)
                    }
                }, BorderLayout.EAST)
            }
        }
    }

    // Кнопка добавления нового элемента
    val addButton = JButton("Добавить элемент").apply {
        addActionListener {
            val chooserFactory = TreeClassChooserFactory.getInstance(project)
            val chooser: TreeClassChooser = chooserFactory.createNoInnerClassesScopeChooser(
                "Select a Class",
                GlobalSearchScope.projectScope(project),
                null,
                null
            )

            chooser.showDialog()
            val selectedClass: PsiClass? = chooser.selected
            selectedClass?.let { listModel.addElement(selectedClass.name) }
        }
    }
    panel.add(addButton)

    return panel
}

fun createPanel1(): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

    // Верхнее текстовое поле для редактирования заголовка
    val headerField = JTextField("Заголовок")
    headerField.maximumSize = Dimension(Int.MAX_VALUE, headerField.preferredSize.height)
    panel.add(headerField)

    // Панель для списка элементов
    val listPanel = JPanel()
    listPanel.layout = BoxLayout(listPanel, BoxLayout.Y_AXIS)
    panel.add(listPanel)

    // Функция для добавления элемента в панель
    fun addElement() {
        val itemPanel = JPanel()
        itemPanel.layout = BoxLayout(itemPanel, BoxLayout.X_AXIS)

        // Значок удаления и его действие
        val deleteLabel = JLabel("🗑")
        deleteLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        deleteLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                listPanel.remove(itemPanel)
                listPanel.revalidate()
                listPanel.repaint()
            }
        })

        // Значок с подсказкой
        val infoLabel = JLabel("ℹ")
        infoLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        infoLabel.toolTipText = "Это подсказка"

        // Текст элемента
        val itemLabel = JLabel("Элемент ${Random.nextInt(100)}")

        // Добавляем компоненты в панель элемента
        itemPanel.add(deleteLabel)
        itemPanel.add(Box.createRigidArea(Dimension(5, 0)))
        itemPanel.add(infoLabel)
        itemPanel.add(Box.createRigidArea(Dimension(5, 0)))
        itemPanel.add(itemLabel)

        // Добавляем панель элемента в панель списка
        listPanel.add(itemPanel)
        listPanel.revalidate()
    }

    // Кнопка добавить
    val addButton = JButton("Добавить элемент")
    addButton.addActionListener {
        addElement()
    }
    panel.add(addButton)

    // Добавляем начальный элемент
    addElement()

    return panel
}




fun createPanel(project: Project): JPanel {
    val panel = JPanel(GridLayout(3, 3, 5, 5))
    panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    // Список компонентов для отслеживания количества добавленных блоков
    val blocks = mutableListOf<JPanel>()

    // Функция для добавления нового блока
    val addBlock: () -> Unit = {
        if (blocks.size < 9) {
            val newBlock = JPanel()
          //  newBlock.preferredSize = Dimension(300, 300)
            //newBlock.size = Dimension(300, 300)
            newBlock.background = Color.WHITE
            newBlock.border = BorderFactory.createLineBorder(Color.PINK)
            panel.add(newBlock)
            blocks.add(newBlock)
            panel.revalidate()
            panel.repaint()
        }
    }

    // Добавляем первый блок
    addBlock()

    panel.add(createPanelWithEditableTitle(project))
    panel.add(createPanelWithEditableTitle(project))
    panel.add(createPanel1())
    panel.add(createPanel1())
    panel.revalidate()
    panel.repaint()



    // Создаем кнопку добавления
    val addButton = JButton("Добавить блок")
    addButton.addActionListener {
        addBlock()
    }

    // Добавляем кнопку и панель в основную панель
    val mainPanel = JPanel(BorderLayout())
    mainPanel.add(panel, BorderLayout.CENTER)
    mainPanel.add(addButton, BorderLayout.SOUTH)

    return mainPanel
}


fun createMainComponent(pr: Project): JComponent {
    // Основной Панель с BorderLayout
    val mainPanel = JPanel(BorderLayout())


    // Правая панель, которая занимает правые 40% ширины
    val rightPanel = JPanel(BorderLayout())

    rightPanel.add(EditorTextField(pr, KotlinFileType.INSTANCE).apply { text = "abobus"; isVisible = true; }, BorderLayout.CENTER)

    rightPanel.isVisible = true
    mainPanel.add(rightPanel, BorderLayout.LINE_END)

    // Левая панель заключает в себе текст, центральную панель и кнопку
    val leftPanel = JPanel(BorderLayout())
  //  leftPanel.preferredSize = Dimension(600, screenBounds.height) // Это будет изменено компоновщиком
    mainPanel.add(leftPanel, BorderLayout.CENTER)

    // Текст наверху
    val topLabel = JLabel("Текст сверху", SwingConstants.CENTER)
    leftPanel.add(topLabel, BorderLayout.NORTH)

    // Центральная панель между текстом и кнопкой
    val centerPanel = JPanel()
    leftPanel.add(centerPanel, BorderLayout.CENTER)

    // Панель для кнопки в самом низу
    val buttonPanel = JPanel()
    buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.Y_AXIS)
    val button = JButton("Кнопка")
    button.alignmentX = Component.CENTER_ALIGNMENT

    // Добавляем пустое пространство, чтобы кнопка была внизу
    buttonPanel.add(Box.createVerticalGlue())
    buttonPanel.add(button)
    buttonPanel.add(Box.createVerticalStrut(10)) // Отступ снизу

    leftPanel.add(buttonPanel, BorderLayout.SOUTH)

    val splitPanel = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createPanel(pr), rightPanel)

    splitPanel.resizeWeight = 0.5

    mainPanel.add(splitPanel, BorderLayout.CENTER)

    mainPanel.isVisible = true

    return mainPanel
}




class Test(private val project: Project, file: VirtualFile) : FileEditor {
    //private val panel = MyPanel(project, file)
    private val panel = createMainComponent(project)

    init {
//        panel.add(JBLabel("This is a custom editor tab"))
//
//        panel.isVisible = true
    }

    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent = panel

    override fun getName(): String = "Custom Editor"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getFile(): VirtualFile = LightVirtualFile("myCustomVirtualFile", MyFileType(), "")

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

