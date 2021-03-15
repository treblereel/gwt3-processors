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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

public abstract class AbstractGenerator {

  protected final AptContext context;

  public AbstractGenerator(AptContext context, Class<? extends Annotation> annotation) {
    this.context = context;
    context.register(annotation, this);
  }

  public abstract void generate(Element element);

  protected void writeResource(String filename, String path, String content) {
    try {
      FileObject file =
          context
              .getProcessingEnv()
              .getFiler()
              .createResource(StandardLocation.SOURCE_OUTPUT, path, filename);
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(file.openOutputStream(), "UTF-8"));
      pw.print(content);
      pw.close();
    } catch (IOException e) {
      context
          .getProcessingEnv()
          .getMessager()
          .printMessage(Diagnostic.Kind.ERROR, "Failed to write file: " + e);
      throw new GenerationException("Failed to write file: " + e, e);
    }
  }
}
