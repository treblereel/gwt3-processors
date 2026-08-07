/*
 * Copyright © 2024
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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public class J2CLUtils {

  private final Types types;
  private final Elements elements;

  public J2CLUtils(ProcessingEnvironment processingEnv) {
    this.types = processingEnv.getTypeUtils();
    this.elements = processingEnv.getElementUtils();
  }

  public String getDefaultConstructorMangledName(TypeElement parent) {
    String typeMangledName = mangleType(parent.asType());
    return MangledNameComputer.mangleDefaultConstructorName(typeMangledName);
  }

  public String getMethodMangledName(ExecutableElement method) {
    if (method.getAnnotation(JsProperty.class) != null) {
      return resolveJsPropertyName(method);
    }
    TypeElement enclosingType = (TypeElement) method.getEnclosingElement();
    if (isJsType(enclosingType)) {
      return method.getSimpleName().toString();
    }
    return computeMethodMangledName(method, enclosingType);
  }

  public String getMethodMangledName(ExecutableElement method, TypeElement enclosingType) {
    if (method.getAnnotation(JsProperty.class) != null) {
      return resolveJsPropertyName(method);
    }
    return computeMethodMangledName(method, enclosingType);
  }

  public String getVariableMangledName(VariableElement variableElement) {
    if (variableElement.getAnnotation(JsProperty.class) != null) {
      return resolveJsPropertyName(variableElement);
    }
    TypeElement enclosingType = (TypeElement) variableElement.getEnclosingElement();
    String enclosingTypeMangledName = mangleType(enclosingType.asType());
    MangledNameComputer.MemberVisibility visibility = getVisibility(variableElement);
    return MangledNameComputer.mangleFieldName(
        variableElement.getSimpleName().toString(), enclosingTypeMangledName, visibility);
  }

  public boolean isJsType(TypeElement element) {
    return element.getAnnotation(JsType.class) != null
        && !element.getAnnotation(JsType.class).isNative();
  }

  private String computeMethodMangledName(ExecutableElement method, TypeElement enclosingType) {
    List<String> paramMangledNames = new ArrayList<>();
    for (VariableElement param : method.getParameters()) {
      paramMangledNames.add(mangleType(types.erasure(param.asType())));
    }
    String returnMangledName = mangleType(types.erasure(method.getReturnType()));
    MangledNameComputer.MemberVisibility visibility = getVisibility(method);
    boolean isInstance = !method.getModifiers().contains(Modifier.STATIC);
    String enclosingTypeMangledName = mangleType(enclosingType.asType());
    String packageName = elements.getPackageOf(enclosingType).getQualifiedName().toString();
    return MangledNameComputer.mangleMethodName(
        method.getSimpleName().toString(),
        paramMangledNames,
        returnMangledName,
        visibility,
        isInstance,
        enclosingTypeMangledName,
        packageName);
  }

  private String mangleType(TypeMirror typeMirror) {
    TypeKind kind = typeMirror.getKind();
    if (kind.isPrimitive()) {
      return typeMirror.toString();
    }
    switch (kind) {
      case VOID:
        return "void";
      case ARRAY:
        ArrayType arrayType = (ArrayType) typeMirror;
        int dimensions = 0;
        TypeMirror component = typeMirror;
        while (component.getKind() == TypeKind.ARRAY) {
          dimensions++;
          component = ((ArrayType) component).getComponentType();
        }
        String leafName =
            component.getKind().isPrimitive()
                ? component.toString()
                : ((DeclaredType) component).asElement().toString();
        return MangledNameComputer.mangleArrayTypeName(leafName, dimensions);
      case DECLARED:
        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
        return MangledNameComputer.mangleTypeName(typeElement.getQualifiedName().toString());
      case TYPEVAR:
        return mangleType(types.erasure(typeMirror));
      default:
        return MangledNameComputer.mangleTypeName(typeMirror.toString());
    }
  }

  private MangledNameComputer.MemberVisibility getVisibility(Element element) {
    if (element.getModifiers().contains(Modifier.PRIVATE)) {
      return MangledNameComputer.MemberVisibility.PRIVATE;
    }
    if (element.getModifiers().contains(Modifier.PROTECTED)) {
      return MangledNameComputer.MemberVisibility.PROTECTED;
    }
    if (element.getModifiers().contains(Modifier.PUBLIC)) {
      return MangledNameComputer.MemberVisibility.PUBLIC;
    }
    return MangledNameComputer.MemberVisibility.PACKAGE_PRIVATE;
  }

  private String resolveJsPropertyName(Element element) {
    JsProperty jsProperty = element.getAnnotation(JsProperty.class);
    return jsProperty.name().equals("<auto>")
        ? element.getSimpleName().toString()
        : jsProperty.name();
  }
}
