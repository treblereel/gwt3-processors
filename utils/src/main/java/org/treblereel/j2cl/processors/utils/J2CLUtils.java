/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.treblereel.j2cl.processors.utils;

import com.google.auto.common.MoreElements;
import com.google.j2cl.transpiler.ast.*;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Utility functions to interact with JavaC internal representations.
 *
 * <p>it's taken from J2CL project
 */
public class J2CLUtils {

  private final HackedJavaEnvironment javaEnvironment;

  public J2CLUtils(ProcessingEnvironment processingEnv) {
    javaEnvironment = new HackedJavaEnvironment(processingEnv);
  }

  public MemberDescriptor getDefaultConstructor(TypeElement parent) {
    return javaEnvironment.getDefaultConstructor(parent);
  }

  public DeclaredTypeDescriptor createDeclaredTypeDescriptor(DeclaredType declaredType) {
    return javaEnvironment.createDeclaredTypeDescriptor(declaredType);
  }

  public MethodDescriptor createDeclarationMethodDescriptor(ExecutableElement method) {
    return javaEnvironment.createDeclarationMethodDescriptor(method);
  }

  public MethodDescriptor createDeclarationMethodDescriptor(
      ExecutableElement methodElement, DeclaredTypeDescriptor enclosingTypeDescriptor) {
    return javaEnvironment.createDeclarationMethodDescriptor(
        methodElement, enclosingTypeDescriptor);
  }

  public FieldDescriptor createFieldDescriptor(VariableElement variableElement) {
    return javaEnvironment.createFieldDescriptor(variableElement);
  }

  public TypeDescriptor createTypeDescriptor(TypeElement element) {
    return createTypeDescriptor(element.asType());
  }

  public TypeDescriptor createTypeDescriptor(TypeMirror type) {
    return javaEnvironment.createTypeDescriptor(type);
  }

  public String getMethodMangledName(ExecutableElement method) {
    if (method.getAnnotation(JsProperty.class) != null) {
      JsProperty jsProperty = method.getAnnotation(JsProperty.class);
      return jsProperty.name().equals("<auto>")
          ? method.getSimpleName().toString()
          : jsProperty.name();
    }

    if (isJsType(MoreElements.asType(method.getEnclosingElement()))) {
      return method.getSimpleName().toString();
    }
    return javaEnvironment.createDeclarationMethodDescriptor(method).getMangledName();
  }

  public String getVariableMangledName(VariableElement variableElement) {
    if (variableElement.getAnnotation(JsProperty.class) != null) {
      JsProperty jsProperty = variableElement.getAnnotation(JsProperty.class);
      return jsProperty.name().equals("<auto>")
          ? variableElement.getSimpleName().toString()
          : jsProperty.name();
    }
    return javaEnvironment.createFieldDescriptor(variableElement).getMangledName();
  }

  public boolean isJsType(TypeElement element) {
    return element.getAnnotation(JsType.class) != null
        && !element.getAnnotation(JsType.class).isNative();
  }
}
