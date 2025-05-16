import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project


import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager

import org.jetbrains.kotlin.psi.KtFile
import ru.itmo.ivandor.plugin.actions.MicroservicesConfigVirtualFile


class AnalyseCodeAction : AnAction() {
    private fun openCustomTab(project: Project, event: AnActionEvent) {
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
            return@mapNotNull ((file as? PsiJavaFile) ?: (file as? KtFile))
        }.map { it.classes.toList() }.flatten()

        FileEditorManager.getInstance(project).openFile(MicroservicesConfigVirtualFile(javaClasses), true)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        openCustomTab(project, event)
    }
}


