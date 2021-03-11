package org.treblereel.j2cl.processors.context;

import org.treblereel.j2cl.processors.exception.GenerationException;
import org.treblereel.j2cl.processors.generator.AbstractGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AptContext {

    private final RoundEnvironment roundEnv;
    private final ProcessingEnvironment processingEnv;
    private final Map<String, List<AbstractGenerator>> generators = new HashMap<>();

    public AptContext(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        this.roundEnv = roundEnv;
        this.processingEnv = processingEnv;
    }

    public void register(String annotation, AbstractGenerator generator) {
        if (!generators.containsKey(annotation)) {
            generators.put(annotation, new ArrayList<>());
        }
        generators.get(annotation).add(generator);
    }

    public RoundEnvironment getRoundEnv() {
        return roundEnv;
    }

    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    public boolean isAnnotationSupported(String annotation) {
        return generators.containsKey(annotation);
    }

    public List<AbstractGenerator> getRegistredGeneratorsByAnnotation(String annotation) {
        return generators.get(annotation);
    }
}