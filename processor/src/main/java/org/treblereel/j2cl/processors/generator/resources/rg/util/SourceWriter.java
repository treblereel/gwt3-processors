/*
 * Copyright 2011 Google Inc.
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
package org.treblereel.j2cl.processors.generator.resources.rg.util;

import org.treblereel.gwt.resources.ext.TreeLogger;

/**
 * A mechanism to write source files.
 *
 * @see JavaSourceWriterBuilder
 *     <p>Experimental API - subject to change.
 */
public interface SourceWriter {

  /** Begin emitting a JavaDoc comment. */
  void beginJavaDocComment();

  void commit(TreeLogger logger);

  /** End emitting a JavaDoc comment. */
  void endJavaDocComment();

  void indent();

  void indentln(String s);

  /** Emit a printf-style string. */
  void indentln(String s, Object... args);

  void outdent();

  void print(String s);

  /** Emit a printf-style string. */
  void print(String s, Object... args);

  void println();

  void println(String s);

  /** Emit a printf-style string. */
  void println(String s, Object... args);
}
