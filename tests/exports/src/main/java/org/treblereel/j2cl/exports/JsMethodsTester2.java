package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@GWT3Export
public class JsMethodsTester2 {

    @JsProperty
    private static String staticProperty = "staticProperty";

    @JsProperty
    private String id = "qwerty";

    @JsConstructor
    public JsMethodsTester2() {

    }

    @JsMethod
    public String test1(String s) {
        return s;
    }

    @JsMethod
    public static String test2(String s) {
        return s;
    }

    @JsMethod
    public String test3() {
        return staticProperty;
    }

    @JsMethod
    public static String test4() {
        return staticProperty;
    }

    @JsMethod
    public String test5() {
        return id;
    }

    @JsMethod
    @GWT3Export(name = "new_custome_method_name")
    public String test6() {
        return "new_custome_method_name";
    }
}
