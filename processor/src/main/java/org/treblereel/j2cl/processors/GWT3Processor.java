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

package org.treblereel.j2cl.processors;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.generator.AbstractGenerator;
import org.treblereel.j2cl.processors.generator.ES6ModuleShimGenerator;
import org.treblereel.j2cl.processors.generator.GWT3EntryPointGenerator;
import org.treblereel.j2cl.processors.generator.GWT3ExportGenerator;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
  "org.treblereel.j2cl.processors.annotations.GWT3EntryPoint",
  "org.treblereel.j2cl.processors.annotations.ES6Module",
  "org.treblereel.j2cl.processors.annotations.GWT3Export"
})
public class GWT3Processor extends AbstractProcessor {

  private final String NATIVE_JS = ".native.js";

  @Override
  public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {
    if (elements.isEmpty()) {
      return false;
    }

    AptContext context = new AptContext(roundEnv, processingEnv);

    new GWT3EntryPointGenerator(context);
    new ES6ModuleShimGenerator(context);
    new GWT3ExportGenerator(context);

    Map<TypeElement, StringBuffer> beans = new HashMap<>();

    for (TypeElement element : elements) {
      if (context.isAnnotationSupported(element.getQualifiedName().toString())) {
        for (AbstractGenerator generator :
            context.getRegistredGeneratorsByAnnotation(element.getQualifiedName().toString())) {
          for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(element)) {
            TypeElement parent = null;
            if (annotatedElement.getKind().isClass()) {
              parent = MoreElements.asType(annotatedElement);
            } else if (annotatedElement.getKind().equals(ElementKind.METHOD)) {
              parent = MoreElements.asType(annotatedElement.getEnclosingElement());
            }
            if (!beans.containsKey(parent)) {
              beans.put(parent, new StringBuffer());
            }
            StringBuffer source = beans.get(parent);
            generator.generate(annotatedElement, source);
          }
        }
      }
    }
    for (Map.Entry<TypeElement, StringBuffer> typeElementStringBufferEntry : beans.entrySet()) {
      if (typeElementStringBufferEntry.getValue() != null
          && !typeElementStringBufferEntry.getValue().toString().isEmpty()) {
        String className = typeElementStringBufferEntry.getKey().getSimpleName().toString();
        String pkg =
            MoreElements.getPackage(typeElementStringBufferEntry.getKey())
                .getQualifiedName()
                .toString();
        context.writeResource(
            className + NATIVE_JS, pkg, typeElementStringBufferEntry.getValue().toString());
      }
    }
    return false;
  }
}
