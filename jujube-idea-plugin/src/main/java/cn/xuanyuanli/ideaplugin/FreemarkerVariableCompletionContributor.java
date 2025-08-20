package cn.xuanyuanli.ideaplugin;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * @author John Li
 * @date 2023/4/21
 */
public class FreemarkerVariableCompletionContributor extends CompletionContributor {

    public FreemarkerVariableCompletionContributor() {
        extend(null, PlatformPatterns.psiElement(), new FreemarkerVariableCompletionProvider());
    }

    private static class FreemarkerVariableCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                @NotNull ProcessingContext context,
                @NotNull CompletionResultSet result) {
            // 提供一个预定义的变量列表
            List<String> predefinedVariables = Arrays.asList("abc", "efg", "hig");

            for (String variable : predefinedVariables) {
                result.addElement(LookupElementBuilder.create(variable));
            }
        }
    }
}
