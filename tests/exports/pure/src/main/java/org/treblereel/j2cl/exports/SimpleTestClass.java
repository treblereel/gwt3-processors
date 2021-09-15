package org.treblereel.j2cl.exports;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 9/14/21
 */
//@GWT3Export
public class SimpleTestClass {

    static {
        DomGlobal.console.log("static init");
    }

    {
        DomGlobal.console.log("instance init");
    }

    private String value;

    public static String staticValue = "staticValue";

    public String instanceValue = "instanceValue";

    @JsProperty
    public static String staticValueJsProperty = "staticValueJsProperty";

    @JsProperty
    public String instanceValueJsProperty = "instanceValueJsProperty";

    @JsMethod
    public String test2(String s) {
        return s;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
