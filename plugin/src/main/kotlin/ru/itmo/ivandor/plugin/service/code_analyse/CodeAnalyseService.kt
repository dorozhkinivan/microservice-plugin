package ru.itmo.ivandor.plugin.service.code_analyse

import com.intellij.psi.PsiClass
import ru.itmo.ivandor.plugin.dto.ClassDto

interface CodeAnalyseService {
    fun buildAnalyseData() : List<ClassDto>

    fun getPsiClasses() : List<PsiClass>
}