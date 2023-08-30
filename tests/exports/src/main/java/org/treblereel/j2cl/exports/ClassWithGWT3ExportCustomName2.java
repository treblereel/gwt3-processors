package org.treblereel.j2cl.exports;

import elemental2.core.JsArray;
import elemental2.promise.Promise;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@GWT3Export(name = "TestBean", namespace = "org.treblereel.j2cl.processors")
public class ClassWithGWT3ExportCustomName2 {
  private String value = "default value";

  public static String staticTest(String s) {
    return s;
  }

  public static String static_id = "qwerty";

  public String id = "qwerty";

  public String test() {
    return "ExportTestClass";
  }

  public String test1(String s) {
    return s;
  }

  public String[] test2(String[] array) {
    return array;
  }

  public JsArray<String> test3(JsArray<String> array) {
    return array;
  }

  public String test4(String arg, Number number) {
    return arg + "+" + number.toString();
  }

  public Promise<String> promise1(String arg) {
    return Promise.resolve(arg);
  }

  public Promise<String> promise2(String arg, Number number) {
    return Promise.resolve(arg + "+" + number.toString());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
