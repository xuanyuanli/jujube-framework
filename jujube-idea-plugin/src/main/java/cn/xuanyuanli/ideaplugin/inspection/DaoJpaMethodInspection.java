package cn.xuanyuanli.ideaplugin.inspection;

import cn.xuanyuanli.jdbc.exception.DaoQueryException;
import cn.xuanyuanli.ideaplugin.support.Consoles;
import cn.xuanyuanli.ideaplugin.support.Utils;
import cn.xuanyuanli.ideaplugin.JujubeBundle;
import cn.xuanyuanli.jdbc.base.jpa.strategy.BaseQueryStrategy;
import cn.xuanyuanli.jdbc.base.jpa.strategy.JpaQuerier;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityClass;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityField;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.binding.DaoSqlRegistry;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuanyuanli
 */
public class DaoJpaMethodInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethod(@NotNull PsiMethod method) {
                super.visitMethod(method);

                if (Utils.isDefaultMethod(method)) {
                    return;
                }
                PsiClass daoClass = method.getContainingClass();
                String methodName = method.getName();
                PsiDaoMethod psiDaoMethod = new PsiDaoMethod(method);
                if (daoClass != null && (!Utils.isBaseDao(daoClass) || !Utils.isJpaMethod(methodName) || !DaoSqlRegistry.isJpaMethod(methodName)
                        || psiDaoMethod.hasSelectFieldAnnotation())) {
                    return;
                }

