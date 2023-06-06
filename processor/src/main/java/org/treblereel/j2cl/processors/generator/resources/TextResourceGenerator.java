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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.common.resources.TextResource;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

class TextResourceGenerator extends AbstractResourceGenerator {

    private Template template;

    TextResourceGenerator(AptContext context) {
        super(
                context,
                TextResource.class,
                TextResource.class.getAnnotation(ResourcePrototype.DefaultExtensions.class));
    }

    @Override
    void initializer(Map<String, Object> root, TypeElement clientBundle, ExecutableElement method) {
        String resource = lookupResource(method);
        String encoded = write(resource, method, s -> "return " + s);
        Map<String, Object> definition = new HashMap<>();
        definition.put("name", method.getSimpleName().toString());
        definition.put("impl", encoded);

        StringOutputStream os = new StringOutputStream();
        try (Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            if (template == null) {
                template = cfg.getTemplate("textresource.ftlh");
            }
            template.process(definition, out);
            root.put("initializer", os.toString());
        } catch (TemplateException | IOException e) {
            throw new GenerationException(e);
        }
    }

    private String lookupResource(ExecutableElement method) {
        URL resource = getResource(method, defaultExtensions.value());
        return readURLAsString(resource, method);
    }
}