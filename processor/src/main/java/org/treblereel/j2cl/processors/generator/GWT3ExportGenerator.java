/*
 * Copyright © 2021
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

package org.treblereel.j2cl.processors.generator;

import com.google.auto.common.MoreElements;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;
import org.treblereel.j2cl.processors.annotations.GWT3Export;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

public class GWT3ExportGenerator extends AbstractGenerator {

  public GWT3ExportGenerator(AptContext context) {
    super(context, GWT3Export.class);
  }

  @Override
  public void generate(Element element, StringBuffer source) {
    TypeElement parent = (TypeElement) element;

    check(parent);

    Set<Element> elements =
        parent.getEnclosedElements().stream()
            .filter(elm -> elm.getKind().isField() || elm.getKind().equals(ElementKind.METHOD))
            .filter(elm -> elm.getModifiers().contains(Modifier.PUBLIC))
            .collect(Collectors.toSet());

    elements.forEach(this::check);

    generate(parent, elements, source);
  }

  private void check(TypeElement parent) {
    if (!parent.getModifiers().contains(Modifier.PUBLIC)) {
      throw new GenerationException(
          "Class,  that contains methods/fields annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", must be public");
    }
    if (parent.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new GenerationException(
          "Class,  that contains methods/fields annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", mustn't be abstract");
    }
    if (parent.getAnnotation(JsType.class) != null
        && parent.getAnnotation(JsType.class).isNative()) {
      throw new GenerationException(
          "If Class,  that contains methods/fields annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", is @JsType, it mustn't be isNative=true");
    }
    if (parent.getAnnotation(GWT3EntryPoint.class) != null) {
      throw new GenerationException(
          "If Class,  that contains methods/fields annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", mustn't be annotated with @GWT3EntryPoint");
    }
    if (parent.getAnnotation(ES6Module.class) != null) {
      throw new GenerationException(
          "If Class,  that contains methods/fields annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", mustn't be annotated with @ES6Module");
    }
  }

  private void check(Element element) {
    if (element.getKind().isField()) {
      check((VariableElement) element);
    } else {
      check((ExecutableElement) element);
    }
  }

  private void generate(TypeElement parent, Set<Element> elements, StringBuffer source) {
    generateClassExport(parent, source);
    generateStaticFieldsOrMethods(parent, elements, source);
  }

  private void check(VariableElement field) {
    if (!field.getModifiers().contains(Modifier.PUBLIC)) {
      throw new GenerationException(
          "Field,  annotated with " + GWT3Export.class.getCanonicalName() + ", must be public");
    }
  }

  private void check(ExecutableElement method) {
    if (!method.getModifiers().contains(Modifier.PUBLIC)) {
      throw new GenerationException(
          "Method,  annotated with " + GWT3Export.class.getCanonicalName() + ", must be public");
    }
    if (method.getModifiers().contains(Modifier.NATIVE)) {
      throw new GenerationException(
          "Method,  annotated with " + GWT3Export.class.getCanonicalName() + ", mustn't be native");
    }
  }

  private void generateClassExport(TypeElement parent, StringBuffer source) {
    source.append("const _");
    source.append(parent.getSimpleName());
    source.append(" = ");

    if (parent.getAnnotation(JsType.class) == null) {
      source.append(parent.getSimpleName().toString().replaceAll("_", "__"));
      source.append(".$create__;");
    } else {
      getNativeFullName(parent, source);
    }
    source.append(";");

    source.append(System.lineSeparator());

    source.append("goog.exportSymbol('");
    maybeAddNamespace(parent.getAnnotation(GWT3Export.class), source);
    source.append(getTypeName(parent));
    source.append("', _");
    source.append(parent.getSimpleName());
    source.append(");");
    source.append(System.lineSeparator());
  }

  private void getNativeFullName(TypeElement parent, StringBuffer source) {
    String pkg =
        MoreElements.getPackage(parent).getQualifiedName().toString().replaceAll("\\.", "_");
    String clazz = parent.getSimpleName().toString().replaceAll("_", "__");
    source.append(pkg);
    source.append("_");
    source.append(clazz);
  }

  private void generateStaticFieldsOrMethods(
      TypeElement parent, Set<Element> elements, StringBuffer source) {
    elements.stream()
        .forEach(
            element -> {
              if (element.getKind().isField()) {
                if (element.getModifiers().contains(Modifier.STATIC)) {
                  generateStaticField((VariableElement) element, parent, source);
                } else if (element.getAnnotation(JsProperty.class) == null
                    && element.getEnclosingElement().getAnnotation(JsType.class) == null) {
                  generateField((VariableElement) element, parent, source);
                }
              } else {
                generateMethod((ExecutableElement) element, parent, source);
              }
            });
  }

  private void maybeAddNamespace(GWT3Export gwt3Export, StringBuffer stringBuffer) {
    if (gwt3Export != null
        && !gwt3Export.namespace().equals("<auto>")
        && !gwt3Export.name().isEmpty()) {
      stringBuffer.append(gwt3Export.namespace()).append(".");
    }
  }

  private String getTypeName(TypeElement parent) {
    GWT3Export gwt3Export = parent.getAnnotation(GWT3Export.class);
    if (gwt3Export != null && !gwt3Export.name().equals("<auto>") && !gwt3Export.name().isEmpty()) {
      return parent.getAnnotation(GWT3Export.class).name();
    }
    return parent.getQualifiedName().toString();
  }

  private void generateStaticField(
      VariableElement element, TypeElement parent, StringBuffer source) {
    source.append("goog.exportProperty(_");
    source.append(parent.getSimpleName());
    source.append(", '");
    source.append(element.getSimpleName());
    source.append("', ");
    source.append(parent.getSimpleName().toString().replaceAll("_", "__"));
    source.append(".$static_");
    source.append(element.getSimpleName());
    source.append("__");
    source.append(parent.getQualifiedName().toString().replaceAll("\\.", "_"));
    source.append(");");
    source.append(System.lineSeparator());
  }

  private void generateField(VariableElement element, TypeElement parent, StringBuffer source) {
    // TODO find out how to export non-static property on non-jstyped class
  }

  private void generateMethod(ExecutableElement element, TypeElement parent, StringBuffer source) {
    source.append("goog.exportSymbol('");
    maybeAddNamespace(parent.getAnnotation(GWT3Export.class), source);
    source.append(getTypeName(parent));
    source.append(".");
    if (!element.getModifiers().contains(Modifier.STATIC)) {
      source.append("prototype.");
    }
    source.append(element.getSimpleName().toString());
    source.append("', ");
    source.append(parent.getSimpleName().toString().replaceAll("_", "__"));
    getMethodName(element, source);
    source.append(");");
    source.append(System.lineSeparator());
  }

  private void getMethodName(ExecutableElement element, StringBuffer source) {
    source.append(".");
    if (!element.getModifiers().contains(Modifier.STATIC)) {
      source.append("prototype.");
    }

    if (element.getAnnotation(JsMethod.class) != null
        || element.getEnclosingElement().getAnnotation(JsType.class) != null) {
      source.append(element.getSimpleName().toString());
    } else {
      source.append("m_");
      source.append(element.getSimpleName().toString());
      source.append("__");
      source.append(element.getReturnType().toString().replaceAll("\\.", "_"));
    }
  }
}
