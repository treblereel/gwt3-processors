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
import com.google.auto.common.MoreTypes;
import com.google.j2cl.transpiler.ast.DeclaredTypeDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
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
  public void generate(Set<Element> elements) {
    HashMap<TypeElement, Set<ExecutableElement>> exports = new HashMap<>();

    for (Element element : elements) {
      if (element.getKind().equals(ElementKind.METHOD)) {
        TypeElement parent = (TypeElement) element.getEnclosingElement();
        if (!exports.containsKey(parent)) {
          exports.put(checkClazz(parent), new HashSet<>());
        }
        exports.get(parent).add(checkMethod(element));
      } else if (element.getKind().isClass()) {
        TypeElement parent = (TypeElement) element;
        Set<ExecutableElement> methods =
            ElementFilter.methodsIn(parent.getEnclosedElements()).stream()
                .filter(elm -> elm.getModifiers().contains(Modifier.PUBLIC))
                .filter(elm -> !elm.getModifiers().contains(Modifier.NATIVE))
                .filter(elm -> !elm.getModifiers().contains(Modifier.ABSTRACT))
                .collect(Collectors.toSet());
        exports.put(checkClazz(parent), methods);
      }
    }

    exports.forEach(this::generate);
  }

  private ExecutableElement checkMethod(Element candidate) {
    ExecutableElement method = (ExecutableElement) candidate;

    if (!method.getModifiers().contains(Modifier.PUBLIC)) {
      throw new GenerationException(
          method,
          "Method, annotated with " + GWT3Export.class.getCanonicalName() + ", must be public");
    }
    if (method.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new GenerationException(
          method,
          "Method, annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", mustn't be abstract");
    }

    if (method.getModifiers().contains(Modifier.NATIVE)) {
      throw new GenerationException(
          method,
          "Method, annotated with " + GWT3Export.class.getCanonicalName() + ", mustn't be native");
    }
    return method;
  }

  private TypeElement checkClazz(TypeElement parent) {

    if (!parent.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)) {
      throw new GenerationException(
          parent,
          "Class, that contains methods annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", must be top level class");
    }

    if (!parent.getModifiers().contains(Modifier.PUBLIC)) {
      throw new GenerationException(
          parent,
          "Class, that contains methods annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", must be public");
    }
    if (parent.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new GenerationException(
          parent,
          "Class,  that contains methods annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", mustn't be abstract");
    }
    if (parent.getAnnotation(JsType.class) != null
        && parent.getAnnotation(JsType.class).isNative()) {
      throw new GenerationException(
          parent,
          "If Class, that contains methods annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", is @JsType, it mustn't be isNative=true");
    }
    if (parent.getAnnotation(GWT3EntryPoint.class) != null) {
      throw new GenerationException(
          parent,
          "If Class,  that contains methods annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", mustn't be annotated with @GWT3EntryPoint");
    }
    if (parent.getAnnotation(ES6Module.class) != null) {
      throw new GenerationException(
          parent,
          "If Class,  that contains methods annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", mustn't be annotated with @ES6Module");
    }
    Set<ExecutableElement> constructors =
        ElementFilter.constructorsIn(parent.getEnclosedElements()).stream()
            .collect(Collectors.toSet());
    if (!constructors.isEmpty()) {
      constructors.stream()
          .filter(elm -> elm.getModifiers().contains(Modifier.PUBLIC))
          .filter(elm -> elm.getParameters().isEmpty())
          .findAny()
          .orElseThrow(
              () ->
                  new GenerationException(
                      parent,
                      "Class,  that contains methods annotated with "
                          + GWT3Export.class.getCanonicalName()
                          + ", must have public constructor"));
    }

    return parent;
  }

  private void generate(TypeElement parent, Set<ExecutableElement> elements) {

    StringBuffer source = new StringBuffer();
    generateClassExport(parent, source);
    generateMethods(parent, elements, source);

    String className = parent.getSimpleName().toString();
    String classPkg = MoreElements.getPackage(parent).getQualifiedName().toString();

    writeResource(className + ".native.js", classPkg, source.toString());
  }

  private void generateMethods(
      TypeElement parent, Set<ExecutableElement> elements, StringBuffer source) {
    elements.forEach(method -> generateMethod(method, parent, source));
  }

  private void generateClassExport(TypeElement parent, StringBuffer source) {
    generateClassWrapper(parent, source);
    source.append(System.lineSeparator());

    source.append("goog.exportSymbol('");
    source.append(getTypeName(parent));
    source.append("', _");
    source.append(parent.getSimpleName());
    source.append(");");
    source.append(System.lineSeparator());
  }

  private void generateClassWrapper(TypeElement parent, StringBuffer source) {
    String nameCtor = utils.getDefaultConstructor(parent).getMangledName();

    source.append("class _");
    source.append(parent.getSimpleName());
    source.append(" extends ");
    source.append(parent.getSimpleName().toString().replaceAll("_", "__"));
    source.append(" {");
    source.append(System.lineSeparator());

    source.append("    constructor() {");
    source.append(System.lineSeparator());
    source.append("        super();");
    source.append(System.lineSeparator());
    source.append(String.format("        this.%s();", nameCtor));

    source.append("    }");
    source.append(System.lineSeparator());
    source.append("}");
    source.append(System.lineSeparator());
  }

  private String getTypeName(TypeElement parent) {
    GWT3Export gwt3Export = parent.getAnnotation(GWT3Export.class);
    String pkg = MoreElements.getPackage(parent).getQualifiedName().toString();
    String clazz = parent.getSimpleName().toString();

    if (gwt3Export != null) {
      if (!gwt3Export.name().equals("<auto>") && !gwt3Export.name().isEmpty()) {
        clazz = parent.getAnnotation(GWT3Export.class).name();
      }
      if (!gwt3Export.namespace().equals("<auto>")) {
        pkg = gwt3Export.namespace();
      }
    }

    return (!pkg.isEmpty() ? pkg + "." : "") + clazz;
  }

  private void generateMethod(ExecutableElement element, TypeElement parent, StringBuffer source) {

    source.append("goog.exportSymbol('");
    source.append(getTypeName(parent));
    source.append(".");
    if (!element.getModifiers().contains(Modifier.STATIC)) {
      source.append("prototype.");
    }

    if (element.getAnnotation(GWT3Export.class) == null
        || element.getAnnotation(GWT3Export.class).name().equals("<auto>")) {
      source.append(element.getSimpleName().toString());
    } else {
      source.append(element.getAnnotation(GWT3Export.class).name());
    }

    source.append("', ");
    source.append(parent.getSimpleName().toString().replaceAll("_", "__"));
    getMethodName(element, parent, source);
    source.append(");");
    source.append(System.lineSeparator());
  }

  private void getMethodName(ExecutableElement element, TypeElement parent, StringBuffer source) {
    DeclaredType declaredType = MoreTypes.asDeclared(parent.asType());

    DeclaredTypeDescriptor enclosingTypeDescriptor =
        utils.createDeclaredTypeDescriptor(declaredType);

    String methodName =
        utils.createDeclarationMethodDescriptor(element, enclosingTypeDescriptor).getMangledName();

    source.append(".");
    if (!element.getModifiers().contains(Modifier.STATIC)) {
      source.append("prototype.");
    }
    source.append(methodName);
  }
}
