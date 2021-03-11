package org.treblereel.j2cl.processors.generator;

import com.google.auto.common.MoreElements;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

import javax.lang.model.element.*;
import java.util.Map;
import java.util.Optional;

public class ES6ModuleShimGenerator extends AbstractGenerator {

    private final String CLOSURE_JS = ".closure.js";
    private final String SHIM_JS = ".shim.js";

    public ES6ModuleShimGenerator(AptContext context) {
        super(context);
        context.register(JsType.class.getSimpleName(), this);
    }

    @Override
    public void generate(Element element) {
        if (element.getAnnotationMirrors()
                .stream()
                .filter(e -> e.getAnnotationType().asElement()
                        .getSimpleName().toString().equals(ES6Module.class.getSimpleName())).count() != 1) {
            return;
        }

        JsType jsType = element.getAnnotation(JsType.class);
        if (!jsType.isNative()) {
            throw new GenerationException("@ES6Module class must be annotated with @JsType.isNative=true annotation");
        }

        process(element, jsType);

    }

    private void process(Element element, JsType jsType) {
        TypeElement typeElement = MoreElements.asType(element);
        String clazzName = jsType.name().equals("<auto>") ? typeElement.getSimpleName().toString() : jsType.name();
        String moduleFileName;
        Optional<String> isPathDefined = isPathDefined(element);
        if (isPathDefined.isPresent()) {
            moduleFileName = isPathDefined.get();
        } else {
            moduleFileName = clazzName + ".js";
        }

        generateClosure(typeElement, clazzName);
        generateShim(typeElement, clazzName, moduleFileName);
    }

    private Optional<String> isPathDefined(Element element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().asElement().getSimpleName().toString().equals("ES6Module")) {
                if (!annotationMirror.getElementValues().isEmpty()) {
                    Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry = annotationMirror.getElementValues()
                            .entrySet()
                            .iterator()
                            .next();
                    if (entry.getKey().toString().equals("value()")) {
                        String value = entry.getValue().getValue().toString();
                        if (!value.equals("<auto>")) {
                            return Optional.of(value);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void generateClosure(TypeElement typeElement, String clazzName) {
        String pkg = MoreElements.getPackage(typeElement).getQualifiedName().toString();

        StringBuffer source = new StringBuffer();
        source.append("goog.module('");
        source.append(pkg + "." + clazzName);
        source.append("');");
        source.append(System.lineSeparator());
        source.append("const shim = goog.require('");
        source.append(typeElement.getQualifiedName());
        source.append(".shim');");
        source.append(System.lineSeparator());

        source.append("exports = shim.");
        source.append(clazzName);
        source.append(";");
        source.append(System.lineSeparator());

        writeResource(typeElement.getSimpleName() + CLOSURE_JS, pkg, source.toString());
    }

    private void generateShim(TypeElement typeElement, String clazzName, String moduleFileName) {
        String pkg = MoreElements.getPackage(typeElement).getQualifiedName().toString();
        StringBuffer source = new StringBuffer();

        source.append("import {");
        source.append(clazzName);
        source.append("} from './");
        source.append(moduleFileName);
        source.append("';");
        source.append(System.lineSeparator());

        source.append("goog.declareModuleId('");
        source.append(typeElement.getQualifiedName());
        source.append(".shim');");
        source.append(System.lineSeparator());

        source.append("export {");
        source.append(clazzName);
        source.append("};");
        source.append(System.lineSeparator());

        writeResource(typeElement.getSimpleName() + SHIM_JS, pkg, source.toString());
    }
}
