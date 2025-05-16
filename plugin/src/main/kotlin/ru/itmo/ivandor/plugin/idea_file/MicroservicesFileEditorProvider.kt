package ru.itmo.ivandor.plugin.idea_file

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.itmo.ivandor.plugin.actions.MicroservicesConfigVirtualFile
import ru.itmo.ivandor.plugin.actions.MicroservicesFacadesFileEditor

class MicroservicesFileEditorProvider : FileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile) = file.name == "Microservices"

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return MicroservicesFacadesFileEditor(project, file as MicroservicesConfigVirtualFile)
    }

    override fun getEditorTypeId(): String = "microservices"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}