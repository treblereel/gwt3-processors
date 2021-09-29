package org.treblereel.j2cl.exports.impl.v1_0;

import jsinterop.annotations.JsType;
import jsinterop.annotations.JsMethod;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@JsType
@GWT3Export
public class MyInterface implements org.treblereel.j2cl.exports.api.MyInterface {

    @JsMethod
    public String test1(String s) {
        return s;
    };

}
