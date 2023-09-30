/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.treblereel.j2cl.processors.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.j2cl.common.InternalCompilerError;
import com.google.j2cl.transpiler.ast.ArrayTypeDescriptor;
import com.google.j2cl.transpiler.ast.AstUtils;
import com.google.j2cl.transpiler.ast.DeclaredTypeDescriptor;
import com.google.j2cl.transpiler.ast.FieldDescriptor;
import com.google.j2cl.transpiler.ast.JsEnumInfo;
import com.google.j2cl.transpiler.ast.JsInfo;
import com.google.j2cl.transpiler.ast.JsMemberType;
import com.google.j2cl.transpiler.ast.Kind;
import com.google.j2cl.transpiler.ast.MemberDescriptor;
import com.google.j2cl.transpiler.ast.MethodDescriptor;
import com.google.j2cl.transpiler.ast.MethodDescriptor.ParameterDescriptor;
import com.google.j2cl.transpiler.ast.PrimitiveTypes;
import com.google.j2cl.transpiler.ast.TypeDeclaration;
import com.google.j2cl.transpiler.ast.TypeDescriptor;
import com.google.j2cl.transpiler.ast.TypeDescriptors;
import com.google.j2cl.transpiler.ast.TypeVariable;
import com.google.j2cl.transpiler.ast.Visibility;
import com.google.j2cl.transpiler.frontend.javac.JsInteropAnnotationUtils;
import com.google.j2cl.transpiler.frontend.javac.JsInteropUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import jsinterop.annotations.JsPackage;

/**
 * Utility functions to interact with JavaC internal representations.
 *
 * <p>it's taken from J2CL project
 */
public class J2CLUtils {
  private final Types types;
  private final Elements elements;

  private final ProcessingEnvironment processingEnv;

  public J2CLUtils(ProcessingEnvironment processingEnv) {
    this.types = processingEnv.getTypeUtils();
    this.elements = processingEnv.getElementUtils();
    this.processingEnv = processingEnv;
  }

  public static boolean hasJsMemberAnnotation(ExecutableElement method) {
    return JsInteropAnnotationUtils.getJsMethodAnnotation(method) != null
        || JsInteropAnnotationUtils.getJsPropertyAnnotation(method) != null
        || JsInteropAnnotationUtils.getJsConstructorAnnotation(method) != null;
  }

  public static boolean isAnonymous(TypeMirror type) {
    if (type.getKind().equals(TypeKind.DECLARED)) {
      DeclaredType declaredType = (DeclaredType) type;
      TypeElement typeElem = (TypeElement) declaredType.asElement();
      if (typeElem.getNestingKind().equals(NestingKind.ANONYMOUS)) {
        return true;
      }
    }
    return false;
  }

  private static List<TypeParameterElement> getTypeParameters(TypeElement typeElement) {
    List<TypeParameterElement> typeParameterElements =
        new ArrayList<>(typeElement.getTypeParameters());
    Element currentElement = typeElement;
    Element enclosingElement = typeElement.getEnclosingElement();
    while (enclosingElement != null) {
      if (isStatic(currentElement)) {
        break;
      }

      if (enclosingElement.getKind() != ElementKind.STATIC_INIT
          && enclosingElement.getKind() != ElementKind.INSTANCE_INIT
          && enclosingElement instanceof Parameterizable) {
        // Add the enclosing element type variables, skip STATIC_INIT and INSTANCE_INIT since they
        // never define type variables, and throw NPE if getTypeParameters is called on them.
        typeParameterElements.addAll(((Parameterizable) enclosingElement).getTypeParameters());
      }
      currentElement = enclosingElement;
      enclosingElement = enclosingElement.getEnclosingElement();
    }
    return typeParameterElements;
  }

  public static TypeElement getEnclosingType(Element typeElement) {
    Element enclosing = typeElement.getEnclosingElement();
    while (enclosing != null && !(enclosing instanceof TypeElement)) {
      enclosing = enclosing.getEnclosingElement();
    }
    return (TypeElement) enclosing;
  }

  public static String getJsName(final TypeElement classSymbol) {
    return JsInteropAnnotationUtils.getJsName(classSymbol);
  }

  public static boolean isLocal(TypeElement typeElement) {
    return typeElement.getNestingKind() == NestingKind.LOCAL;
  }

  public static boolean isStatic(Element element) {
    return element.getModifiers().contains(Modifier.STATIC);
  }

  public static boolean isNative(Element element) {
    return element.getModifiers().contains(Modifier.NATIVE);
  }

  public static Visibility getVisibility(Element element) {
    if (element.getModifiers().contains(Modifier.PUBLIC)) {
      return Visibility.PUBLIC;
    } else if (element.getModifiers().contains(Modifier.PROTECTED)) {
      return Visibility.PROTECTED;
    } else if (element.getModifiers().contains(Modifier.PRIVATE)) {
      return Visibility.PRIVATE;
    } else {
      return Visibility.PACKAGE_PRIVATE;
    }
  }

