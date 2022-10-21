package org.treblereel.j2cl.shim;

import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;

@ES6Module("../../../js/ES6TestPath.js")
@JsType(isNative = true, namespace = "org.treblereel.j2cl.shim")
public class ES6TestPath {

  public String id;

  public native boolean isES6TestPath();
}
