/*
 * Copyright © 2023 Treblereel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.treblereel.j2cl.processors.common.injectors;

import elemental2.core.Reflect;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDocument;
import elemental2.dom.HTMLScriptElement;
import elemental2.dom.Window;

import java.util.function.Consumer;

public class ScriptInjector {

    private Window window = DomGlobal.window;

    private HTMLScriptElement style;

    private ScriptInjector(HTMLScriptElement style) {
        this.style = style;
    }

    public static ScriptInjector fromString(String contents) {
        return fromString(contents, null, null);
    }

    public static ScriptInjector fromString(String contents, Consumer<HTMLScriptElement> onResolve) {
        return fromString(contents, onResolve, null);

    }

    public static ScriptInjector fromString(String contents, Consumer<HTMLScriptElement> onResolve, Consumer<HTMLScriptElement> onReject) {
        HTMLScriptElement element = createElement(onResolve, onReject);
        element.text = contents;
        return new ScriptInjector(element);
    }

    public static ScriptInjector fromUrl(String url) {
        return fromUrl(url, null, null);
    }

    public static ScriptInjector fromUrl(String url, Consumer<HTMLScriptElement> onResolve) {
        return fromUrl(url, onResolve, null);
    }


    public static ScriptInjector fromUrl(String url, Consumer<HTMLScriptElement> onResolve, Consumer<HTMLScriptElement> onReject) {
        HTMLScriptElement element = createElement(onResolve, onReject);
        element.src = url;
        return new ScriptInjector(element);
    }

    private static HTMLScriptElement createElement(Consumer<HTMLScriptElement> onResolve, Consumer<HTMLScriptElement> onReject) {
        HTMLScriptElement script = (HTMLScriptElement) DomGlobal.document.createElement("script");
        if (onResolve != null) {
            script.onreadystatechange = (e) -> {
                onResolve.accept(script);
                return null;
            };
        }
        if (onReject != null) {
            script.onerror = (e) -> {
                onReject.accept(script);
                return null;
            };
        }
        return script;
    }

    public ScriptInjector setWindow(Window window) {
        this.window = window;
        return this;
    }

    public void inject() {
        ((HTMLDocument) Reflect.get(window, "document")).head.appendChild(style);
    }
}