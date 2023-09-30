package org.treblereel.j2cl.entrypoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ExportsTest {

  private ChromeDriver driver;

  @Before
  public void setup() throws MalformedURLException {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless", "--window-size=1920,1200");

    driver = new ChromeDriver(options);
    Path path = Path.of("target", "j2cl", "launcherDir", "index.html");
    driver.get(path.toUri().toURL().toString());
    assertEquals("J2CL", driver.getTitle());
  }

  @Test
  public void testPromiseReturned() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.ExportTestClass().promise1('PROMISE CALL EXPECTED');");
    assertEquals("resolved PROMISE CALL EXPECTED", result);
  }

  @Test
  public void testShouldBe2ArgPromise() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.ExportTestClass().promise2('PROMISE CALL EXPECTED', 222);");
    assertEquals("PROMISE CALL EXPECTED+222", result);
  }

  @Test
  public void testExportTestClassTestMethod() {
    String script =
        "return new org.treblereel.j2cl.exports.ExportTestClass().test() === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassTest1Method() {
    String script =
        "return new org.treblereel.j2cl.exports.ExportTestClass().test1('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassTest2Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.ExportTestClass().test2(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassTest3Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.ExportTestClass().test3(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassTest4Method() {
    String script =
        "return new org.treblereel.j2cl.exports.ExportTestClass().test4('ExportTestClass',444) === 'ExportTestClass+444';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassStaticTest() {
    String script =
        "return org.treblereel.j2cl.exports.ExportTestClass.staticTest('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassDefaultValue() {
    String script =
        "return new org.treblereel.j2cl.exports.ExportTestClass().getValue() === 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassSetValue() {
    String script =
        "var t = new org.treblereel.j2cl.exports.ExportTestClass(); "
            + System.lineSeparator()
            + "t.setValue('new text value');"
            + System.lineSeparator()
            + "return t.getValue() === 'new text value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassDiffValues() {
    String script =
        "var test = new org.treblereel.j2cl.exports.ExportTestClass(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.ExportTestClass(); "
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassDiffValues2() {
    String script =
        "var test = new org.treblereel.j2cl.exports.ExportTestClass(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.ExportTestClass(); test2.setValue('new text value 2');"
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'new text value 2';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testExportTestClassNewCustomeMethodName() {
    String script =
        "return new org.treblereel.j2cl.exports.ExportTestClass().new_custome_method_name() == 'new_custome_method_name'; ";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassTestMethod() {
    String script =
        "return new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass().test() === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassTest1Method() {
    String script =
        "return new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass().test1('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassTest2Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass().test2(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassTest3Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass().test3(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassTest4Method() {
    String script =
        "return new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass().test4('ExportTestClass',444) === 'ExportTestClass+444';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassStaticTest() {
    String script =
        "return org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass.staticTest('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassDefaultValue() {
    String script =
        "return new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass().getValue() === 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassSetValue() {
    String script =
        "var t = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass(); "
            + System.lineSeparator()
            + "t.setValue('new text value');"
            + System.lineSeparator()
            + "return t.getValue() === 'new text value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassDiffValues() {
    String script =
        "var test = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass(); "
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testA_B_C_D_ExportTestClassDiffValues2() {
    String script =
        "var test = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass(); test2.setValue('new text value 2');"
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'new text value 2';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testJsTypeClassTest() {
    String script =
        "return new org.treblereel.j2cl.exports.JsMethodsTester().test1('JsMethodsTester') === 'JsMethodsTester';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testJsTypeClassStaticTest() {
    String script =
        "return org.treblereel.j2cl.exports.JsMethodsTester.test2('JsMethodsTester') === 'JsMethodsTester';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testJsTypeClassTest3() {
    String script =
        "return new org.treblereel.j2cl.exports.JsMethodsTester().test3() === 'staticProperty';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testJsTypeClassStaticTest4() {
    String script =
        "return org.treblereel.j2cl.exports.JsMethodsTester.test4() === 'staticProperty';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testJsTypeClassTest5() {
    String script =
        "return new org.treblereel.j2cl.exports.JsMethodsTester().test5() === 'qwerty';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testJsTypeClassNew_custome_method_name() {
    String script =
        "return new org.treblereel.j2cl.exports.JsMethodsTester().new_custome_method_name() === 'new_custome_method_name';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassTestMethod() {
    String script =
        "return new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl().test() === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassTest1Method() {
    String script =
        "return new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl().test1('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassTest2Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl().test2(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassTest3Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl().test3(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassTest4Method() {
    String script =
        "return new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl().test4('ExportTestClass',444) === 'ExportTestClass+444';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassStaticTest() {
    String script =
        "return org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl.staticTest('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassDefaultValue() {
    String script =
        "return new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl().getValue() === 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassSetValue() {
    String script =
        "var t = new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl(); "
            + System.lineSeparator()
            + "t.setValue('new text value');"
            + System.lineSeparator()
            + "return t.getValue() === 'new text value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassDiffValues() {
    String script =
        "var test = new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.ExportTestClass(); "
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassDiffValues2() {
    String script =
        "var test = new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl(); test2.setValue('new text value 2');"
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'new text value 2';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportOnDeclClassNewCustomeMethodName() {
    String script =
        "return new org.treblereel.j2cl.exports.ClassWithGWT3ExportOnDecl().new_custome_method_name() == 'new_custome_method_name'; ";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassTestMethod() {
    String script = "return new QWERTY_OLOLO().test() === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassTest1Method() {
    String script = "return new QWERTY_OLOLO().test1('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassTest2Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new QWERTY_OLOLO().test2(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassTest3Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new QWERTY_OLOLO().test3(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassTest4Method() {
    String script =
        "return new QWERTY_OLOLO().test4('ExportTestClass',444) === 'ExportTestClass+444';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassStaticTest() {
    String script = "return QWERTY_OLOLO.staticTest('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassDefaultValue() {
    String script = "return new QWERTY_OLOLO().getValue() === 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassSetValue() {
    String script =
        "var t = new QWERTY_OLOLO(); "
            + System.lineSeparator()
            + "t.setValue('new text value');"
            + System.lineSeparator()
            + "return t.getValue() === 'new text value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassDiffValues() {
    String script =
        "var test = new QWERTY_OLOLO(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new QWERTY_OLOLO(); "
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameClassDiffValues2() {
    String script =
        "var test = new QWERTY_OLOLO(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new QWERTY_OLOLO(); test2.setValue('new text value 2');"
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'new text value 2';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassTestMethod() {
    String script =
        "return new org.treblereel.j2cl.exports.QWERTY_OLOLO().test() === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassTest1Method() {
    String script =
        "return new org.treblereel.j2cl.exports.QWERTY_OLOLO().test1('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassTest2Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.QWERTY_OLOLO().test2(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassTest3Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.exports.QWERTY_OLOLO().test3(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassTest4Method() {
    String script =
        "return new org.treblereel.j2cl.exports.QWERTY_OLOLO().test4('ExportTestClass',444) === 'ExportTestClass+444';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassStaticTest() {
    String script =
        "return org.treblereel.j2cl.exports.QWERTY_OLOLO.staticTest('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassDefaultValue() {
    String script =
        "return new org.treblereel.j2cl.exports.QWERTY_OLOLO().getValue() === 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassSetValue() {
    String script =
        "var t = new org.treblereel.j2cl.exports.QWERTY_OLOLO(); "
            + System.lineSeparator()
            + "t.setValue('new text value');"
            + System.lineSeparator()
            + "return t.getValue() === 'new text value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassDiffValues() {
    String script =
        "var test = new org.treblereel.j2cl.exports.QWERTY_OLOLO(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.QWERTY_OLOLO(); "
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomNameAndDefaultPkgClassDiffValues2() {
    String script =
        "var test = new org.treblereel.j2cl.exports.QWERTY_OLOLO(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.exports.QWERTY_OLOLO(); test2.setValue('new text value 2');"
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'new text value 2';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassTestMethod() {
    String script =
        "return new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg().test() === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassTest1Method() {
    String script =
        "return new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg().test1('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassTest2Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg().test2(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassTest3Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg().test3(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassTest4Method() {
    String script =
        "return new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg().test4('ExportTestClass',444) === 'ExportTestClass+444';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassStaticTest() {
    String script =
        "return q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg.staticTest('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassDefaultValue() {
    String script =
        "return new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg().getValue() === 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassSetValue() {
    String script =
        "var t = new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg(); "
            + System.lineSeparator()
            + "t.setValue('new text value');"
            + System.lineSeparator()
            + "return t.getValue() === 'new text value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassDiffValues() {
    String script =
        "var test = new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg(); "
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomPkgClassDiffValues2() {
    String script =
        "var test = new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new q.w.e.r.t.y.ClassWithGWT3ExportCustomPkg(); test2.setValue('new text value 2');"
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'new text value 2';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassTestMethod() {
    String script =
        "return new org.treblereel.j2cl.processors.TestBean().test() === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassTest1Method() {
    String script =
        "return new org.treblereel.j2cl.processors.TestBean().test1('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassTest2Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.processors.TestBean().test2(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassTest3Method() {
    String script =
        "var arr = ['a','b','c','d']; "
            + System.lineSeparator()
            + "return new org.treblereel.j2cl.processors.TestBean().test3(arr) === arr;";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassTest4Method() {
    String script =
        "return new org.treblereel.j2cl.processors.TestBean().test4('ExportTestClass',444) === 'ExportTestClass+444';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassStaticTest() {
    String script =
        "return org.treblereel.j2cl.processors.TestBean.staticTest('ExportTestClass') === 'ExportTestClass';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassDefaultValue() {
    String script =
        "return new org.treblereel.j2cl.processors.TestBean().getValue() === 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassSetValue() {
    String script =
        "var t = new org.treblereel.j2cl.processors.TestBean(); "
            + System.lineSeparator()
            + "t.setValue('new text value');"
            + System.lineSeparator()
            + "return t.getValue() === 'new text value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassDiffValues() {
    String script =
        "var test = new org.treblereel.j2cl.processors.TestBean(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.processors.TestBean(); "
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'default value';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testClassWithGWT3ExportCustomName2ClassDiffValues2() {
    String script =
        "var test = new org.treblereel.j2cl.processors.TestBean(); test.setValue('new text value');"
            + System.lineSeparator()
            + "var test2 = new org.treblereel.j2cl.processors.TestBean(); test2.setValue('new text value 2');"
            + System.lineSeparator()
            + "return test.getValue() == 'new text value' && test2.getValue() == 'new text value 2';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testInterfaceUsageClassDefaultValue() {
    String script =
        "return new org.treblereel.j2cl.exports.impl.v1_0.MyInterface().test1('INSTANCE METHOD CALL') === 'INSTANCE METHOD CALL';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  public void testInterfaceUsageClassDefaultValue2() {
    String script =
        "return new org.treblereel.j2cl.exports.impl.v10.MyInterface().test1('INSTANCE METHOD CALL') === 'INSTANCE METHOD CALL';";
    Boolean result = (Boolean) driver.executeScript(script);
    assertTrue(result);
  }

  @Test
  @Ignore("not implemented yet")
  public void testSimplePojoJsTypeDefault() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.SimplePojoJsType().instance_fieldOne';");
    assertEquals("static_fieldOne", result);
  }

  @Test
  @Ignore("not implemented yet")
  public void testSimplePojoJsTypeProtected() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.SimplePojoJsType().instance_fieldTwo';");
    assertEquals("static_fieldTwo", result);
  }

  @Test
  @Ignore("not implemented yet")
  public void testSimplePojoJsTypePublic() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.SimplePojoJsType().instance_fieldThree';");
    assertEquals("static_fieldThree", result);
  }

  @Test
  @Ignore("not implemented yet")
  public void testSimplePojoJsTypeStaticDefault() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.SimplePojoJsType.static_fieldOne';");
    assertEquals("static_fieldOne", result);
  }

  @Test
  @Ignore("not implemented yet")
  public void testSimplePojoJsTypeStaticProtected() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.SimplePojoJsType.static_fieldTwo';");
    assertEquals("static_fieldTwo", result);
  }

  @Test
  @Ignore("not implemented yet")
  public void testSimplePojoJsTypeStaticPublic() {
    String result =
        (String)
            driver.executeScript(
                "return new org.treblereel.j2cl.exports.SimplePojoJsType.static_fieldThree';");
    assertEquals("static_fieldThree", result);
  }

  @After
  public void after() {
    driver.quit();
  }
}
