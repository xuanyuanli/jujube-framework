package cn.xuanyuanli.ideaplugin.support;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiTypes;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.sql.SqlFileType;
import com.intellij.util.Query;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.xuanyuanli.jdbc.binding.DaoSqlRegistry;
import cn.xuanyuanli.core.util.CamelCase;
import cn.xuanyuanli.core.util.Texts;

/**
 * @author John Li
 */
public class Utils {

    public static final String LIST_NAME = "java.util.List";
    public static final String PAGEABLE_NAME = "cn.xuanyuanli.jdbc.support.pagination.Pageable";
    public static final String BASE_DAO_NAME = "cn.xuanyuanli.jdbc.base.BaseDao";
    public static final String BASE_ENTITY_NAME = "cn.xuanyuanli.jdbc.support.entity.BaseEntity";
    public static final String MAP_NAME = "java.util.Map";
    public static final String CLASS_NAME = "java.lang.Class";
    public static final String RECORD_NAME = "cn.xuanyuanli.lang.Record";

    /**
     * 是否是默认方法
     */
    public static boolean isDefaultMethod(@NotNull PsiMethod method) {
        return method.hasModifierProperty(PsiModifier.DEFAULT) && PsiUtil.getLanguageLevel(Objects.requireNonNull(method.getContainingClass())).isAtLeast(LanguageLevel.JDK_1_8);
    }

    /**
     * 是否是BaseDao
     */
    public static boolean isBaseDao(PsiClass clazz) {
        PsiClass[] interfaces = clazz.getInterfaces();
        return Arrays.stream(interfaces).anyMatch(i -> BASE_DAO_NAME.equals(i.getQualifiedName()));
    }


    /**
     * 是否是覆盖方法
     */
    public static boolean isOverwriteMethod(PsiMethod method) {
        return method.findDeepestSuperMethods().length > 0;
    }

    /**
     * 获得行标记
     */
    public static RelatedItemLineMarkerInfo<PsiElement> getLineMarker(PsiElement targe, PsiElement currentEle, String tooltipText) {
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(Icons.LOGO).setAlignment(GutterIconRenderer.Alignment.CENTER)
                .setTargets(Collections.singletonList(targe)).setTooltipText(tooltipText);
        return builder.createLineMarkerInfo(currentEle);
    }

    /**
     * 从模块搜索java文件
     */
    @Nullable
    public static PsiFile searchJavaFileFromModule(PsiFile currentFile, String fileName) {
        Module module = ModuleUtilCore.findModuleForPsiElement(currentFile);
        if (module != null) {
            GlobalSearchScope moduleScope = module.getModuleWithDependenciesScope();
            PsiClass[] classes = PsiShortNamesCache.getInstance(currentFile.getProject()).getClassesByName(fileName, moduleScope);
            if (classes.length > 0) {
                return classes[0].getContainingFile();
            }
        }
        return null;
    }

