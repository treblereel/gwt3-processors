/*
 * Copyright © 2024
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

package org.treblereel.j2cl.processors.utils;

import java.util.List;

public final class MangledNameComputer {

  public enum MemberVisibility {
    PUBLIC,
    PROTECTED,
    PACKAGE_PRIVATE,
    PRIVATE
  }

  private MangledNameComputer() {}

  public static String mangleTypeName(String qualifiedName) {
    return qualifiedName.replace('.', '_');
  }

  public static String mangleArrayTypeName(String leafQualifiedName, int dimensions) {
    return "arrayOf_".repeat(dimensions) + mangleTypeName(leafQualifiedName);
  }

  public static String mangleMethodName(
      String methodName,
      List<String> parameterTypeMangledNames,
      String returnTypeMangledName,
      MemberVisibility visibility,
      boolean isInstance,
      String enclosingTypeMangledName,
      String packageName) {
    StringBuilder signature = new StringBuilder();
    for (String param : parameterTypeMangledNames) {
      signature.append("__").append(param);
    }
    signature.append("__").append(returnTypeMangledName);

    String suffix = "";
    if (isInstance) {
      switch (visibility) {
        case PRIVATE:
          suffix = "_$p_" + enclosingTypeMangledName;
          break;
        case PACKAGE_PRIVATE:
          suffix = "_$pp_" + packageName.replace('.', '_');
          break;
        default:
          break;
      }
    }

    return "m_" + methodName + signature + suffix;
  }

  public static String mangleFieldName(
      String fieldName, String enclosingTypeMangledName, MemberVisibility visibility) {
    String privateSuffix = visibility == MemberVisibility.PRIVATE ? "_" : "";
    return "f_" + fieldName + "__" + enclosingTypeMangledName + privateSuffix;
  }

  public static String mangleDefaultConstructorName(String enclosingTypeMangledName) {
    return "$ctor__" + enclosingTypeMangledName + "__void";
  }
}
