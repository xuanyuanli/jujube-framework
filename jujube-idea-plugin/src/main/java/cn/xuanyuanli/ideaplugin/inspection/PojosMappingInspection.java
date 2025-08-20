package cn.xuanyuanli.ideaplugin.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypes;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.ideaplugin.JujubeBundle;
import cn.xuanyuanli.core.util.Pojos.FieldMapping;
import cn.xuanyuanli.core.util.Texts;

/**
 * @author John Li
 */
public class PojosMappingInspection extends LocalInspectionTool {


    @Override
    public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }
                // 检查Pojos类
                pojosInspection(expression, method, holder);
                // 检查BaseEntity.toBO方法
                baseEntityToBoInspection(expression, method, holder);
                // 检查Pageable.toGenericType方法
                pageableToGenericTypeInspection(expression, method, holder);
            }
        };
    }

    /**
     * Pojos类方法检验
     */
    private void pojosInspection(PsiMethodCallExpression expression, PsiMethod method, ProblemsHolder holder) {
        DiffField diffField = getDiffFieldOfPojos(expression, method, holder);
        if (diffField == null) {
            return;
        }
        List<PsiField> psiFields = diffField.misFieldsOfTarget();
        List<String> misFields = psiFields.stream().map(PsiField::getName).toList();
        // 如果存在不匹配的字段，报告问题
        String message = null;
        boolean hasSourceMis = !diffField.misFieldMappingOfSource().isEmpty();
        boolean hasTargetMis = !diffField.misFieldMappingOfTarget().isEmpty();
        if (hasSourceMis && hasTargetMis) {
            message = JujubeBundle.message("inspection.pojos.mapping.filemap.problem.source.target", 
                String.join(", ", diffField.misFieldMappingOfSource()),
                String.join(", ", diffField.misFieldMappingOfTarget()));
        } else if (hasSourceMis) {
            message = JujubeBundle.message("inspection.pojos.mapping.filemap.problem.source", 
                String.join(", ", diffField.misFieldMappingOfSource()));
        } else if (hasTargetMis) {
            message = JujubeBundle.message("inspection.pojos.mapping.filemap.problem.target", 
                String.join(", ", diffField.misFieldMappingOfTarget()));
        }
        if (!misFields.isEmpty()) {
            message = JujubeBundle.message("inspection.pojos.mapping.missing.fields", String.join(", ", misFields));
        }
        if (message != null) {
            holder.registerProblem(expression, message, new PojosLocalQuickFix());
        }
    }

    /**
     * BaseEntity.toBO检查
     */
    private void baseEntityToBoInspection(PsiMethodCallExpression methodCall, PsiMethod method, ProblemsHolder holder) {
        DiffField diffField = getDiffFieldOfBaseEntiryToBo(methodCall, method, holder);
        if (diffField == null) {
            return;
        }
        List<PsiField> psiFields = diffField.misFieldsOfTarget();
        List<String> misFields = psiFields.stream().map(PsiField::getName).toList();
        if (!misFields.isEmpty()) {
            String message = JujubeBundle.message("inspection.baseentity.tobo.missing.fields", String.join(", ", misFields));
            holder.registerProblem(methodCall, message, new PojosLocalQuickFix());
        }
    }

    /**
     * Pageable.toGenericType检查
     */
    private void pageableToGenericTypeInspection(PsiMethodCallExpression methodCall, PsiMethod method, ProblemsHolder holder) {
        DiffField diffField = getDiffFieldOfPageableToGeneric(methodCall, method, holder);
        if (diffField == null) {
            return;
        }
        // 如果存在不匹配的字段，报告问题
        List<PsiField> psiFields = diffField.misFieldsOfTarget();
        List<String> misFields = psiFields.stream().map(PsiField::getName).toList();
        if (!misFields.isEmpty()) {
            String message = JujubeBundle.message("inspection.pageable.togeneric.missing.fields", String.join(", ", misFields));
            holder.registerProblem(methodCall, message, new PojosLocalQuickFix());
        }
    }

    /**
     * 得到Pageable.toGenericType差异字段
     */
    @Nullable
    private static DiffField getDiffFieldOfPageableToGeneric(PsiMethodCallExpression methodCall, PsiMethod method, ProblemsHolder holder) {
        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
        PsiExpression[] arguments = methodCall.getArgumentList().getExpressions();
        PsiExpression caller = methodExpression.getQualifierExpression();
        if (caller == null || !"toGenericType".equals(method.getName()) || arguments.length != 1 || Utils.isNotClassType(Objects.requireNonNull(arguments[0].getType()))
                || caller.getType() == null || !caller.getType().getCanonicalText().startsWith(Utils.PAGEABLE_NAME)) {
            return null;
        }
        PsiType callerType = caller.getType();
        PsiType originType = Utils.getClassOriginType(arguments[0].getType(), method.getProject());
        if (Utils.isMapType(originType) && holder != null) {
            String message = JujubeBundle.getText("inspection.record.not.allowed.target");
            holder.registerProblem(methodCall, message);
            return null;
        }
        PsiType firstGenericType = Utils.getFirstGenericTypeOfClass(callerType);
        PsiClass sourceClass = PsiUtil.resolveClassInClassTypeOnly(firstGenericType);
        PsiClass targetClass = PsiUtil.resolveClassInClassTypeOnly(originType);
        return getMismatchedFields(null, sourceClass, targetClass);
    }

    /**
     * 得到BaseEntity.toBO差异字段
     */
    @Nullable
    private static DiffField getDiffFieldOfBaseEntiryToBo(PsiMethodCallExpression methodCall, PsiMethod method, ProblemsHolder holder) {
        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
        PsiExpression[] arguments = methodCall.getArgumentList().getExpressions();
        PsiExpression caller = methodExpression.getQualifierExpression();
        if (caller == null || !"toBO".equals(method.getName()) || arguments.length != 1 || Utils.isNotClassType(Objects.requireNonNull(arguments[0].getType()))
                || caller.getType() == null || !Utils.isBaseEntity(caller.getType())) {
            return null;
        }
        PsiType callerType = caller.getType();
        PsiType originType = Utils.getClassOriginType(arguments[0].getType(), method.getProject());
        if (Utils.isMapType(originType) && holder != null) {
            String message = JujubeBundle.getText("inspection.record.not.allowed.target");
            holder.registerProblem(methodCall, message);
            return null;
        }
        PsiClass sourceClass = PsiUtil.resolveClassInClassTypeOnly(callerType);
        PsiClass targetClass = PsiUtil.resolveClassInClassTypeOnly(originType);
        return getMismatchedFields(null, sourceClass, targetClass);
    }

    @Nullable
    private static DiffField getDiffFieldOfPojos(PsiMethodCallExpression expression, PsiMethod method, ProblemsHolder holder) {
        if (method == null || !"cn.xuanyuanli.util.Pojos".equals(Objects.requireNonNull(method.getContainingClass()).getQualifiedName())) {
            return null;
        }

        // 检查第二个参数是否是Map
        PsiExpression[] arguments = expression.getArgumentList().getExpressions();
        if (arguments.length < 2) {
            return null;
        }
        PsiType targetType = arguments[1].getType();
        if (targetType == null) {
            return null;
        }
        targetType = Utils.getClassOriginType(targetType, expression.getProject());
        if (Utils.isMapType(targetType) && holder != null) {
            String message = JujubeBundle.getText("inspection.record.not.allowed.pojos");
            holder.registerProblem(expression, message);
            return null;
        }

        // 检查目标类和源类字段的差异
        DiffField diffField = null;
        if ("mapping".equals(method.getName())) {
            diffField = checkPojosMappingMethod(expression);
        }
        if ("copy".equals(method.getName())) {
            diffField = checkPojosCopyMethod(expression);
        }
        if ("mappingArray".equals(method.getName())) {
            diffField = checkPojosMappingArrayMethod(expression);
        }
        return diffField;
    }

    /**
     * 获取不匹配字段
     *
     * @param thirdArgument 第三个参数表达式
     * @param sourceClass   源类
     * @param targetClass   目标类
     */
    private static DiffField getMismatchedFields(PsiExpression thirdArgument, PsiClass sourceClass, PsiClass targetClass) {
        List<PsiField> misTargetFields = new ArrayList<>();
        List<String> misFieldMappingOfSource = new ArrayList<>();
        List<String> misFieldMappingOfTarget = new ArrayList<>();
        DiffField diffField = new DiffField(misTargetFields, misFieldMappingOfSource, misFieldMappingOfTarget, sourceClass, targetClass);
        if (sourceClass == null || targetClass == null) {
            return diffField;
        }
        // 忽略Map类型
        if (Utils.isMapType(Utils.classToType(sourceClass.getProject(), sourceClass)) || Utils.isMapType(
                Utils.classToType(targetClass.getProject(), targetClass))) {
            return diffField;
        }
        FieldMapping fieldMapping = new FieldMapping();
        if (thirdArgument != null) {
            // 检查第三个参数是否为一个方法调用
            if (thirdArgument instanceof PsiMethodCallExpression fieldMappingCall) {
                processFieldMappingCall(fieldMappingCall, fieldMapping);
            } else if (thirdArgument instanceof PsiReferenceExpression thirdReference) {
                // 获取引用的定义
                PsiElement referenceElement = thirdReference.resolve();
                if (referenceElement instanceof PsiLocalVariable localVar) {
                    PsiExpression initializer = localVar.getInitializer();
                    if (initializer instanceof PsiMethodCallExpression fieldMappingCall) {
                        processFieldMappingCall(fieldMappingCall, fieldMapping);
                    }
                }
            }
        }
        Map<String, String> fieldMappingMap = fieldMapping.getFieldMapping();
        // 填充misFieldMappingOfSource
        fieldMappingMap.keySet().stream().filter(e -> sourceClass.findFieldByName(e, true) == null).forEach(misFieldMappingOfSource::add);
        // 填充misFieldMappingOfTarget
        fieldMappingMap.values().stream().filter(e -> targetClass.findFieldByName(e, true) == null).forEach(misFieldMappingOfTarget::add);
        // 填充misTargetFields
        List<PsiField> fieldList = Utils.getAllFieldsFilterStaticAndFinal(targetClass);
        fieldList = fieldList.stream().filter(e -> !e.hasAnnotation("cn.xuanyuanli.lang.annotation.IgnoreCheck")).toList();
        for (PsiField field : fieldList) {
            String targetFieldName = field.getName();
            // 忽略已mapping的字段，只处理未mapping的字段
            if (!fieldMappingMap.containsValue(targetFieldName)) {
                // 看源类中没有没此字段
                if (Utils.getFieldFromClass(targetFieldName, sourceClass) == null) {
                    // 再看此字段有没有被set
                    PsiMethod[] methods = targetClass.findMethodsByName("set" + Texts.capitalize(targetFieldName), true);
                    if (methods.length == 1) {
                        List<PsiReference> setCall = Utils.findMethodCall(methods[0]);
                        if (setCall.isEmpty()) {
                            misTargetFields.add(field);
                        }
                    }
                }
            }
        }
        return diffField;
    }

    /**
     * 检查mappingArray方法
     */
    private static DiffField checkPojosMappingArrayMethod(PsiMethodCallExpression expression) {
        // 获取方法参数
        PsiExpression[] arguments = expression.getArgumentList().getExpressions();
        if (arguments.length != 2 && arguments.length != 3) {
            return null;
        }

        // 获取参数类型
        PsiType sourceType = arguments[0].getType();
        PsiType targetType = arguments[1].getType();

        if (isNull(sourceType, targetType) || Utils.isNotClassType(targetType)) {
            return null;
        }
        targetType = Utils.getClassOriginType(targetType, expression.getProject());
        sourceType = Utils.getFirstGenericTypeOfClass(sourceType);
        if (sourceType != null) {
            return checkPojosMappingMethod(arguments, sourceType, targetType);
        }
        return null;
    }

    /**
     * 检查mapping方法
     */
    private static DiffField checkPojosMappingMethod(PsiMethodCallExpression expression) {
        // 获取方法参数
        PsiExpression[] arguments = expression.getArgumentList().getExpressions();
        if (arguments.length != 2 && arguments.length != 3) {
            return null;
        }

        // 获取参数类型
        PsiType sourceType = arguments[0].getType();
        PsiType targetType = arguments[1].getType();

        if (isNull(sourceType, targetType) || Utils.isNotClassType(targetType)) {
            return null;
        }
        targetType = Utils.getClassOriginType(targetType, expression.getProject());
        return checkPojosMappingMethod(arguments, sourceType, targetType);
    }

    private static DiffField checkPojosMappingMethod(PsiExpression[] arguments, PsiType sourceType, PsiType targetType) {
        // 解析参数类型为 PsiClass
        PsiClass sourceClass = PsiUtil.resolveClassInClassTypeOnly(sourceType);
        PsiClass targetClass = PsiUtil.resolveClassInClassTypeOnly(targetType);
        PsiExpression thirdArgument = null;
        if (arguments.length == 3) {
            thirdArgument = arguments[2];
        }
        return getMismatchedFields(thirdArgument, sourceClass, targetClass);
    }

    private static boolean isNull(PsiType sourceType, PsiType targetType) {
        return sourceType == null || targetType == null || sourceType.equals(PsiTypes.nullType()) || targetType.equals(PsiTypes.nullType());
    }

    /**
     * 字段field方法调用
     */
    private static void processFieldMappingCall(PsiMethodCallExpression expression, FieldMapping fieldMapping) {
        PsiMethod method = expression.resolveMethod();
        if (method == null || !"field".equals(method.getName())) {
            return;
        }

        PsiExpression[] arguments = expression.getArgumentList().getExpressions();
        if (arguments.length == 2) {
            String arg1 = Objects.requireNonNull(((PsiLiteralExpressionImpl) arguments[0]).getValue()).toString();
            String arg2 = Objects.requireNonNull(((PsiLiteralExpressionImpl) arguments[1]).getValue()).toString();
            fieldMapping.field(arg1, arg2);
        }

        PsiExpression qualifierExpression = expression.getMethodExpression().getQualifierExpression();
        if (qualifierExpression instanceof PsiMethodCallExpression) {
            processFieldMappingCall((PsiMethodCallExpression) qualifierExpression, fieldMapping);
        }
    }

    /**
     * 检查copy方法
     */
    private static DiffField checkPojosCopyMethod(PsiMethodCallExpression expression) {
        // 获取方法参数
        PsiExpression[] arguments = expression.getArgumentList().getExpressions();
        if (arguments.length != 2 && arguments.length != 3) {
            return null;
        }

        // 获取参数类型
        PsiType sourceType = arguments[0].getType();
        PsiType targetType = arguments[1].getType();

        if (isNull(sourceType, targetType)) {
            return null;
        }

        // 解析参数类型为 PsiClass
        PsiClass sourceClass = PsiUtil.resolveClassInClassTypeOnly(sourceType);
        PsiClass targetClass = PsiUtil.resolveClassInClassTypeOnly(targetType);
        PsiExpression thirdArgument = null;
        if (arguments.length == 3) {
            thirdArgument = arguments[2];
        }
        return getMismatchedFields(thirdArgument, sourceClass, targetClass);
    }

    private static class PojosLocalQuickFix implements LocalQuickFix {

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return JujubeBundle.getText("quick.fix.pojos.mapping.add.fields");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement psiElement = descriptor.getPsiElement();
            if (!(psiElement instanceof PsiMethodCallExpression methodCallExpression)) {
                return;
            }
            PsiMethod psiMethod = methodCallExpression.resolveMethod();
            DiffField diffField = getDiffFieldOfPageableToGeneric(methodCallExpression, psiMethod, null);
            if (diffField == null) {
                diffField = getDiffFieldOfBaseEntiryToBo(methodCallExpression, psiMethod, null);
            }
            if (diffField == null) {
                diffField = getDiffFieldOfPojos(methodCallExpression, psiMethod, null);
            }
            if (diffField != null) {
                Consoles.info("暂未实现，差异信息：{}", diffField);
            }
        }
    }

    /**
     * 差异字段信息
     *
     * @param misFieldsOfTarget       target未被赋值的字段
     * @param misFieldMappingOfSource 自定义FiledMapping，在source中不存在的字段
     * @param misFieldMappingOfTarget 自定义FiledMapping，在target中不存在的字段
     */
    record DiffField(List<PsiField> misFieldsOfTarget, List<String> misFieldMappingOfSource, List<String> misFieldMappingOfTarget, PsiClass source,
                     PsiClass target) {

    }
}

