package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;

import java.util.Collection;

import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.sql.psi.impl.SqlTokenElement;
import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.core.util.Texts;

/**
 * @author John Li
 */
public class SqlToJavaLineMarkerProvider extends RelatedItemLineMarkerProvider {

    public static final String REG_EX = "^<@(.*?)>$";

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        System.out.println(element.getClass().getName() + " : " + element.getText().length()
                + " : " + Texts.truncate(element.getText(), 100).replace("\n", "\t"));
        // 检查元素是否是 SQL 文件中的方法定义
        if (Utils.isSqlFile(element.getContainingFile()) && isMethodDefinition(element)) {
            // 获取方法名
            String text = element.getText();
            String methodName;
            if (isLanguageEle(element)) {
                methodName = Texts.getGroups(REG_EX, text)[1];
            } else {
                methodName = element.getNextSibling().getText();
            }

            // 从 SQL 文件获取对应的 Java 文件
            PsiFile javaFile = Utils.getJavaFileFromSqlFile(element.getContainingFile());
            if (javaFile == null) {
                return;
            }

            // 在 Java 文件中查找对应的方法
            PsiMethod method = Utils.findMethodInJavaFile(javaFile, methodName);
            if (method != null) {
                String tooltipText = "Navigate to " + methodName + " in " + javaFile.getName();
                result.add(Utils.getLineMarker(method.getNameIdentifier(), element, tooltipText));
            } else {
                Consoles.info("Can not find method <" + methodName + "> in " + javaFile.getName());
            }
        }
    }

    private boolean isMethodDefinition(PsiElement element) {
        return isLanguageEle(element) || isSqlTokenEle(element);
    }

    private static boolean isSqlTokenEle(PsiElement element) {
        PsiElement prevSibling = element.getPrevSibling();
        if (prevSibling == null){
            prevSibling = element.getParent().getPrevSibling();
        }
        PsiElement nextSibling = element.getNextSibling();
        return element instanceof SqlTokenElement && "@".equals(element.getText())
                && prevSibling != null && "<".equals(prevSibling.getText())
                && nextSibling != null;
    }

    private static boolean isLanguageEle(PsiElement element) {
        return element instanceof OuterLanguageElement && Texts.find(element.getText(), REG_EX);
    }

}
