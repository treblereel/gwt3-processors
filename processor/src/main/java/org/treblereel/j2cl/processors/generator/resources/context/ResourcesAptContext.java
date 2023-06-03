package org.treblereel.j2cl.processors.generator.resources.context;

import com.google.auto.common.MoreElements;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.common.resources.DataResource;
import org.treblereel.j2cl.processors.common.resources.ImageResource;
import org.treblereel.j2cl.processors.common.resources.TextResource;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.generator.resources.ext.ResourceGenerator;
import org.treblereel.j2cl.processors.generator.resources.rg.BundleResourceGenerator;
import org.treblereel.j2cl.processors.generator.resources.rg.DataResourceGenerator;
import org.treblereel.j2cl.processors.generator.resources.rg.ImageResourceGenerator;
import org.treblereel.j2cl.processors.generator.resources.rg.TextResourceGenerator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourcesAptContext {

    public final Messager messager;
    public final Filer filer;
    public final Elements elements;
    public final Types types;
    public final RoundEnvironment roundEnvironment;
    public final ProcessingEnvironment processingEnv;

    public final Map<Element, Class<? extends ResourceGenerator>> generators = new HashMap<>();

    public ResourcesAptContext(final AptContext context) {
        this.filer = context.getProcessingEnv().getFiler();
        this.messager = context.getProcessingEnv().getMessager();
        this.elements = context.getProcessingEnv().getElementUtils();
        this.types = context.getProcessingEnv().getTypeUtils();
        this.roundEnvironment = context.getRoundEnv();
        this.processingEnv = context.getProcessingEnv();
        initGenerators();
    }

    public Set<TypeElement> getClassesWithAnnotation(Class<? extends Annotation> annotation) {
        Set<TypeElement> rez =
                roundEnvironment
                        .getElementsAnnotatedWith(annotation)
                        .stream()
                        .map(element -> MoreElements.asType(element))
                        .collect(Collectors.toSet());
        return rez;
    }

    private void initGenerators() {
        preBuildGenerators();
    }

    private void preBuildGenerators() {
        generators.put(
                elements.getTypeElement(ClientBundle.class.getCanonicalName()),
                BundleResourceGenerator.class);
        generators.put(
                elements.getTypeElement(DataResource.class.getCanonicalName()),
                DataResourceGenerator.class);
        generators.put(
                elements.getTypeElement(ImageResource.class.getCanonicalName()),
                ImageResourceGenerator.class);
        generators.put(
                elements.getTypeElement(TextResource.class.getCanonicalName()),
                TextResourceGenerator.class);
    }
}
