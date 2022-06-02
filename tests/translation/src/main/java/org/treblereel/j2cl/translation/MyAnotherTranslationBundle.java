package org.treblereel.j2cl.translation;

import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;

@TranslationBundle
public interface MyAnotherTranslationBundle {

    @TranslationKey(defaultValue = "Good bye")
    String bye();
}
