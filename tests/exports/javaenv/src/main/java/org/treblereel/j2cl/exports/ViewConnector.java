package org.treblereel.j2cl.exports;

public interface ViewConnector<W> {

    void connect(W widget);

    void disconnect(W widget);
}
