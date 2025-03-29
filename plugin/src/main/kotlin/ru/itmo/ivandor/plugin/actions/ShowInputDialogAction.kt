import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project


import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import java.util.concurrent.TimeUnit

import org.jetbrains.kotlin.psi.KtFile
import ru.itmo.ivandor.plugin.actions.LightVirtualFile1
import ru.itmo.ivandor.plugin.auth.MSAuthService
import ru.itmo.ivandor.plugin.settings.MsSettings


class AnalyseCodeAction : AnAction() {
    private fun openCustomTab(project: Project, event: AnActionEvent) {

        try {

            val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: throw RuntimeException("No files selected")
            val classFiles = mutableListOf<VirtualFile>()

            fun findAllClassFilesInDirectory(directory: VirtualFile, classFiles: MutableList<VirtualFile>) {
                for (child in (directory.children ?: emptyArray())) {
                    if (child.isDirectory) {
                        findAllClassFilesInDirectory(child, classFiles)
                    } else {
                        classFiles.add(child)
                    }
                }
            }

            files.forEach {
                if (it.isDirectory){
                    findAllClassFilesInDirectory(it, classFiles)
                }
            }

            val javaClasses = classFiles.mapNotNull {
                val file = PsiManager.getInstance(event.project!!).findFile(it)
                println("GOT FFF ${file?.name} ${file is PsiJavaFile} ${file is KtFile}")
                return@mapNotNull ((file as? PsiJavaFile) ?: (file as? KtFile))
            }.map { it.classes.toList().also { println(">>>  ${it.size}") } }.flatten().also { println("Got classes ${it.map { it.name }}") }

            FileEditorManager.getInstance(project).openFile(LightVirtualFile1(javaClasses), true)
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

        val project = event.project ?: return

        openCustomTab(project, event)
    }
}


