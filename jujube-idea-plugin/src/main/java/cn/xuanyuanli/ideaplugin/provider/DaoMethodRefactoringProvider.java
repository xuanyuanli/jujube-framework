package cn.xuanyuanli.ideaplugin.provider;

import com.intellij.freemarker.psi.FtlPsiUtil;
import com.intellij.freemarker.psi.directives.FtlMacro;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.xuanyuanli.ideaplugin.support.Utils;

/**
 * @author John Li
 * @date 2023/4/23
 */
public class DaoMethodRefactoringProvider implements RefactoringElementListenerProvider {

    @Override
    public @Nullable RefactoringElementListener getListener(PsiElement element) {
        if (!(element instanceof PsiMethod method)) {
            return null;
        }
        String methodName = method.getName();
        if (!Utils.isBaseDao(Objects.requireNonNull(method.getContainingClass())) || Utils.isJpaMethod(methodName)) {
            return null;
        }
        return new RefactoringElementListener() {
            @Override
            public void elementMoved(@NotNull PsiElement newElement) {
            }

            @Override
            public void elementRenamed(@NotNull final PsiElement newElement) {
                renameDaoMethod(methodName, newElement);
            }
        };
    }

    private void renameDaoMethod(String oldName, PsiElement newElement) {
        if (newElement instanceof PsiMethod renamedMethod) {
            Project project = renamedMethod.getProject();
            String newName = renamedMethod.getName();
            PsiFile sqlFile = Utils.getSqlFileFromDaoClass(Objects.requireNonNull(renamedMethod.getContainingClass()));
            Arrays.stream(Objects.requireNonNull(sqlFile).getChildren()[0].getChildren()).filter(element -> element instanceof FtlMacro macro
                    && macro.getName().equals("@" + oldName)).findFirst().ifPresent(element -> {
                @NotNull PsiElement[] children = element.getChildren();
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    children[0].replace(FtlPsiUtil.parseFtlExpression(newName, project));
                    children[children.length - 1].replace(FtlPsiUtil.parseFtlExpression(newName, project));
                });
            });
        }
    }

}
