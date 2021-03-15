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
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.*;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

public class ES6ModuleShimGenerator extends AbstractGenerator {

  private final String CLOSURE_JS = ".closure.js";
  private final String SHIM_JS = ".shim.js";

  public ES6ModuleShimGenerator(AptContext context) {
    super(context, ES6Module.class);
  }

  @Override
  public void generate(Element element) {
    JsType jsType = check(element);
    process(element, jsType);
  }

  private JsType check(Element element) {
    JsType jsType = element.getAnnotation(JsType.class);
    if (jsType == null) {
      throw new GenerationException("@ES6Module class must be annotated with @JsType annotation");
    }
    if (!jsType.isNative()) {
      throw new GenerationException(
          "@ES6Module class must be annotated with @JsType.isNative=true annotation");
    }
    return jsType;
  }

  private void process(Element element, JsType jsType) {
    TypeElement typeElement = MoreElements.asType(element);
    String clazzName =
        jsType.name().equals("<auto>") ? typeElement.getSimpleName().toString() : jsType.name();
    String moduleFileName;
    Optional<String> isPathDefined = isPathDefined(element);
    if (isPathDefined.isPresent()) {
      moduleFileName = isPathDefined.get();
    } else {
      moduleFileName = clazzName + ".js";
    }

    generateClosure(typeElement, clazzName);
    generateShim(typeElement, clazzName, moduleFileName);
  }

  private Optional<String> isPathDefined(Element element) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (annotationMirror
          .getAnnotationType()
          .asElement()
          .getSimpleName()
          .toString()
          .equals("ES6Module")) {
        if (!annotationMirror.getElementValues().isEmpty()) {
          Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry =
              annotationMirror.getElementValues().entrySet().iterator().next();
          if (entry.getKey().toString().equals("value()")) {
            String value = entry.getValue().getValue().toString();
            if (!value.equals("<auto>")) {
              return Optional.of(value);
            }
          }
        }
      }
    }
    return Optional.empty();
  }

  private void generateClosure(TypeElement typeElement, String clazzName) {
    String pkg = MoreElements.getPackage(typeElement).getQualifiedName().toString();

    StringBuffer source = new StringBuffer();
    source.append("goog.module('");
    source.append(pkg + "." + clazzName);
    source.append("');");
    source.append(System.lineSeparator());
    source.append("const shim = goog.require('");
    source.append(typeElement.getQualifiedName());
    source.append(".shim');");
    source.append(System.lineSeparator());

    source.append("exports = shim.");
    source.append(clazzName);
    source.append(";");
    source.append(System.lineSeparator());

    writeResource(typeElement.getSimpleName() + CLOSURE_JS, pkg, source.toString());
  }

  private void generateShim(TypeElement typeElement, String clazzName, String moduleFileName) {
    String pkg = MoreElements.getPackage(typeElement).getQualifiedName().toString();
    StringBuffer source = new StringBuffer();

    source.append("import {");
    source.append(clazzName);
    source.append("} from './");
    source.append(moduleFileName);
    source.append("';");
    source.append(System.lineSeparator());

    source.append("goog.declareModuleId('");
    source.append(typeElement.getQualifiedName());
    source.append(".shim');");
    source.append(System.lineSeparator());

    source.append("export {");
    source.append(clazzName);
    source.append("};");
    source.append(System.lineSeparator());

    writeResource(typeElement.getSimpleName() + SHIM_JS, pkg, source.toString());
  }
}
