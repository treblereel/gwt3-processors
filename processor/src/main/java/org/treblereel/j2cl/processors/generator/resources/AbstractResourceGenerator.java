/*
 * Copyright © 2023 treblereel
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

package org.treblereel.j2cl.processors.generator.resources;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype.DefaultExtensions;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

public abstract class AbstractResourceGenerator {

  protected final Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

  {
    cfg.setClassForTemplateLoading(this.getClass(), "/templates/resources");
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
  }

  private static final int MAX_STRING_CHUNK = 16383;

  private Template template;

  protected final AptContext context;
  private final Class<? extends ResourcePrototype> prototype;

  protected final DefaultExtensions defaultExtensions;

  AbstractResourceGenerator(
      AptContext context,
      Class<? extends ResourcePrototype> prototype,
      DefaultExtensions defaultExtensions) {
    this.context = context;
    this.prototype = prototype;
    this.defaultExtensions = defaultExtensions;
  }

  void generate(Map<String, Object> root, TypeElement clientBundle, ExecutableElement method) {
    ((List<String>) root.get("resources")).add(method.getSimpleName().toString());
    Map<String, Object> definition = new HashMap<>();
    definition.put("name", method.getSimpleName().toString());
    definition.put("initializer", initializer(definition, clientBundle, method));
    definition.put("prototype", prototype.getCanonicalName());

    StringOutputStream os = new StringOutputStream();
    try (Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
      if (template == null) {
        template = cfg.getTemplate("method.ftlh");
      }
      template.process(definition, out);
      ((List<String>) root.get("methods")).add(os.toString());
    } catch (TemplateException | IOException e) {
      throw new GenerationException(e);
    }
  }

  protected String write(String toWrite) {
    if (toWrite.length() > MAX_STRING_CHUNK) {
      return writeLongString(toWrite);
    } else {
      return "return \"" + escape(toWrite) + "\";";
    }
  }

  /**
   * A single constant that is too long will crash the compiler with an out of memory error. Break
   * up the constant and generate code that appends using a buffer.
   *
   * @apiNote from gwt2 generator
   */
  private String writeLongString(String toWrite) {
    StringBuilder builder = new StringBuilder();
    builder.append("StringBuilder builder = new StringBuilder();").append("\n");
    int offset = 0;
    int length = toWrite.length();
    while (offset < length - 1) {
      int subLength = Math.min(MAX_STRING_CHUNK, length - offset);
      builder.append("                ");
      builder.append("builder.append(\"");
      builder.append(escape(toWrite.substring(offset, offset + subLength)));
      builder.append("\");").append("\n");
      offset += subLength;
    }
    builder.append("                ");
    builder.append("return builder.toString();").append("\n");
    return builder.toString();
  }

  /**
   * Escapes string content to be a valid string literal.
   *
   * @return an escaped version of <code>unescaped</code>, suitable for being enclosed in double
   *     quotes in Java source
   * @apiNote from gwt2 generator
   */
  public static String escape(String unescaped) {
    int extra = 0;
    for (int in = 0, n = unescaped.length(); in < n; ++in) {
      switch (unescaped.charAt(in)) {
        case '\0':
        case '\n':
        case '\r':
        case '\"':
        case '\\':
          ++extra;
          break;
      }
    }

    if (extra == 0) {
      return unescaped;
    }

    char[] oldChars = unescaped.toCharArray();
    char[] newChars = new char[oldChars.length + extra];
    for (int in = 0, out = 0, n = oldChars.length; in < n; ++in, ++out) {
      char c = oldChars[in];
      switch (c) {
        case '\0':
          newChars[out++] = '\\';
          c = '0';
          break;
        case '\n':
          newChars[out++] = '\\';
          c = 'n';
          break;
        case '\r':
          newChars[out++] = '\\';
          c = 'r';
          break;
        case '\"':
          newChars[out++] = '\\';
          c = '"';
          break;
        case '\\':
          newChars[out++] = '\\';
          c = '\\';
          break;
      }
      newChars[out] = c;
    }

    return String.valueOf(newChars);
  }

  protected String readURLAsString(URL url) {
    byte[] bytes = readURLAsBytes(url);
    if (bytes != null) {
      return new String(bytes, StandardCharsets.UTF_8);
    }
    return null;
  }

  protected byte[] readURLAsBytes(URL url) {
    try {
      URLConnection conn = url.openConnection();
      conn.setUseCaches(false);
      return readURLConnectionAsBytes(conn);
    } catch (IOException e) {
      return null;
    }
  }

  protected byte[] readURLConnectionAsBytes(URLConnection connection) {
    try (InputStream input = connection.getInputStream()) {
      int contentLength = connection.getContentLength();
      if (contentLength < 0) {
        return null;
      }
      return readBytesFromInputStream(input, contentLength);
    } catch (IOException e) {
      return null;
    }
  }

  protected byte[] readBytesFromInputStream(InputStream input, int byteLength) {
    try {
      byte[] bytes = new byte[byteLength];
      int byteOffset = 0;
      while (byteOffset < byteLength) {
        int bytesReadCount = input.read(bytes, byteOffset, byteLength - byteOffset);
        if (bytesReadCount == -1) {
          return null;
        }
        byteOffset += bytesReadCount;
      }
      return bytes;
    } catch (IOException e) {
      // Ignored.
    }
    return null;
  }

  abstract String initializer(
      Map<String, Object> root, TypeElement clientBundle, ExecutableElement method);
}
