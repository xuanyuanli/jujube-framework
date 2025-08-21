package cn.xuanyuanli.ideaplugin;

import com.intellij.freemarker.psi.directives.FtlMacro;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.safeDelete.NonCodeUsageSearchInfo;
import com.intellij.refactoring.safeDelete.SafeDeleteProcessorDelegateBase;
import com.intellij.refactoring.safeDelete.usageInfo.SafeDeleteReferenceUsageInfo;
import com.intellij.refactoring.safeDelete.usageInfo.SafeDeleteUsageInfo;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.xuanyuanli.ideaplugin.support.Utils;

/**
 * @author xuanyuanli
 * @date 2023/4/23
 */
public class DaoMethodSafeDeleteProcessor extends SafeDeleteProcessorDelegateBase {

    @Override
    public @Nullable Collection<? extends PsiElement> getElementsToSearch(@NotNull PsiElement element, @Nullable Module module,
            @NotNull Collection<? extends PsiElement> allElementsToDelete) {
        PsiMethod method = (PsiMethod) element;
        List<PsiElement> elementsToSearch = new ArrayList<>();
        PsiFile sqlFile = Utils.getSqlFileFromDaoClass(Objects.requireNonNull(method.getContainingClass()));
        Arrays.stream(Objects.requireNonNull(sqlFile).getChildren()[0].getChildren()).filter(e -> e instanceof FtlMacro macro
                && macro.getName().equals("@" + method.getName())).findFirst().ifPresent(elementsToSearch::add);
        return elementsToSearch;
    }

    @Override
    public boolean handlesElement(PsiElement element) {
        return element instanceof PsiMethod method && !Utils.isJpaMethod(method.getName())
                && Utils.isBaseDao(Objects.requireNonNull(method.getContainingClass())) && !Utils.isDefaultMethod(method);
    }

    @Override
    public @Nullable NonCodeUsageSearchInfo findUsages(@NotNull PsiElement element, PsiElement @NotNull [] allElementsToDelete,
            @NotNull List<? super UsageInfo> result) {
        return null;
    }

    @Override
    public @Nullable Collection<PsiElement> getAdditionalElementsToDelete(@NotNull PsiElement element, @NotNull Collection<? extends PsiElement> allElementsToDelete,
            boolean askUser) {
        return null;
    }

    @Override
    public @Nullable Collection<String> findConflicts(@NotNull PsiElement element, PsiElement @NotNull [] allElementsToDelete) {
        return null;
    }

    @Override
    public UsageInfo[] preprocessUsages(@NotNull Project project, UsageInfo[] usages) {
        if (usages.length > 0) {
            UsageInfo usage = usages[0];
            if (usage instanceof SafeDeleteUsageInfo srInfo) {
                if (srInfo.getReferencedElement() instanceof PsiMethod method) {
                    if (!handlesElement(method)) {
                        return usages;
                    }
                    PsiFile sqlFile = Utils.getSqlFileFromDaoClass(Objects.requireNonNull(method.getContainingClass()));
                    List<UsageInfo> usageInfos = new ArrayList<>(List.of(usages));
                    Arrays.stream(Objects.requireNonNull(sqlFile).getChildren()[0].getChildren()).filter(e -> e instanceof FtlMacro macro
                            && macro.getName().equals("@" + method.getName())).findFirst().ifPresent(e -> {
                        // 在这里实现删除逻辑
                        usageInfos.add(new SafeDeleteReferenceUsageInfo(e, e, true) {
                            @Override
                            public void deleteElement() throws IncorrectOperationException {
                                Objects.requireNonNull(super.getElement()).delete();
                            }
                        });
                    });
                    usages = usageInfos.toArray(new UsageInfo[0]);
                }
            }
        }
        return usages;
    }

    @Override
    public void prepareForDeletion(@NotNull PsiElement element) throws IncorrectOperationException {
    }

    @Override
    public boolean isToSearchInComments(PsiElement element) {
        return false;
    }

    @Override
    public void setToSearchInComments(PsiElement element, boolean enabled) {
    }

    @Override
    public boolean isToSearchForTextOccurrences(PsiElement element) {
        return false;
    }

    @Override
    public void setToSearchForTextOccurrences(PsiElement element, boolean enabled) {
    }
}
