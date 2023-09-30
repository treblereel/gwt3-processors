package org.treblereel.j2cl.translation;

import static org.junit.Assert.assertEquals;

import com.google.j2cl.junit.apt.J2clTestInput;
import org.junit.Test;

@J2clTestInput(TranslationEscapedValuesTest.class)
public class TranslationEscapedValuesTest {

  private static final MyBundle impl = new MyBundleImpl();

  @Test
  public void test() {
    assertEquals(
        "@TranslationKey org.treblereel.j2cl.translation.TranslationEscapedValuesTest !",
        impl.test(getClass().getCanonicalName()));
  }

  @Test
  public void test2() {
    assertEquals("TranslationService", impl.test2("TranslationService"));
  }

  @Test
  public void test3() {
    assertEquals("arg1arg2", impl.test3("arg1", "arg2"));
  }

  @Test
  public void test4() {
    assertEquals(
        "Tests run: 0, Failures: 2, Errors: 3, Skipped: 4, Time elapsed: 5 sec",
        impl.test4("0", "2", "3", "4", "5"));
  }

  @Test
  public void test5() {
    assertEquals(
        "arg1 TranslationKey arg1 TranslationKey arg2 ", impl.test5("arg1 ", " arg1 ", " arg2 "));
  }

  @Test
  public void test6() {
    assertEquals(
        "<br/>TranslationService<div id=\"this\">TranslationKeyTranslationService</div>",
        impl.test6("TranslationService", "TranslationService"));
  }

  @Test
  public void test7() {
    assertEquals(
        "TranslationService&TranslationService",
        impl.test7("TranslationService", "TranslationService"));
  }

  @Test
  public void test8() {
    assertEquals(
        "<div id=\"this\">!@#$%^*(((</div>TranslationService&TranslationService",
        impl.test8("TranslationService", "TranslationService"));
  }

  @Test
  public void test9() {
    assertEquals(
        "<div id=\"this\">!@#$%^*(((</div>TranslationService&TranslationService@#$%^&*(<div>TranslationService</div>",
        impl.test9("TranslationService", "TranslationService", "TranslationService"));
  }

  @Test
  public void test10() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"someId\">some content<br /><a href=\"#someRef\">TranslationService</a>,</div>",
          impl.test10("TranslationService"));
    } else {
      assertEquals(
          "<div id=\"someId\">some content<br/><a href=\"#someRef\">TranslationService</a>,</div>",
          impl.test10("TranslationService"));
    }
  }

  @Test
  public void test11() {
    assertEquals(
        "<div id=\"WOW\">3TranslationService-!!!!!!!!!!!!!!!!!</div>",
        impl.test11("TranslationService"));
  }

  @Test
  public void test12() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"div1\"><div id=\"div2\"><div id=\"div3\"></div><div id=\"div4\"></div></div></div>",
          impl.test12());
    } else {
      assertEquals(
          "<div id=\"div1\"><div id=\"div2\"><div id=\"div3\"/><div id=\"div4\"/></div></div>",
          impl.test12());
    }
  }

  @Test
  public void test13() {
    assertEquals("<div>TranslationKey</div>", impl.test13());
  }

  @Test
  public void test14() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"div1\"><div id=\"div2\"><div id=\"div3\">RRRRRRRTranslationService></div><div id=\"div4\"></div></div></div>",
          impl.test14("TranslationService"));
    } else {
      assertEquals(
          "<div id=\"div1\"><div id=\"div2\"><div id=\"div3\">RRRRRRRTranslationService></div><div id=\"div4\"/></div></div>",
          impl.test14("TranslationService"));
    }
  }

  @Test
  public void test15() {
    assertEquals(
        "7128890306670950348232162507662243061846645994402114603180927379544880168678174568758504050459062584",
        impl.test15());
  }

  @Test
  public void test16() {
    assertEquals("&<div id=\"my_widget\">@TranslationKey</div>", impl.test16("my_widget"));
  }

  @Test
  public void test17() {
    // YEAP, j2cl-m-p force escaping of html tags
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<br/>my_widget<div id=\"this\">inner text my_widget</div>",
          impl.test17("my_widget", "my_widget"));
    } else {
      assertEquals(
          "&lt;br/&gt;my_widget&lt;div id=&quot;this&quot;&gt;inner text my_widget&lt;/div&gt;",
          impl.test17("my_widget", "my_widget"));
    }
  }

  @Test
  public void test18() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"_this\">!@#$%^*(((</div>my_widget&amp;my_widget",
          impl.test18("my_widget", "my_widget"));
    } else {
      assertEquals(
          "&lt;div id=&quot;_this&quot;&gt;!@#$%^*(((&lt;/div&gt;my_widget&amp;my_widget",
          impl.test18("my_widget", "my_widget"));
    }
  }

  @Test
  public void test19() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"_this\">!@#$%^*(((</div>my_widget&amp;qwerty@#$%^&amp;*(<div>asdfg</div>",
          impl.test19("my_widget", "qwerty", "asdfg"));
    } else {
      assertEquals(
          "&lt;div id=&quot;_this&quot;&gt;!@#$%^*(((&lt;/div&gt;my_widget&amp;qwerty@#$%^&amp;*(&lt;div&gt;asdfg&lt;/div&gt;",
          impl.test19("my_widget", "qwerty", "asdfg"));
    }
  }

  @Test
  public void test20() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"_someId\">some content<br /><a href=\"#someRef\">my_widget</a>,</div>",
          impl.test20("my_widget"));
    } else {
      assertEquals(
          "&lt;div id=&quot;_someId&quot;&gt;some content&lt;br/&gt;&lt;a href=&quot;#someRef&quot;&gt;my_widget&lt;/a&gt;,&lt;/div&gt;",
          impl.test20("my_widget"));
    }
  }

  @Test
  public void test21() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"div_id\">3my_widget-!!!!!!!!!!!!!!!!!</div>", impl.test21("my_widget"));
    } else {
      assertEquals(
          "&lt;div id=&quot;div_id&quot;&gt;3my_widget-!!!!!!!!!!!!!!!!!&lt;/div&gt;",
          impl.test21("my_widget"));
    }
  }

  @Test
  public void test22() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"div21\"><div id=\"div22\"><div id=\"div23\"></div><div id=\"div14\"></div></div></div>",
          impl.test22());
    } else {
      assertEquals(
          "&lt;div id=&quot;div21&quot;&gt;&lt;div id=&quot;div22&quot;&gt;&lt;div id=&quot;div23&quot;/&gt;&lt;div id=&quot;div14&quot;/&gt;&lt;/div&gt;&lt;/div&gt;",
          impl.test22());
    }
  }

  @Test
  public void test23() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals("<div>inner content</div>", impl.test23());
    } else {
      assertEquals("&lt;div&gt;inner content&lt;/div&gt;", impl.test23());
    }
  }

  @Test
  public void test24() {
    if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
      assertEquals(
          "<div id=\"div21\"><div id=\"div22\"><div id=\"div23\">RRRRRRRmy_widget></div><div id=\"div4\"></div></div></div>",
          impl.test24("my_widget"));
    } else {
      assertEquals(
          "&lt;div id=&quot;div21&quot;&gt;&lt;div id=&quot;div22&quot;&gt;&lt;div id=&quot;div23&quot;&gt;RRRRRRRmy_widget&gt;&lt;/div&gt;&lt;div id=&quot;div4&quot;/&gt;&lt;/div&gt;&lt;/div&gt;",
          impl.test24("my_widget"));
    }
  }
}
