package com.github.judalabs.intellijjsonmvc.actions

import com.github.judalabs.intellijjsonmvc.utils.NotificationUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.ui.TextTransferable
import org.apache.commons.lang.StringUtils


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

    private fun generateJson(actualClass: PsiClass?,
                             alreadyvisited: HashSet<PsiClass> = HashSet(),
                             delayedIdent: String = "", ): String? {

        val ident = delayedIdent.plus(TAB)
        val allGetMethods = actualClass?.allMethods?.filter { e -> isGetterMethod(e) }

        if (actualClass != null) {
            alreadyvisited.add(actualClass)
        }
        if (allGetMethods?.size == 0) return "null"
        return allGetMethods
            ?.joinToString(
                ",\n", "{\n", "\n${delayedIdent}}"
            ) { method -> buildProperty(method, ident, alreadyvisited, delayedIdent) }
    }

    private fun buildProperty(
        method: PsiMethod,
        ident: String,
        alreadyvisited: HashSet<PsiClass>,
        delayedIdent: String
    ): CharSequence {
        val referedClass = findClass(method)
        val key = "${ident}\"${buildKeyProperty(method)}\""
        return if (referedClass != null) {
            if (alreadyvisited.contains(referedClass)) {
                "$key: null"
            } else {
                "$key: ${generateJson(referedClass, alreadyvisited, delayedIdent.plus(TAB))!!}"
            }
        } else {
            "$key: ${LoremIpsumGenerator.getLoremIpsum(method)}"
        }
    }

    private fun isGetterMethod(method:PsiMethod):Boolean {
        val methodName = method.name
        if(!method.hasModifierProperty(PsiModifier.PUBLIC))
            return false
        if(methodName.length < 4)
            return false
        if(!methodName.startsWith("get"))
            return false
        if(methodName == "getClass")
            return false

        return true
    }

    private fun buildKeyProperty(method: PsiMethod):String {
        val getMethod = method.name
        var fieldName = getMethod.substring(3, 4).lowercase()
        if(getMethod.length > 4)
            fieldName += getMethod.substring(4)

        return getJsonProperty(method, fieldName) ?: fieldName
    }

    private fun getJsonProperty(method: PsiMethod, fieldName:String): String? {
        return method.containingClass?.allFields
            ?.firstOrNull { e -> e.name == fieldName }
            ?.annotations?.firstOrNull { a -> a.text.contains("JsonProperty") }
            ?.findAttributeValue("value")?.text?.replace("\"", "")
    }

    private fun findClass(method: PsiMethod?): PsiClass? {
        val project = method?.project!!

        val psiClass = PsiTypesUtil.getPsiClass(method.returnType) ?: return null
        val javaFile = psiClass.containingFile as PsiJavaFile

        val packageName = javaFile.packageName
        if (packageName.startsWith("java")) return null

        val actualPackage = JavaPsiFacade.getInstance(project).findPackage(packageName)
        val fieldName = StringUtils.capitalize(buildKeyProperty(method))
        val referedClass = actualPackage?.findClassByShortName(fieldName, GlobalSearchScope.allScope(project))
        return if (referedClass?.size == 0 || referedClass?.get(0)?.isEnum == true)
            null
        else
            referedClass?.get(0)
    }

}