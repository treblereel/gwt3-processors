package org.treblereel.j2cl.translation;

import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;

public class App {

    static MyTranslationBundle bundle = new org.treblereel.j2cl.translation.MyTranslationBundleImpl();

    @GWT3EntryPoint
    public void init() {

    }
}
