package org.treblereel.j2cl.shim;

import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 1/27/21
 */
@ES6Module
@JsType(isNative = true, namespace = "org.treblereel.j2cl.shim")
public class ES6TransitiveTest {

    private ES6Test es6Test;
    private ES6Test2 es6Test2;

    public native boolean getEs6Testid();

    public native boolean getEs6Test2id();
}
