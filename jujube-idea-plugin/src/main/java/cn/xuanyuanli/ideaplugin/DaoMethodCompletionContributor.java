package cn.xuanyuanli.ideaplugin;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.CustomHighlighterTokenType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.core.util.Texts;

/**
 * @author John Li
 */
public class DaoMethodCompletionContributor extends CompletionContributor {

    public DaoMethodCompletionContributor() {
        // 为方法名提供自动补全建议
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                    @NotNull ProcessingContext context,
                    @NotNull CompletionResultSet resultSet) {
                PsiElement originalPosition = parameters.getOriginalPosition();
                if (originalPosition == null) {
                    return;
                }
                if (!checkPosition(parameters)) {
                    return;
                }
                PsiClass daoClass = PsiTreeUtil.getParentOfType(originalPosition, PsiClass.class);
                PsiClass genericClass = Utils.getFirstGenericTypeOfBaseDao(Objects.requireNonNull(daoClass));
                String prefix = originalPosition.getText();
                resultSet = resultSet.withPrefixMatcher("");
                Consoles.info("prefix:{}", prefix);
                // 实体类字段名
                List<String> fieldNames = Utils.getAllFieldsFilterStaticAndFinal(Objects.requireNonNull(genericClass)).stream().map(PsiField::getName).toList();
                List<String> suggestions = new ArrayList<>();
                for (String field : fieldNames) {
                    String capitalizedField = Texts.capitalize(field);
                    suggestions.add(capitalizedField);
                }
                suggestions.stream().map(LookupElementBuilder::create).forEach(resultSet::addElement);
            }
        });
    }


    private boolean checkPosition(CompletionParameters parameters) {
        if (parameters.getCompletionType() != CompletionType.BASIC) {
            return false;
        }

        // 验证当前类不在方法内部
        PsiElement originalPosition = parameters.getOriginalPosition();
        PsiMethod currentMethod = PsiTreeUtil.getParentOfType(originalPosition, PsiMethod.class);
        if (currentMethod != null) {
            return false;
        }
        // 验证当前类必须是接口
        PsiClass daoClass = PsiTreeUtil.getParentOfType(originalPosition, PsiClass.class);
        if (daoClass == null || !daoClass.isInterface() || !Utils.isBaseDao(daoClass)) {
            return false;
        }
        return !inCommentOrLiteral(parameters);
    }

    /**
     * 在注释区间
     */
    private static boolean inCommentOrLiteral(CompletionParameters parameters) {
        HighlighterIterator iterator = parameters.getEditor().getHighlighter().createIterator(parameters.getOffset());
        if (iterator.atEnd()) {
            return false;
        }

        IElementType elementType = iterator.getTokenType();
        if (elementType == CustomHighlighterTokenType.WHITESPACE) {
            iterator.retreat();
            elementType = iterator.getTokenType();
        }
        return elementType == CustomHighlighterTokenType.LINE_COMMENT ||
                elementType == CustomHighlighterTokenType.MULTI_LINE_COMMENT ||
                elementType == CustomHighlighterTokenType.STRING ||
                elementType == CustomHighlighterTokenType.SINGLE_QUOTED_STRING;
    }
}