  public static String getJsNamespace(TypeElement classSymbol) {
    String jsNamespace = JsInteropAnnotationUtils.getJsNamespace(classSymbol);
    if (jsNamespace != null) {
      return jsNamespace;
    }

    // Maybe namespace is set via package-info file?
    boolean isTopLevelType = classSymbol.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    if (isTopLevelType) {
      JsPackage jsPackage = classSymbol.getEnclosingElement().getAnnotation(JsPackage.class);
      return jsPackage != null ? jsPackage.namespace() : null;
    }
    return null;
  }

  private static List<TypeMirror> getTypeArguments(DeclaredType declaredType) {
    List<TypeMirror> typeArguments = new ArrayList<>();
    DeclaredType currentType = declaredType;
    do {
      typeArguments.addAll(currentType.getTypeArguments());
      Element enclosingElement = currentType.asElement().getEnclosingElement();
      if (enclosingElement.getKind() == ElementKind.METHOD
          || enclosingElement.getKind() == ElementKind.CONSTRUCTOR) {
        typeArguments.addAll(
            ((Parameterizable) enclosingElement)
                .getTypeParameters().stream().map(Element::asType).collect(toImmutableList()));
      }
      currentType =
          currentType.getEnclosingType() instanceof DeclaredType
              ? (DeclaredType) currentType.getEnclosingType()
              : null;
    } while (currentType != null);
    return typeArguments;
  }

  private static DeclaredTypeDescriptor withNullability(
      DeclaredTypeDescriptor typeDescriptor, boolean nullable) {
    return nullable ? typeDescriptor.toNullable() : typeDescriptor.toNonNullable();
  }

  private static boolean isNonNullAnnotation(AnnotationMirror annotation) {
    DeclaredType annotationType = annotation.getAnnotationType();
    String name = annotationType.asElement().getSimpleName().toString();
    return name.equals("NonNull") || name.equals("JsNonNull");
  }

  private static boolean isNullableAnnotation(AnnotationMirror annotation) {
    DeclaredType annotationType = annotation.getAnnotationType();
    return annotationType.asElement().getSimpleName().toString().equals("Nullable");
  }

  public static boolean isStatic(VariableElement variableElement) {
    return variableElement.getModifiers().contains(Modifier.STATIC);
  }

  public static boolean isFinal(Element declarationMethodElement) {
    return declarationMethodElement.getModifiers().contains(Modifier.FINAL);
  }

  public static boolean isInterface(TypeElement typeElement) {
    return typeElement.getKind().isInterface();
  }

  public static boolean isInterface(TypeMirror typeElement) {
    return isInterface(MoreTypes.asTypeElement(typeElement));
  }

  public static boolean isDeprecated(AnnotatedConstruct binding) {
    return hasAnnotation(binding, Deprecated.class.getName());
  }

  public static boolean hasAnnotation(AnnotatedConstruct construct, String annotationSourceName) {
    return findAnnotationBindingByName(construct.getAnnotationMirrors(), annotationSourceName)
        != null;
  }

  public static AnnotationMirror findAnnotationBindingByName(
      List<? extends AnnotationMirror> annotations, String name) {
    if (annotations == null) {
      return null;
    }
    for (AnnotationMirror annotationBinding : annotations) {
      if (((TypeElement) annotationBinding.getAnnotationType().asElement())
          .getQualifiedName()
          .contentEquals(name)) {
        return annotationBinding;
      }
    }
    return null;
  }

  public static boolean isDefaultMethod(Element element) {
    return element.getModifiers().contains(Modifier.DEFAULT);
  }

  public static boolean isClass(TypeElement typeElement) {
    return typeElement.getKind().isClass();
  }

  public static boolean isEnum(TypeElement typeElement) {
    return typeElement.getKind().equals(ElementKind.ENUM);
  }

  public static boolean isAbstract(Element element) {
    return element.getModifiers().contains(Modifier.ABSTRACT);
  }

  public static boolean isJsEnum(TypeElement typeElement) {
    return JsInteropUtils.isJsEnum(typeElement);
  }

