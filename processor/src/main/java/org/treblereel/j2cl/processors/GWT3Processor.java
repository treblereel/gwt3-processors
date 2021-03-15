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

import com.google.auto.service.AutoService;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import org.treblereel.j2cl.processors.context.AptContext;
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

  @Override
  public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {
    if (elements.isEmpty()) {
      return false;
    }

    AptContext context = new AptContext(roundEnv, processingEnv);

    new GWT3EntryPointGenerator(context);
    new ES6ModuleShimGenerator(context);
    new GWT3ExportGenerator(context);

    for (TypeElement element : elements) {
      if (context.isAnnotationSupported(element.getQualifiedName().toString())) {
        context
            .getRegistredGeneratorsByAnnotation(element.getQualifiedName().toString())
            .forEach(
                generator ->
                    roundEnv.getElementsAnnotatedWith(element).forEach(generator::generate));
      }
    }
    return false;
  }
}
