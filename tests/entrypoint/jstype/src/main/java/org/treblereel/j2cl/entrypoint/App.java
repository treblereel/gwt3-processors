package org.treblereel.j2cl.entrypoint;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;

@JsType
public class App {

  @GWT3EntryPoint
  public void init() {
    Js.asPropertyMap(DomGlobal.window).set("started", true);
  }
}
