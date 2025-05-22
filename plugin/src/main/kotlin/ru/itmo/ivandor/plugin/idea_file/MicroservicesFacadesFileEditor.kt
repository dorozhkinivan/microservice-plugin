package ru.itmo.ivandor.plugin.idea_file

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import ru.itmo.ivandor.plugin.service.BusinessLogicHolder
import ru.itmo.ivandor.plugin.service.code_analyse.CodeAnalyseServiceImpl
import ru.itmo.ivandor.plugin.ui.createMainComponent

class MicroservicesFacadesFileEditor(
    project: Project,
    private val file: MicroservicesConfigVirtualFile,
) : FileEditor {
    private val codeAnalyseService = CodeAnalyseServiceImpl(file.projectFiles, project)
    private val businessLogicHolder = BusinessLogicHolder(
        project = project,
        requestedClasses = codeAnalyseService.getPsiClasses(),
        requestId = null,
    )

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
