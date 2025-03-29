import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiImportStatement
import com.intellij.psi.PsiImportStatementBase
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.j2k.JavaToKotlinConverter

fun removeUnusedImports(javaFileContent: String, project: Project): String {
    val factory = PsiFileFactory.getInstance(project)
    val psiFile = factory.createFileFromText("Dummy.java", JavaLanguage.INSTANCE, javaFileContent)

    if (psiFile is PsiJavaFile) {
        val importList = psiFile.importList ?: return javaFileContent

        val usedImports = mutableSetOf<String>()

        PsiTreeUtil.processElements(psiFile) { element ->
            if (element is PsiJavaCodeReferenceElement) {
                element.resolve()?.let { resolvedElement ->
                    JavaPsiFacade.getInstance(project).elementFactory.createReferenceFromText(resolvedElement.text, psiFile)
                    usedImports.add(resolvedElement.text!!)
                }
            }
            true
        }

        val importsToRemove = mutableListOf<PsiImportStatementBase>()

        for (import in importList.allImportStatements) {
            val importedReference = import.importReference?.qualifiedName
            if (importedReference != null && importedReference !in usedImports) {
                importsToRemove.add(import)
            }
        }

        for (importStatement in importsToRemove) {
            importList.deleteChildRange(importStatement, importStatement)
        }
        return psiFile.text
    }

    return javaFileContent
}