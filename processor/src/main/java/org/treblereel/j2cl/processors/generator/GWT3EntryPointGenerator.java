package org.treblereel.j2cl.processors.generator;

import com.google.auto.common.MoreElements;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class GWT3EntryPointGenerator extends AbstractGenerator {


    public GWT3EntryPointGenerator(AptContext context) {
        super(context);
        context.register(GWT3EntryPoint.class.getSimpleName(), this);
    }

    @Override
    public void generate(Element element) {
        generate(checkMethod(element));
    }

    private ExecutableElement checkMethod(Element target) {
        if (!target.getKind().equals(ElementKind.METHOD)) {
            throw new GenerationException("Only method can be annotated with " + GWT3EntryPoint.class.getCanonicalName());
        }
        ExecutableElement methodInfo = (ExecutableElement) target;
        if (!methodInfo.getParameters().isEmpty()) {
            throw new GenerationException("Method, annotated " + GWT3EntryPoint.class.getCanonicalName() + " , must have no params");
        }

        if (methodInfo.getModifiers().contains(javax.lang.model.element.Modifier.STATIC)) {
            throw new GenerationException("Method, annotated with " + GWT3EntryPoint.class.getCanonicalName() + ", must not be static");
        }

        if (!methodInfo.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC)) {
            throw new GenerationException("Method, annotated with " + GWT3EntryPoint.class.getCanonicalName() + ", must be public");
        }

        if (methodInfo.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT)) {
            throw new GenerationException("Method, annotated with " + GWT3EntryPoint.class.getCanonicalName() + ", must not be abstract");
        }

        if (methodInfo.getModifiers().contains(javax.lang.model.element.Modifier.NATIVE)) {
            throw new GenerationException("Method, annotated with " + GWT3EntryPoint.class.getCanonicalName() + ", must not be native");
        }
        Element clazz = methodInfo.getEnclosingElement();

        if (clazz.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT)) {
            throw new GenerationException("Class with method, annotated with " + GWT3EntryPoint.class.getCanonicalName() + ", must not be abstract");
        }

        if (!clazz.getModifiers().contains(Modifier.PUBLIC)) {
            throw new GenerationException("Class with method, annotated with " + GWT3EntryPoint.class.getCanonicalName() + ", must be public");
        }
        return methodInfo;
    }

    private void generate(ExecutableElement methodInfo) {
        String methodName = methodInfo.getSimpleName().toString();
        TypeElement clazz = (TypeElement) methodInfo.getEnclosingElement();
        String className = clazz.getSimpleName().toString();
        String classPkg = MoreElements.getPackage(clazz).getQualifiedName().toString();

        String source = generateNativeJsSource(methodName, className);

        writeResource(className + ".native.js", classPkg, source);
    }

    private String generateNativeJsSource(String methodName, String className) {
        StringBuffer source = new StringBuffer();
        source.append("setTimeout(function(){");
        source.append(System.lineSeparator());
        source.append("var ep = ");
        source.append(className);
        source.append(".$create__();");
        source.append(System.lineSeparator());
        source.append("    ep.m_");
        source.append(methodName);
        source.append("__()");
        source.append(System.lineSeparator());
        source.append("}, 0);");
        source.append(System.lineSeparator());
        return source.toString();
    }

}
