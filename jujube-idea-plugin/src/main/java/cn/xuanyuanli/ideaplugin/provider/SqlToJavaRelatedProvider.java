package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.core.util.Texts;

/**
 * 提供从SQL方法定义导航到对应Java DAO方法的功能。
 * 支持在SQL文件中使用Ctrl+Alt+B跳转到Java文件。
 *
 * @author John Li
 */
public class SqlToJavaRelatedProvider extends GotoRelatedProvider {

    @Override
    public @NotNull List<? extends GotoRelatedItem> getItems(@NotNull PsiElement element) {
        // 检查元素是否在SQL文件中且是方法定义
        if (Utils.isSqlFile(element.getContainingFile()) && isMethodDefinition(element)) {
            // 从SQL元素获取方法名
            String text = element.getText();
            String methodName = text.substring(2, text.length() - 1).trim();
            if (methodName.isEmpty()) {
                return super.getItems(element);
            }

            // 从SQL文件获取对应的Java文件
            PsiFile javaFile = Utils.getJavaFileFromSqlFile(element.getContainingFile());
            if (javaFile == null) {
                return super.getItems(element);
            }

            // 在Java文件中查找对应的方法
            PsiMethod method = Utils.findMethodInJavaFile(javaFile, methodName);
            if (method != null) {
                ArrayList<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();
                gotoRelatedItems.add(new GotoRelatedItem(method, "Java Method"));
                return gotoRelatedItems;
            }
        }
        return super.getItems(element);
    }

    /**
     * 检查元素是否表示SQL方法定义（格式：&lt;@methodName&gt;）
     */
    private boolean isMethodDefinition(PsiElement element) {
        return Texts.find(element.getText(), "^<@(.*?)>$");
    }
}