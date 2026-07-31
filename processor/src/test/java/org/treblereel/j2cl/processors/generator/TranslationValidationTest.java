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

package org.treblereel.j2cl.processors.generator;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;
import org.treblereel.j2cl.processors.GWT3Processor;

public class TranslationValidationTest {

  private void assertProcessorRejects(JavaFileObject source, String expectedMessagePart) {
    try {
      javac().withProcessors(new GWT3Processor()).compile(source);
      fail("Expected processor to reject with: " + expectedMessagePart);
    } catch (RuntimeException e) {
      assertTrue(
          "Expected message containing '" + expectedMessagePart + "' but got: " + e.getMessage(),
          e.getMessage().contains(expectedMessagePart));
    }
  }

  // ========== P0: Bean validation (checkBean) ==========

  @Test
  public void testNonPublicInterface() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.PackagePrivateBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "interface PackagePrivateBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello\")\n"
                + "  String hello();\n"
                + "}");
    assertProcessorRejects(source, "must be public");
  }

  @Test
  public void testClassNotInterface() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.NotAnInterface",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public abstract class NotAnInterface {\n"
                + "  @TranslationKey(defaultValue = \"hello\")\n"
                + "  public abstract String hello();\n"
                + "}");
    assertProcessorRejects(source, "must be interface");
  }

  // ========== P0: Method validation (check) ==========

  @Test
  public void testNonAbstractMethod() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.DefaultMethodBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface DefaultMethodBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello\")\n"
                + "  default String hello() { return \"hello\"; }\n"
                + "}");
    assertProcessorRejects(source, "must be abstract");
  }

  @Test
  public void testNonStringParameter() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.NonStringParamBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface NonStringParamBundle {\n"
                + "  @TranslationKey(defaultValue = \"count: {$count}\")\n"
                + "  String withCount(int count);\n"
                + "}");
    assertProcessorRejects(source, "Method params must be Strings");
  }

  // ========== P1: Placeholder mismatch (validatePlaceHolders) ==========

  @Test
  public void testExtraMethodParam() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ExtraParamBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface ExtraParamBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello\")\n"
                + "  String hello(String unused);\n"
                + "}");
    assertProcessorRejects(source, "Size of placeholders and method args is not the same");
  }

  @Test
  public void testExtraPlaceholder() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ExtraPlaceholderBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface ExtraPlaceholderBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello {$name}\")\n"
                + "  String hello();\n"
                + "}");
    assertProcessorRejects(source, "Size of placeholders and method args is not the same");
  }

  @Test
  public void testMismatchedPlaceholderName() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.MismatchedNameBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface MismatchedNameBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello {$bar}\")\n"
                + "  String hello(String foo);\n"
                + "}");
    assertProcessorRejects(source, "Placeholder bar must have corresponding method arg");
  }

  @Test
  public void testValidPlaceholders() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ValidBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface ValidBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello {$name}, you are {$age}\")\n"
                + "  String greet(String name, String age);\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();
  }

  // ========== Key collision across bundles ==========

  @Test
  public void testSameKeyAcrossTwoBundles() {
    JavaFileObject bundle1 =
        JavaFileObjects.forSourceString(
            "test.BundleA",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface BundleA {\n"
                + "  @TranslationKey(defaultValue = \"Hello from A\", key = \"greeting\")\n"
                + "  String hello();\n"
                + "}");
    JavaFileObject bundle2 =
        JavaFileObjects.forSourceString(
            "test.BundleB",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface BundleB {\n"
                + "  @TranslationKey(defaultValue = \"Hello from B\", key = \"greeting\")\n"
                + "  String hello();\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(bundle1, bundle2);
    assertThat(compilation).succeeded();
  }

  // ========== TranslationBundle.defaultValue ==========

  @Test
  public void testBundleWithCustomDefaultValue() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.CustomNameBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle(defaultValue = \"MyCustomMessages\")\n"
                + "public interface CustomNameBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello\")\n"
                + "  String hello();\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();
  }

  // ========== P3: Edge cases ==========

  @Test
  public void testEmptyBundle() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.EmptyBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "@TranslationBundle\n"
                + "public interface EmptyBundle {\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();
  }

  @Test
  public void testDuplicatePlaceholderInMessage() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.DupPlaceholderBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface DupPlaceholderBundle {\n"
                + "  @TranslationKey(defaultValue = \"{$arg} and {$arg}\")\n"
                + "  String dup(String arg);\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();
  }
}
