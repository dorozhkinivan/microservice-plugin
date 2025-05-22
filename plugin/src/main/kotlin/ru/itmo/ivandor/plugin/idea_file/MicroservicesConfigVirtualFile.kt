package ru.itmo.ivandor.plugin.idea_file

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile

class MicroservicesConfigVirtualFile(
    val projectFiles: Array<VirtualFile>,
) : LightVirtualFile("Microservices", MicroservicesFileType(), "")