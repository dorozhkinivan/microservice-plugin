import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*



import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ide.util.TreeClassChooser
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.SlowOperations
import javax.swing.JButton
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dialog
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorTextField

import java.awt.Window
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import java.awt.geom.QuadCurve2D
import ru.itmo.ivandor.plugin.actions.MyFileType
import ru.itmo.ivandor.plugin.actions.Test


class ShowJBCefDialogAction : AnAction() {
    private fun openCustomTab(project: Project) {

        val virtualFile = MyFileType()

        try {
            FileEditorManager.getInstance(project).openFile(LightVirtualFile("myCustomVirtualFile", virtualFile, ""), true)
        } catch (e: Throwable){
            logger<Any>().warn("START!!!")
            e.printStackTrace()
            logger<Any>().warn("END!!!")
            throw e
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
//
//        SwingUtilities.invokeLater {
//            createAndShowGUI()
//        }

        // baza
        val project = event.project ?: return
        openCustomTab(project)

//        // Показать окно выбора классов
//        SwingUtilities.invokeLater {
//            val frame = JFrame("Class Selector")
//            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//
//            val ideWindow: Window? = WindowManager.getInstance().getFrame(project)
//
//            frame.add(MyForm(project, frame))
//            frame.setSize(400, 600)
//            frame.setLocationRelativeTo(ideWindow)
//            frame.isVisible = true
//
//        }
    }


//    override fun actionPerformed(event: AnActionEvent) {
//        if (!JBCefApp.isSupported()) {
//            println("JCEF is not supported on this platform.")
//            return
//        }
//
//        SwingUtilities.invokeLater {
//                val frame = JFrame("JCEF Browser")
//                val browser = JBCefBrowser("https://stackoverflow.com/questions/70793829") // Замените на вашу страницу
//
////                // Создаем JBCefJSQuery для передачи данных из JS в Kotlin
////                val jsQuery = JBCefJSQuery.create(browser)
////
////                // Установка слушателя для ответа от JS
////                jsQuery.addHandler { jsResponse ->
////                    println("Received from JS: $jsResponse")
////                    null // Возвращает null, чтобы завершить обработчик
////                }
//
//                // JS-код для отправки данных в Kotlin
//                val jsCode = """
//                window.sendDataToKotlin = function(data) {
//                    cefQuery({
//                        request: JSON.stringify(data),
//                        onSuccess: function(response) { console.log('Success:', response); },
//                        onFailure: function(error_code, error_message) { console.log('Error:', error_message); }
//                    });
//                };
//            """
//
////                // Выполнение JS-кода в браузере
////                browser.executeJavaScript(jsCode, 0)
//
//                // Пример передачи данных в JavaScript
//                val kotlinDataToSend = mapOf("key" to "value")
//
//
//                frame.contentPane.add(browser.component)
//                frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//                frame.setSize(1024, 768)
//                frame.setLocationRelativeTo(null)
//                frame.isVisible = true
//
//            browser.cefBrowser.client.addLoadHandler(object : CefLoadHandlerAdapter() {
//                override fun onLoadEnd(browser: org.cef.browser.CefBrowser?, frame: org.cef.browser.CefFrame?, httpStatusCode: Int) {
//                    frame?.executeJavaScript(
//                        "document.body.innerHTML = 'a';",
//                        browser!!.url,
//                        0
//                    )
//                }
//            })
//
//        }
//        }


    private fun createAndShowGUI() {
        val frame = JFrame("Connection Example")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.size = Dimension(800, 600)
        frame.layout = BorderLayout()

        val leftPanel = JPanel()
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        leftPanel.add(JLabel("Left Item 1"))
        leftPanel.add(JLabel("Left Item 2"))

        val rightPanel = JPanel()
        rightPanel.layout = BoxLayout(rightPanel, BoxLayout.Y_AXIS)
        rightPanel.add(JLabel("Right Item 1"))
        rightPanel.add(JLabel("Right Item 2"))

        val connectorPanel = ConnectorPanel(leftPanel, rightPanel)
        frame.add(leftPanel, BorderLayout.WEST)
        frame.add(rightPanel, BorderLayout.EAST)
        frame.add(connectorPanel, BorderLayout.CENTER)

        frame.isVisible = true
    }

    class ConnectorPanel(private val leftPanel: JPanel, private val rightPanel: JPanel) : JPanel() {

        private var startPoint: Point? = null
        private var endPoint: Point? = null

        init {
            background = Color.WHITE

            leftPanel.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    startPoint = e.point
                }
            })

            rightPanel.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    endPoint = e.point
                    repaint()
                }
            })
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.stroke = BasicStroke(2f)
            g2.color = Color.BLUE

            startPoint?.let { start ->
                endPoint?.let { end ->
                    val globalStart = SwingUtilities.convertPoint(leftPanel, start, this)
                    val globalEnd = SwingUtilities.convertPoint(rightPanel, end, this)
                    drawCurvedLine(g2, globalStart, globalEnd)
                }
            }
        }

        private fun drawCurvedLine(g: Graphics2D, start: Point, end: Point) {
            val ctrl1X = start.x + (end.x - start.x) / 2
            val ctrl1Y = start.y
            val ctrl2X = start.x + (end.x - start.x) / 2
            val ctrl2Y = end.y
            val curve = QuadCurve2D.Float(start.x.toFloat(), start.y.toFloat(),
                ctrl1X.toFloat(), ctrl1Y.toFloat(),
                end.x.toFloat(), end.y.toFloat())
            g.draw(curve)
        }
    }
    }


