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
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;
import org.treblereel.j2cl.processors.annotations.GWT3Export;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;
import org.treblereel.j2cl.processors.generator.dto.ExportDTO;
import org.treblereel.j2cl.processors.generator.dto.MethodDTO;
import org.treblereel.j2cl.processors.generator.dto.PropertyDTO;
import org.treblereel.j2cl.processors.generator.resources.StringOutputStream;

public class GWT3ExportGenerator extends AbstractGenerator {

  protected final Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

  {
    cfg.setClassForTemplateLoading(this.getClass(), "/templates/resources");
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
  }

  private Template template;

  private final Map<TypeElement, ExportDTO> exportDTOs = new HashMap<>();

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

        if (!exportDTOs.containsKey(parent)) {
          exportDTOs.put(parent, getExportDTO(parent));
        }

        exportDTOs.get(parent).addMethod(getMethodDTO(parent, element));
      } else if (element.getKind().equals(ElementKind.FIELD)) {
        TypeElement parent = (TypeElement) element.getEnclosingElement();
        if (!exports.containsKey(parent)) {
          exports.put(checkClazz(parent), new HashSet<>());
        }
        if (!exportDTOs.containsKey(parent)) {
          exportDTOs.put(parent, getExportDTO(parent));
        }
        exportDTOs.get(parent).addProperty(getPropertyDTO(element));
      } else if (element.getKind().isClass()) {
        TypeElement parent = (TypeElement) element;
        exportDTOs.put(parent, getExportDTO(parent));

        Set<ExecutableElement> methods =
            ElementFilter.methodsIn(parent.getEnclosedElements()).stream()
                .filter(elm -> !elm.getModifiers().contains(Modifier.PRIVATE))
                .filter(elm -> !elm.getModifiers().contains(Modifier.NATIVE))
                .filter(elm -> !elm.getModifiers().contains(Modifier.ABSTRACT))
                .collect(Collectors.toSet());
        exports.put(checkClazz(parent), new HashSet<>());
        exports.get(parent).addAll(methods);

        getAllMethodsIn(parent).stream()
            .filter(elm -> !elm.getModifiers().contains(Modifier.PRIVATE))
            .filter(elm -> !elm.getModifiers().contains(Modifier.NATIVE))
            .filter(elm -> !elm.getModifiers().contains(Modifier.ABSTRACT))
            .forEach(elm -> exportDTOs.get(parent).addMethod(getMethodDTO(parent, elm)));

        getAllFieldsIn(parent).stream()
            .filter(elm -> !elm.getModifiers().contains(Modifier.PRIVATE))
            .filter(elm -> !elm.getModifiers().contains(Modifier.NATIVE))
            .filter(elm -> !elm.getModifiers().contains(Modifier.ABSTRACT))
            .forEach(elm -> exportDTOs.get(parent).addProperty(getPropertyDTO(elm)));
      }
    }
    exportDTOs.forEach(this::generate);
  }

  private void generate(TypeElement typeElement, ExportDTO exportDTO) {
    try {
      if (template == null) {
        template = cfg.getTemplate("export.ftlh");
      }
      String pkg = MoreElements.getPackage(typeElement).getQualifiedName().toString();

      StringOutputStream os = new StringOutputStream();
      try (Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
        if (template == null) {
          template = cfg.getTemplate("textresource.ftlh");
        }
        template.process(exportDTO, out);
      } catch (TemplateException | IOException e) {
        throw new GenerationException(e);
      }
      writeResource(typeElement.getSimpleName().toString() + "$$GWT3Export.js", pkg, os.toString());
    } catch (Exception e) {
      throw new GenerationException(e);
    }
  }

  private ExportDTO getExportDTO(TypeElement parent) {
    checkClazz(parent);
    TypeMirror type = context.getProcessingEnv().getTypeUtils().erasure(parent.asType());
    String name = getExportName(parent);
    String nameCtor = utils.getDefaultConstructor(parent).getMangledName();
    boolean isNative = parent.getAnnotation(JsType.class) != null;
    return new ExportDTO(name, type.toString(), type.toString(), nameCtor, isNative);
  }

  private MethodDTO getMethodDTO(TypeElement parent, Element m) {
    ExecutableElement method = checkMethod(m);
    String methodName = getSimpleName(method);
    String mangleName = utils.getMethodMangledName(method);
    return new MethodDTO(methodName, mangleName, m.getModifiers().contains(Modifier.STATIC));
  }

  private PropertyDTO getPropertyDTO(Element method) {
    VariableElement variableElement = checkProperty(method);
    String name = getSimpleName(variableElement);
    String mangleName = utils.getVariableMangledName(variableElement);

    return new PropertyDTO(name, mangleName);
  }

  private String getSimpleName(Element element) {
    if (element.getAnnotation(GWT3Export.class) == null
        || element.getAnnotation(GWT3Export.class).name().equals("<auto>")) {
      return element.getSimpleName().toString();
    } else {
      return element.getAnnotation(GWT3Export.class).name();
    }
  }

  private String getExportName(Element element) {
    StringBuffer stringBuffer = new StringBuffer();
    if (element.getAnnotation(GWT3Export.class) == null
        || element.getAnnotation(GWT3Export.class).namespace().equals("<auto>")) {
      stringBuffer
          .append(MoreElements.getPackage(element).getQualifiedName().toString())
          .append(".");
    } else {
      String namespace = element.getAnnotation(GWT3Export.class).namespace();
      if (!namespace.isEmpty()) {
        stringBuffer.append(namespace).append(".");
      }
    }

    if (element.getAnnotation(GWT3Export.class) == null
        || element.getAnnotation(GWT3Export.class).name().equals("<auto>")) {
      stringBuffer.append(element.getSimpleName().toString());
    } else {
      stringBuffer.append(element.getAnnotation(GWT3Export.class).name());
    }
    return stringBuffer.toString();
  }

  private ExecutableElement checkMethod(Element candidate) {
    ExecutableElement method = (ExecutableElement) candidate;

    if (method.getModifiers().contains(Modifier.PRIVATE)) {
      throw new GenerationException(
          method,
          "Method, annotated with "
              + GWT3Export.class.getCanonicalName()
              + ", must not be private");
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

  private VariableElement checkProperty(Element element) {
    return (VariableElement) element;
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
        new HashSet<>(ElementFilter.constructorsIn(parent.getEnclosedElements()));
    if (parent.getAnnotation(JsType.class) == null && !constructors.isEmpty()) {
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

  private Set<ExecutableElement> getAllMethodsIn(TypeElement parent) {
    Set<ExecutableElement> elements = new HashSet<>();
    Queue<TypeElement> queue = new LinkedList<>();
    queue.add(parent);
    while (!queue.isEmpty()) {
      TypeElement current = queue.poll();
      elements.addAll(ElementFilter.methodsIn(current.getEnclosedElements()));
      if (!current.getSuperclass().toString().equals("java.lang.Object")
          && MoreTypes.asElement(current.getSuperclass()).getKind().isClass()) {
        queue.offer((TypeElement) MoreTypes.asElement(current.getSuperclass()));
      }
    }
    return elements;
  }

  private Set<VariableElement> getAllFieldsIn(TypeElement parent) {
    Set<VariableElement> elements = new HashSet<>();
    Queue<TypeElement> queue = new LinkedList<>();
    queue.add(parent);
    while (!queue.isEmpty()) {
      TypeElement current = queue.poll();
      elements.addAll(ElementFilter.fieldsIn(current.getEnclosedElements()));
      if (!current.getSuperclass().toString().equals("java.lang.Object")
          && MoreTypes.asElement(current.getSuperclass()).getKind().isClass()) {
        queue.offer((TypeElement) MoreTypes.asElement(current.getSuperclass()));
      }
    }
    return elements;
  }
}
