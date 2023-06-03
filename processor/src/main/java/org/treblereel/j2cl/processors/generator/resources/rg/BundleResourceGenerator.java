/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.treblereel.j2cl.processors.generator.resources.rg;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import org.treblereel.j2cl.processors.generator.resources.ext.AbstractResourceGenerator;
import org.treblereel.j2cl.processors.generator.resources.ext.ResourceContext;
import org.treblereel.j2cl.processors.generator.resources.ext.ResourceGeneratorUtil;
import org.treblereel.j2cl.processors.logger.TreeLogger;


/** This is a special case of ResourceGenerator that handles nested bundles. */
public final class BundleResourceGenerator extends AbstractResourceGenerator {

  @Override
  public String createAssignment(
          TreeLogger logger, ResourceContext context, ExecutableElement method, String locale) {
    TypeMirror toReturn = method.getReturnType();
    String implName =
        MoreElements.getPackage(MoreTypes.asTypeElement(toReturn))
            + "."
            + ResourceGeneratorUtil.generateSimpleSourceName(
                logger, MoreTypes.asTypeElement(toReturn), locale);
    return "new " + implName + "();";
  }
}
