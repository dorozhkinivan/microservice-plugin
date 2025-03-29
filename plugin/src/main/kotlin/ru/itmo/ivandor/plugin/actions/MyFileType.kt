package ru.itmo.ivandor.plugin.actions

import com.intellij.icons.AllIcons
import com.intellij.icons.AllIcons.Icons
import com.intellij.openapi.fileTypes.FileType
import java.awt.Image.SCALE_DEFAULT
import java.awt.Image.SCALE_SMOOTH
import javax.swing.Icon
import javax.swing.ImageIcon

class MyFileType : FileType {
    override fun getName(): String {
        return "Microservice refactoring"
    }
    override fun getDescription(): String {
        return "Microservice refactoring"
    }

    override fun getDefaultExtension(): String {
        return ""
    }

    override fun isReadOnly(): Boolean {
        return true
    }

    override fun getIcon(): Icon {
        val iconUrl = MyFileType::class.java.getResource("/META-INF/img.png")
        return ImageIcon(ImageIcon(iconUrl).image.getScaledInstance(16, 16, SCALE_SMOOTH))
    }

    override fun isBinary(): Boolean {
        return true
    }
}