  public TypeDeclaration createDeclarationForType(final TypeElement typeElement) {
    if (typeElement == null) {
      return null;
    }

    boolean isFromSource = false;
    // Compute these first since they're reused in other calculations.
    String packageName = getPackageOf(typeElement).getQualifiedName().toString();
    boolean isAbstract = isAbstract(typeElement) && !isInterface(typeElement);
    boolean isFinal = isFinal(typeElement);

    Supplier<ImmutableList<MethodDescriptor>> declaredMethods =
        () -> {
          ImmutableList.Builder<MethodDescriptor> listBuilder = ImmutableList.builder();
          for (ExecutableElement methodElement :
              ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            MethodDescriptor methodDescriptor = createDeclarationMethodDescriptor(methodElement);
            listBuilder.add(methodDescriptor);
          }
          return listBuilder.build();
        };

    Supplier<ImmutableList<FieldDescriptor>> declaredFields =
        () ->
            typeElement.getEnclosedElements().stream()
                .filter(
                    element ->
                        element.getKind() == ElementKind.FIELD
                            || element.getKind() == ElementKind.ENUM_CONSTANT)
                .map(VariableElement.class::cast)
                .map(this::createFieldDescriptor)
                .collect(toImmutableList());

    JsEnumInfo jsEnumInfo = JsInteropUtils.getJsEnumInfo(typeElement);

    List<TypeParameterElement> typeParameterElements = getTypeParameters(typeElement);

    boolean isNullMarked = isNullMarked(typeElement);
    return TypeDeclaration.newBuilder()
        .setClassComponents(getClassComponents(typeElement))
        .setEnclosingTypeDeclaration(createDeclarationForType(getEnclosingType(typeElement)))
        .setInterfaceTypeDescriptorsFactory(
            () ->
                createTypeDescriptors(
                    typeElement.getInterfaces(),
                    isNullMarked,
                    DeclaredTypeDescriptor.class,
                    typeElement))
        .setUnparameterizedTypeDescriptorFactory(
            () -> createDeclaredTypeDescriptor(typeElement.asType()))
        .setHasAbstractModifier(isAbstract)
        .setKind(getKindFromTypeBinding(typeElement))
        .setCapturingEnclosingInstance(capturesEnclosingInstance(typeElement))
        .setFinal(isFinal)
        .setFunctionalInterface(isFunctionalInterface(typeElement.asType()))
        .setJsFunctionInterface(JsInteropUtils.isJsFunction(typeElement))
        .setJsType(JsInteropUtils.isJsType(typeElement))
        .setJsEnumInfo(jsEnumInfo)
        .setNative(JsInteropUtils.isJsNativeType(typeElement))
        .setAnonymous(isAnonymous(typeElement))
        .setLocal(isLocal(typeElement))
        .setSimpleJsName(getJsName(typeElement))
        .setCustomizedJsNamespace(getJsNamespace(typeElement))
        .setNullMarked(isNullMarked)
        .setPackageName(packageName)
        .setSuperTypeDescriptorFactory(
            () ->
                (DeclaredTypeDescriptor)
                    applyNullabilityAnnotations(
                        createDeclaredTypeDescriptor(typeElement.getSuperclass(), isNullMarked),
                        typeElement,
                        (obj) -> false))
        .setTypeParameterDescriptors(
            typeParameterElements.stream()
                .map(TypeParameterElement::asType)
                .map(javax.lang.model.type.TypeVariable.class::cast)
                .map(this::createTypeVariable)
                .collect(toImmutableList()))
        .setVisibility(getVisibility(typeElement))
        .setDeclaredMethodDescriptorsFactory(declaredMethods)
        .setDeclaredFieldDescriptorsFactory(declaredFields)
        .setUnusableByJsSuppressed(JsInteropAnnotationUtils.isUnusableByJsSuppressed(typeElement))
        .setDeprecated(isDeprecated(typeElement))
        .build();
  }

