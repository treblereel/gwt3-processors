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

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.treblereel.j2cl.processors.common.resources.DataResource;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

class DataResourceGenerator extends AbstractResourceGenerator {

  DataResourceGenerator(AptContext context) {
    super(context, DataResource.class, null);
  }

  @Override
  String initializer(Map<String, Object> root, TypeElement clientBundle, ExecutableElement method) {
    root.put("getter", "asString");
    DataResource.MimeType mimeTypeAnnotation = method.getAnnotation(DataResource.MimeType.class);
    String mimeType = mimeTypeAnnotation != null ? mimeTypeAnnotation.value() : null;
    URL resource = getResource(method, null);
    byte[] data = readURLAsBytes(resource);
    try {
      String finalMimeType =
          (mimeType != null) ? mimeType : resource.openConnection().getContentType();
      String base64Contents = toBase64(data);
      StringBuilder encoded = new StringBuilder();
      encoded.append("\"data:");
      encoded.append(finalMimeType.replaceAll("\"", "\\\\\""));
      encoded.append(";base64,");
      encoded.append(base64Contents);
      encoded.append("\"");

      if (encoded.length() < MAX_STRING_CHUNK) {
        return "return " + encoded + ";";
      }
      StringBuilder builder = new StringBuilder();
      builder.append("StringBuilder builder = new StringBuilder();");
      builder.append("\n                ");
      builder.append("builder.append(\"");
      builder.append("data:");
      builder.append(finalMimeType.replaceAll("\"", "\\\\\""));
      builder.append(";base64,");
      builder.append("\");\n");
      int offset = 0;
      int length = base64Contents.length();
      while (offset < length - 1) {
        int subLength = Math.min(MAX_STRING_CHUNK, length - offset);
        builder.append("                ");
        builder.append("builder.append(\"");
        builder.append(escape(base64Contents.substring(offset, offset + subLength)));
        builder.append("\");").append("\n");
        offset += subLength;
      }
      builder.append("                ");
      builder.append("return builder.toString();").append("\n");
      return builder.toString();
    } catch (IOException e) {
      throw new GenerationException(e);
    }
  }
}
