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
import static com.google.j2cl.transpiler.ast.TypeDeclaration.Kind;
import static com.google.j2cl.transpiler.ast.TypeDeclaration.newBuilder;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.j2cl.common.InternalCompilerError;
import com.google.j2cl.transpiler.ast.ArrayTypeDescriptor;
import com.google.j2cl.transpiler.ast.AstUtils;
import com.google.j2cl.transpiler.ast.DeclaredTypeDescriptor;
import com.google.j2cl.transpiler.ast.FieldDescriptor;
import com.google.j2cl.transpiler.ast.JsEnumInfo;
import com.google.j2cl.transpiler.ast.JsInfo;
import com.google.j2cl.transpiler.ast.Literal;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Utility functions to interact with JavaC internal representations.
 *
 * <p>it's taken from J2CL project
 */
public class J2CLUtils {
  private final Types javacTypes;
  private final Elements elements;

  private final ProcessingEnvironment processingEnv;
  private final Map<DeclaredType, DeclaredTypeDescriptor>
      cachedDeclaredTypeDescriptorByDeclaredTypeInNullMarkedScope = new HashMap<>();
  private final Map<DeclaredType, DeclaredTypeDescriptor>
      cachedDeclaredTypeDescriptorByDeclaredTypeOutOfNullMarkedScope = new HashMap<>();

  public J2CLUtils(ProcessingEnvironment processingEnv) {
    this.javacTypes = processingEnv.getTypeUtils();
    this.elements = processingEnv.getElementUtils();
    this.processingEnv = processingEnv;
    initWellKnownTypes(TypeDescriptors.getWellKnownTypeNames());
  }

  private boolean isNonNullAnnotation(AnnotationMirror annotation) {
    DeclaredType annotationType = annotation.getAnnotationType();
    String name = annotationType.asElement().getSimpleName().toString();
    return name.equals("NonNull") || name.equals("JsNonNull");
  }

  private boolean isNullableAnnotation(AnnotationMirror annotation) {
    DeclaredType annotationType = annotation.getAnnotationType();
    return annotationType.asElement().getSimpleName().toString().equals("Nullable");
  }

  private DeclaredTypeDescriptor withNullability(
      DeclaredTypeDescriptor typeDescriptor, boolean nullable) {
    return nullable ? typeDescriptor.toNullable() : typeDescriptor.toNonNullable();
  }

  /**
   * In case the given type element is nested, return the outermost possible enclosing type element.
   */
  private TypeElement toTopLevelTypeBinding(Element element) {
    if (element.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
      return (TypeElement) element;
    }
    return toTopLevelTypeBinding(element.getEnclosingElement());
  }

