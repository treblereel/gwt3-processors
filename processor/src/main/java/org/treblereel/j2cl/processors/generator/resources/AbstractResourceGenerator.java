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

import com.google.auto.common.MoreElements;
import com.google.common.io.BaseEncoding;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.apache.commons.text.StringEscapeUtils;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype.DefaultExtensions;
import org.treblereel.j2cl.processors.common.resources.TextResource;
import org.treblereel.j2cl.processors.common.resources.exception.ResourceException;
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

  protected static final int MAX_STRING_CHUNK = 16383;

  protected static final int MAX_ENCODED_SIZE = (2 << 15) - 1;
  protected static final int MAX_INLINE_SIZE = 2 << 15;

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
    definition.put("prototype", prototype.getCanonicalName());

    initializer(definition, clientBundle, method);

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

  protected String write(String toWrite, ExecutableElement method, Function<String, String> call) {
    if (toWrite.length() > MAX_STRING_CHUNK) {
      String encoded = writeLongString(toWrite);
      return call.apply(encoded + ";");
    } else {
      return call.apply("\"" + escape(toWrite) + "\";");
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
    builder.append("new StringBuilder()").append("\n");
    int offset = 0;
    int length = toWrite.length();
    while (offset < length - 1) {
      int subLength = Math.min(MAX_STRING_CHUNK, length - offset);
      builder.append("                                           ");
      builder.append(".append(\"");
      builder.append(escape(toWrite.substring(offset, offset + subLength)));
      builder.append("\")").append("\n");
      offset += subLength;
    }
    builder.append("                                           ");
    builder.append(".toString()").append("\n");
    return builder.toString();
  }

  protected URL getResource(ExecutableElement method, String[] extensions) {
    String pkg =
        MoreElements.getPackage(method).getQualifiedName().toString().replaceAll("\\.", "/");
    if (method.getAnnotation(ClientBundle.Source.class) != null) {
      ClientBundle.Source source = method.getAnnotation(ClientBundle.Source.class);
      for (int i = 0; i < source.value().length; i++) {
        String fullPath = pkg + "/" + source.value()[i];
        URL url = context.resourceOracle.findResource(fullPath);
        if (url != null) {
          return url;
        }
      }
    } else if (extensions != null) {
      for (int i = 0; i < defaultExtensions.value().length; i++) {
        String extension = defaultExtensions.value()[i];
        String fullPath = pkg + "/" + method.getSimpleName().toString() + extension;
        URL url = context.resourceOracle.findResource(fullPath);
        if (url != null) {
          return url;
        }
      }
    }
    throw new ResourceException(
        String.format(
            "Unable to find resource [%s] at %s.%s",
            TextResource.class.getSimpleName(),
            method.getEnclosingElement().toString(),
            method.getSimpleName().toString()));
  }

  /**
   * Escapes string content to be a valid string literal.
   *
   * @return an escaped version of <code>unescaped</code>, suitable for being enclosed in double
   *     quotes in Java source
   * @apiNote from gwt2 generator
   */
  public static String escape(String unescaped) {
    return StringEscapeUtils.escapeJava(unescaped);
  }

  protected static String toBase64(byte[] data) {
    return BaseEncoding.base64().encode(data).replaceAll("\\s+", "");
  }

  protected String readURLAsString(URL url, ExecutableElement method) {
    byte[] bytes = readURLAsBytes(url);
    if (bytes != null) {
      if (bytes.length < MAX_INLINE_SIZE) {
        return new String(bytes, StandardCharsets.UTF_8);
      }
      throw new GenerationException(
          "Resource is too large to inline: "
              + method.getEnclosingElement()
              + "."
              + method.getSimpleName()
              + "()");
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

  abstract void initializer(
      Map<String, Object> root, TypeElement clientBundle, ExecutableElement method);
}
