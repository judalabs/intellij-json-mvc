package com.github.judalabs.intellijjsonmvc.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.math.RandomUtils
import java.util.*


private const val TAB = "\t"

class GenerateJsonActionKotlin : AnAction("POJO From JSON") {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val editor: Editor? = anActionEvent.getData(CommonDataKeys.EDITOR)
        val psiFile: PsiFile? = anActionEvent.getData(CommonDataKeys.PSI_FILE)
        if (editor == null || psiFile == null || psiFile.fileType.description != "Java") {
            return
        }
        val offset: Int = editor.caretModel.offset

        val element = psiFile.findElementAt(offset)
        if (element != null) {

            var initialClass = PsiTreeUtil.getTopmostParentOfType(element, PsiClass::class.java)
            val allFields = generateJson(initialClass)
            println(allFields)
            Messages.showMessageDialog(anActionEvent.project, allFields, "JSON", null)
        }
    }

    private val s = TAB

    private fun generateJson(actualClass: PsiClass?, delayedIdent: String = ""): String? {
        val ident = delayedIdent.plus(TAB)
        return actualClass
            ?.allFields
            ?.joinToString(
                ",\n", "{\n", "\n${delayedIdent}}"
            ) { field ->
                val referedClass = findClass(field)
                if (referedClass != null) {
                    if(referedClass == actualClass) {
                        "${ident}\"${field.name}\": null"
                    } else {
                        "${ident}\"${field.name}\": ${generateJson(referedClass, delayedIdent.plus(TAB))!!}"
                    }
                } else {
                    "${ident}\"${field.name}\": ${generateLoremIpsum(field)}"
                }
            }
    }

    fun generateLoremIpsum(field: PsiField?): String {
        val fieldName = field?.type?.presentableText?.lowercase()
        if(fieldName.equals("uuid"))
            return UUID.randomUUID().toString()
        if (listOf("long", "int", "biginteger").contains(fieldName))
            return RandomUtils.nextInt(500).toString()
        if(listOf("double", "bigdecimal").contains(fieldName))
            return RandomUtils.nextDouble().toString()
        if(fieldName.equals("string"))
            return RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(10));
        return ""
    }

    fun findClass(field: PsiField?): PsiClass? {
        val project = field?.project!!
        val actualPackage = JavaPsiFacade.getInstance(project).findPackage((field.containingFile as PsiJavaFileImpl).packageName)
        PsiUtil.resolveClassInClassTypeOnly(field.type)
        var fieldName = field.type.presentableText
        var referedClass = actualPackage?.findClassByShortName(fieldName, GlobalSearchScope.allScope(project))
        if(referedClass?.size == 0)
            return null
        else
        return referedClass?.get(0);
    }

}