                try {
                    BaseQueryStrategy queryStrategy = getQueryStrategy(method);
                    if (queryStrategy != null) {
                        getQuery(method, queryStrategy);
                    }
                } catch (DaoQueryException e) {
                    String regex = "entityFieldName:";
                    if (e.getMessage().contains(regex)) {
                        String field = e.getMessage().split(regex)[1];
                        String message = JujubeBundle.message("inspection.jpa.method.invalid.field", String.join(", ", field));
                        holder.registerProblem(method, message);
                    } else {
                        Consoles.info(e.getMessage());
                    }
                } catch (IndexOutOfBoundsException e) {
                    String message = JujubeBundle.getText("inspection.jpa.method.missing.params");
                    holder.registerProblem(method, message);
                } catch (Exception e) {
                    String message = JujubeBundle.message("inspection.jpa.method.unknown.error", e.getMessage());
                    holder.registerProblem(method, message);
                }
            }
        };
    }

    /**
     * 获取查询
     *
     * @param method   方法
     * @param strategy 策略
     * @return {@link Query }
     */
    public static Query getQuery(PsiMethod method, BaseQueryStrategy strategy) {
        PsiClass daoClass = method.getContainingClass();
        PsiDaoMethod psiDaoMethod = new PsiDaoMethod(method);
        List<Object> args = Arrays.stream(method.getParameterList().getParameters()).map(e -> getDefaultValue(e.getType())).toList();
        return strategy.getQuery(getTableName(Objects.requireNonNull(daoClass)), psiDaoMethod, args.toArray());
    }

    /**
     * 获取查询策略
     *
     * @param method 方法
     * @return {@link BaseQueryStrategy }
     */
    public static BaseQueryStrategy getQueryStrategy(PsiMethod method) {
        List<BaseQueryStrategy> strategies = JpaQuerier.getStrategies();
        for (BaseQueryStrategy strategy : strategies) {
            if (strategy.accept(method.getName())) {
                return strategy;
            }
        }
        return null;
    }

    /**
     * 获取默认值
     *
     * @param type 类型
     * @return {@link Object }
     */
    private static Object getDefaultValue(PsiType type) {
        if (type instanceof PsiClassType classType) {
            String canonicalText = classType.getCanonicalText();
            if (canonicalText.startsWith("java.util.List") || canonicalText.startsWith("java.util.Collection")) {
                // 获取泛型参数类型
                PsiType[] typeParameters = classType.getParameters();
                if (typeParameters.length > 0) {
                    PsiType elementType = typeParameters[0];
                    // 为泛型参数类型创建具有2个默认元素的列表
                    return getListByType(elementType);
                }
            }
            if (canonicalText.startsWith("java.util.Set")) {
                // 获取泛型参数类型
                PsiType[] typeParameters = classType.getParameters();
                if (typeParameters.length > 0) {
                    PsiType elementType = typeParameters[0];
                    // 为泛型参数类型创建具有2个默认元素的列表
                    return new HashSet<>(getListByType(elementType));
                }
            }
        }
        if (type instanceof PsiArrayType) {
            PsiType componentType = ((PsiArrayType) type).getComponentType();
            // 为泛型参数类型创建具有2个默认元素的列表
            Object[] arr = new Object[2];
            arr[0] = getPrimitiveDefaultValue(componentType);
            arr[1] = getPrimitiveDefaultValue(componentType);
            return arr;
        }
        return getPrimitiveDefaultValue(type);
    }

    /**
     * 按类型获取列表
     *
     * @param elementType 元素类型
     * @return {@link List }<{@link Object }>
     */
    private static @NotNull List<Object> getListByType(PsiType elementType) {
        List<Object> list = new ArrayList<>();
        list.add(getPrimitiveDefaultValue(elementType));
        list.add(getPrimitiveDefaultValue(elementType));
        return list;
    }

    /**
     * 获取原始类型默认值
     *
     * @param type 类型
     * @return {@link Object }
     */
    private static @Nullable Object getPrimitiveDefaultValue(PsiType type) {
        if (type.equalsToText("java.lang.Integer")) {
            return 0;
        } else if (type.equalsToText("java.lang.Long")) {
            return 0L;
        } else if (type.equalsToText("java.lang.Float")) {
            return 0.0f;
        } else if (type.equalsToText("java.lang.Double")) {
            return 0.0;
        } else if (type.equalsToText("java.lang.Boolean")) {
            return false;
        } else if (type.equalsToText("java.lang.Character")) {
            return '\u0000';
        } else if (type.equalsToText("java.lang.Short")) {
            return (short) 0;
        } else if (type.equalsToText("java.lang.Byte")) {
            return (byte) 0;
        } else if (type.equalsToText("java.lang.String")) {
            return "s";
        } else if (type.equals(PsiTypes.intType())) {
            return 0;
        } else if (type.equals(PsiTypes.longType())) {
            return 0L;
        } else if (type.equals(PsiTypes.floatType())) {
            return 0.0f;
        } else if (type.equals(PsiTypes.doubleType())) {
            return 0.0;
        } else if (type.equals(PsiTypes.booleanType())) {
            return false;
        } else if (type.equals(PsiTypes.charType())) {
            return '\u0000';
        } else if (type.equals(PsiTypes.shortType())) {
            return (short) 0;
        } else if (type.equals(PsiTypes.byteType())) {
            return (byte) 0;
        } else {
            return null;
        }
    }

    private static String getTableName(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.findMethodsByName("getTableName", true);
        return getPrimaryKeyName(methods);
    }

    public static String getPrimaryKeyName(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.findMethodsByName("getPrimaryKeyName", true);
        String methodBody = getPrimaryKeyName(methods);
        if (methodBody != null) {
            return methodBody;
        }
        return "id";
    }

    @Nullable
    private static String getPrimaryKeyName(PsiMethod[] methods) {
        if (methods.length > 0) {
            PsiMethod method = methods[0];
            PsiCodeBlock codeBlock = method.getBody();
            if (codeBlock != null) {
                String methodBody = codeBlock.getText();
                return methodBody.substring(6).replace("\"", "").trim();
            }
        }
        return null;
    }

    public static class PsiDaoMethod implements DaoMethod {

        private final PsiMethod method;

        public PsiDaoMethod(PsiMethod method) {
            this.method = method;
        }

        @Override
        public EntityClass getEntityClass() {
            return new PsiEntityClass(Utils.getFirstGenericTypeOfBaseDao(Objects.requireNonNull(method.getContainingClass())));
        }

        @Override
        public String getName() {
            return method.getName();
        }

        @Override
        public boolean hasSelectFieldAnnotation() {
            return method.hasAnnotation("cn.xuanyuanli.jdbc.base.annotation.SelectField");
        }

        @Override
        public String[] getSelectFieldAnnotationValue() {
            return new String[0];
        }
    }

    public static class PsiEntityClass implements EntityClass {

        private final PsiClass entityClass;

        public PsiEntityClass(PsiClass entityClass) {
            this.entityClass = entityClass;
        }

        @Override
        public EntityField[] getDeclaredFields() {
            return Utils.getAllFieldsFilterStaticAndFinal(entityClass).stream().map(PsiEntityField::new).toArray(EntityField[]::new);
        }

        @Override
        public String getName() {
            return Utils.getAllFieldsFilterStaticAndFinal(entityClass).stream().map(PsiField::getName).collect(Collectors.joining(","));
        }

        /**
         * 获得源类
         *
         * @return {@link PsiClass}
         */
        public PsiClass getOriginClass() {
            return entityClass;
        }
    }

    public static class PsiEntityField implements EntityField {

        private final PsiField field;

        public PsiEntityField(PsiField field) {
            this.field = field;
        }

        @Override
        public boolean hasColumnAnnotation() {
            return field.hasAnnotation("cn.xuanyuanli.jdbc.base.annotation.Column");
        }

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public String getColumnAnnotationValue() {
            PsiAnnotation annotation = field.getAnnotation("cn.xuanyuanli.jdbc.base.annotation.Column");
            if (annotation != null) {
                PsiAnnotationMemberValue attributeValue = annotation.findAttributeValue("value");
                if (attributeValue instanceof PsiLiteralExpression literalValue) {
                    return (String) literalValue.getValue();
                }
            }
            return null;
        }

        @Override
        public boolean hasVisualColumnAnnotation() {
            return field.hasAnnotation("cn.xuanyuanli.jdbc.base.annotation.VisualColumn");
        }
    }
}
