/*
 * Copyright © 2021
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

import com.google.auto.common.MoreElements;
import com.google.javascript.jscomp.GoogleJsMessageIdGenerator;
import com.google.javascript.jscomp.JsMessage;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

public class TranslationGenerator extends AbstractGenerator {

  private final GoogleJsMessageIdGenerator idGenerator = new GoogleJsMessageIdGenerator(null);

  public TranslationGenerator(AptContext context) {
    super(context, TranslationBundle.class);
  }

  @Override
  public void generate(Set<Element> elements) {
    Map<TypeElement, Set<ExecutableElement>> beansAndMethods = new HashMap<>();
    for (Element element : elements) {
      TypeElement parent = checkBean(element);
      Set<ExecutableElement> methods =
          ElementFilter.methodsIn(element.getEnclosedElements()).stream()
              .filter(e -> e.getAnnotation(TranslationKey.class) != null)
              .map(elm -> check(elm))
              .collect(Collectors.toSet());
      beansAndMethods.put(parent, methods);
    }

    Map<String, Map<String, String>> toMapping = new HashMap<>();

    beansAndMethods.forEach(
        (bean, methods) -> {
          generateImpl(bean, methods);
          generateNative(bean, methods);
          Map<String, Set<Properties>> bundles = processBundles(bean);
          for (Map.Entry<String, Set<Properties>> entry : bundles.entrySet()) {
            if (!toMapping.containsKey(entry.getKey())) {
              toMapping.put(entry.getKey(), new HashMap<>());
            }
            for (Properties property : entry.getValue()) {
              Set<String> keys = property.stringPropertyNames();
              for (String key : keys) {
                toMapping.get(entry.getKey()).put(key, property.getProperty(key));
              }
            }
          }
        });

    Map<String, Map<String, String>> propertiesMapping = processMapping(toMapping);
    for (Map.Entry<String, Map<String, String>> entry : propertiesMapping.entrySet()) {
      generateXTB(entry.getKey(), entry.getValue());
    }
  }

  private void generateNative(TypeElement bean, Set<ExecutableElement> methods) {
    String impl = bean.getSimpleName().toString() + "Impl";
    StringBuilder sb = new StringBuilder();

    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());

    methods.forEach(
        method -> {
          writeMethod(sb, method, impl);
        });

    writeResource(
        impl + ".native.js",
        MoreElements.getPackage(bean).getQualifiedName().toString(),
        sb.toString());
  }

  private void writeMsg(StringBuilder sb, ExecutableElement method, JsMessage asJsMessage) {
    TranslationKey translationKey = method.getAnnotation(TranslationKey.class);
    String key = getKey(method);
    String id = getId(asJsMessage);
    MessageMapping messageMapping = new MessageMapping(id, key, translationKey.defaultValue());
    defaultMessageMapping.put(key, messageMapping);

    String defaultValue = translationKey.defaultValue();
    sb.append(String.format("/** @desc %s */", key));
    sb.append(System.lineSeparator());

    sb.append("   var ");
    sb.append("MSG_" + key);
    sb.append(" = goog.getMsg('");
    sb.append(defaultValue);
    sb.append("'");

    if (!asJsMessage.placeholders().isEmpty()) {
      sb.append(", { ");
      sb.append(
          asJsMessage.placeholders().stream()
              .map(placeholder -> placeholder + ": _" + placeholder)
              .collect(Collectors.joining(",")));
      sb.append(" }");
    }

    if (translationKey.html() || translationKey.unescapeHtmlEntities()) {
      if (asJsMessage.placeholders().isEmpty()) {
        sb.append(",{}");
      }

      sb.append(", { ");
      if (translationKey.html()) {
        sb.append("html: true");
        if (translationKey.unescapeHtmlEntities()) {
          sb.append(",");
        }
      }
      if (translationKey.unescapeHtmlEntities()) {
        sb.append("unescapeHtmlEntities: true");
      }
      sb.append(" }");
    }

    sb.append(");");
    sb.append(System.lineSeparator());
  }

  private String getKey(ExecutableElement method) {
    if (!method.getAnnotation(TranslationKey.class).key().equals("<auto>")) {
      return method.getAnnotation(TranslationKey.class).key();
    }

    return method.getSimpleName().toString();
  }

  private String getId(JsMessage jsMessage) {
    return idGenerator.generateId(jsMessage.getKey(), jsMessage.getParts());
  }

  private void writeMethod(StringBuilder sb, ExecutableElement method, String impl) {
    TranslationKey translationKey = method.getAnnotation(TranslationKey.class);
    String key = getKey(method);
    JsMessage asJsMessage = toJsMessage(key, translationKey.defaultValue());
    validatePlaceHolders(method, asJsMessage);

    sb.append(impl);
    sb.append(".prototype.");
    sb.append(generateJsMethodName(method));
    sb.append(" = function(");
    sb.append(
        method.getParameters().stream()
            .map(p -> "_" + p.getSimpleName().toString())
            .collect(Collectors.joining(",")));
    sb.append(") {");
    sb.append(System.lineSeparator());
    writeMsg(sb, method, asJsMessage);
    sb.append(String.format("  return %s;", "MSG_" + getKey(method)));
    sb.append(System.lineSeparator());

    sb.append("}");
    sb.append(System.lineSeparator());
  }

  private void validatePlaceHolders(ExecutableElement method, JsMessage message) {
    Set<String> methodParams =
        method.getParameters().stream()
            .map(p -> p.getSimpleName().toString())
            .collect(Collectors.toSet());

    Set<String> placeholders = message.placeholders().stream().collect(Collectors.toSet());
    if (methodParams.size() != placeholders.size()) {
      throw new GenerationException(method, "Size of placeholders and method args is not the same");
    }
    for (String placeholder : placeholders) {
      if (!methodParams.contains(placeholder)) {
        throw new GenerationException(
            method,
            String.format("Placeholder %s must have corresponding method arg", placeholder));
      }
    }
  }

  private JsMessage toJsMessage(String k, String msg) {
    String key = "MSG_" + k;
    try {
      return new JsMessage.Builder().setKey(key).setMsgText(msg).build();
    } catch (JsMessage.PlaceholderFormatException e) {
      throw new Error(e);
    }
  }

  private String generateJsMethodName(ExecutableElement method) {
    StringBuilder sb = new StringBuilder();
    sb.append("m_");
    sb.append(method.getSimpleName());
    sb.append("__");
    sb.append(
        method.getParameters().stream()
            .map(p -> "java_lang_String")
            .collect(Collectors.joining("__")));
    return sb.toString();
  }

  private void generateImpl(TypeElement bean, Set<ExecutableElement> methods) {
    String impl = bean.getSimpleName().toString() + "Impl";
    StringBuilder sb = new StringBuilder();
    sb.append("package ");
    sb.append(MoreElements.getPackage(bean));
    sb.append(";");
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());

    sb.append("public class ");
    sb.append(impl);
    sb.append(" implements ");
    sb.append(bean.getSimpleName().toString());
    sb.append(" {");
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());

    sb.append(
        "  private final UnsupportedOperationException exception = new UnsupportedOperationException(\"must be implemented by gwt3-processors\");");
    sb.append(System.lineSeparator());

    methods.forEach(
        method -> {
          sb.append(System.lineSeparator());
          sb.append(System.lineSeparator());
          sb.append("public String ");
          sb.append(method.getSimpleName().toString());
          sb.append("(");
          sb.append(
              method.getParameters().stream()
                  .map(p -> "String " + p.getSimpleName().toString())
                  .collect(Collectors.joining(",")));
          sb.append(") { throw exception; }");
          sb.append(System.lineSeparator());
          sb.append(System.lineSeparator());
        });

    sb.append("}");
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());

    String name = MoreElements.getPackage(bean).getQualifiedName().toString() + "." + impl;
    writeSource(name, sb.toString());
  }

  private Map<String, Set<Properties>> processBundles(Element element) {
    Trees trees = Trees.instance(context.getProcessingEnv());
    JavaFileObject sourceFile = trees.getPath(element).getCompilationUnit().getSourceFile();
    URI uri = sourceFile.toUri();

    File f = new File(uri.getPath());
    File folder = f.getParentFile();

    File[] files =
        folder.listFiles(
            (dir, candidate) ->
                candidate.startsWith(element.getSimpleName().toString())
                    && candidate.endsWith(".properties"));

    Map<String, Set<Properties>> result = new HashMap<>();

    if (files == null) {
      return result;
    }

    for (File file : files) {
      String filename = new File(file.getPath()).getName();
      String locale =
          filename.replaceFirst(element.getSimpleName().toString(), "").replace(".properties", "");
      if (locale.startsWith("_")) {
        locale = locale.replaceFirst("_", "");
      }
      if (!result.containsKey(locale)) {
        result.put(locale, new HashSet<>());
      }
      try {
        Properties prop = new Properties();
        prop.load(file.toURL().openStream());
        result.get(locale).add(prop);
      } catch (IOException e) {
        throw new Error(e);
      }
    }
    return result;
  }

  private ExecutableElement check(Element elm) {
    ExecutableElement method = (ExecutableElement) elm;

    if (!method.getModifiers().contains(Modifier.PUBLIC)) {
      throw new GenerationException(
          method,
          "Method, annotated with "
              + TranslationBundle.class.getCanonicalName()
              + ", must be public");
    }
    if (!method.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new GenerationException(
          method,
          "Method, annotated with "
              + TranslationBundle.class.getCanonicalName()
              + ", must be abstract");
    }

    if (method.getModifiers().contains(Modifier.NATIVE)) {
      throw new GenerationException(
          method,
          "Method, annotated with "
              + TranslationBundle.class.getCanonicalName()
              + ", mustn't be native");
    }
    if (!method.getParameters().isEmpty()) {
      for (VariableElement parameter : method.getParameters()) {
        if (!parameter.asType().toString().equals(String.class.getCanonicalName())) {
          throw new GenerationException(method, "Method params must be Strings");
        }
      }
    }

    return method;
  }

  private TypeElement checkBean(Element elm) {
    TypeElement parent = (TypeElement) elm;

    if (!parent.getModifiers().contains(Modifier.PUBLIC)) {
      throw new GenerationException(
          parent,
          "Interface that contains method annotated with "
              + TranslationBundle.class.getCanonicalName()
              + ", must be public");
    }

    if (!parent.getKind().isInterface()) {
      throw new GenerationException(
          parent,
          "Bean that contains method annotated with "
              + TranslationBundle.class.getCanonicalName()
              + ", must be interface");
    }

    return parent;
  }

  private static class MessageMapping {
    private String id;
    private String key;
    private String value;

    private MessageMapping(String id, String key, String value) {
      this.id = id;
      this.key = key;
      this.value = value;
    }
  }

  private void generateXTB(String locale, Map<String, String> mapping) {
    if (!locale.isEmpty()) {
      String source = new XTBGenerator(locale, mapping).generate();
      writeResource(
          "gwt3_message_bundle_" + locale + ".xtb",
          "org.treblereel.j2cl.processors.translation",
          source);
    }
  }

  private class XTBGenerator {
    private String locale;
    private Map<String, String> messages;

    private XTBGenerator(String locale, Map<String, String> messages) {
      this.locale = locale;
      this.messages = messages;
    }

    private static final String PH_JS_PREFIX = "{$";
    private static final String PH_JS_SUFFIX = "}";

    private String generate() {
      StringBuffer source = new StringBuffer();
      source.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      source.append(System.lineSeparator());
      source.append("<!DOCTYPE translationbundle SYSTEM \"translationbundle.dtd\">");
      source.append(System.lineSeparator());
      source.append(String.format("<translationbundle lang=\"%s\">", locale));
      source.append(System.lineSeparator());

      for (Map.Entry<String, String> message : messages.entrySet()) {
        String key = "MSG_" + message.getKey();
        String id = defaultMessageMapping.get(message.getKey()).id;
        JsMessage asJsMessage = toJsMessage(message.getKey(), message.getValue());
        StringBuilder parts = new StringBuilder();

        asJsMessage.getParts().stream()
            .map(String::valueOf)
            .forEach(
                p -> {
                  int phBegin = p.indexOf(PH_JS_PREFIX);
                  if (phBegin == 0) {
                    int phEnd = p.indexOf(PH_JS_SUFFIX, phBegin);
                    String phName = p.substring(phBegin + PH_JS_PREFIX.length(), phEnd);
                    parts.append("<ph name=\"");
                    parts.append(phName);
                    parts.append("\" />");
                  } else {
                    parts.append(escapeHtml(p));
                  }
                });
        source.append(
            String.format("<translation id=\"%s\" key=\"%s\">%s</translation>", id, key, parts));
        source.append(System.lineSeparator());
      }

      source.append("</translationbundle>");
      source.append(System.lineSeparator());
      return source.toString();
    }
  }

  private String escapeHtml(String part) {
    return part.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private Map<String, Map<String, String>> processMapping(
      Map<String, Map<String, String>> bundles) {
    Map<String, Map<String, String>> mapping = new HashMap<>();

    bundles
        .keySet()
        .forEach(
            locale -> {
              mapping.put(locale, new HashMap<>());

              for (Map.Entry<String, MessageMapping> entry : defaultMessageMapping.entrySet()) {
                String key = entry.getKey();
                MessageMapping messageMapping = entry.getValue();
                if (bundles.get(locale).containsKey(key)) {
                  mapping.get(locale).put(key, bundles.get(locale).get(key));
                } else {
                  mapping.get(locale).put(key, messageMapping.value);
                }
              }
            });
    return mapping;
  }

  private final Map<String, MessageMapping> defaultMessageMapping = new HashMap<>();
}
