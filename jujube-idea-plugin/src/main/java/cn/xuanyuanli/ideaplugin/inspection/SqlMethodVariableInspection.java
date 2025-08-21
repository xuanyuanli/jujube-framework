package cn.xuanyuanli.ideaplugin.inspection;

import cn.xuanyuanli.ideaplugin.JujubeBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.freemarker.psi.FtlExpression;
import com.intellij.freemarker.psi.variables.FtlVariable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author John Li
 * @date 2023/4/21
 */
public class SqlMethodVariableInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new FreemarkerVariableVisitor(holder);
    }


    public static class FreemarkerVariableVisitor extends PsiElementVisitor {

        private final ProblemsHolder holder;

        public FreemarkerVariableVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitElement(@NotNull PsiElement element) {
//            if (element instanceof FtlReferenceExpression
//                    || element instanceof FtlDirective) {
//                PsiElement parent = element.getParent();
//                System.out.println(element.getText() + "\t" + (parent instanceof FtlMacro ? "自定义标签" : parent.getText().replace("\n", "")));
//                System.out.println("------");
//                System.out.println(element.getClass() + "\t" + parent.getClass());
//                System.out.println("------------------------------------------------------------------------------------\n");
//            }
            if (element instanceof FtlExpression expression) {
                checkExpressionVariable(expression);
            } else {
                super.visitElement(element);
            }
        }

        private void checkExpressionVariable(FtlExpression expression) {
            List<FtlVariable> variables = PsiTreeUtil.getChildrenOfTypeAsList(expression, FtlVariable.class);
            for (FtlVariable variable : variables) {
                checkVariableName(variable);
            }
        }

        private void checkVariableName(FtlVariable variable) {
            String variableName = variable.getName();
            // 检查变量名是否符合指定规则
            if (!isVariableNameValid(variableName)) {
                // 如果变量名无效，将问题添加到ProblemsHolder
                holder.registerProblem(variable, JujubeBundle.message("inspection.sqlvariable.invalid.name", variableName));
            }
        }

        private boolean isVariableNameValid(String ignoredVariableName) {
            // 在这里实现你的变量名检查规则
            // 例如，你可以要求变量名遵循某种命名规范
            return true; // 默认返回true，表示变量名有效
        }
    }

}
