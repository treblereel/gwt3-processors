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

import static org.treblereel.j2cl.processors.common.resources.ClientBundle.*;

import com.google.auto.common.MoreElements;
import java.net.URL;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.common.resources.TextResource;
import org.treblereel.j2cl.processors.common.resources.exception.ResourceException;
import org.treblereel.j2cl.processors.context.AptContext;

class TextResourceGenerator extends AbstractResourceGenerator {

  TextResourceGenerator(AptContext context) {
    super(
        context,
        TextResource.class,
        TextResource.class.getAnnotation(ResourcePrototype.DefaultExtensions.class));
  }

  @Override
  String initializer(Map<String, Object> root, TypeElement clientBundle, ExecutableElement method) {
    root.put("getter", "getText");
    String resource = lookupResource(method);
    return write(resource);
  }

  private String lookupResource(ExecutableElement method) {
    String pkg =
        MoreElements.getPackage(method).getQualifiedName().toString().replaceAll("\\.", "/");
    if (method.getAnnotation(Source.class) != null) {
      Source source = method.getAnnotation(Source.class);
      for (int i = 0; i < source.value().length; i++) {
        String fullPath = pkg + "/" + source.value()[i];
        URL url = context.resourceOracle.findResource(fullPath);
        if (url != null) {
          return readURLAsString(url);
        }
      }
    } else {
      for (int i = 0; i < defaultExtensions.value().length; i++) {
        String extension = defaultExtensions.value()[i];
        String fullPath = pkg + "/" + method.getSimpleName().toString() + extension;
        URL url = context.resourceOracle.findResource(fullPath);
        if (url != null) {
          return readURLAsString(url);
        }
      }
    }
    throw new ResourceException(
        String.format(
            "Unable to find resource [%s] at %s.%s",
            TextResource.class.getSimpleName(),
            method.getEnclosingElement().toString(),
            method.getSimpleName().toString()));
  }
}