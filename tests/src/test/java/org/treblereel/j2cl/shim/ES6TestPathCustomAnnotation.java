package org.treblereel.j2cl.shim;

import jsinterop.annotations.JsType;

@ES6Module("javascript/ES6TestPath.js")
@JsType(isNative = true, namespace = "org.treblereel.j2cl.shim")
public class ES6TestPathCustomAnnotation {

    public String id;

    public native boolean isES6TestPathCustomAnnotation();

}
