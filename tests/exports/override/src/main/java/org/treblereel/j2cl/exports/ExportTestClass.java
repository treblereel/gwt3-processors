package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@JsType
@GWT3Export(namespace = "window", name = "aaa.bbb.ccc.ddd.ZZZ")
public class ExportTestClass {

    @JsProperty
    public static String staticProperty = "staticProperty";

    @JsMethod
    public static String test2(String s) {
        return s;
    }

    @JsProperty
    public String id = "qwerty";

    @JsMethod
    public String test1(String s) {
        return s;
    }
}
