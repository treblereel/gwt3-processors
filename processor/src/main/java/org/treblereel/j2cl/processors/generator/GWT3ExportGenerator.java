package org.treblereel.j2cl.processors.generator;

import com.google.auto.common.MoreElements;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.ES6Module;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;
import org.treblereel.j2cl.processors.annotations.GWT3Export;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;
import java.util.stream.Collectors;

public class GWT3ExportGenerator extends AbstractGenerator {

    public GWT3ExportGenerator(AptContext context) {
        super(context, GWT3Export.class);
    }

    @Override
    public void generate(Element element) {
        TypeElement parent = (TypeElement) element;

        check(parent);

        Set<Element> elements = parent.getEnclosedElements()
                .stream()
                .filter(elm -> elm.getKind().isField() || elm.getKind().equals(ElementKind.METHOD))
                .filter(elm -> elm.getAnnotation(JsMethod.class) != null || elm.getAnnotation(JsProperty.class) != null)
                .filter(elm -> elm.getModifiers().contains(Modifier.PUBLIC))
                .collect(Collectors.toSet());

        elements.forEach(this::check);

        generate(parent, elements);
    }

    private void generate(TypeElement parent, Set<Element> elements) {
        StringBuffer source = new StringBuffer();
        generateClassExport(parent, source);
        generateStaticFieldsOrMethods(parent, elements, source);

        String className = parent.getSimpleName().toString();
        String classPkg = MoreElements.getPackage(parent).getQualifiedName().toString();

        writeResource(className + ".native.js", classPkg, source.toString());
    }

    private void generateStaticFieldsOrMethods(TypeElement parent, Set<Element> elements, StringBuffer source) {
        elements.stream().filter(elm -> elm.getModifiers().contains(Modifier.STATIC)).forEach(element -> {
            if (element.getKind().isField()) {
                generateStaticField((VariableElement) element, parent, source);
            } else {
                generateStaticMethod((ExecutableElement) element, parent, source);
            }
        });
    }

    private void generateStaticField(VariableElement element, TypeElement parent, StringBuffer source) {
        source.append("goog.exportProperty(_");
        source.append(parent.getSimpleName());
        source.append(", '");
        source.append(element.getSimpleName());
        source.append("', ");
        source.append(parent.getSimpleName());
        source.append(".$static_");
        source.append(element.getSimpleName());
        source.append("__");
        source.append(parent.getQualifiedName().toString().replaceAll("\\.", "_"));
        source.append(");");
        source.append(System.lineSeparator());
    }

    private void generateStaticMethod(ExecutableElement element, TypeElement parent, StringBuffer source) {
        source.append("goog.exportSymbol('window.");
        source.append(parent.getQualifiedName());
        source.append(".");
        source.append(element.getSimpleName().toString());
        source.append("', ");
        source.append(parent.getSimpleName());
        source.append(".");
        source.append(element.getSimpleName().toString());
        source.append(");");
        source.append(System.lineSeparator());
    }

    private void generateClassExport(TypeElement parent, StringBuffer source) {
        source.append("const _");
        source.append(parent.getSimpleName());
        source.append(" = ");

        if (parent.getAnnotation(JsType.class) == null) {
            source.append(parent.getSimpleName());
            source.append(".$create__;");
        } else {
            source.append(parent.getQualifiedName().toString().replaceAll("\\.","_"));
        }

        source.append(System.lineSeparator());

        source.append("goog.exportSymbol('window.");
        source.append(parent.getQualifiedName());
        source.append("', _");
        source.append(parent.getSimpleName());
        source.append(");");
        source.append(System.lineSeparator());
    }

    private void check(Element element) {
        if (element.getKind().isField()) {
            check((VariableElement) element);
        } else {
            check((ExecutableElement) element);
        }

    }

    private void check(TypeElement parent) {
        if (!parent.getModifiers().contains(Modifier.PUBLIC)) {
            throw new GenerationException("Class,  that contains methods/fields annotated with " + GWT3Export.class.getCanonicalName() + ", must be public");
        }
        if (parent.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new GenerationException("Class,  that contains methods/fields annotated with " + GWT3Export.class.getCanonicalName() + ", mustn't be abstract");
        }
        if (parent.getAnnotation(JsType.class) != null && parent.getAnnotation(JsType.class).isNative()) {
            throw new GenerationException("If Class,  that contains methods/fields annotated with " + GWT3Export.class.getCanonicalName() + ", is @JsType, it mustn't be isNative=true");
        }
        if (parent.getAnnotation(GWT3EntryPoint.class) != null) {
            throw new GenerationException("If Class,  that contains methods/fields annotated with " + GWT3Export.class.getCanonicalName() + ", mustn't be annotated with @GWT3EntryPoint");
        }
        if (parent.getAnnotation(ES6Module.class) != null) {
            throw new GenerationException("If Class,  that contains methods/fields annotated with " + GWT3Export.class.getCanonicalName() + ", mustn't be annotated with @ES6Module");
        }

    }

    private void check(VariableElement field) {
        if (!field.getModifiers().contains(Modifier.PUBLIC)) {
            throw new GenerationException("Field,  annotated with " + GWT3Export.class.getCanonicalName() + ", must be public");
        }
    }

    private void check(ExecutableElement method) {
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
            throw new GenerationException("Method,  annotated with " + GWT3Export.class.getCanonicalName() + ", must be public");
        }
        if (method.getModifiers().contains(Modifier.NATIVE)) {
            throw new GenerationException("Method,  annotated with " + GWT3Export.class.getCanonicalName() + ", mustn't be native");
        }
    }

}