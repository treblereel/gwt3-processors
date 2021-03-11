package org.treblereel.j2cl.shim;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"jsinterop.annotations.JsType", "org.treblereel.j2cl.processors.annotations.ES6Module"})
public class GWT3Processor extends AbstractProcessor {

    private final String ES6MODULE = "ES6Module";
    private final String CLOSURE_JS = ".closure.js";
    private final String SHIM_JS = ".shim.js";
    private int counter = 0;

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {
        if (elements.isEmpty()) {
            return false;
        }

        roundEnv.getElementsAnnotatedWith(JsType.class)
                .stream()
                .filter(e -> e.getAnnotation(JsType.class).isNative())
                .filter(elm -> elm.getAnnotationMirrors().stream().filter(e -> e.getAnnotationType().asElement().getSimpleName().toString().equals(ES6MODULE)).count() == 1)
                .forEach(module -> {
                    process(module, module.getAnnotation(JsType.class));
                    counter++;
                });

        System.out.println("Total processed files " + counter);
        return false;
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

    protected void writeResource(String filename, String path, String content) {
        try {
            FileObject file =
                    processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, path, filename);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(file.openOutputStream(), "UTF-8"));
            pw.print(content);
            pw.close();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write file: " + e);
            throw new RuntimeException(e);
        }
    }
}
