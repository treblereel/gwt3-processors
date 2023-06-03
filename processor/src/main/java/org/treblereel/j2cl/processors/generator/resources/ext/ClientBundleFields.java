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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;

/**
 * Allows ResourceGenerators to define fields within the implementation class for a bundle type. An
 * instance of this interface will be provided via the {@link ResourceGenerator#createFields}
 * method.
 *
 * <p>Because multiple, unrelated ResourceGenerators may be generating method implementations within
 * a single bundle implementation, it is necessary to ensure that they do not attempt to declare
 * multiple fields with the same name. The methods in this interface will provide a
 * guaranteed-unique identifier to use when generating method implementations.
 *
 * <p>Multiple invocations of the {@link #define} method with the same inputs will result in
 * different identifiers being produced.
 */
public interface ClientBundleFields {
  /**
   * Adds a field to the bundle. Equivalent to <code>defineField(type, name, null, true, false)
   * </code>.
   *
   * @param type the declared type of the field
   * @param name a Java identifier to be used as the basis for the name of the field
   * @return the identifier that must be used to access the field
   */
  String define(TypeElement type, String name);

  /**
   * Adds a field to the bundle. Equivalent to <code>defineField(type, name, null, true, false)
   * </code>.
   *
   * @param rowType the declared type of the field as string
   * @param name a Java identifier to be used as the basis for the name of the field
   * @return the identifier that must be used to access the field
   */
  String define(String rowType, String name);

  /**
   * Adds a field to the bundle.
   *
   * @param type the declared type of the field
   * @param name a Java identifier to be used as the basis for the name of the field
   * @param initializer a Java expression that will be used as the field's initializer, or <code>
   *     null</code> if no initialization expression is desired
   * @param isStatic if <code>true</code> the field will be declared to be static
   * @param isFinal if <code>true</code> the fields will be declared as final
   * @return the identifier that must be used to access the field
   */
  String define(
      TypeElement type, String name, String initializer, boolean isStatic, boolean isFinal);

  /**
   * Adds a field to the bundle.
   *
   * @param array the declared array of the field
   * @param name a Java identifier to be used as the basis for the name of the field
   * @param initializer a Java expression that will be used as the field's initializer, or <code>
   *     null</code> if no initialization expression is desired
   * @param isStatic if <code>true</code> the field will be declared to be static
   * @param isFinal if <code>true</code> the fields will be declared as final
   * @return the identifier that must be used to access the field
   */
  String define(
      ArrayType array, String name, String initializer, boolean isStatic, boolean isFinal);

  /**
   * Adds a field to the bundle.
   *
   * @param rowType the declared type as row string
   * @param name a Java identifier to be used as the basis for the name of the field
   * @param initializer a Java expression that will be used as the field's initializer, or <code>
   *     null</code> if no initialization expression is desired
   * @param isStatic if <code>true</code> the field will be declared to be static
   * @param isFinal if <code>true</code> the fields will be declared as final
   * @return the identifier that must be used to access the field
   */
  String define(String rowType, String name, String initializer, boolean isStatic, boolean isFinal);
}
