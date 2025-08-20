package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.codeInsight.navigation.GotoTargetPresentationProvider;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.xuanyuanli.ideaplugin.support.Icons;

/**
 * @author John Li
 */
public class GotoSqlPresentationProvider implements GotoTargetPresentationProvider {


    @Override
    public @Nullable TargetPresentation getTargetPresentation(@NotNull PsiElement element, boolean differentNames) {
        String name = element.getText();
        return TargetPresentation.builder(name).icon(Icons.LOGO).presentation();
    }
}
