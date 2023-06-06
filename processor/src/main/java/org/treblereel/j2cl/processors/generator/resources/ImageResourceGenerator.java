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

import static org.treblereel.j2cl.processors.common.resources.ImageResource.ImageOptions;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.treblereel.j2cl.processors.common.resources.ImageResource;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

class ImageResourceGenerator extends AbstractResourceGenerator {

  private Template template;

  ImageResourceGenerator(AptContext context) {
    super(
        context,
        ImageResource.class,
        ImageResource.class.getAnnotation(ResourcePrototype.DefaultExtensions.class));
  }

  @Override
  void initializer(Map<String, Object> root, TypeElement clientBundle, ExecutableElement method) {
    Map<String, Object> definition = new HashMap<>();
    definition.put("name", method.getSimpleName().toString());

    URL resource = getResource(method, defaultExtensions.value());
    Path imagePath = Paths.get(resource.getPath());
    try {
      String mimeType = String.format("data:%s;base64,", Files.probeContentType(imagePath));
      setSize(method, imagePath, definition);

      byte[] data = readURLAsBytes(resource);
      String base64Contents = mimeType + toBase64(data);
      String encoded = write(base64Contents, method, s -> "String encoded =  " + s);
      definition.put("encoded", encoded);
      StringOutputStream os = new StringOutputStream();
      try (Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
        if (template == null) {
          template = cfg.getTemplate("imageresource.ftlh");
        }
        template.process(definition, out);
        root.put("initializer", os.toString());
      } catch (TemplateException | IOException e) {
        throw new GenerationException(e);
      }
    } catch (IOException e) {
      throw new GenerationException("Unable to determine mime type for " + imagePath, e);
    }
  }

  private static void setSize(
      ExecutableElement method, Path imagePath, Map<String, Object> definition) throws IOException {
    BufferedImage image = ImageIO.read(imagePath.toFile());
    int width = image.getWidth();
    int height = image.getHeight();
    ImageOptions imageOptions = method.getAnnotation(ImageOptions.class);
    if (imageOptions != null) {
      if (imageOptions.width() != -1) {
        width = imageOptions.width();
      }
      if (imageOptions.height() != -1) {
        height = imageOptions.height();
      }
    }
    definition.put("width", String.valueOf(width));
    definition.put("height", String.valueOf(height));
  }
}
