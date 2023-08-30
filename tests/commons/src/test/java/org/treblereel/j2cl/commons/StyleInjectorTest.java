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

import static org.junit.Assert.assertEquals;

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import jsinterop.base.Js;
import org.junit.Test;
import org.treblereel.j2cl.processors.common.injectors.StyleInjector;

@J2clTestInput(StyleInjectorTest.class)
public class StyleInjectorTest {

  @Test
  public void testStyleInjection() {
    String css = ".myText { font-weight: bold; }";

    StyleInjector.injectStyleSheet(css).injectStyleSheet();

    HTMLDivElement testElement = (HTMLDivElement) DomGlobal.document.createElement("div");
    testElement.id = "testElement";
    testElement.className = "myText";
    testElement.textContent = "Hello, world!";
    DomGlobal.document.body.append(testElement);

    HTMLDivElement tested = (HTMLDivElement) DomGlobal.document.getElementById("testElement");

    assertEquals("myText", tested.className);
    Window window = Js.uncheckedCast(DomGlobal.window);

    assertEquals("700", window.getComputedStyle(tested).fontWeight); // alias for bold
  }

  @Test
  public void testStyleInjectionAtStart() {
    String css = ".myText { background-color: yellow; }";

    StyleInjector.injectStyleSheet(css).injectStyleSheetAtStart();

    HTMLDivElement tested = (HTMLDivElement) DomGlobal.document.getElementById("testElement");
    assertEquals("myText", tested.className);

    Window window = Js.uncheckedCast(DomGlobal.window);

    assertEquals("700", window.getComputedStyle(tested).fontWeight); // alias for bold
    assertEquals(
        "rgb(255, 255, 0)", window.getComputedStyle(tested).backgroundColor); // code for yellow
    assertEquals(css, DomGlobal.document.head.firstChild.textContent);
  }
}
