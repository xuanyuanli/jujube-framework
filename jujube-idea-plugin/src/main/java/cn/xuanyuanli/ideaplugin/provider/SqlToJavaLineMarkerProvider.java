package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.core.util.Texts;

/**
 * @author John Li
 */
public class SqlToJavaLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // 检查元素是否是 SQL 文件中的方法定义
        if (Utils.isSqlFile(element.getContainingFile()) && isMethodDefinition(element)) {
            // 获取方法名
            String text = element.getText();
            String methodName = text.substring(2, text.length() - 1).trim();
            if (methodName == null) {
                return;
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
        return Texts.find(element.getText(), "^<@(.*?)>$");
    }


}
