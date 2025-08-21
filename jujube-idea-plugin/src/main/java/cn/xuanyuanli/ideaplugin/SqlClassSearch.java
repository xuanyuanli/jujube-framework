package cn.xuanyuanli.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.searches.DefinitionsScopedSearch.SearchParameters;
import com.intellij.util.Processor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.xuanyuanli.ideaplugin.support.Utils;

/**
 * @author xuanyuanli
 */
public class SqlClassSearch extends QueryExecutorBase<PsiElement, SearchParameters> {

    @Override
    public void processQuery(@NotNull SearchParameters queryParameters, @NotNull Processor<? super PsiElement> consumer) {
        final PsiElement element = queryParameters.getElement();
        PsiElement target = getSqlElementFromJavaMethodOrClass(element);
        if (target != null) {
            consumer.process(target);
        }
    }

    @Nullable
    public static PsiElement getSqlElementFromJavaMethodOrClass(PsiElement element) {
        AtomicReference<PsiElement> target = new AtomicReference<>();
        try {
            ApplicationManager.getApplication().runReadAction(() -> {
                if (element instanceof final PsiMethod psiMethod) {
                    PsiClass psiClass = psiMethod.getContainingClass();
                    ReadAction.run(() -> {
                        if (Utils.isBaseDao(Objects.requireNonNull(psiClass))) {
                            PsiFile sqlFile = Utils.getSqlFileFromDaoClass(psiClass);
                            target.set(Utils.findSqlMethodInSqlFile(Objects.requireNonNull(sqlFile), psiMethod.getName()));
                        }
                    });
                }
                if (element instanceof PsiClass psiClass) {
                    ReadAction.run(() -> {
                        if (Utils.isBaseDao(psiClass)) {
                            target.set(Utils.getSqlFileFromDaoClass(psiClass));
                        }
                    });
                }
            });
        } catch (Throwable ignored) {
        }
        return target.get();
    }

}
