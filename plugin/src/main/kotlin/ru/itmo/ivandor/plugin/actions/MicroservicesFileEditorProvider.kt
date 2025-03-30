package ru.itmo.ivandor.plugin.actions

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class MicroservicesFileEditorProvider : FileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.name == "Microservices"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return MicroservicesFacadesFileEditor(project, file as LightVirtualFile1)
    }

    override fun getEditorTypeId(): String = "microservices"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR


}