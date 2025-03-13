package ru.itmo.ivandor.plugin.actions

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class MyCustomFileEditorProvider : FileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        // Условие, по которому ваш редактор будет открыт
        return file.name == "myCustomVirtualFile"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return Test(project, file)
    }

    override fun getEditorTypeId(): String = "my-custom-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR


}