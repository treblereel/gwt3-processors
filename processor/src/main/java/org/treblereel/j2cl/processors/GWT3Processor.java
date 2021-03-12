package org.treblereel.j2cl.processors;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;

import com.google.auto.service.AutoService;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.generator.ES6ModuleShimGenerator;
import org.treblereel.j2cl.processors.generator.GWT3EntryPointGenerator;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"org.treblereel.j2cl.processors.annotations.GWT3EntryPoint", "org.treblereel.j2cl.processors.annotations.ES6Module"})
public class GWT3Processor extends AbstractProcessor {

    private AptContext context;

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {
        if (elements.isEmpty()) {
            return false;
        }

        context = new AptContext(roundEnv, processingEnv);

        new GWT3EntryPointGenerator(context);
        new ES6ModuleShimGenerator(context);


        for (TypeElement element : elements) {
            if (context.isAnnotationSupported(element.getSimpleName().toString())) {
                context.getRegistredGeneratorsByAnnotation(element.getSimpleName().toString())
                        .forEach(generator -> roundEnv.getElementsAnnotatedWith(element)
                                .forEach(generator::generate));
            }
        }
        return false;
    }

}
