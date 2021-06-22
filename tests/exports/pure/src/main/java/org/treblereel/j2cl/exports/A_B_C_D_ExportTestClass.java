package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@GWT3Export
public class A_B_C_D_ExportTestClass {

    public static String staticProperty = "staticProperty";

    public static String test2(String s) {
        return s;
    }

    @JsProperty
    public String id = "qwerty";

    public String id2 = "QWERTY";

    @JsMethod
    public String test1(String s) {
        return s;
    }

    public String test3(String s) {
        return s;
    }
}
