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

import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.treblereel.j2cl.processors.common.resources.ImageResource;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.context.AptContext;

class ImageResourceGenerator extends AbstractResourceGenerator {

  ImageResourceGenerator(AptContext context) {
    super(
        context,
        ImageResource.class,
        ImageResource.class.getAnnotation(ResourcePrototype.DefaultExtensions.class));
  }

  @Override
  String initializer(Map<String, Object> root, TypeElement clientBundle, ExecutableElement method) {
    return null;
  }
}
