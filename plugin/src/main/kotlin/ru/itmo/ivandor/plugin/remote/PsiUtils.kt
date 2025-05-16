package ru.itmo.ivandor.plugin.remote

import com.intellij.psi.PsiClass
import kotlin.reflect.KProperty1

data class Demo(val name: String)

val defaultMethods = Demo::class.members.filter { it !is KProperty1<*, *> }
    .map { it.name }

val PsiClass.filteredMethods: List<String>
    get() = this.methods.map { it.name }.filter { it !in defaultMethods }