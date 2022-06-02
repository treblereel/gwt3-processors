package org.treblereel.j2cl.translation;

import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;

@TranslationBundle
public interface MyAnotherTranslationBundle {

    @TranslationKey(defaultValue = "Good bye {$arg} !")
    String bye(String arg);

    @TranslationKey(defaultValue = "{$arg} and {$arg}")
    String dups(String arg);

    @TranslationKey(defaultValue = "<div>{$arg}</div", html = true)
    String simpleDiv(String content);

}
