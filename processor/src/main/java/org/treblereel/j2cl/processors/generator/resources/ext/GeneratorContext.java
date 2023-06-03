/*
 * Copyright 2007 Google Inc.
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

import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.logger.TreeLogger;

import java.io.OutputStream;
import java.io.PrintWriter;

/** Provides metadata to deferred binding generators. */
public interface GeneratorContext {

  /**
   * Checks whether a rebind rule is available for a given sourceTypeName, such as can appear in a
   * replace-with or generate-with rule.
   *
   * @param sourceTypeName the name of a type to check for rebind rule availability.
   * @return true if a rebind rule is available
   */
  boolean checkRebindRuleAvailable(String sourceTypeName);

  /** Commits source generation begun with {@link #tryCreate(TreeLogger, String, String)}. */
  void commit(TreeLogger logger, PrintWriter pw);

  /**
   * Commits resource generation begun with {@link #tryCreateResource(TreeLogger, String)}.
   *
   * @return the GeneratedResource that was created as a result of committing the OutputStream.
   * @throws UnableToCompleteException if the resource cannot be written to disk, if the specified
   *     stream is unknown, or if the stream has already been committed
   */
  void commitResource(TreeLogger logger, OutputStream os) throws UnableToCompleteException;

  /**
   * Returns a resource oracle containing all resources that are mapped into the module's source (or
   * super-source) paths. Conceptually, this resource oracle exposes resources which are "siblings"
   * to GWT-compatible Java classes. For example, if the module includes <code>
   * com.google.gwt.core.client</code> as a source package, then a resource at <code>
   * com/google/gwt/core/client/Foo.properties</code> would be exposed by this resource oracle.
   */
  ResourceOracle getResourcesOracle();

  /** Returns an apt context */
  AptContext getAptContext();

  /**
   * Attempts to get a <code>PrintWriter</code> so that the caller can generate the source code for
   * the named type. If the named types already exists, <code>null</code> is returned to indicate
   * that no work needs to be done. The file is not committed until {@link #commit(TreeLogger,
   * PrintWriter)} is called.
   *
   * @param logger a logger; normally the logger passed into the currently invoked generator, or a
   *     branch thereof
   * @param packageName the name of the package to which the create type belongs
   * @param simpleName the unqualified source name of the type being generated
   * @return <code>null</code> if the package and class already exists, otherwise a <code>
   *     PrintWriter</code> is returned.
   */
  PrintWriter tryCreate(TreeLogger logger, String packageName, String simpleName)
      throws UnableToCompleteException;

  /**
   * Attempts to get an <code>OutputStream</code> so that the caller can write file contents into
   * the named file underneath the compilation output directory.
   *
   * @param logger a logger; normally the logger passed into the currently invoked generator, or a
   *     branch thereof
   * @param partialPath the name of the file whose contents are to be written; the name can include
   *     subdirectories separated by forward slashes ('/')
   * @return an <code>OutputStream</code> into which file contents can be written, or <code>null
   *     </code> if a resource by that name is already pending or already exists
   * @throws UnableToCompleteException if the resource could not be initialized for some reason,
   *     such as if the specified partial path is invalid
   */
  OutputStream tryCreateResource(TreeLogger logger, String partialPath)
      throws UnableToCompleteException;
}
