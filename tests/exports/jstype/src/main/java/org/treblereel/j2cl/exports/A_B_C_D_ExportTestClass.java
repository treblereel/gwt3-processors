package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@JsType
@GWT3Export
public class A_B_C_D_ExportTestClass {

    public static String staticProperty = "staticProperty";

    public static String test2(String s) {
        return s;
    }

    public String id = "qwerty";

    public String test1(String s) {
        return s;
    }
}
