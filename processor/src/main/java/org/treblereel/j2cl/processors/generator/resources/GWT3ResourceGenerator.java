/*
 * Copyright © 2023 treblereel
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

package org.treblereel.j2cl.processors.generator.resources;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import org.treblereel.j2cl.processors.annotations.GWT3Resource;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.common.resources.DataResource;
import org.treblereel.j2cl.processors.common.resources.ImageResource;
import org.treblereel.j2cl.processors.common.resources.TextResource;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;
import org.treblereel.j2cl.processors.generator.AbstractGenerator;

public class GWT3ResourceGenerator extends AbstractGenerator {

  protected final Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

  {
    cfg.setClassForTemplateLoading(this.getClass(), "/templates/resources");
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
  }

  private final Types types;
  private final Elements elements;

  private final TypeMirror clientBundle;

  private Template temp;

  private final TypeMirror textResource;
  private final TypeMirror dataResource;
  private final TypeMirror imageResource;

  private final Map<TypeMirror, AbstractResourceGenerator> generators = new HashMap<>();

  public GWT3ResourceGenerator(AptContext context) {
    super(context, GWT3Resource.class);
    this.types = context.getProcessingEnv().getTypeUtils();
    this.elements = context.getProcessingEnv().getElementUtils();

    this.clientBundle = elements.getTypeElement(ClientBundle.class.getCanonicalName()).asType();
    this.textResource = elements.getTypeElement(TextResource.class.getCanonicalName()).asType();
    this.dataResource = elements.getTypeElement(DataResource.class.getCanonicalName()).asType();
    this.imageResource = elements.getTypeElement(ImageResource.class.getCanonicalName()).asType();
    generators.put(textResource, new TextResourceGenerator(context));
    generators.put(dataResource, new DataResourceGenerator(context));
    generators.put(imageResource, new ImageResourceGenerator(context));
  }

  @Override
  public void generate(Set<Element> elements) {
    elements.stream().map(this::validate).forEach(this::generate);
  }

  private void generate(TypeElement clientBundle) {
    Map<String, Object> root = new HashMap<>();
    String pkg = elements.getPackageOf(clientBundle).getQualifiedName().toString();
    String className = classImplName(clientBundle);

    String parent = parentClassName(clientBundle);
    root.put("package", pkg);
    root.put("className", className);
    root.put("parent", parent);
    root.put("resources", new ArrayList<String>());
    root.put("methods", new ArrayList<String>());

    processFields(root, clientBundle);

    write(root, fullClassName(clientBundle) + "Impl");
  }

  private void processFields(Map<String, Object> root, TypeElement clientBundle) {
    ElementFilter.methodsIn(clientBundle.getEnclosedElements()).stream()
        .filter(method -> method.getParameters().isEmpty())
        .forEach(
            method -> {
              TypeMirror rtrn = method.getReturnType();
              if (generators.containsKey(rtrn)) {
                generators.get(rtrn).generate(root, clientBundle, method);
              }
            });
  }

  private TypeElement validate(Element element) {
    if (!element.getKind().isInterface()) {
      throw new Error("GWT3Resource annotation can be used only on interfaces");
    }
    if (!types.isSubtype(element.asType(), clientBundle)) {
      throw new Error(
          "GWT3Resource annotation can be used only on interfaces that extends ClientBundle");
    }
    if (element.getModifiers().contains(Modifier.PRIVATE)) {
      throw new Error("Interface annotated with GWT3Resource can not be private");
    }

    return (TypeElement) element;
  }

  protected void write(Map<String, Object> root, String fileName) {
    StringOutputStream os = new StringOutputStream();
    try (Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
      if (temp == null) {
        temp = cfg.getTemplate("clientbundle.ftlh");
      }
      temp.process(root, out);
      JavaFileObject sourceFile = context.getProcessingEnv().getFiler().createSourceFile(fileName);
      try (Writer writer = sourceFile.openWriter()) {
        writer.write(os.toString());
      }
    } catch (FilerException e) {
      System.out.println("FilerException: " + e.getMessage());
      throw new GenerationException(e);
    } catch (TemplateException | IOException e) {
      throw new GenerationException(e);
    }
  }

  private String fullClassName(TypeElement clientBundle) {
    String pkg = elements.getPackageOf(clientBundle).getQualifiedName().toString();

    StringBuilder sb = new StringBuilder();
    if (pkg != null) {
      sb.append(pkg);
      sb.append(".");
    }
    if (!clientBundle.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)) {
      sb.append(clientBundle.getEnclosingElement().getSimpleName());
      sb.append("_");
    }
    sb.append(clientBundle.getSimpleName());
    return sb.toString();
  }

  private String parentClassName(TypeElement clientBundle) {
    String pkg = elements.getPackageOf(clientBundle).getQualifiedName().toString();

    StringBuilder sb = new StringBuilder();
    if (pkg != null) {
      sb.append(pkg);
      sb.append(".");
    }
    if (!clientBundle.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)) {
      sb.append(clientBundle.getEnclosingElement().getSimpleName());
      sb.append(".");
    }
    sb.append(clientBundle.getSimpleName());

    return sb.toString();
  }

  private String classImplName(TypeElement clientBundle) {
    StringBuilder sb = new StringBuilder();
    if (!clientBundle.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)) {
      sb.append(clientBundle.getEnclosingElement().getSimpleName());
      sb.append("_");
    }
    sb.append(clientBundle.getSimpleName());
    sb.append("Impl");
    return sb.toString();
  }
}
