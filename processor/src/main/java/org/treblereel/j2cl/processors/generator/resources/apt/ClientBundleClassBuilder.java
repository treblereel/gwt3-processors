/*
 *
 * Copyright © ${year} ${name}
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
package org.treblereel.j2cl.processors.generator.resources.apt;

import org.treblereel.gwt.resources.context.AptContext;
import org.treblereel.gwt.resources.context.InlineClientBundleGenerator;
import org.treblereel.gwt.resources.ext.StandardGeneratorContext;
import org.treblereel.gwt.resources.ext.TreeLogger;
import org.treblereel.gwt.resources.ext.UnableToCompleteException;

import javax.lang.model.element.TypeElement;
import java.util.Set;

/** @author Dmitrii Tikhomirov Created by treblereel 11/11/18 */
public class ClientBundleClassBuilder {
  private final TreeLogger logger;
  private final AptContext context;

  private final Set<TypeElement> elements;

  public ClientBundleClassBuilder(
      TreeLogger logger, AptContext context, Set<TypeElement> elements) {
    this.logger = logger;
    this.context = context;
    this.elements = elements;
  }

  public void process() throws UnableToCompleteException {
    StandardGeneratorContext standardGeneratorContext = new StandardGeneratorContext(context);
    InlineClientBundleGenerator inlineClientBundleGenerator = new InlineClientBundleGenerator();
    inlineClientBundleGenerator.generate(logger, standardGeneratorContext, elements);
  }
}
