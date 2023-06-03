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
package org.treblereel.j2cl.processors.generator.resources.ext;

import org.treblereel.j2cl.processors.logger.TreeLogger;

import javax.lang.model.element.ExecutableElement;

/**
 * A base class providing common methods for ResourceGenerator implementations.
 *
 * @see ResourceGeneratorUtil
 */
public abstract class AbstractResourceGenerator implements ResourceGenerator {
  protected static final boolean STRIP_COMMENTS =
      System.getProperty("gwt.resourceBundle.stripComments") != null;

  public abstract String createAssignment(
          TreeLogger logger, ResourceContext context, ExecutableElement method, String locale)
      throws UnableToCompleteException;

  /** A no-op implementation. */
  public void createFields(TreeLogger logger, ResourceContext context, ClientBundleFields fields)
      throws UnableToCompleteException {}

  /** A no-op implementation. */
  public void finish(TreeLogger logger, ResourceContext context) {}

  /** A no-op implementation. */
  public void init(TreeLogger logger, ResourceContext context) throws UnableToCompleteException {}

  /** A no-op implementation. */
  public void prepare(
      TreeLogger logger, ResourceContext context, ExecutableElement method, String locale)
      throws UnableToCompleteException {}
}
