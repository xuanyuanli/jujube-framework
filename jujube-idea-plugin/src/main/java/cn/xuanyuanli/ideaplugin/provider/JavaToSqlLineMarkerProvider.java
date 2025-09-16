package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Utils;

/**
 * 实现Dao方法调整到Sql对应位置
 *
 * @author xuanyuanli
 */
public class JavaToSqlLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (element instanceof PsiMethod method) {
            String methodName = method.getName();

            PsiClass psiClass = method.getContainingClass();
            if (psiClass == null) {
                return;
            }
            if (!Utils.isBaseDao(psiClass)) {
                return;
            }
            if (Utils.isOverwriteMethod(method) || Utils.isDefaultMethod(method) || Utils.isJpaMethod(methodName)) {
                return;
            }
            // 从方法的 PsiFile 获取 SQL 文件
            PsiFile sqlFile = Utils.getSqlFileFromDaoClass(method.getContainingClass());
            if (sqlFile == null) {
                return;
            }

            // 在 SQL 文件中查找对应的定义
            PsiElement sqlElement = Utils.findSqlMethodInSqlFile(sqlFile, methodName);
            if (sqlElement != null) {
                String tooltipText = "Navigate to " + methodName + " in " + sqlFile.getName();
                // 添加行标记
                RelatedItemLineMarkerInfo<PsiElement> navigate = Utils.getLineMarker(sqlElement, method.getNameIdentifier(), tooltipText);
                result.add(navigate);
            } else {
                Consoles.info("Can not find method <" + methodName + "> in " + sqlFile.getName());
            }
        }
    }

}
