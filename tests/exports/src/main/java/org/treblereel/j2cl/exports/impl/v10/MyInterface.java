package org.treblereel.j2cl.exports.impl.v10;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@JsType
public class MyInterface implements org.treblereel.j2cl.exports.apis.MyInterface {

    @JsMethod
    @GWT3Export
    public String test1(String s) {
        return s;
    };

}
