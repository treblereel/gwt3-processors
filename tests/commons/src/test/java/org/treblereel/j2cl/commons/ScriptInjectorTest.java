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
import elemental2.dom.HTMLScriptElement;
import jsinterop.base.Js;
import org.junit.Test;
import org.treblereel.j2cl.processors.common.injectors.ScriptInjector;

@J2clTestInput(ScriptInjectorTest.class)
public class ScriptInjectorTest {

  @Test
  public void testInjectScript() {
    String jsCode = "function myFunction() { return 'Hello, world!'; }";
    ScriptInjector.fromString(jsCode).setWindow(ScriptInjector.TOP_WINDOW).inject();
    String result = ((Window) Js.uncheckedCast(DomGlobal.window)).myFunction();
    assertEquals("Hello, world!", result);
  }

  @Test
  public void testInjectScriptUrl() {
    ScriptInjector.fromUrl(
            "test_function.js",
            new ScriptInjector.Callback() {
              @Override
              public void accept(HTMLScriptElement htmlScriptElement) {
                String result = ((Window) Js.uncheckedCast(DomGlobal.window)).myFunction2();
                assertEquals("TEST2", result);
              }
            },
            new ScriptInjector.Callback() {
              @Override
              public void accept(HTMLScriptElement htmlScriptElement) {
                DomGlobal.console.error("Error loading script");
              }
            })
        .setWindow(ScriptInjector.TOP_WINDOW)
        .inject();
  }
}
