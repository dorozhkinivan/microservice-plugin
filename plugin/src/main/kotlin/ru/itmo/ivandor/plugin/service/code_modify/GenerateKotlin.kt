package ru.itmo.ivandor.plugin.service.code_modify

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiModifier
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile

fun generateNewKotlinClass(
    project: Project,
    className: String,
    classes: List<PsiClass>
): String {
    val importList = mutableSetOf<String>()

    val classContent = StringBuilder("class $className {\n")
    classContent.append("    private val useMicroservice: Boolean = false\n")
    classes.forEach { originalClass ->
        val fieldName = originalClass.name?.decapitalize() ?: "unknown"
        val classType = originalClass.qualifiedName ?: originalClass.name ?: "Any"

        classContent.append("    private val $fieldName = $classType()\n")

        originalClass.methods.filter {
            it.hasModifierProperty(PsiModifier.PUBLIC)
        }.forEach { method ->
            val methodName = method.name
            val isSuspend = method.modifierList.hasModifierProperty("suspend")
            val returnType = method.returnType?.canonicalText.takeIf { it != "Unit" && it != "void" }.orEmpty()

            val parameterList = method.parameterList.parameters.joinToString(", ") { param ->
                importList.add(param.type.canonicalText)
                "${param.name}: ${param.type.presentableText}"
            }

            val callArguments = method.parameterList.parameters.joinToString(", ") { param ->
                param.name ?: ""
            }

            val returnPrefix = if (returnType.isNotEmpty()) "return " else ""
            val suspendPrefix = if (isSuspend) "suspend " else ""

            classContent.append("""
                |    
                |    ${suspendPrefix}fun $methodName($parameterList)${if (returnType.isEmpty()) "" else ":$returnType"} {
                |       if (!useMicroservice)
                |        $returnPrefix$fieldName.$methodName($callArguments)
                |       else
                |        throw NotImplementedError()
                |    }
            """.trimMargin())
        }
    }

    classContent.append("}")

    val fileText = StringBuilder().apply {
        importList.forEach { import ->
            append("import $import\n")
        }
        append("\n")
        append(classContent.toString())
    }.toString()

    val ktFile = PsiFileFactory.getInstance(project).createFileFromText(
        "$className.kt", KotlinFileType.INSTANCE, fileText
    ) as? KtFile

    WriteCommandAction.runWriteCommandAction(project) {
        CodeStyleManager.getInstance(PsiManager.getInstance(project)).reformat(ktFile!!)
    }

    return ktFile!!.text
}