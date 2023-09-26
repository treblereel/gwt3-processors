/*
 * Copyright © 2023 Treblereel
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

package org.treblereel.j2cl.processors.generator.dto;

import java.util.HashSet;
import java.util.Set;

public class ExportDTO {

  private final String type;
  private final String ctor;
  private final String module;
  private final String target;
  private final Set<MethodDTO> methods = new HashSet<>();
  private final Set<PropertyDTO> properties = new HashSet<>();

  private boolean isNative = false;

  public ExportDTO(String type, String module, String target, String ctor, boolean isNative) {
    this.type = type;
    this.module = module;
    this.ctor = ctor;
    this.target = target;
    this.isNative = isNative;
  }

  public void addMethod(MethodDTO methodDTO) {
    methods.add(methodDTO);
  }

  public void addProperty(PropertyDTO propertyDTO) {
    properties.add(propertyDTO);
  }

  public Set<MethodDTO> getMethods() {
    return methods;
  }

  public Set<PropertyDTO> getProperties() {
    return properties;
  }

  public String getType() {
    return type;
  }

  public String getCtor() {
    return ctor;
  }

  public String getModule() {
    return module;
  }

  public String getTarget() {
    return target;
  }

  public boolean isIsNative() {
    return isNative;
  }
}
