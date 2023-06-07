/*
 * Copyright 2009 Google Inc.
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

import java.util.Set;

/** A named configuration (property, values) pair. */
public interface ConfigurationProperty {

  /**
   * The name of the property.
   *
   * @return the property name as a String.
   */
  String getName();

  /**
   * The values for the permutation currently being considered.
   *
   * @return the property values as a List of Strings.
   */
  Set<String> getValues();

  String asSingleValue() throws UnableToCompleteException;

  Boolean asSingleBooleanValue() throws UnableToCompleteException;
}