    /**
     * java文件中找到方法
     */
    public static PsiMethod findMethodInJavaFile(PsiFile javaFile, String methodName) {
        if (javaFile instanceof PsiJavaFile psiJavaFile) {
            PsiClass[] classes = psiJavaFile.getClasses();
            if (classes.length > 0) {
                PsiClass mainClass = classes[0];
                for (PsiMethod method : mainClass.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 是否是sql文件
     */
    public static boolean isSqlFile(PsiFile containingFile) {
        if (containingFile != null) {
            return containingFile.getFileType() instanceof SqlFileType || containingFile.getName().endsWith(".sql");
        }
        return false;
    }

    /**
     * 从sql文件获取java文件
     */
    public static PsiFile getJavaFileFromSqlFile(PsiFile sqlFile) {
        String javaFileName = sqlFile.getName().replace(".sql", ".java");
        String fileName = javaFileName.substring(0, javaFileName.lastIndexOf("."));
        return searchJavaFileFromModule(sqlFile, fileName);
    }

    /**
     * 打开文件并跳转到指定行
     */
    public static void goLine(Project project, VirtualFile virtualFile, int offset) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openEditor(new OpenFileDescriptor(project, virtualFile, offset), true);
    }

    /**
     * 检查类型是否为Map
     */
    public static boolean isMapType(PsiType variableType) {
        return isMapType(variableType.getCanonicalText());
    }

    /**
     * 检查类型是否不为Class
     */
    public static boolean isNotClassType(PsiType variableType) {
        return !variableType.getCanonicalText().startsWith(CLASS_NAME);
    }

    /**
     * 检查类型是否为Map
     */
    public static boolean isMapType(String variableType) {
        return variableType.startsWith(MAP_NAME) || variableType.startsWith(RECORD_NAME);
    }

    /**
     * 检查类型是否为List&lt;Map&gt;
     */
    public static boolean isListMapType(PsiType variableType) {
        String canonicalText = variableType.getCanonicalText();
        boolean startList = canonicalText.startsWith(LIST_NAME);
        if (startList) {
            String[] arr = canonicalText.split("<");
            if (arr.length > 1) {
                return isMapType(arr[1]);
            }
        }
        return false;
    }

    /**
     * 检查类型是否为Pageable&lt;Map&gt;
     */
    public static boolean isPageableMapType(PsiType variableType) {
        String canonicalText = variableType.getCanonicalText();
        boolean startList = canonicalText.startsWith(PAGEABLE_NAME);
        if (startList) {
            String[] arr = canonicalText.split("<");
            if (arr.length > 1) {
                return isMapType(arr[1]);
            }
        }
        return false;
    }

    /**
     * 是否是java文件
     */
    public static boolean isJavaFile(PsiFile psiFile) {
        return psiFile instanceof PsiJavaFile;
    }

    /**
     * 获得变量的某个方法的所有引用表达式
     */
    public static List<PsiMethodCallExpression> findVariableMethodCallExpressionsInMethod(PsiMethod psiMethod, String methodRegex) {
        List<PsiMethodCallExpression> result = new ArrayList<>();
        psiMethod.accept(new JavaRecursiveElementWalkingVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                PsiReferenceExpression methodExpression = expression.getMethodExpression();
                if (methodExpression.getQualifiedName() != null && Texts.find(methodExpression.getQualifiedName(), methodRegex)) {
                    result.add(expression);
                }
            }
        });
        return result;
    }

    /**
     * 查找给定PsiMethod的所有引用
     */
    public static List<PsiReference> findMethodCall(PsiMethod method) {
        Query<PsiReference> search = ReferencesSearch.search(method, GlobalSearchScope.projectScope(method.getProject()));
        return new ArrayList<>(search.findAll());
    }

    /**
     * 添加内部类(Entity)到java文件
     */
    public static PsiClass addInnerClassToJavaFile(Project project, PsiClass target, String innerClassName, List<Column> columns) {
        AtomicReference<PsiClass> reference = new AtomicReference<>();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            // 创建一个内部类
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            reference.set(elementFactory.createClass(innerClassName));
            PsiClass innerClass = reference.get();
            if (target.isInterface()) {
                Objects.requireNonNull(innerClass.getModifierList()).setModifierProperty(PsiModifier.PUBLIC, false);
            } else {
                Objects.requireNonNull(innerClass.getModifierList()).setModifierProperty(PsiModifier.PUBLIC, true);
                innerClass.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
            }
            makeClassImpl(innerClass, "cn.xuanyuanli.jdbc.support.entity.BaseEntity");
            // 在这里为内部类添加注解
            PsiAnnotation dataAnnotation = elementFactory.createAnnotationFromText("@Data", innerClass);
            innerClass.getModifierList().addBefore(dataAnnotation, innerClass.getModifierList().getFirstChild());
            // 创建字段
            for (Column column : columns) {
                PsiField idField = elementFactory.createField(column.getField(), toWrapperPsiType(column.getPsiType(), project, target));
                innerClass.add(idField);
            }
            // 将内部类添加到接口中
            PsiClass addedInnerClass = (PsiClass) target.add(innerClass);
            // 优化导入和代码样式
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedInnerClass);
        });
        return reference.get();
    }

    /**
     * 使类实现自指定类名
     */
    public static void makeClassImpl(PsiClass aClass, String extendClassName) {
        Project project = aClass.getProject();
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiClass extendClass = JavaPsiFacade.getInstance(project).findClass(extendClassName, GlobalSearchScope.allScope(project));
        // 创建一个新的继承关系
        PsiJavaCodeReferenceElement superClassReference = elementFactory.createClassReferenceElement(Objects.requireNonNull(extendClass));
        // 添加继承关系到innerClass的extends列表中
        Objects.requireNonNull(aClass.getImplementsList()).add(superClassReference);
    }


    /**
     * 转换为包装类型
     */
    private static PsiType toWrapperPsiType(PsiType type, Project project, PsiClass psiClass) {
        if (type.equals(PsiTypes.nullType()) || type.equals(PsiTypes.voidType())) {
            return PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope());
        } else if (type instanceof PsiPrimitiveType primitiveType) {
            return PsiType.getTypeByName(Objects.requireNonNull(primitiveType.getBoxedTypeName()), project, psiClass.getResolveScope());
        }
        return type;
    }

    /**
     * 替换变量类型
     */
    public static void changeVariableType(Project project, PsiVariable psiVariable, PsiClass newClass) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiTypeElement variableTypeElement = psiVariable.getTypeElement();
            PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
            if (variableTypeElement != null) {
                PsiClassType newType = classToType(project, newClass);
                PsiTypeElement newTypeElement = factory.createTypeElement(newType);
                variableTypeElement.replace(newTypeElement);
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiVariable);
            }
            if (checkVariableIsNew(psiVariable)) {
                PsiNewExpression newConstructorCall = (PsiNewExpression) factory.createExpressionFromText("new " + newClass.getQualifiedName() + "()", null);
                Objects.requireNonNull(psiVariable.getInitializer()).replace(newConstructorCall);
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiVariable);
            }
        });
    }

    /**
     * 检测变量是否是new构造的
     */
    public static boolean checkVariableIsNew(PsiVariable localVariable) {
        PsiExpression initializer = localVariable.getInitializer();
        return initializer instanceof PsiNewExpression;
    }

    /**
     * 类to类型
     */
    public static PsiClassType classToType(Project project, PsiClass innerClass) {
        return JavaPsiFacade.getInstance(project).getElementFactory().createType(innerClass);
    }

    /**
     * 替换变量方法调用为新Bean.set调用
     */
    public static void replaceVariableMethodWithSetCall(Project project, List<PsiMethodCallExpression> putCalls, PsiVariable psiVariable) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (PsiMethodCallExpression putCall : putCalls) {
                PsiExpression[] arguments = putCall.getArgumentList().getExpressions();
                if (arguments.length == 2 && arguments[0] instanceof PsiLiteralExpression && arguments[1] != null) {
                    String key = Objects.requireNonNull(((PsiLiteralExpression) arguments[0]).getValue()).toString();
                    String fieldName = CamelCase.toCapitalizeCamelCase(key);
                    String setterMethodCall = psiVariable.getName() + ".set" + fieldName + "(" + arguments[1].getText() + ")";
                    PsiExpression setterMethodCallExpression = JavaPsiFacade.getElementFactory(putCall.getProject())
                            .createExpressionFromText(setterMethodCall, putCall);
                    putCall.replace(setterMethodCallExpression);
                }
            }
        });
    }

    /**
     * 替代方法返回类型(包括父类)
     */
    public static void replaceMethodReturnType(Project project, PsiMethod method, PsiType psiType) {
        // 修改方法的返回类型
        PsiTypeElement returnTypeElement = method.getReturnTypeElement();
        if (returnTypeElement != null) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiTypeElement newTypeElement = JavaPsiFacade.getElementFactory(method.getProject()).createTypeElement(psiType);
                returnTypeElement.replace(newTypeElement);
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(Objects.requireNonNull(method.getContainingClass()));
            });
        }

        PsiMethod[] superMethods = method.findSuperMethods();
        for (PsiMethod superMethod : superMethods) {
            replaceMethodReturnType(project, superMethod, psiType);
        }
    }

    /**
     * 从Dao类获取sql文件
     */
    public static PsiFile getSqlFileFromDaoClass(PsiClass psiClass) {
        String sqlFileName = psiClass.getName() + ".sql";
        Module module = ModuleUtil.findModuleForPsiElement(psiClass);
        if (module != null) {
            VirtualFile[] sourcesRoots = ModuleRootManager.getInstance(module).getSourceRoots(false);
            for (VirtualFile root : sourcesRoots) {
                VirtualFile resourcesRoot = root.findFileByRelativePath("dao-sql");
                if (resourcesRoot != null) {
                    PsiDirectory resourcesDir = PsiManager.getInstance(psiClass.getProject()).findDirectory(resourcesRoot);
                    if (resourcesDir != null) {
                        PsiFile sqlFile = resourcesDir.findFile(sqlFileName);
                        if (sqlFile != null) {
                            return sqlFile;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 在sql文件找到对应的sql方法
     */
    public static PsiElement findSqlMethodInSqlFile(PsiFile sqlFile, String methodName) {
        String fileText = sqlFile.getText();
        Pattern pattern = Pattern.compile("<@" + methodName + ">");
        Matcher matcher = pattern.matcher(fileText);
        if (matcher.find()) {
            int startOffset = matcher.start();
            return sqlFile.getViewProvider().findElementAt(startOffset, sqlFile.getLanguage());
        }
        return null;
    }

    /**
     * 得到Class-源类型（如果不为Class，则返回原始参数）
     */
    public static PsiType getClassOriginType(PsiType classType, @NotNull Project project) {
        String canonicalText = classType.getCanonicalText();
        if (canonicalText.length() > CLASS_NAME.length() + 1 && !isNotClassType(classType)) {
            String typeName = canonicalText.substring(CLASS_NAME.length() + 1, canonicalText.length() - 1);
            return PsiType.getTypeByName(typeName, project, GlobalSearchScope.allScope(project));
        }
        return classType;
    }

    /**
     * 得到类第一个泛型类型
     */
    public static PsiType getFirstGenericTypeOfClass(PsiType psiType) {
        if (psiType instanceof PsiClassType classType) {
            List<PsiType> typeParameters = List.of(classType.getParameters());
            if (!typeParameters.isEmpty()) {
                return typeParameters.get(0);
            }
        }
        return null;
    }

    /**
     * 得到BaseDao第一个泛型类型
     */
    public static PsiClass getFirstGenericTypeOfBaseDao(PsiClass psiClass) {
        PsiClassType[] superClassTypes = psiClass.getSuperTypes();

        for (PsiClassType superClassType : superClassTypes) {
            PsiClass superClass = superClassType.resolve();
            if (superClass != null && BASE_DAO_NAME.equals(superClass.getQualifiedName())) {
                return PsiUtil.resolveClassInType(getFirstGenericTypeOfClass(superClassType));
            }
        }
        return null;
    }

    /**
     * 替换变量的所有get方法
     */
    public static void replaceVariableMethodWithGetCall(Project project, List<PsiMethodCallExpression> callExpressions, PsiVariable psiVariable) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (PsiMethodCallExpression callExpression : callExpressions) {
                PsiExpression[] arguments = callExpression.getArgumentList().getExpressions();
                if (arguments.length == 1 && arguments[0] instanceof PsiLiteralExpression) {
                    String key = Objects.requireNonNull(((PsiLiteralExpression) arguments[0]).getValue()).toString();
                    String fieldName = CamelCase.toCapitalizeCamelCase(key);
                    String getterMethodCall = psiVariable.getName() + ".get" + fieldName + "()";
                    PsiExpression setterMethodCallExpression = JavaPsiFacade.getElementFactory(callExpression.getProject())
                            .createExpressionFromText(getterMethodCall, callExpression);
                    callExpression.replace(setterMethodCallExpression);
                }
            }
        });
    }

    /**
     * 是否是BaseEntity
     */
    public static boolean isBaseEntity(PsiType callerType) {
        PsiClass psiClass = PsiUtil.resolveClassInType(callerType);
        if (psiClass == null) {
            return false;
        }
        PsiClass[] interfaces = psiClass.getInterfaces();
        return Arrays.stream(interfaces).anyMatch(i -> BASE_ENTITY_NAME.equals(i.getQualifiedName()));
    }

    /**
     * 得到所有字段,过滤static和final
     */
    public static List<PsiField> getAllFieldsFilterStaticAndFinal(PsiClass sourceClass) {
        return Arrays.stream(sourceClass.getAllFields()).filter(e -> {
            PsiModifierList modifierList = e.getModifierList();
            if (modifierList == null) {
                return false;
            }
            return !modifierList.hasExplicitModifier(PsiModifier.STATIC) && !modifierList.hasExplicitModifier(PsiModifier.FINAL) && hasSetter(e);
        }).toList();
    }

    /**
     * 有setter
     *
     * @param psiField psi字段
     * @return boolean
     */
    static boolean hasSetter(PsiField psiField) {
        PsiClass containingClass = psiField.getContainingClass();
        if (containingClass == null) {
            return false;
        }

        String setterName = "set" + Texts.capitalize(psiField.getName());
        PsiType fieldType = psiField.getType();

        for (PsiMethod method : containingClass.getMethods()) {
            // 检查方法名
            if (!setterName.equals(method.getName())) {
                continue;
            }

            // 检查方法参数
            PsiParameter[] parameters = method.getParameterList().getParameters();
            if (parameters.length != 1 || !parameters[0].getType().equals(fieldType)) {
                continue;
            }

            // 如果满足所有条件，则找到了setter方法
            return true;
        }

        return false;
    }

    /**
     * 获得类中此字段（包括下划线、驼峰格式）
     */
    public static PsiField getFieldFromClass(String fieldName, PsiClass psiClass) {
        PsiField targetField = psiClass.findFieldByName(fieldName, true);
        if (targetField == null) {
            targetField = psiClass.findFieldByName(CamelCase.toCamelCase(fieldName), true);
            if (targetField == null) {
                targetField = psiClass.findFieldByName(CamelCase.toUnderlineName(fieldName), true);
            }
        }
        return targetField;
    }

    /**
     * 修改方法返回类型(如果方法返回值类型为Map)
     *
     * @param innerClass 要修改为的类型
     * @param method     方法
     */
    public static boolean modifyMethodReturnTypeIfMap(PsiClass innerClass, PsiMethod method) {
        Project project = method.getProject();
        PsiType returnType = method.getReturnType();
        boolean listMapType = isListMapType(Objects.requireNonNull(returnType));
        boolean pageableMapType = isPageableMapType(returnType);
        boolean mapType = isMapType(returnType);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        // 如果返回值是Map，则修改为DO
        if (mapType || listMapType || pageableMapType) {
            // 创建DO类型
            PsiType newReturnType;
            PsiClassType boType = PsiTypesUtil.getClassType(innerClass);
            if (mapType) {
                newReturnType = boType;
            } else if (listMapType) {
                newReturnType = elementFactory.createType(Objects.requireNonNull(javaPsiFacade.findClass(LIST_NAME, GlobalSearchScope.allScope(project))),
                        new PsiType[]{boType});
            } else {
                newReturnType = elementFactory.createType(Objects.requireNonNull(javaPsiFacade.findClass(PAGEABLE_NAME, GlobalSearchScope.allScope(project))),
                        new PsiType[]{boType});
            }
            PsiTypeElement returnTypeElement = method.getReturnTypeElement();
            // 更新方法的返回类型
            if (returnTypeElement != null) {
                WriteCommandAction.runWriteCommandAction(project, () -> {

                    PsiTypeElement newReturnTypeElement = elementFactory.createTypeElement(newReturnType);
                    returnTypeElement.replace(newReturnTypeElement);
                    JavaCodeStyleManager.getInstance(project).shortenClassReferences(Objects.requireNonNull(method.getContainingClass()));
                });
                return true;
            }
        }
        return false;
    }


    /**
     * 找到接口方法
     */
    public static PsiMethod findInterfaceMethod(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return null;
        }

        for (PsiClass superClass : containingClass.getSupers()) {
            if (superClass.isInterface()) {
                MethodSignature signature = method.getSignature(PsiSubstitutor.EMPTY);
                PsiMethod interfaceMethod = MethodSignatureUtil.findMethodBySignature(superClass, signature, false);
                if (interfaceMethod != null) {
                    return interfaceMethod;
                }
            }
        }

        return null;
    }

    /**
     * 包装为List泛型
     *
     * @param qualifiedName 限定名
     * @return {@link String}
     */
    public static String wrapToList(String qualifiedName) {
        return LIST_NAME + "<" + qualifiedName + ">";
    }

    /**
     * 是否是jpa方法
     */
    public static boolean isJpaMethod(String methodName) {
        return DaoSqlRegistry.isJpaMethod(methodName);
    }

    /**
     * 从mechod得到变量调用
     *
     * @param methodCallExpression 方法调用表达式
     * @return {@link PsiVariable}
     */
    public static PsiVariable getVariableFromMechodCall(PsiMethodCallExpression methodCallExpression) {
        PsiExpression callerExpression = methodCallExpression.getMethodExpression().getQualifierExpression();

        if (callerExpression != null) {
            if (callerExpression instanceof PsiReferenceExpression) {
                PsiElement resolvedElement = ((PsiReferenceExpression) callerExpression).resolve();
                if (resolvedElement instanceof PsiVariable) {
                    return (PsiVariable) resolvedElement;
                }
            }
        }
        return null;
    }

    /**
     * 添加字段到指定类
     *
     * @param psiClass psi类
     * @param column   列
     */
    public static PsiField addFieldToClass(PsiClass psiClass, Column column) {
        Project project = psiClass.getProject();
        PsiField psiField = getFieldFromClass(column.getField(), psiClass);
        if (psiField == null) {
            final PsiField[] fields = new PsiField[1];
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                fields[0] = elementFactory.createField(column.getField(), toWrapperPsiType(column.getPsiType(), project, psiClass));
                psiClass.add(fields[0]);
            });
            psiField = fields[0];
        }
        return psiField;
    }

    /**
     * 是paimitive类型
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean isPaimitiveType(@Nullable PsiType type) {
        if (Objects.requireNonNull(type).equalsToText("java.lang.Integer")) {
            return true;
        } else if (type.equalsToText("java.lang.Long")) {
            return true;
        } else if (type.equalsToText("java.lang.Float")) {
            return true;
        } else if (type.equalsToText("java.lang.Double")) {
            return true;
        } else if (type.equalsToText("java.lang.Boolean")) {
            return true;
        } else if (type.equalsToText("java.lang.Character")) {
            return true;
        } else if (type.equalsToText("java.lang.Short")) {
            return true;
        } else if (type.equalsToText("java.lang.Byte")) {
            return true;
        } else if (type.equalsToText("java.lang.String")) {
            return true;
        }
        return type instanceof PsiPrimitiveType;
    }

    /**
     * 第一个小写字符
     *
     * @param input 输入
     * @return {@link String}
     */
    public static String firstCharToLowerCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

}
