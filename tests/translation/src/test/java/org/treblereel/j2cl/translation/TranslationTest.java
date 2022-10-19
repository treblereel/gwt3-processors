package org.treblereel.j2cl.translation;

import com.google.j2cl.junit.apt.J2clTestInput;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@J2clTestInput(TranslationTest.class)
public class TranslationTest {
    MyAnotherTranslationBundle bundle2 = new MyAnotherTranslationBundleImpl();
    MyTranslationBundle bundle = new MyTranslationBundleImpl();

    @Test
    public void somethingHappened() {
        if (System.getProperty("goog.LOCALE").equals("fr")) {
            assertEquals("Je suppose que quelque chose s'est passé", bundle.somethingHappened());
        }else if(System.getProperty("goog.LOCALE").equals("fr-nr")){
            assertEquals("NR : Je suppose que quelque chose s'est passé", bundle.somethingHappened());
        } else {
            assertEquals("I guess something happened!", bundle.somethingHappened());
        }
    }

    @Test
    public void hello() {
        if (System.getProperty("goog.LOCALE").equals("fr")) {
            assertEquals("le salut", bundle.hello());
        } else if(System.getProperty("goog.LOCALE").equals("fr-nr")) {
            assertEquals("NR : le salut", bundle.hello());
        } else if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
            assertEquals("Hello", bundle.hello());
        } else {
            assertEquals("greeting", bundle.hello());
        }
    }

    @Test
    public void defaultValue() {
        assertEquals("defaultValue", bundle.defaultValue());
    }

    @Test
    public void bye() {
        if (System.getProperty("goog.LOCALE").startsWith("fr")) {
            assertEquals("Au revoir Billy !", bundle2.bye("Billy"));
        } else {
            assertEquals("Good bye Billy !", bundle2.bye("Billy"));
        }
    }

    @Test
    public void dups() {
        assertEquals("Billy and Billy", bundle2.dups("Billy"));
    }


    @Test
    public void br() {
        assertEquals("<br>", bundle2.br("<br>"));
    }

    @Test
    public void divContent() {
        assertEquals("<div>HELLo</div>", bundle2.divContent("<div>HELLo</div>"));
    }

    @Test
    public void divContent2() {
        assertEquals("<div>HELLo</div>", bundle2.divContent2("<div>HELLo</div>"));
    }

    @Test
    public void htmlContent() {
        if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
            // YEAH, j2cl-m-p force escaping of html entities
            assertEquals("<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\"><div>HELLo</div></span></div>",
                    bundle2.htmlContent("<div>HELLo</div>"));
        } else {
            assertEquals("&lt;div class=&quot;blob-code blob-code-inner js-file-line&quot; id=&quot;LC1813&quot;&gt;&lt;span class=&quot;pl-c&quot;&gt;<div>HELLo</div>&lt;/span&gt;&lt;/div&gt;",
                    bundle2.htmlContent("<div>HELLo</div>"));
        }
    }

    @Test
    public void htmlContentHtmlTrue() {
        if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
            assertEquals("&lt;div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\">&lt;span class=\"pl-c\"><div>HELLo</div>&lt;/span>&lt;/div>",
                    bundle2.htmlContentHtmlTrue("<div>HELLo</div>"));
        } else {
            assertEquals("&lt;div class=&quot;blob-code blob-code-inner js-file-line&quot; id=&quot;LC1813&quot;&gt;&lt;span class=&quot;pl-c&quot;&gt;<div>HELLo</div>&lt;/span&gt;&lt;/div&gt;",
                    bundle2.htmlContentHtmlTrue("<div>HELLo</div>"));
        }
    }

    @Test
    public void htmlContentUnescapeHtmlEntitiesTrue() {
        if (System.getProperty("goog.LOCALE").startsWith("fr")) {
            assertEquals("<div class=\"blob-code blob-code-inner js-file-line\" id=\"LC1813\"><span class=\"pl-c\"> * Messages must be initialized in the form:</span></div>",
                    bundle2.htmlContentUnescapeHtmlEntitiesTrue(" * Messages must be initialized in the form:"));
        } else if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
            assertEquals("<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\">br_rf</span></div>",
                    bundle2.htmlContentUnescapeHtmlEntitiesTrue("br_rf"));
        } else {
            assertEquals("<div class=\"blob-code blob-code-inner js-file-line\" id=\"LC1813\"><span class=\"pl-c\"> * Messages must be initialized in the form:</span></div>",
                    bundle2.htmlContentUnescapeHtmlEntitiesTrue(" * Messages must be initialized in the form:"));
        }
    }

    @Test
    public void htmlContentHtmlTrueUnescapeHtmlEntitiesTrue() {
        if (System.getProperty("goog.LOCALE").startsWith("br_rf")) {
            assertEquals("<div id=\"LC1813\" class=\"blob-code blob-code-inner js-file-line\"><span class=\"pl-c\">br_rf</span></div>",
                    bundle2.htmlContentHtmlTrueUnescapeHtmlEntitiesTrue("br_rf"));
        } else {
            assertEquals("<div class=\"blob-code blob-code-inner js-file-line\" id=\"LC1813\"><span class=\"pl-c\"> * Messages must be initialized in the form:</span></div>",
                    bundle2.htmlContentHtmlTrueUnescapeHtmlEntitiesTrue(" * Messages must be initialized in the form:"));
        }
    }
}
