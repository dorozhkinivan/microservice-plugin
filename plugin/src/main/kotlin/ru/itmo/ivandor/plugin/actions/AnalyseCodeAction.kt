import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import ru.itmo.ivandor.plugin.idea_file.MicroservicesConfigVirtualFile


class AnalyseCodeAction : AnAction() {
    private fun openCustomTab(project: Project, event: AnActionEvent) {
        val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: throw RuntimeException("No files selected")

        FileEditorManager.getInstance(project).openFile(MicroservicesConfigVirtualFile(files), true)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        openCustomTab(project, event)
    }
}


