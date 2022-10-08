package com.github.judalabs.intellijjsonmvc.actions

import com.github.judalabs.intellijjsonmvc.utils.NotificationUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.ui.TextTransferable
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.math.RandomUtils
import java.util.*


private const val TAB = "\t"

class GenerateJsonActionKotlin : AnAction("POJO From JSON") {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.getData(CommonDataKeys.PROJECT)
        val psiFile: PsiJavaFile = anActionEvent.getData(CommonDataKeys.PSI_FILE) as PsiJavaFile
        val classesFromFile = psiFile.classes
        if (psiFile.fileType.description != "Java" || classesFromFile.isEmpty()) {
            return
        }

        val allFields = generateJson(classesFromFile[0])
        println(allFields)

        CopyPasteManager.getInstance().setContents(TextTransferable(allFields))
        NotificationUtils.jsonGenerated(project, psiFile.name)
    }

    private fun generateJson(actualClass: PsiClass?, delayedIdent: String = ""): String? {
        val ident = delayedIdent.plus(TAB)
        val allFields = actualClass?.allFields
        if (allFields?.size == 0) return "null"
        return allFields
            ?.joinToString(
                ",\n", "{\n", "\n${delayedIdent}}"
            ) { field ->
                val referedClass = findClass(field)
                if (referedClass != null) {
                    if (referedClass == actualClass) {
                        "${ident}\"${field.name}\": null"
                    } else {
                        "${ident}\"${field.name}\": ${generateJson(referedClass, delayedIdent.plus(TAB))!!}"
                    }
                } else {
                    "${ident}\"${field.name}\": ${generateLoremIpsum(field)}"
                }
            }
    }

    private fun generateLoremIpsum(field: PsiField?): String {
        val fieldName = field?.type?.presentableText?.lowercase()
        if (fieldName.equals("uuid"))
            return UUID.randomUUID().toString()
        if (listOf("long", "int", "biginteger").contains(fieldName))
            return RandomUtils.nextInt(500).toString()
        if (listOf("double", "bigdecimal").contains(fieldName))
            return RandomUtils.nextDouble().toString()
        if (fieldName.equals("string"))
            return "\"${RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(10))}\""
        if(fieldName.equals("boolean"))
            return RandomUtils.nextBoolean().toString()
        return "null"
    }

    private fun findClass(field: PsiField?): PsiClass? {
        val project = field?.project!!

        val javaFile = PsiTypesUtil.getPsiClass(field.type)?.containingFile as PsiJavaFile
        val packageName = javaFile.packageName
        if (packageName.startsWith("java")) return null

        val actualPackage = JavaPsiFacade.getInstance(project).findPackage(packageName)
        val fieldName = field.type.presentableText
        val referedClass = actualPackage?.findClassByShortName(fieldName, GlobalSearchScope.allScope(project))
        return if (referedClass?.size == 0)
            null
        else
            referedClass?.get(0);
    }

}