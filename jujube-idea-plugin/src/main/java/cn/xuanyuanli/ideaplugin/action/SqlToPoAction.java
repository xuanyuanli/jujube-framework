package cn.xuanyuanli.ideaplugin.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.xuanyuanli.ideaplugin.support.Column;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Sqls;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.core.util.CamelCase;
import cn.xuanyuanli.core.util.Exceptions;
import cn.xuanyuanli.ideaplugin.JujubeBundle;

/**
 * @author xuanyuanli
 */
public class SqlToPoAction extends AnAction {

    public SqlToPoAction() {
        super();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (isDisable(e)) {
            return;
        }
        // 获取当前的项目、文件、编辑器等
        Project project = e.getProject();
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(LangDataKeys.EDITOR);
        Document document = Objects.requireNonNull(editor).getDocument();
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = getFilterSql(Objects.requireNonNull(selectionModel.getSelectedText()));
        // 获取 Java 文件
        PsiJavaFile javaFile = (PsiJavaFile) Utils.getJavaFileFromSqlFile(Objects.requireNonNull(psiFile));
        if (javaFile == null) {
            return;
        }

        List<Column> columns;
        try {
            columns = Sqls.getColumns(selectedText, project);
        } catch (Exception ex) {
            Consoles.info(Exceptions.exceptionToString(ex));
            Messages.showErrorDialog(JujubeBundle.getText("error.parse.sql"), JujubeBundle.getText("dialog.error"));
            return;
        }
        if (columns.isEmpty()) {
            Messages.showErrorDialog(JujubeBundle.getText("error.no.fields"), JujubeBundle.getText("dialog.error"));
            return;
        }

        String methodName = getMethodName(document, selectionModel);
        if (methodName == null) {
            return;
        }
        // 显示输入对话框以获取类名
        String className = Messages.showInputDialog(project, JujubeBundle.getText("dialog.input.po.name"), JujubeBundle.getText("dialog.generate.po"),
                Messages.getQuestionIcon(), CamelCase.toCapitalizeCamelCase(methodName) + "PO", null);
        // 如果用户取消输入或输入为空，则停止操作
        if (className == null || className.trim().isEmpty()) {
            return;
        }

        // 获取 PsiClass，这里假设文件只包含一个类
        PsiClass daoClass = javaFile.getClasses()[0];
        // 在 PsiClass 中添加内部类
        PsiClass innerClass = Utils.addInnerClassToJavaFile(project, daoClass, className, columns);
        if (innerClass == null) {
            Messages.showErrorDialog(JujubeBundle.getText("dialog.generate.inner.class.failed"), JujubeBundle.getText("dialog.error"));
            return;
        }
        PsiMethod method = Utils.findMethodInJavaFile(javaFile, methodName);
        if (Utils.modifyMethodReturnTypeIfMap(innerClass, method)) {
            TextRange textRange = method.getTextRange();
            Utils.goLine(project, javaFile.getVirtualFile(), textRange.getStartOffset());
        } else {
            // 否则，跳转到DO处
            PsiClass foundInnerClass = daoClass.findInnerClassByName(innerClass.getName(), false);
            TextRange innerClassTextRange = Objects.requireNonNull(foundInnerClass).getTextRange();
            Utils.goLine(project, javaFile.getVirtualFile(), innerClassTextRange.getStartOffset());
        }

        Utils.findMethodCall(method).forEach(call -> {
            PsiElement element = call.getElement();
            PsiMethod callerMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            if (callerMethod != null && !Utils.isBaseDao(Objects.requireNonNull(callerMethod.getContainingClass()))) {
                PsiType returnType = callerMethod.getReturnType();
                if (Utils.isMapType(Objects.requireNonNull(returnType)) || Utils.isListMapType(returnType) || Utils.isPageableMapType(returnType)) {
                    PsiClass targetClass = ConvertMapToBeanAction.getTargetClass(callerMethod.getContainingClass());
                    String boClassName = className.replace("PO", "BO");
                    PsiClass boInnerClass = Utils.addInnerClassToJavaFile(project, targetClass, boClassName, columns);
                    Utils.modifyMethodReturnTypeIfMap(boInnerClass, callerMethod);
                    PsiMethod interfaceMethod = Utils.findInterfaceMethod(callerMethod);
                    if (interfaceMethod != null) {
                        Utils.modifyMethodReturnTypeIfMap(boInnerClass, interfaceMethod);
                    }
                }
            }
        });
    }

    @Nullable
    private static String getMethodName(Document document, SelectionModel selectionModel) {
        String[] arr = getLineHead10(document, selectionModel);
        int index = -1;
        for (int i = arr.length - 1; i >= 0; i--) {
            if (arr[i].startsWith("<@")) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            Messages.showErrorDialog(JujubeBundle.getText("dialog.cannot.find.sql.method"), JujubeBundle.getText("dialog.error"));
            return null;
        }
        String str = arr[index];
        return str.substring(2, str.length() - 1);
    }

    private static String[] getLineHead10(Document document, SelectionModel selectionModel) {
        int startLineNumber = document.getLineNumber(selectionModel.getSelectionStart());
        // 获取所选内容前10行的起始行号，确保它大于等于0
        int firstLineToRetrieve = Math.max(0, startLineNumber - 10);
        int startOffset = document.getLineStartOffset(firstLineToRetrieve);
        int endOffset = document.getLineEndOffset(startLineNumber - 1);
        // 获取前10行文本
        String textBeforeSelection = document.getText(new TextRange(startOffset, endOffset));
        return textBeforeSelection.split("\n");
    }

    private String getFilterSql(String selectedText) {
        return Arrays.stream(selectedText.split("\n")).filter(e -> !e.startsWith("<#") && !e.startsWith("</")).collect(Collectors.joining("\n"));
    }

    private boolean isDisable(AnActionEvent e) {
        Project project = e.getProject();
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(LangDataKeys.EDITOR);
        SelectionModel selectionModel = Objects.requireNonNull(editor).getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        return project == null || !Utils.isSqlFile(psiFile) || selectedText == null || selectedText.isBlank();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(JujubeBundle.getText("action.sql.to.po"));
        if (isDisable(e)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
