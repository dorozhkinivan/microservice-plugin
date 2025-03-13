package ru.itmo.ivandor.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch


class DisplayClassNamesAction : AnAction() {
//    override fun actionPerformed(event: AnActionEvent) {
//        // Получаем текущий проект
//        val project: Project = event.project ?: return
//
//        // Запрашиваем у пользователя имя класса
//        val className = Messages.showInputDialog(
//            project,
//            project,
//            "Enter the name of the class",
//            "Add JavaDoc to Usages",
//            Messages.getQuestionIcon()
//        ) ?: return
//
//
////        // Находим элемент класса по имени
////        val psiClass = JavaPsiFacade.getInstance(project)
////            .findClass(className, GlobalSearchScope.projectScope(project))
////            ?: run {
////                Messages.showMessageDialog(
////                    project,
////                    "Class $className not found in project.",
////                    "Error",
////                    Messages.getErrorIcon()
////                )
////                return
////            }
//
//        // todo
//
//                val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.first() ?: let {
//            println("wtf")
//            println("wtf")
//            return
//        }
//
//        val aaa = PsiManager.getInstance(event.project ?: return).findFile(files) as PsiJavaFile
//
//
//
//
//        // Поиск всех ссылок на класс
//        val usages = ReferencesSearch.search(aaa.classes.first()).findAll()
//
//        // Добавление JavaDoc для каждой ссылки
//        WriteCommandAction.runWriteCommandAction(project) {
//
//        usages.forEach { usage ->
//            val element = usage.element
//            println("element::class.simpleName")
//            println(element::class.simpleName)
//            if (element is PsiJavaCodeReferenceElementImpl) {
//                addJavaDocForElement(element)
//            }
//        }
//        }
//
//        Messages.showMessageDialog(
//            project,
//            "JavaDoc added to ${usages.size} usages of $className.",
//            "Completed",
//            Messages.getInformationIcon()
//        )
//    }
//
//    private fun addJavaDocForElement(element: PsiElement) {
//        val parent = element.parent
//
//        val factory = PsiElementFactory.getInstance(element.project)
//        val docCommentText = """
//            /**
//             * This is a placeholder for usage of the specified class.
//             */
//        """.trimIndent()
//
//        if (element !is PsiDocCommentOwner || element.docComment == null) {
//            val docComment = factory.createDocCommentFromText(docCommentText)
//            val docElement = parent.addBefore(docComment, element)
//            CodeStyleManager.getInstance(element.project).reformat(docElement)
//        }
//    }
//


    override fun actionPerformed(event: AnActionEvent) {
        // Получаем список выделенных файлов
        val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: let {
            println("wtf")
            println("wtf")
            return
        }

        // Для каждого выделенного файла ...
        for (file in files) {
            // Получаем PSI файл из виртуального файла
            val psiFile = PsiManager.getInstance(event.project ?: return).findFile(file)
            println("event.project!!.name")
            println(event.project!!.name)
            if (psiFile is PsiJavaFile) {
                // Обрабатываем только Java файлы
                println("File: ${file.name}")
                println("File: ${file.presentableName}")
                // Для каждого класса в файле
               // for (psiClass in psiFile.language) {
                    println("methods: ${psiFile.classes.flatMap { 
                        
                        it.methods.map { "$.${
                        it.body?.text}." } }} // ${psiFile.packageName}")
                    println("methods: ${psiFile.classes.flatMap { it.methods.map { it.name } }} // ${psiFile.packageName}")
                //}
            }
        }
    }
}