package org.treblereel.j2cl.translation;

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@J2clTestInput(TranslationTest.class)
public class TranslationTest {
    MyAnotherTranslationBundle bundle2 = new MyAnotherTranslationBundleImpl();


    @Test
    public void test() {
        MyTranslationBundle bundle = new MyTranslationBundleImpl();

        assertEquals("Je suppose que quelque chose s'est passé", bundle.somethingHappened());
        assertEquals("le salut", bundle.hello());
        assertEquals("Au revoir Billy !", bundle2.bye("Billy"));
        assertEquals("Billy and Billy", bundle2.dups("Billy"));
        assertEquals("<br>", bundle2.br("<br>"));

        assertEquals("<div>HELLo</div>", bundle2.divContent("<div>HELLo</div>"));
        assertEquals("<div>HELLo</div>", bundle2.divContent2("<div>HELLo</div>"));

    }

    @Test
    public void htmlContent() {
        assertEquals("<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\"> * Messages must be initialized in the form:</span></div>",
                bundle2.htmlContent(" * Messages must be initialized in the form:"));
    }

    @Test
    public void htmlContentHtmlTrue() {
        assertEquals("&lt;div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\">&lt;span class=\"pl-c\"> * Messages must be initialized in the form:&lt;/span>&lt;/div>",
                bundle2.htmlContentHtmlTrue(" * Messages must be initialized in the form:"));
    }

    @Test
    public void htmlContentUnescapeHtmlEntitiesTrue() {
        assertEquals("<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\"> * Messages must be initialized in the form:</span></div>",
                bundle2.htmlContentUnescapeHtmlEntitiesTrue(" * Messages must be initialized in the form:"));
    }

    @Test
    public void htmlContentHtmlTrueUnescapeHtmlEntitiesTrue() {
        assertEquals("<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\"> * Messages must be initialized in the form:</span></div>",
                bundle2.htmlContentHtmlTrueUnescapeHtmlEntitiesTrue(" * Messages must be initialized in the form:"));
    }
}
