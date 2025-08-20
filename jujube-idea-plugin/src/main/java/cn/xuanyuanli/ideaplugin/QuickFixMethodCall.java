package cn.xuanyuanli.ideaplugin;

import cn.xuanyuanli.core.util.CamelCase;
import cn.xuanyuanli.core.util.Texts;
import cn.xuanyuanli.ideaplugin.action.ConvertMapToBeanAction;
import cn.xuanyuanli.ideaplugin.support.Column;
import cn.xuanyuanli.ideaplugin.support.Utils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author John Li
 */
public class QuickFixMethodCall implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiMethodCallExpression methodCallExpression = getPsiMethodCallExpression(editor, file);
        if (methodCallExpression == null) {
            return;
        }
        PsiReferenceExpression referenceExpression = methodCallExpression.getMethodExpression();
        PsiExpression qualifierExpression = referenceExpression.getQualifierExpression();
        if (qualifierExpression == null) {
            return;
        }
        String referenceName = referenceExpression.getReferenceName();
        if (Objects.requireNonNull(referenceName).startsWith("get")) {
            fixGetMethodCall(project, methodCallExpression, qualifierExpression);
        } else if (referenceName.startsWith("set") || referenceName.startsWith("put")) {
            fixSetMethodCall(project, methodCallExpression, qualifierExpression);
        }
    }

    private void fixSetMethodCall(Project project, PsiMethodCallExpression methodCallExpression, PsiExpression ignoredQualifierExpression) {
        PsiExpressionList argumentList = methodCallExpression.getArgumentList();
        PsiExpression[] arguments = argumentList.getExpressions();
        if (arguments.length != 2) {
            return;
        }
        Column column = ConvertMapToBeanAction.getColumn(methodCallExpression);
        PsiVariable psiVariable = Utils.getVariableFromMechodCall(methodCallExpression);
        String variableName = Objects.requireNonNull(psiVariable).getName();

        PsiClass containingClass = PsiUtil.resolveClassInType(psiVariable.getType());
        if (containingClass != null) {
            Utils.addFieldToClass(containingClass, column);
            // 从 JavaPsiFacade 获取 PsiElementFactory
            PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
            // 创建新的 PsiExpression
            String newExp = Texts.format("{}.set{}({})", variableName, Texts.capitalize(column.getField()), arguments[1].getText());
            PsiExpression newExpression = elementFactory.createExpressionFromText(newExp, methodCallExpression);
            methodCallExpression.replace(newExpression);
        }

    }

    private static void fixGetMethodCall(@NotNull Project project, PsiMethodCallExpression methodCallExpression, PsiExpression qualifierExpression) {
        PsiExpressionList argumentList = methodCallExpression.getArgumentList();
        PsiExpression[] arguments = argumentList.getExpressions();
        if (arguments.length != 1) {
            return;
        }
        String getFieldName = Objects.requireNonNull(((PsiLiteralExpressionImpl) arguments[0]).getValue()).toString();
        String variableName = qualifierExpression.getText();

        // 获取 JavaPsiFacade 实例
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        // 从 JavaPsiFacade 获取 PsiElementFactory
        PsiElementFactory elementFactory = psiFacade.getElementFactory();
        // 创建新的 PsiExpression
        PsiExpression newExpression = elementFactory.createExpressionFromText(
                Texts.format("{}.get{}()", variableName, CamelCase.toCapitalizeCamelCase(getFieldName)), methodCallExpression);
        methodCallExpression.replace(newExpression);
    }

    @NotNull
    @Override
    public String getText() {
        return JujubeBundle.getText("quick.fix.map.method.call");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return JujubeBundle.getText("quick.fix.map.method.call");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        PsiMethodCallExpression methodCallExpression = getPsiMethodCallExpression(editor, file);
        if (methodCallExpression != null) {
            PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
            String referenceName = methodExpression.getReferenceName();
            return Objects.requireNonNull(referenceName).startsWith("get") || referenceName.startsWith("put") || referenceName.startsWith("set");
        }
        return false;
    }

    @Nullable
    private static PsiMethodCallExpression getPsiMethodCallExpression(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAtCaret = file.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethodCallExpression.class);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
