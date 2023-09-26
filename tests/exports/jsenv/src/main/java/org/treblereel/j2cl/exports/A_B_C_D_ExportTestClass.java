package org.treblereel.j2cl.exports;

import elemental2.core.JsArray;
import elemental2.promise.Promise;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

public class A_B_C_D_ExportTestClass {

  private String value = "default value";

  @GWT3Export
  public static String staticTest(String s) {
    return s;
  }

  public static String static_id = "qwerty";

  public String id = "qwerty";

  @GWT3Export
  public String test() {
    return "ExportTestClass";
  }

  @GWT3Export
  public String test1(String s) {
    return s;
  }

  @GWT3Export
  public String[] test2(String[] array) {
    return array;
  }

  @GWT3Export
  public JsArray<String> test3(JsArray<String> array) {
    return array;
  }

  @GWT3Export
  public String test4(String arg, Number number) {
    return arg + "+" + number.toString();
  }

  @GWT3Export
  public Promise<String> promise1(String arg) {
    return Promise.resolve(arg);
  }

  @GWT3Export
  public Promise<String> promise2(String arg, Number number) {
    return Promise.resolve(arg + "+" + number.toString());
  }

  @GWT3Export
  public String getValue() {
    return value;
  }

  @GWT3Export
  public void setValue(String value) {
    this.value = value;
  }
}
