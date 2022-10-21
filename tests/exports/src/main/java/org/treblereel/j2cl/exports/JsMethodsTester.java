package org.treblereel.j2cl.exports;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@JsType
public class JsMethodsTester {

  @JsProperty private static String staticProperty = "staticProperty";

  @JsProperty private String id = "qwerty";

  @JsMethod
  @GWT3Export
  public String test1(String s) {
    return s;
  }

  @JsMethod
  @GWT3Export
  public static String test2(String s) {
    return s;
  }

  @JsMethod
  @GWT3Export
  public String test3() {
    return staticProperty;
  }

  @JsMethod
  @GWT3Export
  public static String test4() {
    return staticProperty;
  }

  @JsMethod
  @GWT3Export
  public String test5() {
    return id;
  }

  @JsMethod
  @GWT3Export(name = "new_custome_method_name")
  public String test6() {
    return "new_custome_method_name";
  }
}
