package cn.xuanyuanli.ideaplugin.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Column;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.ideaplugin.JujubeBundle;
import cn.xuanyuanli.core.util.CamelCase;

/**
 * @author John Li
 */
public class ConvertMapToBeanAction extends AnAction {

    public ConvertMapToBeanAction() {
        super();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (isDisabled(e)) {
            return;
        }
        // 获取当前活动的项目、编辑器和文件
        Project project = e.getProject();
        @SuppressWarnings("DuplicatedCode")
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        PsiClass psiClass = psiJavaFile.getClasses()[0];

        // 获取当前光标所在的PsiElement
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAtOffset = psiFile.findElementAt(offset);

        // 获取离当前光标最近的PsiVariable
        PsiVariable psiVariable = PsiTreeUtil.getParentOfType(elementAtOffset, PsiVariable.class);
        PsiMethod method = PsiTreeUtil.getParentOfType(psiVariable, PsiMethod.class);
        // 根据键类型创建一个新的内部类，并放到Api(或当前类)中
        PsiClass targetClass = getTargetClass(psiClass);
        // 显示输入对话框以获取类名
        String className = Messages.showInputDialog(project, JujubeBundle.getText("dialog.input.bo.name"), JujubeBundle.getText("dialog.generate.bo"),
                Messages.getQuestionIcon(), CamelCase.toCapitalizeCamelCase(Objects.requireNonNull(method).getName()) + "BO", null);
        // 如果用户取消输入或输入为空，则停止操作
        if (className == null || className.trim().isEmpty()) {
            return;
        }
        // 生成内部类
        List<PsiMethodCallExpression> setCalls = Utils.findVariableMethodCallExpressionsInMethod(method, psiVariable.getName() + "\\.(pu|se)t(.*?)");
        PsiClass innerClass = Utils.addInnerClassToJavaFile(project, targetClass, className, setCalls.stream().map(ConvertMapToBeanAction::getColumn).collect(Collectors.toList()));

        // 将Map类型变量替换为新的Bean类型
        Utils.changeVariableType(project, psiVariable, innerClass);
        // 替换Map.put为Bean.set...
        Utils.replaceVariableMethodWithSetCall(project, setCalls, psiVariable);
        // 替换Map.get为Bean.get...
        List<PsiMethodCallExpression> getCalls = Utils.findVariableMethodCallExpressionsInMethod(method, psiVariable.getName() + "\\.get(.*?)");
        Utils.replaceVariableMethodWithGetCall(project, getCalls, psiVariable);
        // 检查选中变量是否是当前方法的返回值，如果是的话，则替换返回值类型
        PsiType returnType = method.getReturnType();
        boolean listMapType = Utils.isListMapType(Objects.requireNonNull(returnType));
        boolean pageableMapType = Utils.isPageableMapType(returnType);
        if (Utils.isMapType(returnType) || listMapType ||pageableMapType) {
            PsiClassType psiType = Utils.classToType(project, innerClass);
            if (listMapType) {
                psiType = PsiType.getTypeByName(Utils.LIST_NAME + "<" + innerClass.getQualifiedName() + ">", Objects.requireNonNull(project), method.getResolveScope());
            } else if (pageableMapType) {
                psiType = PsiType.getTypeByName(Utils.PAGEABLE_NAME + "<" + innerClass.getQualifiedName() + ">", Objects.requireNonNull(project), method.getResolveScope());
            }
            Utils.replaceMethodReturnType(project, method, psiType);
        }
    }

    @NotNull
    public static Column getColumn(PsiMethodCallExpression callExpression) {
        Column column = new Column();
        PsiExpression[] expressions = callExpression.getArgumentList().getExpressions();
        String field = Objects.requireNonNull(((PsiLiteralExpressionImpl) expressions[0]).getValue()).toString();
        column.setField(CamelCase.toCamelCase(field));
        column.setPsiType(expressions[1].getType());
        return column;
    }

    /**
     * 得到内部类要放入的目标类
     *
     * @param psiClass 当前
     * @return {@link PsiClass}
     */
    public static PsiClass getTargetClass(PsiClass psiClass) {
        PsiClass[] interfaces = psiClass.getInterfaces();
        PsiClass targetClass;
        if (interfaces.length == 0) {
            targetClass = psiClass;
        } else {
            targetClass = interfaces[0];
        }
        return targetClass;
    }

    public boolean isDisabled(AnActionEvent e) {
        try {
            // 获取当前活动的项目、编辑器和文件
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
            if (!Utils.isJavaFile(psiFile)) {
                return true;
            }
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiClass psiClass = psiJavaFile.getClasses()[0];

            if (psiClass.getAnnotation("org.springframework.stereotype.Service") == null
                    && psiClass.getAnnotation("org.springframework.stereotype.Component") == null
                    && psiClass.getAnnotation("org.apache.dubbo.config.annotation.DubboService") == null) {
                return true;
            }

            // 获取当前光标所在的PsiElement
            int offset = editor.getCaretModel().getOffset();
            PsiElement elementAtOffset = psiFile.findElementAt(offset);

            // 获取离当前光标最近的PsiVariable
            PsiVariable psiVariable = PsiTreeUtil.getParentOfType(elementAtOffset, PsiVariable.class);
            // 确保找到了一个变量
            if (psiVariable == null) {
                return true;
            }

            // 获取变量的类型
            PsiType variableType = psiVariable.getType();
            return !Utils.isMapType(variableType);
        } catch (Throwable ex) {
            return true;
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText(JujubeBundle.getText("action.map.to.bean"));
        e.getPresentation().setEnabled(!isDisabled(e));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
