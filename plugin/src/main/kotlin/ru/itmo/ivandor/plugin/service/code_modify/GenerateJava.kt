package ru.itmo.ivandor.plugin.service.code_modify

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import java.util.*

fun generateNewJavaClass(
    project: Project,
    className: String,
    classes: List<PsiClass>
): String {
    val psiElementFactory = PsiElementFactory.getInstance(project)

    val newClassText = "class $className {}"
    val newJavaFile = PsiFileFactory.getInstance(project).createFileFromText(
        "$className.java", JavaFileType.INSTANCE, newClassText
    ) as PsiJavaFile

    val imports = mutableSetOf<String>()
    val newClass = newJavaFile.classes.firstOrNull() ?: throw IllegalStateException("??")

    classes.forEach { originalClass ->
        val classNameWithoutPackage = originalClass.name
        val field = psiElementFactory.createField(classNameWithoutPackage!!.replaceFirstChar { it.lowercase(Locale.getDefault()) }, originalClass.type())
        field.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
        newClass.add(field)
    }

    val useMicroserviceField = psiElementFactory.createField("useMicroservice", PsiTypes.booleanType())
    useMicroserviceField.initializer = psiElementFactory.createExpressionFromText("false", useMicroserviceField)
    useMicroserviceField.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
    newClass.add(useMicroserviceField)

    val methodsToWrap = mutableListOf<PsiMethod>()

    classes.forEach { originalClass ->
        val methods = originalClass.methods.filter {
            it.hasModifierProperty(PsiModifier.PUBLIC) || it.modifierList.text.isBlank()
        }
        methodsToWrap.addAll(methods)
    }

    methodsToWrap.forEach { method ->
        val methodReturnType = method.returnType?.canonicalText?.also { imports.add(it) } ?: "Unit"
        val methodName = method.name

        val paramsList = method.parameterList.parameters.joinToString(", ") { param ->
            imports.add(param.type.canonicalText)
            "${param.type.presentableText} ${param.name}"
        }

        val callArguments = method.parameterList.parameters.joinToString(", ") { it.name }

        val throwsList = method.throwsList.referencedTypes.joinToString(", ") {
            imports.add(it.canonicalText)
            it.canonicalText
        }

        val throwsClause = if (throwsList.isNotEmpty()) "throws $throwsList" else ""

        val originalClassName = method.containingClass?.name?.replaceFirstChar { it.lowercase(Locale.getDefault()) }

        val methodText = """
            public $methodReturnType $methodName($paramsList) $throwsClause {
                if (!useMicroservice)
                    return $originalClassName.$methodName($callArguments);
                throw new UnsupportedOperationException();
            }
        """.trimIndent()

        val newMethod = psiElementFactory.createMethodFromText(methodText, newClass)
        newClass.add(newMethod)
    }

    WriteCommandAction.runWriteCommandAction(project) {
        val importList = newJavaFile.importList ?: return@runWriteCommandAction
        imports.forEach { fqName ->
            val psiClass = JavaPsiFacade.getInstance(project).findClass(fqName, GlobalSearchScope.allScope(project))
            psiClass?.let {
                importList.add(psiElementFactory.createImportStatement(it))
            }
        }
    }

    WriteCommandAction.runWriteCommandAction(project) {
        CodeStyleManager.getInstance(PsiManager.getInstance(project)).reformat(newJavaFile)
    }

    return newJavaFile.text
}

fun PsiClass.type(): PsiType {
    return PsiClassType.getTypeByName(this.qualifiedName ?: this.name ?: "", this.project, GlobalSearchScope.allScope(this.project))
}