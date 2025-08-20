package cn.xuanyuanli.ideaplugin;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.support.Column;
import cn.xuanyuanli.ideaplugin.support.Constants;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.util.Texts;

/**
 * @author John Li
 */
public class QuickFixJpaMethodParams extends PsiElementBaseIntentionAction {

    @Override
    public @IntentionName @NotNull String getText() {
        return "填充JPA方法参数";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "填充JPA方法参数";
    }


    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        String name = element.getContainingFile().getFileType().getName();
        if (!JavaFileType.INSTANCE.getName().equals(name)) {
            return false;
        }
        PsiMethod parentMethodOfType = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (parentMethodOfType != null) {
            return false;
        }
        PsiClass parentClassOfType = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        return parentClassOfType != null && (parentClassOfType.isInterface()) && Utils.isBaseDao(parentClassOfType);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiTypeElement statementElement = PsiTreeUtil.getParentOfType(element, PsiTypeElement.class);
        if (statementElement == null) {
            statementElement = PsiTreeUtil.getPrevSiblingOfType(element, PsiTypeElement.class);
        }
        PsiClass daoClass = PsiTreeUtil.getParentOfType(statementElement, PsiClass.class);
        PsiClass entityClass = Utils.getFirstGenericTypeOfBaseDao(daoClass);
        List<PsiField> fields = Utils.getAllFieldsFilterStaticAndFinal(entityClass);
        String text = statementElement.getText();
        if (!Utils.isJpaMethod(text)) {
            Messages.showErrorDialog("不是JPA方法", "错误");
            return;
        }
        FieldNamesAndReturnType fieldNamesAndReturnType = getFieldNamesAndReturnType(project, text, daoClass, entityClass, false);
        String returnType = fieldNamesAndReturnType.returnType();
        List<String> fieldNames = fieldNamesAndReturnType.fieldNames();
        List<PsiField> newParams = getFieldByNames(fields, fieldNames);
        String methodText = returnType + " " + statementElement.getText() + "(" + newParams.stream()
                .map(e -> e.getType().getCanonicalText() + " " + e.getName()).collect(Collectors.joining(" ,")) + ");";
        PsiTypeElement finalStatementElement = statementElement;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            // 使用 PSI 元素工厂创建方法
            PsiMethod newMethod = elementFactory.createMethodFromText(methodText, null);
            // 在方法调用的位置插入新方法
            finalStatementElement.replace(newMethod);
            // 优化导入和代码样式
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(daoClass);
            CodeStyleManager.getInstance(project).reformat(daoClass);
        });
    }

    /**
     * 获得字段名集合和返回类型
     *
     * @param project        项目
     * @param text           文本
     * @param daoClass       dao类
     * @param entityClass    实体类
     * @param onlyQueryField 只查询字段，不构建返回值
     * @return {@link FieldNamesAndReturnType}
     */
    @SuppressWarnings("SameParameterValue")
    static FieldNamesAndReturnType getFieldNamesAndReturnType(@NotNull Project project, String text, PsiClass daoClass, PsiClass entityClass,
            boolean onlyQueryField) {
        List<PsiField> fields = Utils.getAllFieldsFilterStaticAndFinal(entityClass);
        String returnType = Utils.wrapToList(entityClass.getQualifiedName());
        boolean startsWithFind = text.startsWith(Constants.FIND);
        boolean startsWithFindOne = text.startsWith(Constants.FIND_ONE);
        if (startsWithFind) {
            if (startsWithFindOne) {
                text = text.substring(Constants.FIND_ONE.length());
                returnType = entityClass.getQualifiedName();
            } else {
                text = text.substring(Constants.FIND.length());
            }
        } else if (text.startsWith(Constants.GET_COUNT_BY)) {
            text = text.substring(Constants.GET_COUNT_BY.length()) + Constants.BY;
            returnType = "long";
        } else if (text.startsWith(Constants.GET_SUM_OF)) {
            text = text.substring(Constants.GET_SUM_OF.length());
            returnType = "double";
        }
        List<List<String>> params = new ArrayList<>();
        List<String> arr = split(text, Constants.GROUP_BY);
        String word = arr.get(0);
        if (arr.size() == 2 && onlyQueryField) {
            String ctext = arr.get(1);
            ctext = split(ctext, Constants.LIMIT).get(0);
            params.add(split(ctext, Constants.AND));
        }
        arr = split(word, Constants.ORDER_BY);
        word = arr.get(0);
        if (arr.size() == 2 && onlyQueryField) {
            String ctext = arr.get(1);
            params.add(split(ctext, Constants.AND).stream().map(e -> {
                if (e.endsWith(Constants.DESC)) {
                    e = e.substring(0, e.length() - Constants.DESC.length());
                } else if (e.endsWith(Constants.ASC)) {
                    e = e.substring(0, e.length() - Constants.ASC.length());
                }
                return e;
            }).collect(Collectors.toList()));
        }
        if (word.startsWith(Constants.BY)) {
            params.add(split(word.substring(Constants.BY.length()), Constants.AND));
        } else {
            arr = split(word, Constants.BY);
            if (arr.size() == 2) {
                if (onlyQueryField) {
                    params.add(split(arr.get(0), Constants.AND));
                }
                params.add(split(arr.get(1), Constants.AND));
                List<PsiField> fieldByNames = getFieldByNames(fields, split(arr.get(0), Constants.AND));
                if (startsWithFind) {
                    if (fieldByNames.size() == 1) {
                        returnType = fieldByNames.get(0).getType().getCanonicalText();
                    } else if (!onlyQueryField) {
                        String innerClassName = fieldByNames.stream().map(e -> Texts.capitalize(e.getName())).collect(Collectors.joining("And")) + "PO";
                        PsiClass innerClass = Utils.addInnerClassToJavaFile(project, daoClass, innerClassName, fieldByNames.stream().map(e -> {
                            Column column = new Column();
                            column.setField(e.getName());
                            column.setPsiType(e.getType());
                            return column;
                        }).toList());
                        returnType = startsWithFindOne ? innerClass.getQualifiedName() : (Utils.wrapToList(innerClass.getQualifiedName()));
                    }
                }
            }
        }
        Collections.reverse(params);
        List<String> list = params.stream().flatMap(Collection::stream).toList();
        return new FieldNamesAndReturnType(returnType, list);
    }

    public static List<PsiField> getFieldByNames(List<PsiField> fields, List<String> arr) {
        List<PsiField> result = new ArrayList<>();
        for (String ele : arr) {
            fields.stream().filter(field -> Texts.capitalize(field.getName()).equals(ele)).forEach(result::add);
        }
        return result;
    }

    public static List<String> split(String text, String separator) {
        return Arrays.asList(text.split("(?<=([a-z0-9]|^))" + separator + "(?=([A-Z0-9]|$))"));
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    record FieldNamesAndReturnType(String returnType, List<String> fieldNames) {
    }
}
