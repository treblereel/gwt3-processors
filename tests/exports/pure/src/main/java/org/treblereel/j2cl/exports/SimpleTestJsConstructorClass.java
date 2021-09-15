package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 9/14/21
 */
public class SimpleTestJsConstructorClass {


    @JsConstructor
    public SimpleTestJsConstructorClass() {

    }

    private String value;

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
