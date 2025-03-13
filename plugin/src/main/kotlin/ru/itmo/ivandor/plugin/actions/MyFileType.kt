package ru.itmo.ivandor.plugin.actions

import com.intellij.icons.AllIcons
import com.intellij.icons.AllIcons.Icons
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class MyFileType : FileType {
    override fun getName(): String {
        return "abobus"
    }
    override fun getDescription(): String {
        return "ABOBIS"
    }

    override fun getDefaultExtension(): String {
        return ""
    }

    override fun isReadOnly(): Boolean {
        return true
    }

    override fun getIcon(): Icon {
        return AllIcons.Actions.Diff
    }

    override fun isBinary(): Boolean {
        return true
    }
}