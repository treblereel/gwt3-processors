package org.treblereel.j2cl.entrypoint;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLButtonElement;
import jsinterop.base.Js;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;

public class App {

    public App() {

    }

    public App(String GWT3EntryPoint) {

    }

    @GWT3EntryPoint
    public void init() {
        HTMLButtonElement btn = (HTMLButtonElement) DomGlobal.document.createElement("button");
        btn.textContent = "PRESS ME !";
        btn.addEventListener("click", evt -> DomGlobal.window.alert("HELLO WORLD!"));
        DomGlobal.document.body.appendChild(btn);

        DomGlobal.console.log("Hello");
        Js.asPropertyMap(DomGlobal.window).set("started", true);
    }
}
