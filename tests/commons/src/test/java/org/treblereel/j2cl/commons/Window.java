/*
 * Copyright 2010 Google Inc.
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

package org.treblereel.j2cl.commons;

import elemental2.core.Function;
import elemental2.core.Reflect;
import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.Element;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "<global>")
public class Window {

  @JsMethod
  public native CSSStyleDeclaration getComputedStyle(Element elt);

  @JsOverlay
  public final String myFunction() {
    Function f = (Function) Reflect.get(this, "myFunction");
    Object result = f.bind(this).apply();
    return (String) result;
  }

  @JsOverlay
  public final String myFunction2() {
    Function f = (Function) Reflect.get(this, "myFunction2");
    Object result = f.bind(this).apply();
    return (String) result;
  }
}
