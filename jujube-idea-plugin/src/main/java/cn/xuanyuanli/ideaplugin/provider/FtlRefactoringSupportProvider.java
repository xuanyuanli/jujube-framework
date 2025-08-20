package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author John Li
 * @date 2023/4/23
 */
public class FtlRefactoringSupportProvider extends RefactoringSupportProvider {

    @Override
    public boolean isAvailable(@NotNull PsiElement context) {
        return true;
    }

    @Override
    public boolean isSafeDeleteAvailable(@NotNull PsiElement element) {
        return true;
    }
}
