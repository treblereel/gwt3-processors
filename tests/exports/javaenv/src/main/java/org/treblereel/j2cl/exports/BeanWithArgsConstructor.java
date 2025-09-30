package org.treblereel.j2cl.exports;


import jsinterop.annotations.JsType;
import org.treblereel.j2cl.processors.annotations.GWT3Export;

@GWT3Export
@JsType
public final class BeanWithArgsConstructor<W> implements ViewConnector<W> {

    private final String id;

    public BeanWithArgsConstructor(W definition, String id) {
        this.id = id;
    }

    public void translate(double x, double y) {
        System.out.println("Translating to " + x + ", " + y);
    }

    public String getId() {
        return id;
    }

    @Override
    public void connect(W widget) {

    }

    @Override
    public void disconnect(W widget) {

    }
}
