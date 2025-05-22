package ru.itmo.ivandor.plugin.service.code_modify


import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.isAncestor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtPsiFactory
import ru.itmo.ivandor.plugin.service.BusinessLogicHolder
import ru.itmo.ivandor.plugin.remote.HttpCompleteRequestWorker

interface CodeModifyService {
    fun generateCode(microserviceId: String) : String

    fun deprecateClasses(classes: Map<PsiClass, String>)

    fun saveCode() : Boolean
}

class CodeModifyServiceImpl(
    private val businessLogicHolder: BusinessLogicHolder
) : CodeModifyService {
    override fun generateCode(microserviceId: String): String {
        val microservice = businessLogicHolder.microservices[microserviceId]!!
        return if (microservice.useKotlin){
            generateNewKotlinClass(
                project = businessLogicHolder.project,
                className = microservice.name,
                classes = microservice.classes,
            )
        } else {
            generateNewJavaClass(
                project = businessLogicHolder.project,
                className = microservice.name,
                classes = microservice.classes,
            )
        }
    }

    override fun deprecateClasses(classes: Map<PsiClass, String>) {
        for ((cls, facadeName) in classes) {
            val project = cls.project
            if (cls is KtClass) {
                val ktFactory = KtPsiFactory(project)

                    if (!cls.annotationEntries.any { it.shortName?.asString() == "Deprecated" }) {
                        cls.addAnnotationEntry(
                            ktFactory.createAnnotationEntry("@Deprecated(\"This class is deprecated, use $facadeName\")")
                        )
                    }

            } else {
                val psiFactory = PsiElementFactory.getInstance(project)
                    if (cls.modifierList?.annotations?.none { it.qualifiedName == "java.lang.Deprecated" } == true) {
                        val comment = psiFactory.createCommentFromText("// Use $facadeName", null)
                        val deprecatedAnnotation = psiFactory.createAnnotationFromText("@Deprecated // Use $facadeName", null)
                        val added = cls.modifierList?.addBefore(deprecatedAnnotation, cls.modifierList?.firstChild) ?: return
                        cls.modifierList?.addAfter(comment, added)
                    }
                }

        }
    }

    private fun findCommonParentDirectory(directories: List<PsiDirectory>): PsiDirectory {
        if (directories.isEmpty()) {
            throw IllegalArgumentException()
        }

        var commonDirectory: PsiDirectory = directories[0]

        for (i in 1 until directories.size) {
            val currentDirectory = directories[i]
            while (!currentDirectory.isAncestor(commonDirectory)) {
                commonDirectory = commonDirectory.parent!!
            }
        }

        return commonDirectory
    }
    private fun getPackageName(project: Project, dir: PsiDirectory): String? {
        val fileIndex = ProjectFileIndex.getInstance(project)
        val vDir = dir.virtualFile
        val sourceRoot = fileIndex.getSourceRootForFile(vDir) ?: return null
        val sourceRootPath = sourceRoot.path
        val dirPath = vDir.path

        if (dirPath.length <= sourceRootPath.length) return ""

        val relPath = dirPath.substring(sourceRootPath.length)
            .trimStart('/', '\\')
            .replace('/', '.')
            .replace('\\', '.')
        return relPath
    }

    override fun saveCode() : Boolean {
        try {
            HttpCompleteRequestWorker(businessLogicHolder).execute()

            val microservices = businessLogicHolder.microservices.filter { it.value.classes.isNotEmpty() }

            val directory = findCommonParentDirectory(microservices.flatMap { it.value.classes }.map { it.containingFile.containingDirectory })

            val namesToNewClasses = microservices.values.associate {
                if(it.useKotlin){
                    "${it.name}.kt" to generateNewKotlinClass(businessLogicHolder.project, it.name, it.classes)
                } else {
                    "${it.name}.java" to generateNewJavaClass(businessLogicHolder.project, it.name, it.classes)
                }
            }
            ProgressManager.getInstance().run(object : Task.Backgroundable(businessLogicHolder.project, "Code generation", false) {
                override fun run(indicator: ProgressIndicator) {
                    WriteCommandAction.runWriteCommandAction(businessLogicHolder.project){
                        val subDirectory = directory.createSubdirectory(
                            "facades_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}"
                        )
                        val pack = getPackageName(businessLogicHolder.project, subDirectory)

                        val deprecatedClassesMS = microservices.filter { it.value.deprecateClasses }
                        CodeModifyServiceImpl(businessLogicHolder).deprecateClasses(
                            deprecatedClassesMS.map { it.value.classes to it.value.name }.flatMap { it.first.map { clazz -> clazz to "$pack.${it.second}" } }.toMap()
                        )


                        namesToNewClasses.forEach { (fileName, fileText) ->
                            createFileInDirectory(subDirectory, fileName, "package $pack;\n\n$fileText")
                        }

                    }
                }
            })
        } catch (e: Throwable){
            return false
        }
        return true
    }


    private fun createFileInDirectory(psiDirectory: PsiDirectory, fileName: String, fileText: String) {
        val project = psiDirectory.project

        val fileType = when {
            fileName.endsWith(".java") -> JavaFileType.INSTANCE
            fileName.endsWith(".kt") -> KotlinFileType.INSTANCE
            else -> PlainTextFileType.INSTANCE
        }


        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, fileType, fileText)
        psiDirectory.add(psiFile)
    }
}
