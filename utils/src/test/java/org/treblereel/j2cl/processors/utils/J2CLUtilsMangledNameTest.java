/*
 * Copyright © 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.treblereel.j2cl.processors.utils;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.*;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class J2CLUtilsMangledNameTest {

  private void compileAndTest(JavaFileObject source, BiConsumer<J2CLUtils, Elements> assertions) {
    List<AssertionError> errors = new ArrayList<>();
    MangledNameTestProcessor processor =
        new MangledNameTestProcessor(
            (processingEnv, roundEnv) -> {
              try {
                J2CLUtils utils = new J2CLUtils(processingEnv);
                assertions.accept(utils, processingEnv.getElementUtils());
              } catch (AssertionError e) {
                errors.add(e);
              }
            });
    Compilation compilation = javac().withProcessors(processor).compile(source);
    assertEquals(
        "Compilation failed: " + compilation.diagnostics(),
        Compilation.Status.SUCCESS,
        compilation.status());
    if (!errors.isEmpty()) {
      throw errors.get(0);
    }
  }

  private ExecutableElement findMethod(TypeElement type, String name) {
    return ElementFilter.methodsIn(type.getEnclosedElements()).stream()
        .filter(m -> m.getSimpleName().contentEquals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Method not found: " + name));
  }

  private ExecutableElement findMethod(TypeElement type, String name, int paramCount) {
    return ElementFilter.methodsIn(type.getEnclosedElements()).stream()
        .filter(m -> m.getSimpleName().contentEquals(name))
        .filter(m -> m.getParameters().size() == paramCount)
        .findFirst()
        .orElseThrow(
            () ->
                new AssertionError(
                    "Method not found: " + name + " with " + paramCount + " params"));
  }

  private VariableElement findField(TypeElement type, String name) {
    return ElementFilter.fieldsIn(type.getEnclosedElements()).stream()
        .filter(f -> f.getSimpleName().contentEquals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Field not found: " + name));
  }

  // ========== Plain class method tests ==========

  @Test
  public void testPlainClassPublicMethodMangledName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.PlainClass",
            "package test;\n"
                + "public class PlainClass {\n"
                + "  public void publicMethod() {}\n"
                + "  void packageMethod() {}\n"
                + "  private void privateMethod() {}\n"
                + "  public static void staticMethod() {}\n"
                + "  public final void finalMethod() {}\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.PlainClass");

          assertEquals(
              "m_publicMethod__void", utils.getMethodMangledName(findMethod(type, "publicMethod")));
          assertEquals(
              "m_staticMethod__void", utils.getMethodMangledName(findMethod(type, "staticMethod")));
          assertEquals(
              "m_finalMethod__void", utils.getMethodMangledName(findMethod(type, "finalMethod")));

          String packageMangled = utils.getMethodMangledName(findMethod(type, "packageMethod"));
          assertTrue(
              "Package-private should contain '$pp_test': " + packageMangled,
              packageMangled.contains("$pp_test"));

          String privateMangled = utils.getMethodMangledName(findMethod(type, "privateMethod"));
          assertTrue(
              "Private should contain '$p_test_PlainClass': " + privateMangled,
              privateMangled.contains("$p_test_PlainClass"));
        });
  }

  @Test
  public void testPlainClassMethodReturnTypeInMangledName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ReturnTypeClass",
            "package test;\n"
                + "public class ReturnTypeClass {\n"
                + "  public void voidMethod() {}\n"
                + "  public String stringMethod() { return null; }\n"
                + "  public int intMethod() { return 0; }\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.ReturnTypeClass");

          assertEquals(
              "m_voidMethod__void", utils.getMethodMangledName(findMethod(type, "voidMethod")));
          assertEquals(
              "m_stringMethod__java_lang_String",
              utils.getMethodMangledName(findMethod(type, "stringMethod")));
          assertEquals(
              "m_intMethod__int", utils.getMethodMangledName(findMethod(type, "intMethod")));
        });
  }

  @Test
  public void testPlainClassMethodParametersInMangledName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ParamClass",
            "package test;\n"
                + "public class ParamClass {\n"
                + "  public void noArgs() {}\n"
                + "  public void oneArg(String s) {}\n"
                + "  public void twoArgs(String s, int i) {}\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.ParamClass");

          assertEquals("m_noArgs__void", utils.getMethodMangledName(findMethod(type, "noArgs")));
          assertEquals(
              "m_oneArg__java_lang_String__void",
              utils.getMethodMangledName(findMethod(type, "oneArg")));
          assertEquals(
              "m_twoArgs__java_lang_String__int__void",
              utils.getMethodMangledName(findMethod(type, "twoArgs")));
        });
  }

  @Test
  public void testOverloadedMethodsProduceDifferentMangledNames() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.OverloadClass",
            "package test;\n"
                + "public class OverloadClass {\n"
                + "  public void method() {}\n"
                + "  public void method(String s) {}\n"
                + "  public void method(int i) {}\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.OverloadClass");

          assertEquals("m_method__void", utils.getMethodMangledName(findMethod(type, "method", 0)));

          List<ExecutableElement> oneArgMethods =
              ElementFilter.methodsIn(type.getEnclosedElements()).stream()
                  .filter(m -> m.getSimpleName().contentEquals("method"))
                  .filter(m -> m.getParameters().size() == 1)
                  .collect(java.util.stream.Collectors.toList());
          java.util.Map<String, String> byParamType = new java.util.HashMap<>();
          for (ExecutableElement m : oneArgMethods) {
            String paramType = m.getParameters().get(0).asType().toString();
            byParamType.put(paramType, utils.getMethodMangledName(m));
          }
          assertEquals("m_method__java_lang_String__void", byParamType.get("java.lang.String"));
          assertEquals("m_method__int__void", byParamType.get("int"));
        });
  }

  // ========== @JsType(isNative=false) tests ==========

  @Test
  public void testJsTypeMethodsReturnSimpleName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.JsTypeClass",
            "package test;\n"
                + "import jsinterop.annotations.JsType;\n"
                + "@JsType\n"
                + "public class JsTypeClass {\n"
                + "  public void publicMethod() {}\n"
                + "  private void privateMethod() {}\n"
                + "  public static void staticMethod() {}\n"
                + "  public void withParam(String s) {}\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.JsTypeClass");

          assertEquals(
              "publicMethod", utils.getMethodMangledName(findMethod(type, "publicMethod")));
          assertEquals(
              "privateMethod", utils.getMethodMangledName(findMethod(type, "privateMethod")));
          assertEquals(
              "staticMethod", utils.getMethodMangledName(findMethod(type, "staticMethod")));
          assertEquals("withParam", utils.getMethodMangledName(findMethod(type, "withParam")));
        });
  }

  // ========== @JsType(isNative=true) tests ==========

  @Test
  public void testNativeJsTypeMethodsAreMangled() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.NativeJsTypeClass",
            "package test;\n"
                + "import jsinterop.annotations.JsType;\n"
                + "@JsType(isNative = true)\n"
                + "public class NativeJsTypeClass {\n"
                + "  public native void publicMethod();\n"
                + "  public native void withParam(String s);\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.NativeJsTypeClass");

          assertEquals(
              "m_publicMethod__void", utils.getMethodMangledName(findMethod(type, "publicMethod")));
          assertEquals(
              "m_withParam__java_lang_String__void",
              utils.getMethodMangledName(findMethod(type, "withParam")));
        });
  }

  // ========== @JsProperty on methods ==========

  @Test
  public void testJsPropertyMethodReturnsSimpleName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.JsPropertyMethodClass",
            "package test;\n"
                + "import jsinterop.annotations.JsProperty;\n"
                + "public class JsPropertyMethodClass {\n"
                + "  @JsProperty public String getName() { return null; }\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.JsPropertyMethodClass");
          assertEquals("getName", utils.getMethodMangledName(findMethod(type, "getName")));
        });
  }

  @Test
  public void testJsPropertyMethodWithCustomName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.JsPropertyCustomClass",
            "package test;\n"
                + "import jsinterop.annotations.JsProperty;\n"
                + "public class JsPropertyCustomClass {\n"
                + "  @JsProperty(name = \"customProp\") public String getCustom() { return null; }\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.JsPropertyCustomClass");
          assertEquals("customProp", utils.getMethodMangledName(findMethod(type, "getCustom")));
        });
  }

  // ========== Field tests ==========

  @Test
  public void testPlainClassFieldsAreMangledWithExactNames() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.FieldClass",
            "package test;\n"
                + "public class FieldClass {\n"
                + "  public String publicField;\n"
                + "  private int privateField;\n"
                + "  static String staticField;\n"
                + "  final String finalField = \"\";\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.FieldClass");

          assertEquals(
              "f_publicField__test_FieldClass",
              utils.getVariableMangledName(findField(type, "publicField")));
          assertEquals(
              "f_staticField__test_FieldClass",
              utils.getVariableMangledName(findField(type, "staticField")));

          String privateMangled = utils.getVariableMangledName(findField(type, "privateField"));
          assertTrue(
              "Private field should start with 'f_': " + privateMangled,
              privateMangled.startsWith("f_privateField__test_FieldClass"));

          String finalMangled = utils.getVariableMangledName(findField(type, "finalField"));
          assertTrue(
              "Final field should start with 'f_': " + finalMangled,
              finalMangled.startsWith("f_finalField__test_FieldClass"));
        });
  }

  // ========== @JsProperty on fields ==========

  @Test
  public void testJsPropertyFieldReturnsSimpleName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.JsPropertyFieldClass",
            "package test;\n"
                + "import jsinterop.annotations.JsProperty;\n"
                + "public class JsPropertyFieldClass {\n"
                + "  @JsProperty public String value;\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.JsPropertyFieldClass");
          assertEquals("value", utils.getVariableMangledName(findField(type, "value")));
        });
  }

  @Test
  public void testJsPropertyFieldWithCustomName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.JsPropertyFieldCustomClass",
            "package test;\n"
                + "import jsinterop.annotations.JsProperty;\n"
                + "public class JsPropertyFieldCustomClass {\n"
                + "  @JsProperty(name = \"customName\") public String custom;\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.JsPropertyFieldCustomClass");
          assertEquals("customName", utils.getVariableMangledName(findField(type, "custom")));
        });
  }

  // ========== @JsType class fields (should still be mangled) ==========

  @Test
  public void testJsTypeClassFieldsStillMangled() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.JsTypeFieldClass",
            "package test;\n"
                + "import jsinterop.annotations.JsType;\n"
                + "@JsType\n"
                + "public class JsTypeFieldClass {\n"
                + "  public String publicField;\n"
                + "  private int privateField;\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.JsTypeFieldClass");
          String publicMangled = utils.getVariableMangledName(findField(type, "publicField"));
          String privateMangled = utils.getVariableMangledName(findField(type, "privateField"));

          assertTrue(
              "Fields in @JsType class should still be mangled (no @JsProperty): " + publicMangled,
              publicMangled.startsWith("f_"));
          assertTrue(
              "Fields in @JsType class should still be mangled (no @JsProperty): " + privateMangled,
              privateMangled.startsWith("f_"));
        });
  }

  // ========== isJsType tests ==========

  @Test
  public void testIsJsTypeNonNative() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.IsJsTypeClass",
            "package test;\n"
                + "import jsinterop.annotations.JsType;\n"
                + "@JsType\n"
                + "public class IsJsTypeClass {}\n");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.IsJsTypeClass");
          assertTrue("@JsType should be JsType", utils.isJsType(type));
        });
  }

  @Test
  public void testIsJsTypeNative() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.IsNativeJsTypeClass",
            "package test;\n"
                + "import jsinterop.annotations.JsType;\n"
                + "@JsType(isNative = true)\n"
                + "public class IsNativeJsTypeClass {}\n");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.IsNativeJsTypeClass");
          assertFalse("@JsType(isNative=true) should NOT be JsType", utils.isJsType(type));
        });
  }

  @Test
  public void testIsJsTypePlainClass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.PlainIsJsTypeClass", "package test;\n" + "public class PlainIsJsTypeClass {}\n");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.PlainIsJsTypeClass");
          assertFalse("Plain class should NOT be JsType", utils.isJsType(type));
        });
  }

  // ========== Default constructor test ==========

  @Test
  public void testDefaultConstructorMangledName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ConstructorClass",
            "package test;\n"
                + "public class ConstructorClass {\n"
                + "  public ConstructorClass() {}\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.ConstructorClass");
          assertEquals(
              "$ctor__test_ConstructorClass__void", utils.getDefaultConstructorMangledName(type));
        });
  }

  @Test
  public void testImplicitDefaultConstructorMangledName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ImplicitCtorClass",
            "package test;\n" + "public class ImplicitCtorClass {\n" + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.ImplicitCtorClass");
          assertEquals(
              "$ctor__test_ImplicitCtorClass__void", utils.getDefaultConstructorMangledName(type));
        });
  }

  // ========== getMethodMangledName with enclosing type ==========

  @Test
  public void testMethodMangledNameWithEnclosingType() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.EnclosingTypeClass",
            "package test;\n"
                + "public class EnclosingTypeClass {\n"
                + "  public void myMethod() {}\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.EnclosingTypeClass");
          ExecutableElement method = findMethod(type, "myMethod");

          assertEquals("m_myMethod__void", utils.getMethodMangledName(method, type));
          assertEquals(
              "Both overloads should produce the same result for own type",
              utils.getMethodMangledName(method),
              utils.getMethodMangledName(method, type));
        });
  }

  @Test
  public void testInheritedMethodWithChildEnclosingType() {
    JavaFileObject base =
        JavaFileObjects.forSourceString(
            "test.BaseClass",
            "package test;\n"
                + "public class BaseClass {\n"
                + "  public void baseMethod() {}\n"
                + "  public void overridden() {}\n"
                + "}");
    JavaFileObject child =
        JavaFileObjects.forSourceString(
            "test.ChildClass",
            "package test;\n"
                + "public class ChildClass extends BaseClass {\n"
                + "  @Override public void overridden() {}\n"
                + "  public void childOnly() {}\n"
                + "}");
    List<AssertionError> errors = new ArrayList<>();
    MangledNameTestProcessor processor =
        new MangledNameTestProcessor(
            (processingEnv, roundEnv) -> {
              try {
                J2CLUtils utils = new J2CLUtils(processingEnv);
                Elements elems = processingEnv.getElementUtils();
                TypeElement baseType = elems.getTypeElement("test.BaseClass");
                TypeElement childType = elems.getTypeElement("test.ChildClass");

                ExecutableElement baseMethod = findMethod(baseType, "baseMethod");
                ExecutableElement overriddenInBase = findMethod(baseType, "overridden");
                ExecutableElement overriddenInChild = findMethod(childType, "overridden");

                assertEquals(
                    "m_baseMethod__void", utils.getMethodMangledName(baseMethod, baseType));
                assertEquals(
                    "Parent method with child enclosing type should produce same mangled name",
                    "m_baseMethod__void",
                    utils.getMethodMangledName(baseMethod, childType));

                assertEquals(
                    "m_overridden__void", utils.getMethodMangledName(overriddenInBase, childType));
                assertEquals(
                    "m_overridden__void", utils.getMethodMangledName(overriddenInChild, childType));
              } catch (AssertionError e) {
                errors.add(e);
              }
            });
    Compilation compilation = javac().withProcessors(processor).compile(base, child);
    assertEquals(Compilation.Status.SUCCESS, compilation.status());
    if (!errors.isEmpty()) {
      throw errors.get(0);
    }
  }

  @Test
  public void testInheritedMethodFromPlainBaseWithJsTypeChild() {
    JavaFileObject base =
        JavaFileObjects.forSourceString(
            "test.PlainBase",
            "package test;\n"
                + "public class PlainBase {\n"
                + "  public void inherited() {}\n"
                + "}");
    JavaFileObject child =
        JavaFileObjects.forSourceString(
            "test.JsChild",
            "package test;\n"
                + "import jsinterop.annotations.JsType;\n"
                + "@JsType\n"
                + "public class JsChild extends PlainBase {\n"
                + "  public void childMethod() {}\n"
                + "}");
    List<AssertionError> errors = new ArrayList<>();
    MangledNameTestProcessor processor =
        new MangledNameTestProcessor(
            (processingEnv, roundEnv) -> {
              try {
                J2CLUtils utils = new J2CLUtils(processingEnv);
                Elements elems = processingEnv.getElementUtils();
                TypeElement baseType = elems.getTypeElement("test.PlainBase");
                TypeElement childType = elems.getTypeElement("test.JsChild");

                assertFalse(utils.isJsType(baseType));
                assertTrue(utils.isJsType(childType));

                ExecutableElement inherited = findMethod(baseType, "inherited");
                assertEquals(
                    "Inherited method from plain base via JsType child stays mangled",
                    "m_inherited__void",
                    utils.getMethodMangledName(inherited, childType));
              } catch (AssertionError e) {
                errors.add(e);
              }
            });
    Compilation compilation = javac().withProcessors(processor).compile(base, child);
    assertEquals(Compilation.Status.SUCCESS, compilation.status());
    if (!errors.isEmpty()) {
      throw errors.get(0);
    }
  }

  @Test
  public void testMethodMangledNameWithEnclosingTypeAndJsProperty() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.EnclosingJsPropClass",
            "package test;\n"
                + "import jsinterop.annotations.JsProperty;\n"
                + "public class EnclosingJsPropClass {\n"
                + "  @JsProperty public String getName() { return null; }\n"
                + "}");
    compileAndTest(
        source,
        (utils, elements) -> {
          TypeElement type = elements.getTypeElement("test.EnclosingJsPropClass");
          ExecutableElement method = findMethod(type, "getName");

          assertEquals(
              "@JsProperty should return simple name even with enclosing type overload",
              "getName",
              utils.getMethodMangledName(method, type));
        });
  }
}
