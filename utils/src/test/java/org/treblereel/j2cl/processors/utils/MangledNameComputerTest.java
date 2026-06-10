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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.treblereel.j2cl.processors.utils.MangledNameComputer.MemberVisibility;

public class MangledNameComputerTest {

  // ========== Type name mangling ==========

  @Test
  public void testMangleTypeName_qualifiedClass() {
    assertEquals("java_lang_String", MangledNameComputer.mangleTypeName("java.lang.String"));
  }

  @Test
  public void testMangleTypeName_primitive() {
    assertEquals("int", MangledNameComputer.mangleTypeName("int"));
    assertEquals("void", MangledNameComputer.mangleTypeName("void"));
    assertEquals("boolean", MangledNameComputer.mangleTypeName("boolean"));
  }

  @Test
  public void testMangleTypeName_innerClass() {
    assertEquals("test_Outer_Inner", MangledNameComputer.mangleTypeName("test.Outer.Inner"));
  }

  @Test
  public void testMangleTypeName_defaultPackage() {
    assertEquals("MyClass", MangledNameComputer.mangleTypeName("MyClass"));
  }

  @Test
  public void testMangleArrayTypeName_singleDimension() {
    assertEquals(
        "arrayOf_java_lang_String", MangledNameComputer.mangleArrayTypeName("java.lang.String", 1));
  }

  @Test
  public void testMangleArrayTypeName_multiDimension() {
    assertEquals("arrayOf_arrayOf_int", MangledNameComputer.mangleArrayTypeName("int", 2));
  }

  @Test
  public void testMangleArrayTypeName_threeDimensions() {
    assertEquals(
        "arrayOf_arrayOf_arrayOf_byte", MangledNameComputer.mangleArrayTypeName("byte", 3));
  }

  // ========== Method name mangling ==========

  @Test
  public void testMangleMethodName_publicVoidNoArgs() {
    assertEquals(
        "m_foo__void",
        MangledNameComputer.mangleMethodName(
            "foo",
            Collections.emptyList(),
            "void",
            MemberVisibility.PUBLIC,
            true,
            "test_MyClass",
            "test"));
  }

  @Test
  public void testMangleMethodName_withParamsAndReturnType() {
    assertEquals(
        "m_bar__java_lang_String__int__void",
        MangledNameComputer.mangleMethodName(
            "bar",
            List.of("java_lang_String", "int"),
            "void",
            MemberVisibility.PUBLIC,
            true,
            "test_MyClass",
            "test"));
  }

  @Test
  public void testMangleMethodName_withObjectReturnType() {
    assertEquals(
        "m_getValue__java_lang_String",
        MangledNameComputer.mangleMethodName(
            "getValue",
            Collections.emptyList(),
            "java_lang_String",
            MemberVisibility.PUBLIC,
            true,
            "test_MyClass",
            "test"));
  }

  @Test
  public void testMangleMethodName_privateInstance() {
    assertEquals(
        "m_secret__void_$p_test_MyClass",
        MangledNameComputer.mangleMethodName(
            "secret",
            Collections.emptyList(),
            "void",
            MemberVisibility.PRIVATE,
            true,
            "test_MyClass",
            "test"));
  }

  @Test
  public void testMangleMethodName_packagePrivateInstance() {
    assertEquals(
        "m_internal__void_$pp_com_example",
        MangledNameComputer.mangleMethodName(
            "internal",
            Collections.emptyList(),
            "void",
            MemberVisibility.PACKAGE_PRIVATE,
            true,
            "com_example_MyClass",
            "com.example"));
  }

  @Test
  public void testMangleMethodName_privateStatic_noSuffix() {
    assertEquals(
        "m_helper__void",
        MangledNameComputer.mangleMethodName(
            "helper",
            Collections.emptyList(),
            "void",
            MemberVisibility.PRIVATE,
            false,
            "test_MyClass",
            "test"));
  }

  @Test
  public void testMangleMethodName_protectedInstance_noSuffix() {
    assertEquals(
        "m_doWork__void",
        MangledNameComputer.mangleMethodName(
            "doWork",
            Collections.emptyList(),
            "void",
            MemberVisibility.PROTECTED,
            true,
            "test_MyClass",
            "test"));
  }

  // ========== Field name mangling ==========

  @Test
  public void testMangleFieldName_publicField() {
    assertEquals(
        "f_myField__test_MyClass",
        MangledNameComputer.mangleFieldName("myField", "test_MyClass", MemberVisibility.PUBLIC));
  }

  @Test
  public void testMangleFieldName_privateField() {
    assertEquals(
        "f_secret__test_MyClass_",
        MangledNameComputer.mangleFieldName("secret", "test_MyClass", MemberVisibility.PRIVATE));
  }

  @Test
  public void testMangleFieldName_packagePrivateField_noSuffix() {
    assertEquals(
        "f_data__com_example_Holder",
        MangledNameComputer.mangleFieldName(
            "data", "com_example_Holder", MemberVisibility.PACKAGE_PRIVATE));
  }

  // ========== Constructor mangling ==========

  @Test
  public void testMangleDefaultConstructorName() {
    assertEquals(
        "$ctor__test_MyClass__void",
        MangledNameComputer.mangleDefaultConstructorName("test_MyClass"));
  }

  @Test
  public void testMangleDefaultConstructorName_deepPackage() {
    assertEquals(
        "$ctor__com_example_deep_MyClass__void",
        MangledNameComputer.mangleDefaultConstructorName("com_example_deep_MyClass"));
  }
}
