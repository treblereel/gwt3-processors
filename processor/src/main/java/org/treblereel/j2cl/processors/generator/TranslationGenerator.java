/*
 * Copyright © 2022
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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;
import org.treblereel.j2cl.processors.context.AptContext;

public class TranslationGenerator extends AbstractGenerator {

  private final GoogleJsMessageIdGenerator idGenerator = new GoogleJsMessageIdGenerator(null);

  private final Map<String, MessageMapping> defaultMessageMapping = new HashMap<>();

  public TranslationGenerator(AptContext context) {
    super(context, TranslationBundle.class);
  }

  @Override
  public void generate(Element elm) {
    TypeElement element = MoreElements.asType(elm);

    List<VariableElement> fields =
        ElementFilter.fieldsIn(element.getEnclosedElements()).stream()
            .filter(e -> e.getAnnotation(TranslationKey.class) != null)
            .collect(Collectors.toList());

    for (VariableElement field : fields) {
      TranslationKey translationKey = field.getAnnotation(TranslationKey.class);
      String key = (String) field.getConstantValue();
      JsMessage asJsMessage = toJsMessage(key, translationKey.defaultValue());
      String id = getId(asJsMessage);
      MessageMapping messageMapping = new MessageMapping(id, key, translationKey.defaultValue());
      defaultMessageMapping.put(key, messageMapping);
    }

    System.out.println("TranslationGenerator " + element.getSimpleName());

    Map<String, Properties> bundles = processBundles(element);
    Map<String, List<MessageMapping>> propertiesMapping = processMapping(bundles);

    propertiesMapping.forEach(
        (k, v) -> {
          generateXTB(MoreElements.asType(element), k, v);
          // generateJS(k, v);
          v.forEach(
              values -> {
                System.out.println("       " + values.key + " " + values.value);
              });
        });
  }

  private JsMessage toJsMessage(String k, String msg) {
    String key = "MSG_" + k.toUpperCase(Locale.ROOT);
    try {
      JsMessage jsMessage = new JsMessage.Builder().setKey(key).setMsgText(msg).build();
      return jsMessage;
    } catch (JsMessage.PlaceholderFormatException e) {
      throw new Error(e);
    }
  }

  private String getId(JsMessage jsMessage) {
    return idGenerator.generateId(jsMessage.getKey(), jsMessage.getParts());
  }

  private void generateJS(String k, List<MessageMapping> v) {}

  private void generateXTB(TypeElement type, String locale, List<MessageMapping> mapping) {
    if (!locale.isEmpty()) {
      String source = new XTBGenerator(locale, mapping).generate();

      writeResource(
          type.getSimpleName() + "_" + locale + ".xtb",
          MoreElements.getPackage(type).toString(),
          source);
    }
  }

  private Map<String, Properties> processBundles(Element element) {
    String pkg = MoreElements.getPackage(element).getQualifiedName().toString();

    try (ScanResult scanResult = new ClassGraph().enableFieldInfo().acceptPackages(pkg).scan()) {
      return scanResult.getAllResources().stream()
          .filter(
              file -> {
                String candidate = new File(file.getPath()).getName();
                return candidate.startsWith(element.getSimpleName().toString())
                    && candidate.endsWith(".properties");
              })
          .collect(
              Collectors.toMap(
                  file -> {
                    String filename = new File(file.getPath()).getName();
                    String locale =
                        filename
                            .replaceFirst(element.getSimpleName().toString(), "")
                            .replace(".properties", "");
                    if (locale.startsWith("_")) {
                      locale = locale.replaceFirst("_", "");
                    }
                    return locale;
                  },
                  file -> {
                    Properties prop = new Properties();
                    try {
                      prop.load(file.getURL().openStream());
                      return prop;
                    } catch (IOException e) {
                      throw new Error(e);
                    }
                  }));
    }
  }

  private Map<String, List<MessageMapping>> processMapping(Map<String, Properties> bundles) {
    Map<String, List<MessageMapping>> mapping = new HashMap<>();

    for (Map.Entry<String, Properties> bundle : bundles.entrySet()) {
      String locale = bundle.getKey();
      List<MessageMapping> current = new ArrayList<>();
      mapping.put(locale, current);

      for (Map.Entry<String, MessageMapping> entry : defaultMessageMapping.entrySet()) {
        MessageMapping messageMapping = entry.getValue();
        current.add(messageMapping);
        if (bundle.getValue().containsKey(entry.getKey())) {
          messageMapping.value = bundle.getValue().getProperty(entry.getKey());
        }
      }
    }
    return mapping;
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

  private class XTBGenerator {
    private String locale;
    private List<MessageMapping> messages;

    private XTBGenerator(String locale, List<MessageMapping> messages) {
      this.locale = locale;
      this.messages = messages;
    }

    private String generate() {
      StringBuffer source = new StringBuffer();
      source.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      source.append(System.lineSeparator());
      source.append("<!DOCTYPE translationbundle SYSTEM \"translationbundle.dtd\">");
      source.append(System.lineSeparator());
      source.append(String.format("<translationbundle lang=\"%s\">", locale));
      source.append(System.lineSeparator());

      System.out.println("MessageMapping " + messages.size());

      for (MessageMapping message : messages) {
        String key = "MSG_" + message.key.toUpperCase(Locale.ROOT);
        String id = defaultMessageMapping.get(message.key).id;
        source.append(
            String.format(
                "<translation id=\"%s\" key=\"%s\">%s</translation>", id, key, message.value));
        source.append(System.lineSeparator());
      }

      source.append("</translationbundle>");
      source.append(System.lineSeparator());
      return source.toString();
    }
  }
}
