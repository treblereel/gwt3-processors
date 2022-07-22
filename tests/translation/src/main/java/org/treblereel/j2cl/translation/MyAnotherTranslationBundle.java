package org.treblereel.j2cl.translation;

import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;

@TranslationBundle
public interface MyAnotherTranslationBundle {

    @TranslationKey(defaultValue = "Good bye {$arg} !")
    String bye(String arg);

    @TranslationKey(defaultValue = "{$arg} and {$arg}")
    String dups(String arg);

    @TranslationKey(defaultValue = "{$br}", html = true)
    String br(String br);

    @TranslationKey(defaultValue = "{$arg}", html = true, unescapeHtmlEntities = true)
    String divContent(String arg);

    @TranslationKey(defaultValue = "{$arg}", unescapeHtmlEntities = true)
    String divContent2(String arg);

    @TranslationKey(defaultValue = "<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\">{$text}</span></div>")
    String htmlContent(String text);

    @TranslationKey(defaultValue = "<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\">{$text}</span></div>", html = true)
    String htmlContentHtmlTrue(String text);

    @TranslationKey(defaultValue = "<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\">{$text}</span></div>", unescapeHtmlEntities = true)
    String htmlContentUnescapeHtmlEntitiesTrue(String text);

    @TranslationKey(defaultValue = "<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\">{$text}</span></div>", html = true, unescapeHtmlEntities = true)
    String htmlContentHtmlTrueUnescapeHtmlEntitiesTrue(String text);

}