  public MethodDescriptor createDeclarationMethodDescriptor(ExecutableElement methodElement) {
    DeclaredTypeDescriptor enclosingTypeDescriptor =
        createDeclaredTypeDescriptor(methodElement.getEnclosingElement().asType());
    return createDeclarationMethodDescriptor(methodElement, enclosingTypeDescriptor);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility methods to process nullability annotations on classes that are compiled separately.
  // Javac does not present TYPE_USE annotation in the returned type instances.
  private static TypeDescriptor applyParameterNullabilityAnnotations(
      TypeDescriptor typeDescriptor, ExecutableElement declarationMethodElement, int index) {
    return applyNullabilityAnnotations(typeDescriptor, declarationMethodElement, (obj) -> false);
    // position ->
    //    position.parameter_index == index
    //        && position.type == TargetType.METHOD_FORMAL_PARAMETER);
  }

  private static TypeDescriptor applyNullabilityAnnotations(
      TypeDescriptor typeDescriptor,
      Element declarationMethodElement,
      Predicate<Object> positionSelector) {
    return typeDescriptor;
  }

  public boolean isSameType(TypeMirror asType, TypeMirror type) {
    return types.isSameType(asType, type);
  }

  public PackageElement getPackageOf(TypeElement typeElement) {
    return elements.getPackageOf(typeElement);
  }

  /** Create a MethodDescriptor directly based on the given JavaC ExecutableElement. */
  public MethodDescriptor createDeclarationMethodDescriptor(
      ExecutableElement methodElement, DeclaredTypeDescriptor enclosingTypeDescriptor) {
    return createMethodDescriptor(enclosingTypeDescriptor, methodElement, methodElement);
  }

  MethodDescriptor createMethodDescriptor(
      DeclaredTypeDescriptor enclosingTypeDescriptor,
      ExecutableElement methodElement,
      ExecutableElement declarationMethodElement) {

    MethodDescriptor declarationMethodDescriptor = null;

    ImmutableList<TypeMirror> parameters =
        methodElement.getParameters().stream()
            .map(VariableElement::asType)
            .collect(toImmutableList());

    TypeMirror returnType = methodElement.getReturnType();
    if (isSpecialized(declarationMethodElement, parameters, returnType)) {
      declarationMethodDescriptor =
          createDeclarationMethodDescriptor(
              declarationMethodElement, enclosingTypeDescriptor.toUnparameterizedTypeDescriptor());
    }

    TypeDescriptor returnTypeDescriptor =
        createTypeDescriptorWithNullability(
            returnType, declarationMethodElement.getAnnotationMirrors(), false);

    ImmutableList.Builder<TypeDescriptor> parametersBuilder = ImmutableList.builder();
    for (int i = 0; i < parameters.size(); i++) {
      parametersBuilder.add(
          applyParameterNullabilityAnnotations(
              createTypeDescriptorWithNullability(
                  parameters.get(i),
                  declarationMethodElement.getParameters().get(i).getAnnotationMirrors(),
                  false),
              declarationMethodElement,
              i));
    }

    return createDeclaredMethodDescriptor(
        enclosingTypeDescriptor.toNullable(),
        declarationMethodElement,
        declarationMethodDescriptor,
        parametersBuilder.build(),
        returnTypeDescriptor);
  }

  public DeclaredTypeDescriptor createDeclaredTypeDescriptor(TypeMirror typeMirror) {
    return createDeclaredTypeDescriptor(typeMirror, /* inNullMarkedScope= */ false);
  }

  DeclaredTypeDescriptor createDeclaredTypeDescriptor(
      TypeMirror typeMirror, boolean inNullMarkedScope) {
    return createTypeDescriptor(typeMirror, inNullMarkedScope, DeclaredTypeDescriptor.class);
  }

  <T extends TypeDescriptor> T createTypeDescriptor(
      TypeMirror typeMirror, boolean inNullMarkedScope, Class<T> clazz) {
    return clazz.cast(createTypeDescriptor(typeMirror, inNullMarkedScope));
  }

  public boolean hasOuterInstance(TypeElement typeElement) {
    return typeElement.getEnclosingElement().getKind().isClass()
        && !isInterface((TypeElement) typeElement.getEnclosingElement());
  }

  public boolean isAnonymous(TypeElement typeElement) {
    return isAnonymous(typeElement.asType());
  }

  private boolean isFunctionalInterface(TypeMirror type) {
    return false;
  }

  public FieldDescriptor createFieldDescriptor(VariableElement variableElement) {
    return createFieldDescriptor(variableElement, variableElement.asType());
  }

  public FieldDescriptor createFieldDescriptor(VariableElement variableElement, TypeMirror type) {

    boolean isStatic = isStatic(variableElement);
    Visibility visibility = getVisibility(variableElement);
    DeclaredTypeDescriptor enclosingTypeDescriptor =
        createDeclaredTypeDescriptor(getEnclosingType(variableElement).asType());
    String fieldName = variableElement.getSimpleName().toString();

    TypeDescriptor thisTypeDescriptor =
        createTypeDescriptorWithNullability(
            type,
            variableElement.getAnnotationMirrors(),
            isNullMarked(getEnclosingType(variableElement)));

    boolean isEnumConstant = variableElement.getKind().equals(ElementKind.ENUM_CONSTANT);
    if (isEnumConstant) {
      // Enum fields are always non-nullable.
      thisTypeDescriptor = thisTypeDescriptor.toNonNullable();
    }

    FieldDescriptor declarationFieldDescriptor = null;
    if (!isSameType(variableElement.asType(), type)) {
      // Field references might be parameterized, and when they are we set the declaration
      // descriptor to the unparameterized declaration.
      declarationFieldDescriptor = createFieldDescriptor(variableElement, variableElement.asType());
    }

    JsInfo jsInfo = JsInteropUtils.getJsInfo(variableElement);
    boolean isCompileTimeConstant = variableElement.getConstantValue() != null;
    boolean isFinal = isFinal(variableElement);
    return FieldDescriptor.newBuilder()
        .setEnclosingTypeDescriptor(enclosingTypeDescriptor)
        .setName(fieldName)
        .setTypeDescriptor(thisTypeDescriptor)
        .setStatic(isStatic)
        .setVisibility(visibility)
        .setJsInfo(jsInfo)
        .setFinal(isFinal)
        .setCompileTimeConstant(isCompileTimeConstant)
        .setDeclarationDescriptor(declarationFieldDescriptor)
        .setEnumConstant(isEnumConstant)
        .setUnusableByJsSuppressed(
            JsInteropAnnotationUtils.isUnusableByJsSuppressed(variableElement))
        .setDeprecated(isDeprecated(variableElement))
        .build();
  }

  public TypeDescriptor createTypeDescriptor(TypeMirror typeMirror) {
    return createTypeDescriptor(typeMirror, /* inNullMarkedScope= */ false);
  }

  public Element asElement(TypeMirror typeMirror) {
    if (typeMirror.getKind().isPrimitive()) {
      return MoreTypes.asElement(typeMirror);
    }
    if (typeMirror.getKind().equals(TypeKind.DECLARED)) {
      return MoreTypes.asDeclared(typeMirror).asElement();
    }
    return types.asElement(typeMirror);
  }

  public TypeMirror erasure(TypeMirror typeMirror) {
    return types.erasure(typeMirror);
  }

  public String getBinaryNameFromTypeBinding(TypeElement typeElement) {
    return elements.getBinaryName(typeElement).toString();
  }

  /** Creates a TypeDescriptor from a TypeMirror. */
  TypeDescriptor createTypeDescriptor(TypeMirror typeMirror, boolean inNullMarkedScope) {
    return createTypeDescriptorWithNullability(typeMirror, ImmutableList.of(), inNullMarkedScope);
  }

  private MethodDescriptor createDeclaredMethodDescriptor(
      DeclaredTypeDescriptor enclosingTypeDescriptor,
      ExecutableElement declarationMethodElement,
      MethodDescriptor declarationMethodDescriptor,
      List<TypeDescriptor> parameters,
      TypeDescriptor returnTypeDescriptor) {
    ImmutableList<TypeVariable> typeParameterTypeDescriptors =
        declarationMethodElement.getTypeParameters().stream()
            .map(Element::asType)
            .map(this::createTypeDescriptor)
            .map(TypeVariable.class::cast)
            .collect(toImmutableList());

    boolean isStatic = isStatic(declarationMethodElement);
    Visibility visibility = getVisibility(declarationMethodElement);
    boolean isDefault = isDefaultMethod(declarationMethodElement);
    JsInfo jsInfo = computeJsInfo(declarationMethodElement);

    boolean isNative =
        isNative(declarationMethodElement)
            || (!jsInfo.isJsOverlay()
                && enclosingTypeDescriptor.isNative()
                && isAbstract(declarationMethodElement));

    boolean isConstructor = declarationMethodElement.getKind() == ElementKind.CONSTRUCTOR;
    String methodName = declarationMethodElement.getSimpleName().toString();

    ImmutableList.Builder<ParameterDescriptor> parameterDescriptorBuilder = ImmutableList.builder();
    for (int i = 0; i < parameters.size(); i++) {
      parameterDescriptorBuilder.add(
          ParameterDescriptor.newBuilder()
              .setTypeDescriptor(parameters.get(i))
              .setJsOptional(JsInteropUtils.isJsOptional(declarationMethodElement, i))
              .setVarargs(i == parameters.size() - 1 && declarationMethodElement.isVarArgs())
              .setDoNotAutobox(JsInteropUtils.isDoNotAutobox(declarationMethodElement, i))
              .build());
    }

    if (enclosingTypeDescriptor.getTypeDeclaration().isAnonymous()
        && isConstructor
        && enclosingTypeDescriptor.getSuperTypeDescriptor().hasJsConstructor()) {
      jsInfo = JsInfo.Builder.from(jsInfo).setJsMemberType(JsMemberType.CONSTRUCTOR).build();
    }

    boolean hasUncheckedCast = hasUncheckedCastAnnotation(declarationMethodElement);
    return MethodDescriptor.newBuilder()
        .setEnclosingTypeDescriptor(enclosingTypeDescriptor)
        .setName(isConstructor ? null : methodName)
        .setParameterDescriptors(parameterDescriptorBuilder.build())
        .setDeclarationDescriptor(declarationMethodDescriptor)
        .setReturnTypeDescriptor(isConstructor ? enclosingTypeDescriptor : returnTypeDescriptor)
        .setTypeParameterTypeDescriptors(typeParameterTypeDescriptors)
        .setJsInfo(jsInfo)
        .setJsFunction(isOrOverridesJsFunctionMethod(declarationMethodElement))
        .setVisibility(visibility)
        .setStatic(isStatic)
        .setConstructor(isConstructor)
        .setNative(isNative)
        .setFinal(isFinal(declarationMethodElement))
        .setDefaultMethod(isDefault)
        .setAbstract(isAbstract(declarationMethodElement))
        // .setSynthetic(isSynthetic(declarationMethodElement))
        // .setEnumSyntheticMethod(isEnumSyntheticMethod(declarationMethodElement))
        // .setSideEffectFree(isAnnotatedWithHasNoSideEffects(declarationMethodElement))
        .setUnusableByJsSuppressed(
            JsInteropAnnotationUtils.isUnusableByJsSuppressed(declarationMethodElement))
        .setDeprecated(isDeprecated(declarationMethodElement))
        .setUncheckedCast(hasUncheckedCast)
        .build();
  }

  private boolean hasUncheckedCastAnnotation(Element element) {
    return hasAnnotation(element, "javaemul.internal.annotations.UncheckedCast");
  }

  private boolean isOrOverridesJsFunctionMethod(ExecutableElement methodBinding) {
    Element declaringType = methodBinding.getEnclosingElement();
    if (JsInteropUtils.isJsFunction(declaringType)) {
      throw new RuntimeException(" not implemented");
    }
    return false;
  }

  private JsInfo computeJsInfo(ExecutableElement method) {
    JsInfo originalJsInfo = JsInteropUtils.getJsInfo(method);
    if (originalJsInfo.isJsOverlay()
        || originalJsInfo.getJsName() != null
        || originalJsInfo.getJsNamespace() != null) {
      // Do not examine overridden methods if the method is marked as JsOverlay or it has a JsMember
      // annotation that customizes the name.
      return originalJsInfo;
    }
    // Don't inherit @JsAsync annotation from overridden methods.
    return JsInfo.Builder.from(originalJsInfo).setJsAsync(originalJsInfo.isJsAsync()).build();
  }

  private boolean isSpecialized(
      ExecutableElement declarationMethodElement,
      List<? extends TypeMirror> parameters,
      TypeMirror returnType) {
    return !isSameType(returnType, declarationMethodElement.getReturnType())
        || !Streams.zip(
                parameters.stream(),
                declarationMethodElement.getParameters().stream(),
                (thisType, thatType) -> isSameType(thisType, thatType.asType()))
            .allMatch(equals -> equals);
  }

  private ImmutableList<TypeDescriptor> createTypeDescriptors(
      List<? extends TypeMirror> typeMirrors, boolean inNullMarkedScope) {
    return typeMirrors.stream()
        .map(typeMirror -> createTypeDescriptor(typeMirror, inNullMarkedScope))
        .collect(toImmutableList());
  }

  private <T extends TypeDescriptor> ImmutableList<T> createTypeDescriptors(
      List<? extends TypeMirror> typeMirrors,
      boolean inNullMarkedScope,
      Class<T> clazz,
      Element declarationElement) {
    ImmutableList.Builder<T> typeDescriptorsBuilder = ImmutableList.builder();
    for (int i = 0; i < typeMirrors.size(); i++) {
      final int index = i;
      typeDescriptorsBuilder.add(
          createTypeDescriptor(typeMirrors.get(i), inNullMarkedScope, clazz));
    }
    return typeDescriptorsBuilder.build();
  }

  private TypeVariable createTypeVariable(javax.lang.model.type.TypeVariable typeVariable) {
    if (typeVariable.getKind().equals(TypeKind.WILDCARD)) {
      return createWildcardTypeVariable(typeVariable.getUpperBound());
    }

    Supplier<TypeDescriptor> boundTypeDescriptorFactory =
        () -> createTypeDescriptor(typeVariable.getUpperBound());

    List<String> classComponents = getClassComponents(typeVariable);

    return TypeVariable.newBuilder()
        .setUpperBoundTypeDescriptorSupplier(boundTypeDescriptorFactory) // TODO
        .setUniqueKey(
            String.join("::", classComponents)
                + (typeVariable.getUpperBound() != null
                    ? typeVariable.getUpperBound().toString()
                    : ""))
        .setName(typeVariable.asElement().getSimpleName().toString())
        .build();
  }

  private TypeVariable createWildcardTypeVariable(TypeMirror bound) {
    return TypeVariable.newBuilder()
        .setUpperBoundTypeDescriptorSupplier(() -> createTypeDescriptor(bound))
        .setWildcardOrCapture(true)
        .setName("?")
        .setUniqueKey("::?::" + (bound != null ? bound.toString() : ""))
        .build();
  }

  private boolean capturesEnclosingInstance(TypeElement typeElement) {
    if (isAnonymous(typeElement)) {
      return hasOuterInstance(typeElement) || !isStatic(typeElement.getEnclosingElement());
    }
    return hasOuterInstance(typeElement);
  }

  private static boolean isNullMarked(TypeElement classSymbol) {
    return hasNullMarkedAnnotation(classSymbol);
  }

  private static boolean hasNullMarkedAnnotation(TypeElement classSymbol) {
    if (findAnnotationBindingByName(
            classSymbol.getAnnotationMirrors(), "org.jspecify.nullness.NullMarked")
        != null) {
      // The type is NullMarked, no need to look further.
      return true;
    }

    Element enclosingElement = classSymbol.getEnclosingElement();
    return enclosingElement instanceof TypeElement
        && hasNullMarkedAnnotation((TypeElement) enclosingElement);
  }

  private TypeDescriptor createTypeDescriptorWithNullability(
      TypeMirror typeMirror,
      List<? extends AnnotationMirror> elementAnnotations,
      boolean inNullMarkedScope) {
    if (typeMirror == null || typeMirror.getKind() == TypeKind.NONE) {
      return null;
    }

    if (typeMirror.getKind().isPrimitive() || typeMirror.getKind() == TypeKind.VOID) {
      return PrimitiveTypes.get(typeMirror.toString());
    }

    if (typeMirror.getKind() == TypeKind.INTERSECTION) {
      throw new RuntimeException("Not implemented");
    }

    if (typeMirror.getKind() == TypeKind.UNION) {
      throw new RuntimeException("Not implemented");
    }

    if (typeMirror.getKind() == TypeKind.NULL) {
      return TypeDescriptors.get().javaLangObject;
    }

    if (typeMirror.getKind() == TypeKind.TYPEVAR) {
      return createTypeVariable((javax.lang.model.type.TypeVariable) typeMirror);
    }

    if (typeMirror.getKind() == TypeKind.WILDCARD) {
      return createWildcardTypeVariable(
          ((javax.lang.model.type.WildcardType) typeMirror).getExtendsBound());
    }

    boolean isNullable = isNullable(typeMirror, elementAnnotations, inNullMarkedScope);
    if (typeMirror.getKind() == TypeKind.ARRAY) {
      ArrayType arrayType = (ArrayType) typeMirror;
      TypeDescriptor componentTypeDescriptor =
          createTypeDescriptor(arrayType.getComponentType(), inNullMarkedScope);
      return ArrayTypeDescriptor.newBuilder()
          .setComponentTypeDescriptor(componentTypeDescriptor)
          .setNullable(isNullable)
          .build();
    }

    return withNullability(
        createDeclaredType(MoreTypes.asDeclared(typeMirror), inNullMarkedScope), isNullable);
  }

  private DeclaredTypeDescriptor createDeclaredType(
      final DeclaredType classType, boolean inNullMarkedScope) {

    Supplier<ImmutableList<MethodDescriptor>> declaredMethods =
        () ->
            getDeclaredMethods(classType).stream()
                .map(
                    methodDeclarationPair ->
                        createMethodDescriptor(
                            createDeclaredTypeDescriptor(classType, inNullMarkedScope),
                            methodDeclarationPair,
                            methodDeclarationPair))
                .collect(toImmutableList());

    Supplier<ImmutableList<FieldDescriptor>> declaredFields =
        () ->
            ((TypeElement) classType.asElement())
                .getEnclosedElements().stream()
                    .filter(
                        element ->
                            element.getKind() == ElementKind.FIELD
                                || element.getKind() == ElementKind.ENUM_CONSTANT)
                    .map(VariableElement.class::cast)
                    .map(this::createFieldDescriptor)
                    .collect(toImmutableList());

    TypeDeclaration typeDeclaration = createDeclarationForType((TypeElement) classType.asElement());

    // Compute these even later
    DeclaredTypeDescriptor typeDescriptor =
        DeclaredTypeDescriptor.newBuilder()
            .setTypeDeclaration(typeDeclaration)
            .setEnclosingTypeDescriptor(createDeclaredTypeDescriptor(classType.getEnclosingType()))
            .setSuperTypeDescriptorFactory(
                () ->
                    createDeclaredTypeDescriptor(
                        types.directSupertypes(classType).stream()
                            .filter(e -> !isInterface(e))
                            .findFirst()
                            .orElse(null),
                        inNullMarkedScope))
            .setInterfaceTypeDescriptorsFactory(
                td ->
                    createTypeDescriptors(
                        types.directSupertypes(classType).stream()
                            .filter(e -> isInterface(e))
                            .collect(toImmutableList()),
                        inNullMarkedScope,
                        DeclaredTypeDescriptor.class))
            .setSingleAbstractMethodDescriptorFactory(
                td -> {
                  ExecutableElement functionalInterfaceMethod =
                      getFunctionalInterfaceMethod(classType);

                  ExecutableElement asMemberOf =
                      MoreElements.asExecutable(
                          MoreTypes.asElement(
                              types.asMemberOf(classType, functionalInterfaceMethod)));

                  return createMethodDescriptor(
                      td, asMemberOf, getFunctionalInterfaceMethodDecl(classType));
                })
            .setTypeArgumentDescriptors(
                createTypeDescriptors(getTypeArguments(classType), inNullMarkedScope))
            .setDeclaredFieldDescriptorsFactory(declaredFields)
            .setDeclaredMethodDescriptorsFactory(declaredMethods)
            .build();
    return typeDescriptor;
  }

  private ImmutableList<ExecutableElement> getDeclaredMethods(DeclaredType classType) {
    return ElementFilter.methodsIn(classType.asElement().getEnclosedElements()).stream()
        .collect(toImmutableList());
  }

  public <T extends TypeDescriptor> ImmutableList<T> createTypeDescriptors(
      List<? extends TypeMirror> typeMirrors, boolean inNullMarkedScope, Class<T> clazz) {
    return typeMirrors.stream()
        .map(typeMirror -> createTypeDescriptor(typeMirror, inNullMarkedScope, clazz))
        .collect(toImmutableList());
  }

  private ExecutableElement getFunctionalInterfaceMethodDecl(TypeMirror typeMirror) {
    throw new RuntimeException("Not implemented");
  }

  private boolean isNullable(
      TypeMirror typeMirror,
      List<? extends AnnotationMirror> elementAnnotations,
      boolean inNullMarkedScope) {
    checkArgument(!typeMirror.getKind().isPrimitive());

    if (typeMirror.getKind().equals(TypeKind.VOID)) {
      // Void is always nullable.
      return true;
    }

    List<AnnotationMirror> allAnnotations = new ArrayList<>();
    allAnnotations.addAll(elementAnnotations);
    allAnnotations.addAll(typeMirror.getAnnotationMirrors());

    for (AnnotationMirror annotationMirror : allAnnotations) {
      if (isNonNullAnnotation(annotationMirror)) {
        return false;
      }
      if (isNullableAnnotation(annotationMirror)) {
        return true;
      }
    }

    return !inNullMarkedScope;
  }

  private ImmutableList<String> getClassComponents(
      javax.lang.model.type.TypeVariable typeVariable) {
    Element enclosingElement = typeVariable.asElement().getEnclosingElement();
    if (enclosingElement.getKind() == ElementKind.CLASS
        || enclosingElement.getKind() == ElementKind.INTERFACE
        || enclosingElement.getKind() == ElementKind.ENUM) {
      return ImmutableList.<String>builder()
          .addAll(getClassComponents(enclosingElement))
          .add(
              // If it is a class-level type variable, use the simple name (with prefix "C_") as the
              // current name component.
              "C_" + typeVariable.asElement().getSimpleName())
          .build();
    } else {
      return ImmutableList.<String>builder()
          .addAll(getClassComponents(enclosingElement.getEnclosingElement()))
          .add(
              "M_"
                  + enclosingElement.getSimpleName()
                  + "_"
                  + typeVariable.asElement().getSimpleName())
          .build();
    }
  }

  private ImmutableList<String> getClassComponents(Element element) {
    if (!(element instanceof TypeElement)) {
      return ImmutableList.of();
    }
    TypeElement typeElement = (TypeElement) element;
    List<String> classComponents = new ArrayList<>();
    TypeElement currentType = typeElement;
    while (currentType != null) {
      String simpleName;
      if (currentType.getNestingKind() == NestingKind.LOCAL
          || currentType.getNestingKind() == NestingKind.ANONYMOUS) {
        // JavaC binary name for local class is like package.components.EnclosingClass$1SimpleName
        // Extract the generated name by taking the part after the binary name of the declaring
        // class.
        String binaryName = getBinaryNameFromTypeBinding(currentType);
        String declaringClassPrefix =
            getBinaryNameFromTypeBinding(getEnclosingType(currentType)) + "$";
        simpleName = binaryName.substring(declaringClassPrefix.length());
      } else {
        simpleName = asElement(erasure(currentType.asType())).getSimpleName().toString();
      }
      classComponents.add(0, simpleName);
      Element enclosingElement = currentType.getEnclosingElement();
      while (enclosingElement != null
          && enclosingElement.getKind() != ElementKind.CLASS
          && enclosingElement.getKind() != ElementKind.INTERFACE
          && enclosingElement.getKind() != ElementKind.ENUM) {
        enclosingElement = enclosingElement.getEnclosingElement();
      }
      currentType = (TypeElement) enclosingElement;
    }
    return ImmutableList.copyOf(classComponents);
  }

  private Kind getKindFromTypeBinding(TypeElement typeElement) {
    if (isEnum(typeElement) && !isAnonymous(typeElement)) {
      // Do not consider the anonymous classes that constitute enum values as Enums, only the
      // enum "class" itself is considered Kind.ENUM.
      return Kind.ENUM;
    } else if (isClass(typeElement) || (isEnum(typeElement) && isAnonymous(typeElement))) {
      return Kind.CLASS;
    } else if (isInterface(typeElement)) {
      return Kind.INTERFACE;
    }
    throw new InternalCompilerError("Type binding %s not handled.", typeElement);
  }

  private ExecutableElement getFunctionalInterfaceMethod(TypeMirror typeMirror) {
    throw new UnsupportedOperationException();
  }

  public MemberDescriptor getDefaultConstructor(TypeElement clazz) {
    DeclaredTypeDescriptor typeDeclaration =
        (DeclaredTypeDescriptor) createTypeDescriptor(clazz.asType());

    MethodDescriptor ctor =
        createDeclarationForType(clazz).getDeclaredMethodDescriptors().stream()
            .filter(MemberDescriptor::isJsConstructor)
            .filter(m -> m.getParameterDescriptors().isEmpty())
            .findFirst()
            .orElseGet(() -> AstUtils.createImplicitConstructorDescriptor(typeDeclaration));
    return ctorMethodDescriptorFromJavaConstructor(ctor);
  }

  private static String getCtorName(MethodDescriptor methodDescriptor) {
    // Synthesize a name that is unique per class to avoid property clashes in JS.
    return MethodDescriptor.CTOR_METHOD_PREFIX
        + "__"
        + methodDescriptor.getEnclosingTypeDescriptor().getMangledName();
  }

  private static MethodDescriptor ctorMethodDescriptorFromJavaConstructor(
      MethodDescriptor constructor) {
    return constructor.transform(
        builder ->
            builder
                .setReturnTypeDescriptor(PrimitiveTypes.VOID)
                .setName(getCtorName(constructor))
                .setConstructor(false)
                .setStatic(false)
                .setJsInfo(JsInfo.NONE)
                .removeParameterOptionality()
                .setOrigin(MethodDescriptor.MethodOrigin.SYNTHETIC_CTOR_FOR_CONSTRUCTOR)
                .setVisibility(Visibility.PUBLIC));
  }
}