  private boolean isValuesMethod(ExecutableElement methodElement) {
    return methodElement.getSimpleName().contentEquals("values")
        && methodElement.getParameters().isEmpty();
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility methods to process nullability annotations on classes that are compiled separately.
  // Javac does not present TYPE_USE annotation in the returned type instances.
  private TypeDescriptor applyParameterNullabilityAnnotations(
      TypeDescriptor typeDescriptor, ExecutableElement declarationMethodElement, int index) {
    return typeDescriptor;
  }

  private int getInnerDepth(DeclaredTypeDescriptor innerType) {
    if (innerType.getTypeDeclaration().isCapturingEnclosingInstance()) {
      return getInnerDepth(innerType.getEnclosingTypeDescriptor()) + 1;
    }
    return 0;
  }

  /** Returns true if the element is annotated with @UncheckedCast. */
  private boolean hasUncheckedCastAnnotation(Element element) {
    return false;
  }

  /** Returns true if the element is annotated with @HasNoSideEffects. */
  private boolean isAnnotatedWithHasNoSideEffects(Element element) {
    return true;
  }

  private List<TypeMirror> getTypeArguments(DeclaredType declaredType) {
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

  private String getJsName(final TypeElement classSymbol) {
    return JsInteropAnnotationUtils.getJsName(classSymbol);
  }

  private boolean hasNullMarkedAnnotation(TypeElement classSymbol) {
    return false;
  }

  private List<TypeParameterElement> getTypeParameters(TypeElement typeElement) {
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

  public TypeElement getEnclosingType(Element typeElement) {
    Element enclosing = typeElement.getEnclosingElement();
    while (enclosing != null && !(enclosing instanceof TypeElement)) {
      enclosing = enclosing.getEnclosingElement();
    }
    return (TypeElement) enclosing;
  }

  private TypeElement getEnclosingType(TypeElement typeElement) {
    Element enclosing = typeElement.getEnclosingElement();
    while (enclosing != null && !(enclosing instanceof TypeElement)) {
      enclosing = enclosing.getEnclosingElement();
    }
    return (TypeElement) enclosing;
  }

  private boolean isEnum(TypeElement typeElement) {
    return typeElement.getKind() == ElementKind.ENUM;
  }

  private boolean isAnnotation(TypeElement typeElement) {
    return typeElement.getKind() == ElementKind.ANNOTATION_TYPE;
  }

  private boolean isAnonymous(TypeElement typeElement) {
    return typeElement.getNestingKind() == NestingKind.ANONYMOUS;
  }

  private boolean isClass(TypeElement typeElement) {
    return typeElement.getKind() == ElementKind.CLASS;
  }

  private boolean isInterface(TypeElement typeElement) {
    return typeElement.getKind() == ElementKind.INTERFACE
        || typeElement.getKind() == ElementKind.ANNOTATION_TYPE;
  }

  private boolean isLocal(TypeElement typeElement) {
    return typeElement.getNestingKind() == NestingKind.LOCAL;
  }

  public Visibility getVisibility(Element element) {
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

  private boolean isDeprecated(AnnotatedConstruct binding) {
    return binding.getAnnotation(Deprecated.class) != null;
  }

  private boolean isDefaultMethod(Element element) {
    return element.getModifiers().contains(Modifier.DEFAULT);
  }

  private boolean isAbstract(Element element) {
    return element.getModifiers().contains(Modifier.ABSTRACT);
  }

  private boolean isFinal(Element element) {
    return element.getModifiers().contains(Modifier.FINAL);
  }

  public boolean isStatic(Element element) {
    return element.getModifiers().contains(Modifier.STATIC);
  }

  private boolean isNative(Element element) {
    return element.getModifiers().contains(Modifier.NATIVE);
  }

  private boolean isSynthetic(Element element) {
    return false;
    // return element instanceof Symbol && (((Symbol) element).flags() & Flags.SYNTHETIC) != 0;
  }

  private MethodDescriptor ctorMethodDescriptorFromJavaConstructor(MethodDescriptor constructor) {
    return constructor.transform(
        builder ->
            builder
                .setReturnTypeDescriptor(PrimitiveTypes.VOID)
                .setName(getCtorName(constructor))
                .setConstructor(false)
                .setStatic(false)
                .setOriginalJsInfo(JsInfo.NONE)
                .removeParameterOptionality()
                .setOrigin(MethodDescriptor.MethodOrigin.SYNTHETIC_CTOR_FOR_CONSTRUCTOR)
                .setVisibility(Visibility.PUBLIC));
  }

  private String getCtorName(MethodDescriptor methodDescriptor) {
    // Synthesize a name that is unique per class to avoid property clashes in JS.
    return MethodDescriptor.CTOR_METHOD_PREFIX
        + "__"
        + methodDescriptor.getEnclosingTypeDescriptor().getMangledName();
  }

  private void initWellKnownTypes(Collection<String> wellKnownQualifiedBinaryNames) {
    if (TypeDescriptors.isInitialized()) {
      return;
    }
    TypeDescriptors.SingletonBuilder builder = new TypeDescriptors.SingletonBuilder();
    // Add well-known, non-primitive types.
    wellKnownQualifiedBinaryNames.forEach(
        binaryName -> {
          String qualifiedSourceName = binaryName.replace('$', '.');
          TypeElement element = getTypeElement(qualifiedSourceName);
          if (element != null) {
            builder.addReferenceType(createDeclaredTypeDescriptor(element.asType()));
          }
        });

    DeclaredTypeDescriptor typeDescriptor =
        createSyntheticJavaEmulInternalExceptionsTypeDescriptor();
    builder.addReferenceType(typeDescriptor);
    builder.buildSingleton();
  }

  private DeclaredTypeDescriptor createSyntheticJavaEmulInternalExceptionsTypeDescriptor() {
    TypeDeclaration typeDeclaration =
        TypeDeclaration.newBuilder()
            .setClassComponents(ImmutableList.of("Exceptions"))
            .setNative(false)
            .setCustomizedJsNamespace("javaemul.internal")
            .setPackageName("javaemul.internal")
            .setTypeParameterDescriptors(ImmutableList.of())
            .setVisibility(Visibility.PUBLIC)
            .setKind(Kind.CLASS)
            .build();

    return DeclaredTypeDescriptor.newBuilder()
        .setTypeDeclaration(typeDeclaration)
        .setTypeArgumentDescriptors(Collections.EMPTY_LIST)
        .build();
  }

  public DeclaredTypeDescriptor createDeclaredTypeDescriptor(TypeMirror typeMirror) {
    return createDeclaredTypeDescriptor(typeMirror, /* inNullMarkedScope= */ false);
  }

  private DeclaredTypeDescriptor createDeclaredTypeDescriptor(
      TypeMirror typeMirror, boolean inNullMarkedScope) {
    return createTypeDescriptor(typeMirror, inNullMarkedScope, DeclaredTypeDescriptor.class);
  }

  /** Creates a specific subclass of TypeDescriptor from a TypeMirror. */
  public <T extends TypeDescriptor> T createTypeDescriptor(TypeMirror typeMirror, Class<T> clazz) {
    return createTypeDescriptor(typeMirror, /* inNullMarkedScope= */ false, clazz);
  }

  /** Creates a specific subclass of TypeDescriptor from a TypeMirror. */
  private <T extends TypeDescriptor> T createTypeDescriptor(
      TypeMirror typeMirror, boolean inNullMarkedScope, Class<T> clazz) {
    return clazz.cast(createTypeDescriptor(typeMirror, inNullMarkedScope));
  }

  /** Creates a TypeDescriptor from a TypeMirror. */
  public TypeDescriptor createTypeDescriptor(TypeMirror typeMirror) {
    return createTypeDescriptor(typeMirror, /* inNullMarkedScope= */ false);
  }

  /** Creates a TypeDescriptor from a TypeMirror. */
  private TypeDescriptor createTypeDescriptor(TypeMirror typeMirror, boolean inNullMarkedScope) {
    return createTypeDescriptorWithNullability(typeMirror, ImmutableList.of(), inNullMarkedScope);
  }

  /** Creates a type descriptor for the given TypeMirror, taking into account nullability. */
  private TypeDescriptor createTypeDescriptorWithNullability(
      TypeMirror typeMirror,
      List<? extends AnnotationMirror> elementAnnotations,
      boolean inNullMarkedScope) {
    if (typeMirror == null || typeMirror.getKind() == TypeKind.NONE) {
      return null;
    }
    if (typeMirror.getKind().isPrimitive()) {
      return PrimitiveTypes.get(typeMirror.toString());
    }

    if (typeMirror.getKind() == TypeKind.VOID) {
      return PrimitiveTypes.VOID;
    }

    if (typeMirror.getKind() == TypeKind.INTERSECTION) {
      throw new InternalCompilerError("Intersection types are not supported.");
      // return createIntersectionType((IntersectionClassType) typeMirror);
    }

    if (typeMirror.getKind() == TypeKind.UNION) {
      throw new InternalCompilerError("Union types are not supported.");
      // return createUnionType((UnionClassType) typeMirror);
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

  /**
   * Returns whether the given type binding should be nullable, according to the annotations on it
   * and if nullability is enabled for the package containing the binding.
   */
  private boolean isNullable(
      TypeMirror typeMirror,
      List<? extends AnnotationMirror> elementAnnotations,
      boolean inNullMarkedScope) {
    checkArgument(!typeMirror.getKind().isPrimitive());

    if (typeMirror.getKind() == TypeKind.VOID) {
      return true;
    }

    Iterable<? extends AnnotationMirror> allAnnotations =
        Iterables.concat(elementAnnotations, typeMirror.getAnnotationMirrors());

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

  private TypeVariable createTypeVariable(javax.lang.model.type.TypeVariable typeVariable) {
    /*    if (typeVariable instanceof CapturedType) {
      return createWildcardTypeVariable(typeVariable.getUpperBound());
    }*/

    Supplier<TypeDescriptor> boundTypeDescriptorFactory =
        () -> createTypeDescriptor(typeVariable.getUpperBound());

    List<String> classComponents = getClassComponents(typeVariable);
    return TypeVariable.newBuilder()
        .setUpperBoundTypeDescriptorSupplier(boundTypeDescriptorFactory)
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
        .setWildcard(true)
        .setName("?")
        .setUniqueKey("::?::" + (bound != null ? bound.toString() : ""))
        .build();
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

  /** Returns the binary name for a type element. */
  private String getBinaryNameFromTypeBinding(TypeElement typeElement) {
    return elements.getBinaryName(typeElement).toString();
  }

  private boolean isEnumSyntheticMethod(ExecutableElement methodElement) {
    // Enum synthetic methods are not marked as such because per JLS 13.1 these methods are
    // implicitly declared but are not marked as synthetic.
    return getEnclosingType(methodElement).getKind() == ElementKind.ENUM
        && (isValuesMethod(methodElement) || isValueOfMethod(methodElement));
  }

  private boolean isValueOfMethod(ExecutableElement methodElement) {
    return methodElement.getSimpleName().contentEquals("valueOf")
        && methodElement.getParameters().size() == 1
        && asTypeElement(methodElement.getParameters().get(0).asType())
            .getQualifiedName()
            .contentEquals("java.lang.String");
  }

  /**
   * Returns true if instances of this type capture its outer instances; i.e. if it is an non member
   * class, or an anonymous or local class defined in an instance context.
   */
  private boolean capturesEnclosingInstance(TypeElement typeElement) {
    if (isAnonymous(typeElement)) {
      return hasOuterInstance(typeElement) || !isStatic(typeElement.getEnclosingElement());
    }
    return hasOuterInstance(typeElement);
  }

  public boolean hasOuterInstance(TypeElement typeElement) {
    return typeElement.getEnclosingElement().getKind().isClass()
        && !isInterface((TypeElement) typeElement.getEnclosingElement());
  }

  public FieldDescriptor createFieldDescriptor(VariableElement variableElement) {
    return createFieldDescriptor(variableElement, variableElement.asType());
  }

  FieldDescriptor createFieldDescriptor(VariableElement variableElement, TypeMirror type) {

    boolean isStatic = isStatic(variableElement);
    Visibility visibility = getVisibility(variableElement);
    DeclaredTypeDescriptor enclosingTypeDescriptor =
        createDeclaredTypeDescriptor(getEnclosingType(variableElement).asType());
    String fieldName = variableElement.getSimpleName().toString();

    TypeDescriptor thisTypeDescriptor =
        createTypeDescriptorWithNullability(
            type,
            variableElement.getAnnotationMirrors(),
            enclosingTypeDescriptor.getTypeDeclaration().isNullMarked());

    boolean isEnumConstant = variableElement.getKind().equals(ElementKind.ENUM_CONSTANT);
    if (isEnumConstant) {
      // Enum fields are always non-nullable.
      thisTypeDescriptor = thisTypeDescriptor.toNonNullable();
    }

    FieldDescriptor declarationFieldDescriptor = null;
    if (!javacTypes.isSameType(variableElement.asType(), type)) {
      // Field references might be parameterized, and when they are we set the declaration
      // descriptor to the unparameterized declaration.
      declarationFieldDescriptor = createFieldDescriptor(variableElement, variableElement.asType());
    }

    JsInfo jsInfo = JsInteropUtils.getJsInfo(variableElement);
    Object constantValue = variableElement.getConstantValue();
    boolean isCompileTimeConstant = constantValue != null;
    if (isCompileTimeConstant) {
      thisTypeDescriptor = thisTypeDescriptor.toNonNullable();
    }
    boolean isFinal = isFinal(variableElement);
    return FieldDescriptor.newBuilder()
        .setEnclosingTypeDescriptor(enclosingTypeDescriptor)
        .setName(fieldName)
        .setTypeDescriptor(thisTypeDescriptor)
        .setStatic(isStatic)
        .setVisibility(visibility)
        .setOriginalJsInfo(jsInfo)
        .setFinal(isFinal)
        .setCompileTimeConstant(isCompileTimeConstant)
        .setConstantValue(
            constantValue != null ? Literal.fromValue(constantValue, thisTypeDescriptor) : null)
        .setDeclarationDescriptor(declarationFieldDescriptor)
        .setEnumConstant(isEnumConstant)
        .setUnusableByJsSuppressed(
            JsInteropAnnotationUtils.isUnusableByJsSuppressed(variableElement))
        .setDeprecated(isDeprecated(variableElement))
        .build();
  }

  /** Create a MethodDescriptor directly based on the given JavaC ExecutableElement. */
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
            returnType,
            declarationMethodElement.getAnnotationMirrors(),
            enclosingTypeDescriptor.getTypeDeclaration().isNullMarked());

    ImmutableList.Builder<TypeDescriptor> parametersBuilder = ImmutableList.builder();
    for (int i = 0; i < parameters.size(); i++) {
      parametersBuilder.add(
          applyParameterNullabilityAnnotations(
              createTypeDescriptorWithNullability(
                  parameters.get(i),
                  declarationMethodElement.getParameters().get(i).getAnnotationMirrors(),
                  enclosingTypeDescriptor.getTypeDeclaration().isNullMarked()),
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

  /** Create a MethodDescriptor directly based on the given JavaC ExecutableElement. */
  public MethodDescriptor createDeclarationMethodDescriptor(ExecutableElement methodElement) {
    DeclaredTypeDescriptor enclosingTypeDescriptor =
        createDeclaredTypeDescriptor(methodElement.getEnclosingElement().asType());
    return createDeclarationMethodDescriptor(methodElement, enclosingTypeDescriptor);
  }

  /** Create a MethodDescriptor directly based on the given JavaC ExecutableElement. */
  public MethodDescriptor createDeclarationMethodDescriptor(
      ExecutableElement methodElement, DeclaredTypeDescriptor enclosingTypeDescriptor) {
    return createMethodDescriptor(enclosingTypeDescriptor, methodElement, methodElement);
  }

  /**
   * Returns true if any of the type parameters has been specialized.
   *
   * <p>For example the type {@code List<String>} specialized the type variable {@code T} from the
   * class declaration.
   */
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

  private boolean isSameType(TypeMirror thisType, TypeMirror thatType) {
    return javacTypes.isSameType(thisType, thatType);
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
    JsInfo jsInfo = JsInteropUtils.getJsInfo(declarationMethodElement);

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

    boolean hasUncheckedCast = hasUncheckedCastAnnotation(declarationMethodElement);
    return MethodDescriptor.newBuilder()
        .setEnclosingTypeDescriptor(enclosingTypeDescriptor)
        .setName(isConstructor ? null : methodName)
        .setParameterDescriptors(parameterDescriptorBuilder.build())
        .setDeclarationDescriptor(declarationMethodDescriptor)
        .setReturnTypeDescriptor(isConstructor ? enclosingTypeDescriptor : returnTypeDescriptor)
        .setTypeParameterTypeDescriptors(typeParameterTypeDescriptors)
        .setOriginalJsInfo(jsInfo)
        .setVisibility(visibility)
        .setStatic(isStatic)
        .setConstructor(isConstructor)
        .setNative(isNative)
        .setFinal(isFinal(declarationMethodElement))
        .setDefaultMethod(isDefault)
        .setAbstract(isAbstract(declarationMethodElement))
        .setSynthetic(isSynthetic(declarationMethodElement))
        .setEnumSyntheticMethod(isEnumSyntheticMethod(declarationMethodElement))
        .setSideEffectFree(isAnnotatedWithHasNoSideEffects(declarationMethodElement))
        .setUnusableByJsSuppressed(
            JsInteropAnnotationUtils.isUnusableByJsSuppressed(declarationMethodElement))
        .setDeprecated(isDeprecated(declarationMethodElement))
        .setUncheckedCast(hasUncheckedCast)
        .build();
  }

  public ImmutableList<TypeDescriptor> createTypeDescriptors(
      List<? extends TypeMirror> typeMirrors, boolean inNullMarkedScope) {
    return typeMirrors.stream()
        .map(typeMirror -> createTypeDescriptor(typeMirror, inNullMarkedScope))
        .collect(toImmutableList());
  }

  public <T extends TypeDescriptor> ImmutableList<T> createTypeDescriptors(
      List<? extends TypeMirror> typeMirrors,
      boolean inNullMarkedScope,
      Class<T> clazz,
      Element declarationElement) {
    ImmutableList.Builder<T> typeDescriptorsBuilder = ImmutableList.builder();
    for (int i = 0; i < typeMirrors.size(); i++) {
      final int index = i;
      TypeDescriptor typeDescriptor =
          createTypeDescriptor(typeMirrors.get(i), inNullMarkedScope, clazz);
      /*      typeDescriptorsBuilder.add(typeDescriptor);

      typeDescriptorsBuilder.add(
          clazz.cast(
              applyNullabilityAnnotations(
                  createTypeDescriptor(typeMirrors.get(i), inNullMarkedScope, clazz),
                  declarationElement,
                  position ->
                      position.type == TargetType.CLASS_EXTENDS && position.type_index == index)));*/
    }
    return typeDescriptorsBuilder.build();
  }

  public <T extends TypeDescriptor> ImmutableList<T> createTypeDescriptors(
      List<? extends TypeMirror> typeMirrors, boolean inNullMarkedScope, Class<T> clazz) {
    return typeMirrors.stream()
        .map(typeMirror -> createTypeDescriptor(typeMirror, inNullMarkedScope, clazz))
        .collect(toImmutableList());
  }

  private TypeElement getTypeElement(String qualifiedSourceName) {
    return elements.getTypeElement(qualifiedSourceName);
  }

  private Element asElement(TypeMirror typeMirror) {
    if (typeMirror.getKind().isPrimitive()) {
      return MoreTypes.asElement(typeMirror);
    }
    if (typeMirror.getKind().equals(TypeKind.DECLARED)) {
      return MoreTypes.asDeclared(typeMirror).asElement();
    }
    return javacTypes.asElement(typeMirror);
  }

  private TypeElement asTypeElement(TypeMirror typeMirror) {
    return (TypeElement) asElement(typeMirror);
  }

  private TypeMirror erasure(TypeMirror typeMirror) {
    return javacTypes.erasure(typeMirror);
  }

  private PackageElement getPackageOf(TypeElement typeElement) {
    return elements.getPackageOf(typeElement);
  }

  private DeclaredTypeDescriptor createDeclaredType(
      final DeclaredType classType, boolean inNullMarkedScope) {

    DeclaredTypeDescriptor cachedTypeDescriptor =
        getCachedTypeDescriptor(classType, inNullMarkedScope);
    if (cachedTypeDescriptor != null) {
      return cachedTypeDescriptor;
    }

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
                td ->
                    td.isInterface()
                        ? null
                        : createDeclaredTypeDescriptor(
                            javacTypes.directSupertypes(classType).stream()
                                .filter(type -> !asTypeElement(type).getKind().isInterface())
                                .findFirst()
                                .orElse(null),
                            inNullMarkedScope))
            .setInterfaceTypeDescriptorsFactory(
                td ->
                    createTypeDescriptors(
                        javacTypes.directSupertypes(classType).stream()
                            .filter(type -> !asTypeElement(type).getKind().isInterface())
                            .collect(toImmutableList()),
                        inNullMarkedScope,
                        DeclaredTypeDescriptor.class))
            .setSingleAbstractMethodDescriptorFactory(
                td -> {
                  // MethodSymbol functionalInterfaceMethod =
                  // getFunctionalInterfaceMethod(classType);
                  throw new UnsupportedOperationException("Not implemented yet");
                  /*                              return createMethodDescriptor(
                  td,
                  (MethodSymbol)
                          functionalInterfaceMethod.asMemberOf(
                                  ((ClassSymbol) classType.asElement()).asType(), internalTypes),
                  getFunctionalInterfaceMethodDecl(classType));*/
                })
            .setTypeArgumentDescriptors(
                createTypeDescriptors(getTypeArguments(classType), inNullMarkedScope))
            .setDeclaredFieldDescriptorsFactory(declaredFields)
            .setDeclaredMethodDescriptorsFactory(declaredMethods)
            .build();
    putTypeDescriptorInCache(inNullMarkedScope, classType, typeDescriptor);
    return typeDescriptor;
  }

  private DeclaredTypeDescriptor getCachedTypeDescriptor(
      DeclaredType classType, boolean inNullMarkedScope) {
    Map<DeclaredType, DeclaredTypeDescriptor> cache =
        inNullMarkedScope
            ? cachedDeclaredTypeDescriptorByDeclaredTypeInNullMarkedScope
            : cachedDeclaredTypeDescriptorByDeclaredTypeOutOfNullMarkedScope;
    return cache.get(classType);
  }

  private void putTypeDescriptorInCache(
      boolean inNullMarkedScope, DeclaredType classType, DeclaredTypeDescriptor typeDescriptor) {
    Map<DeclaredType, DeclaredTypeDescriptor> cache =
        inNullMarkedScope
            ? cachedDeclaredTypeDescriptorByDeclaredTypeInNullMarkedScope
            : cachedDeclaredTypeDescriptorByDeclaredTypeOutOfNullMarkedScope;
    cache.put(classType, typeDescriptor);
  }

  private ImmutableList<ExecutableElement> getDeclaredMethods(DeclaredType classType) {
    return ElementFilter.methodsIn(classType.asElement().getEnclosedElements()).stream()
        .collect(toImmutableList());
  }

  private String getJsNamespace(TypeElement classSymbol) {
    String jsNamespace = JsInteropAnnotationUtils.getJsNamespace(classSymbol);
    if (jsNamespace != null) {
      return jsNamespace;
    }

    // Maybe namespace is set via package-info file?
    boolean isTopLevelType = classSymbol.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    if (isTopLevelType) {
      return getBinaryNameFromTypeBinding(classSymbol);
    }
    return null;
  }

  TypeDeclaration createDeclarationForType(final TypeElement typeElement) {
    if (typeElement == null) {
      return null;
    }

    // Compute these first since they're reused in other calculations.
    String packageName = getPackageOf(typeElement).getQualifiedName().toString();
    boolean isAbstract = isAbstract(typeElement) && !isInterface(typeElement);
    Kind kind = getKindFromTypeBinding(typeElement);
    // TODO(b/341721484): Even though enums can not have the final modifier, turbine make them final
    // in the header jars.
    boolean isFinal = isFinal(typeElement) && kind != Kind.ENUM;

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

    // boolean isNullMarked = isNullMarked(typeElement, packageInfoCache);
    boolean isNullMarked = false;
    return newBuilder()
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
        .setKind(kind)
        // .setAnnotation(isAnnotation(typeElement))
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
        /*            .setOriginalSimpleSourceName(
        typeElement.getSimpleName() != null ? typeElement.getSimpleName().toString() : null)*/
        .setPackageName(packageName)
        /*        .setSuperTypeDescriptorFactory(
        () ->
            (DeclaredTypeDescriptor)
                applyNullabilityAnnotations(
                    createDeclaredTypeDescriptor(typeElement.getSuperclass(), isNullMarked),
                    typeElement,
                    position ->
                        position.type == TargetType.CLASS_EXTENDS && position.type_index == -1))*/
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

  private boolean isFunctionalInterface(TypeMirror type) {
    return type.getAnnotationsByType(FunctionalInterface.class).length > 0;
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
}
