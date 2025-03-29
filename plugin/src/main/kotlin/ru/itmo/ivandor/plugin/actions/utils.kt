import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType

fun generateNewJavaClass(originalClass: PsiClass, facadeName: String): String {
    val builder = StringBuilder()

    builder.append("import java.util.List;\n" +
            "import java.util.function.BinaryOperator;\n\n")
    builder.append("public class $facadeName {\n\n")
    builder.append("boolean useRemoteService = false;\n\n")
    for (method in originalClass.methods) {
        if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
            generateMethod(method, builder)
        }
    }

    builder.append("}\n")
    return builder.toString()
}

private fun generateMethod(method: PsiMethod, builder: StringBuilder) {
    val returnType = method.returnType?.presentableText ?: "void"
    val methodName = method.name
    val parameterList = generateParameterList(method.parameterList.parameters)
    val throwsList = generateThrowsList(method.throwsList.referencedTypes)

    builder.append("    public $returnType $methodName($parameterList) $throwsList {\n")
    builder.append("        if (!useRemoteService) {\n")

    val callOriginalMethod = buildString {
        if (returnType != "void") append("return ")
        append(methodName)
        append("(")
        append(method.parameterList.parameters.joinToString(", ") { it.name })
        append(");")
    }

    builder.append("            $callOriginalMethod\n")
    builder.append("        } else {\n")
    builder.append("            throw new UnsupportedOperationException(\"Not implemented yet\");\n")
    builder.append("        }\n")
    builder.append("    }\n\n")
}

private fun generateParameterList(parameters: Array<PsiParameter>): String {
    return parameters.joinToString(", ") { "${it.type.presentableText} ${it.name}" }
}

private fun generateThrowsList(throwsTypes: Array<PsiClassType>): String {
    return if (throwsTypes.isNotEmpty()) {
        "throws " + throwsTypes.joinToString(", ") { it.presentableText }
    } else {
        ""
    }
}