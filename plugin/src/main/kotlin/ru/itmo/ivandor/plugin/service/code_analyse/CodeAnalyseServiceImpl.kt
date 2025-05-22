package ru.itmo.ivandor.plugin.service.code_analyse

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import kotlin.reflect.KProperty1
import org.jetbrains.kotlin.psi.KtFile
import ru.itmo.ivandor.plugin.dto.ClassDto

data class Demo(val name: String)

private val defaultMethods = Demo::class.members.filter { it !is KProperty1<*, *> }
    .map { it.name }

private val PsiClass.filteredMethods: List<String>
    get() = this.methods.map { it.name }.filter { it !in defaultMethods }

class CodeAnalyseServiceImpl(
    private val files: Array<VirtualFile>,
    private val project: Project,
) : CodeAnalyseService {
    override fun buildAnalyseData() : List<ClassDto> = getPsiClasses().mapNotNull {
        val methods = it.filteredMethods
        if (methods.isEmpty()) null
        else ClassDto(name = it.name.orEmpty(), methods = methods)
    }

    private fun findAllClassFilesInDirectory(directory: VirtualFile, classFiles: MutableList<VirtualFile>) {
        for (child in (directory.children ?: emptyArray())) {
            if (child.isDirectory) {
                findAllClassFilesInDirectory(child, classFiles)
            } else {
                classFiles.add(child)
            }
        }
    }

    override fun getPsiClasses(): List<PsiClass> {
        val classFiles = mutableListOf<VirtualFile>()

        files.forEach {
            if (it.isDirectory){
                findAllClassFilesInDirectory(it, classFiles)
            }
        }
        return classFiles.mapNotNull {
            val file = PsiManager.getInstance(project).findFile(it)
            return@mapNotNull ((file as? PsiJavaFile) ?: (file as? KtFile))
        }.map { it.classes.toList() }.flatten()
    }
}