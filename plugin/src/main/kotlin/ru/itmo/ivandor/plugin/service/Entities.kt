package ru.itmo.ivandor.plugin.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorTextField
import java.awt.BorderLayout
import java.util.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import ru.itmo.ivandor.plugin.idea_file.MicroservicesFileType

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
