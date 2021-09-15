package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@GWT3Export
public class ExportTestClass {

    //@GWT3EntryPoint
/*    public void init() {

    }*/

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
