package cn.xuanyuanli.ideaplugin.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import cn.xuanyuanli.ideaplugin.inspection.DaoJpaMethodInspection;
import cn.xuanyuanli.ideaplugin.inspection.DaoJpaMethodInspection.PsiDaoMethod;
import cn.xuanyuanli.ideaplugin.inspection.DaoJpaMethodInspection.PsiEntityClass;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.jdbc.base.jpa.strategy.BaseQueryStrategy;
import cn.xuanyuanli.jdbc.base.jpa.strategy.FindAnyByIdQuery;
import cn.xuanyuanli.jdbc.base.jpa.strategy.GetCountByAnyQuery;
import cn.xuanyuanli.jdbc.base.jpa.strategy.GetSumOfByAnyQuery;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.base.spec.QueryCondition;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.JdbcPojos;
import cn.xuanyuanli.jdbc.base.util.JdbcPojos.FieldColumn;
import cn.xuanyuanli.core.util.Texts;
import cn.xuanyuanli.ideaplugin.JujubeBundle;

/**
 * @author John Li
 * @date 2023/4/20
 */
public class JpaMethodQuickDocAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //noinspection DuplicatedCode
        if (isDisabled(e)) {
            return;
        }
        // 获取当前活动的项目、编辑器和文件
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        PsiClass psiClass = psiJavaFile.getClasses()[0];

        // 获取当前光标所在的PsiElement
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAtOffset = psiFile.findElementAt(offset);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(elementAtOffset, PsiMethod.class);
        if (psiMethod == null) {
            Arrays.stream(psiClass.getMethods()).filter(method -> Utils.isJpaMethod(method.getName())).forEach(this::generateDoc);
        } else {
            generateDoc(psiMethod);
        }
    }

    private void generateDoc(PsiMethod psiMethod) {
        if (Utils.isDefaultMethod(psiMethod) || !Utils.isJpaMethod(psiMethod.getName())) {
            return;
        }
        PsiDaoMethod daoMethod = new PsiDaoMethod(psiMethod);
        PsiEntityClass entityClass = (PsiEntityClass) daoMethod.getEntityClass();
        String entityName = entityClass.getOriginClass().getName();
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        String returnCanonicalText = Objects.requireNonNull(psiMethod.getReturnType()).getCanonicalText();
        boolean returnEntity = returnCanonicalText.equals(entityName);
        BaseQueryStrategy strategy = DaoJpaMethodInspection.getQueryStrategy(psiMethod);
        if (strategy == null) {
            return;
        }
        Query query = DaoJpaMethodInspection.getQuery(psiMethod, strategy);

        String comment = JujubeBundle.getText("jpa.comment.based.on.condition");
        String paramDoc = getParamDoc(strategy, query, psiMethod, entityName);
        String returnDoc;
        if (returnEntity) {
            comment += JujubeBundle.getText("jpa.comment.get.object");
            returnDoc = Texts.format("* @return {@link {}}", entityName);
        } else if (returnCanonicalText.startsWith("java.util.List")) {
            comment += JujubeBundle.getText("jpa.comment.get.object.list");
            if (returnCanonicalText.contains(Objects.requireNonNull(entityName))) {
                returnDoc = Texts.format("* @return {@link List}<{@link {}}>", entityName);
            } else {
                PsiType type = Utils.getFirstGenericTypeOfClass(psiMethod.getReturnType());
                returnDoc = Texts.format("* @return {@link List}<{@link {}}>", type.getCanonicalText());
            }
        } else if (daoMethod.hasSelectFieldAnnotation()) {
            if (Utils.isPaimitiveType(psiMethod.getReturnType())) {
                comment += JujubeBundle.getText("jpa.comment.get.field");
                returnDoc = Texts.format("* @return {}", returnCanonicalText);
            } else {
                comment += JujubeBundle.getText("jpa.comment.get.object");
                returnDoc = Texts.format("* @return {@link {}}", returnCanonicalText);
            }
        } else if (Utils.isPaimitiveType(psiMethod.getReturnType())) {
            comment += "获得 ";
            returnDoc = "* @return ";
            if (strategy instanceof GetCountByAnyQuery) {
                comment += JujubeBundle.getText("jpa.comment.get.total");
                returnDoc += JujubeBundle.getText("jpa.comment.get.total");
            } else if (strategy instanceof GetSumOfByAnyQuery) {
                String entityField = query.getSelectFields().get(0).getEntityField();
                String common = Texts.format(" {@link {}#get{} {}}", entityName, entityField, Utils.firstCharToLowerCase(entityField)) + " " + JujubeBundle.getText("jpa.comment.get.sum");
                comment += common;
                returnDoc += common;
            } else {
                String entityField = query.getSelectFields().get(0).getEntityField();
                String link = Texts.format("{@link {}#get{} {}}", entityName, entityField, Utils.firstCharToLowerCase(entityField));
                comment += link;
                returnDoc += link;
            }
        } else {
            String link = Texts.format("{@link {}}", psiMethod.getReturnType().getCanonicalText());
            comment += JujubeBundle.getText("jpa.comment.get.object") + " " + link;
            returnDoc = "* @return " + link;
        }

        // 创建一个新的 Javadoc 注释
        String newJavadocText = Texts.format("""
                    /**
                     * {}
                     *
                {}
                     {}
                     */
                """, comment, paramDoc, returnDoc);
        PsiDocComment newJavadoc = elementFactory.createDocCommentFromText(newJavadocText);

        // 为方法添加或替换 Javadoc 注释
        WriteCommandAction.runWriteCommandAction(psiMethod.getProject(), () -> {
            PsiDocComment oldJavadoc = psiMethod.getDocComment();
            if (oldJavadoc != null) {
                oldJavadoc.replace(newJavadoc);
            } else {
                psiMethod.addBefore(newJavadoc, psiMethod.getModifierList());
            }
        });
    }

    private String getParamDoc(BaseQueryStrategy strategy, Query query, PsiMethod psiMethod, String entityName) {
        List<String> docs = new ArrayList<>();
        Spec spec = query.getSpec();
        if (spec == null) {
            if (strategy instanceof FindAnyByIdQuery) {
                String primaryKeyName = DaoJpaMethodInspection.getPrimaryKeyName(Objects.requireNonNull(psiMethod.getContainingClass()));
                docs.add(Texts.format("    * @param @@@    {@link {}#get{}() {}}", entityName, Texts.capitalize(primaryKeyName), primaryKeyName));
            }
        } else {
            fillDocs(psiMethod, entityName, docs, spec);
        }
        List<String> rdocs = new ArrayList<>();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        if (parameters.length == docs.size()) {
            for (int i = 0; i < parameters.length; i++) {
                PsiParameter parameter = parameters[i];
                rdocs.add(docs.get(i).replace("@@@", parameter.getName()));
            }
        } else {
            Consoles.info("psiMethod:{}, spce:{}, conditions:{}", psiMethod, spec, Objects.requireNonNull(spec).getConditions());
        }
        return String.join("\n", rdocs);
    }

    private void fillDocs(PsiMethod psiMethod, String entityName, List<String> docs, Spec spec) {
        spec.getFilterSql();
        for (QueryCondition condition : spec.getConditions()) {
            String entityFieldName = getEntityFieldName(psiMethod, condition.getField());
            if (condition.getParamNum() == 1) {
                docs.add(Texts.format("    * @param @@@    {@link {}#get{}() {}}", entityName, Texts.capitalize(entityFieldName), entityFieldName));
            } else if (condition.getParamNum() == 2) {
                docs.add(Texts.format("    * @param @@@    before {@link {}#get{}() {}}", entityName, Texts.capitalize(entityFieldName), entityFieldName));
                docs.add(Texts.format("    * @param @@@    after {@link {}#get{}() {}}", entityName, Texts.capitalize(entityFieldName), entityFieldName));
            }
        }
        if (psiMethod.getName().endsWith("Limit")) {
            docs.add("    * @param @@@    " + JujubeBundle.getText("jpa.comment.limit.param"));
        }
    }

    private String getEntityFieldName(PsiMethod psiMethod, String dbField) {
        PsiDaoMethod psiDaoMethod = new PsiDaoMethod(psiMethod);
        Optional<FieldColumn> first = JdbcPojos.getFieldColumns(psiDaoMethod.getEntityClass()).stream().filter(field -> field.getColumn().equals(dbField))
                .findFirst();
        return first.map(FieldColumn::getField).orElse(null);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(!isDisabled(e));
    }

    private boolean isDisabled(AnActionEvent e) {
        try {
            // 获取当前活动的项目、编辑器和文件
            PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            return !Utils.isBaseDao(psiClass);
        } catch (Throwable ex) {
            return true;
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
