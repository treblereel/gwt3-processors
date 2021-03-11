package org.treblereel.j2cl.processors.generator;

import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public abstract class AbstractGenerator {

    protected final AptContext context;

    public AbstractGenerator(AptContext context) {
        this.context = context;
    }

    public abstract void generate(Element element);

    protected void writeResource(String filename, String path, String content) {
        try {
            FileObject file =
                    context.getProcessingEnv().getFiler().createResource(StandardLocation.SOURCE_OUTPUT, path, filename);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(file.openOutputStream(), "UTF-8"));
            pw.print(content);
            pw.close();
        } catch (IOException e) {
            context.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write file: " + e);
            throw new GenerationException("Failed to write file: " + e, e);
        }
    }

}
