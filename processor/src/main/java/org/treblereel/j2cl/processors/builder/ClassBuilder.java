/*
 * Copyright © 2021
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

package org.treblereel.j2cl.processors.builder;

import com.google.auto.common.MoreElements;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import jsinterop.annotations.JsType;

/** @author Dmitrii Tikhomirov Created by treblereel 9/14/21 */
public class ClassBuilder {

  private TypeElement parent;
  private boolean addNewInstanceStatement = false;
  private List<String> expressions = new LinkedList<>();

  public ClassBuilder(TypeElement parent) {
    this.parent = parent;
  }

  public ClassBuilder addNewInstanceStatement() {
    this.addNewInstanceStatement = true;
    return this;
  }

  public ClassBuilder addExpression(String expression) {
    expressions.add(expression);
    return this;
  }

  public String build() {
    StringBuffer sb = new StringBuffer();
    if (addNewInstanceStatement) {
      generateClassExport(sb);
    }
    for (String expression : expressions) {
      sb.append(expression);
    }
    return sb.toString();
  }

  private void getNativeFullName(StringBuffer source) {
    String pkg =
        MoreElements.getPackage(parent).getQualifiedName().toString().replaceAll("\\.", "_");
    String clazz = parent.getSimpleName().toString().replaceAll("_", "__");
    source.append(pkg);
    source.append("_");
    source.append(clazz);
  }

  private void generateClassExport(StringBuffer source) {

    //  App.$clinit();
    //  return new App();

    if (parent.getAnnotation(JsType.class) == null) {
      source.append("const _");
      source.append(parent.getSimpleName());
      source.append(" = ");
      getNativeFullName(source);
      source.append(".$create__");
    } else {
      source.append(parent.getSimpleName().toString());
      source.append(".$clinit();");
      source.append(System.lineSeparator());
      source.append("const _");
      source.append(parent.getSimpleName());
      source.append(" = new ");
      source.append(parent.getSimpleName().toString());
      source.append("()");
    }
    source.append(";");
    source.append(System.lineSeparator());
  }
}
