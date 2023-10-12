package org.treblereel.j2cl.exports;

import org.treblereel.j2cl.processors.annotations.GWT3Export;

@GWT3Export
public class SimpleBean {

  private String id = "qwerty";

  private static String static_id = "qwerty";

  public static String staticTest() {
    return static_id;
  }

  public static void setStatic_id(String s) {
    static_id = s;
  }

  public String test() {
    return "ExportTestClass";
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
