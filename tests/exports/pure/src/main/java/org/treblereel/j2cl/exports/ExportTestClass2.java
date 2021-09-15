package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 9/14/21
 */
@GWT3Export
public class ExportTestClass2 {

    @JsMethod
    public String test1(String s) {
        return s;
    }
}
