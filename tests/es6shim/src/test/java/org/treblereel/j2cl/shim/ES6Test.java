package org.treblereel.j2cl.shim;

import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 1/27/21
 */
@ES6Module
@JsType(isNative = true, namespace = "org.treblereel.j2cl.shim")
public class ES6Test {

    public String id;

    public native boolean isTest();

}
