package ru.itmo.ivandor.plugin.idea_file

import com.intellij.openapi.fileTypes.FileType
import java.awt.Image.SCALE_SMOOTH
import javax.swing.Icon
import javax.swing.ImageIcon

class MicroservicesFileType : FileType {
    override fun getName() = "Microservice refactoring"
    override fun getDescription() = "Microservices configuration before refactoring"
    override fun getDefaultExtension() = ""
    override fun isReadOnly() = true
    override fun getIcon(): Icon {
        val iconUrl = MicroservicesFileType::class.java.getResource("/META-INF/img.png")
        return ImageIcon(ImageIcon(iconUrl).image.getScaledInstance(16, 16, SCALE_SMOOTH))
    }
    override fun isBinary() = true
}