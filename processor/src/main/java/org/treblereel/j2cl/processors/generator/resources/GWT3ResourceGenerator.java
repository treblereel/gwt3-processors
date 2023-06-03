package org.treblereel.j2cl.processors.generator.resources;

import org.treblereel.j2cl.processors.annotations.GWT3Resource;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.generator.AbstractGenerator;

import javax.lang.model.element.Element;
import java.util.Set;

public class GWT3ResourceGenerator extends AbstractGenerator {
    public GWT3ResourceGenerator(AptContext context) {
        super(context, GWT3Resource.class);
    }

    @Override
    public void generate(Set<Element> elements) {

    }
}
