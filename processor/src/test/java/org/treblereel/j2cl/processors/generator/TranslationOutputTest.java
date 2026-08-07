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

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.util.Optional;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;
import org.treblereel.j2cl.processors.GWT3Processor;

public class TranslationOutputTest {

  private String getGeneratedResource(Compilation compilation, String pkg, String filename) {
    Optional<JavaFileObject> file =
        compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, pkg, filename);
    assertTrue("Expected generated file: " + pkg + "/" + filename, file.isPresent());
    try {
      return file.get().getCharContent(true).toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getGeneratedSource(Compilation compilation, String qualifiedName) {
    Optional<JavaFileObject> file = compilation.generatedSourceFile(qualifiedName);
    assertTrue("Expected generated source: " + qualifiedName, file.isPresent());
    try {
      return file.get().getCharContent(true).toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ========== Native JS generation ==========

  @Test
  public void testNativeJsGeneratedForSimpleKey() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.SimpleBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface SimpleBundle {\n"
                + "  @TranslationKey(defaultValue = \"Hello world\")\n"
                + "  String hello();\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();

    String nativeJs = getGeneratedResource(compilation, "test", "SimpleBundleImpl.native.js");
    assertTrue("Should contain goog.getMsg", nativeJs.contains("goog.getMsg('Hello world')"));
    assertTrue("Should contain MSG_hello key", nativeJs.contains("MSG_hello"));
    assertTrue("Should contain @desc", nativeJs.contains("/** @desc hello */"));
  }

  @Test
  public void testNativeJsPlaceholderParams() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ParamBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface ParamBundle {\n"
                + "  @TranslationKey(defaultValue = \"Hello {$name}!\")\n"
                + "  String greet(String name);\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();

    String nativeJs = getGeneratedResource(compilation, "test", "ParamBundleImpl.native.js");
    assertTrue("Should contain placeholder dict", nativeJs.contains("name: _name"));
    assertTrue("Should have _name param", nativeJs.contains("function(_name)"));
  }

  @Test
  public void testNativeJsHtmlFlag() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.HtmlBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface HtmlBundle {\n"
                + "  @TranslationKey(defaultValue = \"{$content}\", html = true)\n"
                + "  String render(String content);\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();

    String nativeJs = getGeneratedResource(compilation, "test", "HtmlBundleImpl.native.js");
    assertTrue("Should contain html: true", nativeJs.contains("html: true"));
  }

  @Test
  public void testNativeJsUnescapeFlag() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.UnescapeBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface UnescapeBundle {\n"
                + "  @TranslationKey(defaultValue = \"Hello\", unescapeHtmlEntities = true)\n"
                + "  String hello();\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();

    String nativeJs = getGeneratedResource(compilation, "test", "UnescapeBundleImpl.native.js");
    assertTrue(
        "Should contain unescapeHtmlEntities: true",
        nativeJs.contains("unescapeHtmlEntities: true"));
  }

  @Test
  public void testNativeJsBothFlags() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.BothFlagsBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface BothFlagsBundle {\n"
                + "  @TranslationKey(defaultValue = \"{$arg}\", html = true, unescapeHtmlEntities = true)\n"
                + "  String render(String arg);\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();

    String nativeJs = getGeneratedResource(compilation, "test", "BothFlagsBundleImpl.native.js");
    assertTrue("Should contain html: true", nativeJs.contains("html: true"));
    assertTrue(
        "Should contain unescapeHtmlEntities: true",
        nativeJs.contains("unescapeHtmlEntities: true"));
  }

  // ========== Impl.java generation ==========

  @Test
  public void testImplClassGenerated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.ImplTestBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface ImplTestBundle {\n"
                + "  @TranslationKey(defaultValue = \"hello\")\n"
                + "  String hello();\n"
                + "  @TranslationKey(defaultValue = \"bye {$name}\")\n"
                + "  String bye(String name);\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();

    String implSource = getGeneratedSource(compilation, "test.ImplTestBundleImpl");
    assertTrue("Should implement the interface", implSource.contains("implements ImplTestBundle"));
    assertTrue("Should have hello() method", implSource.contains("public String hello()"));
    assertTrue(
        "Should have bye(String) method", implSource.contains("public String bye(String name)"));
    assertTrue(
        "Should throw UnsupportedOperationException",
        implSource.contains("UnsupportedOperationException"));
  }

  // ========== Custom key ==========

  @Test
  public void testCustomKeyAttribute() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.CustomKeyBundle",
            "package test;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationBundle;\n"
                + "import org.treblereel.j2cl.processors.annotations.TranslationKey;\n"
                + "@TranslationBundle\n"
                + "public interface CustomKeyBundle {\n"
                + "  @TranslationKey(defaultValue = \"Hello\", key = \"custom_greeting\")\n"
                + "  String hello();\n"
                + "}");
    Compilation compilation = javac().withProcessors(new GWT3Processor()).compile(source);
    assertThat(compilation).succeeded();

    String nativeJs = getGeneratedResource(compilation, "test", "CustomKeyBundleImpl.native.js");
    assertTrue("Should use custom key in MSG variable", nativeJs.contains("MSG_custom_greeting"));
    assertTrue("Should use custom key in @desc", nativeJs.contains("/** @desc custom_greeting */"));
  }
}
