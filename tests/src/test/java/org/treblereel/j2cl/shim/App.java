package org.treblereel.j2cl.shim;

import elemental2.dom.DomGlobal;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;

public class App {

    @GWT3EntryPoint
    public void init() {
        DomGlobal.console.log("Hello");
    }
}
