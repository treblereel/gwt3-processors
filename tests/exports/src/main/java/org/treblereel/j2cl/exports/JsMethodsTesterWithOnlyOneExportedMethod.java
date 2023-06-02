package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsConstructor;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

public class JsMethodsTesterWithOnlyOneExportedMethod {

    private static String staticProperty = "staticProperty";

    private String id = "qwerty";

    @JsConstructor
    public JsMethodsTesterWithOnlyOneExportedMethod() {

    }

    public String test1(String s) {
        return s;
    }

    public static String test2(String s) {
        return s;
    }

    public String test3() {
        return staticProperty;
    }

    public static String test4() {
        return staticProperty;
    }

    public String test5() {
        return id;
    }

    @GWT3Export(name = "new_custome_method_name")
    public String test6() {
        return "new_custome_method_name";
    }
}
