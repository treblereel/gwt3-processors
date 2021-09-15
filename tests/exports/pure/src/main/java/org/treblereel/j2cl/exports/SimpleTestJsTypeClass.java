package org.treblereel.j2cl.exports;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 9/14/21
 */
//@GWT3Export
@JsType
public class SimpleTestJsTypeClass  {

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
