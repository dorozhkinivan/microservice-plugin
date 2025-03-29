package ru.itmo.ivandor.plugin.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope

fun generateNewJavaClassFromPsiClass(originalClass: PsiClass): String {
    val imports = mutableSetOf<String>()

    val className = originalClass.name + "Generated"
    val packageName = originalClass.qualifiedName?.substringBeforeLast(".")

    val methodStubs = originalClass.allMethods
        .filter { it.hasModifierProperty(PsiModifier.PUBLIC) }
        .joinToString("\n") { method ->
            val methodReturn = method.returnType?.canonicalText ?: "void"
            val methodName = method.name
            val parameters = method.parameterList.parameters.joinToString(", ") { param ->
                imports.add(param.type.canonicalText)
                "${param.type.canonicalText} ${param.name}"
            }
            val exceptions =
                if (method.throwsList.referencedTypes.isNotEmpty()) {
                    " throws ${method.throwsList.referencedTypes.joinToString(", ") { it.canonicalText }}"
                } else {
                    ""
                }

            """
            public $methodReturn $methodName($parameters)$exceptions {
                if (!useRemoteService) {
                    return original.$methodName(${method.parameterList.parameters.joinToString(", ") { it.name }});
                } else {
                    throw new UnsupportedOperationException("Not yet implemented");
                }
            }
            """.trimIndent()
        }

    val importStatements = imports.joinToString("\n") { importType ->
        "import $importType;"
    }

    return """
    |package $packageName;
    |
    |$importStatements
    |
    |public class $className {
    |    private final ${originalClass.name} original;
    |
    |    public $className(${originalClass.name} original) {
    |        this.original = original;
    |    }
    |    
    |
    $methodStubs
    |}
    """.trimMargin()
}






fun actionPerformed(event: AnActionEvent) {
    val virtualFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return
    val directories = virtualFiles.filter { it.isDirectory }


    directories.forEach { directory ->

        findAllClassFilesInDirectory(directory, classFiles)

        println("Directory: ${directory.path}")
        classFiles.forEach { classFile ->
            println("  Class: ${classFile.path}")
        }
    }
}
val classFiles = mutableListOf<VirtualFile>()
private fun findAllClassFilesInDirectory(directory: VirtualFile, classFiles: MutableList<VirtualFile>) {
    for (child in directory.children) {
        if (child.isDirectory) {
            findAllClassFilesInDirectory(child, classFiles)
        } else if (child.extension == "class" || child.extension == "kt" || child.extension == "java") {
            classFiles.add(child)
        }
    }
}
