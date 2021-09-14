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

package org.treblereel.j2cl.processors.context;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.treblereel.j2cl.processors.exception.GenerationException;
import org.treblereel.j2cl.processors.generator.AbstractGenerator;

public class AptContext {

  private final RoundEnvironment roundEnv;
  private final ProcessingEnvironment processingEnv;
  private final Map<String, List<AbstractGenerator>> generators = new HashMap<>();

  public AptContext(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
    this.roundEnv = roundEnv;
    this.processingEnv = processingEnv;
  }

  public void register(Class<? extends Annotation> annotation, AbstractGenerator generator) {
    if (!generators.containsKey(annotation.getCanonicalName())) {
      generators.put(annotation.getCanonicalName(), new ArrayList<>());
    }
    generators.get(annotation.getCanonicalName()).add(generator);
  }

  public RoundEnvironment getRoundEnv() {
    return roundEnv;
  }

  public ProcessingEnvironment getProcessingEnv() {
    return processingEnv;
  }

  public boolean isAnnotationSupported(String annotation) {
    return generators.containsKey(annotation);
  }

  public List<AbstractGenerator> getRegistredGeneratorsByAnnotation(String annotation) {
    return generators.get(annotation);
  }

  public void writeResource(String filename, String path, String content) {
    try {
      FileObject file =
          processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, path, filename);
      PrintWriter pw =
          new PrintWriter(new OutputStreamWriter(file.openOutputStream(), "UTF-8"), false);
      pw.print(content);
      pw.close();
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write file: " + e);
      throw new GenerationException("Failed to write file: " + e, e);
    }
  }
}
