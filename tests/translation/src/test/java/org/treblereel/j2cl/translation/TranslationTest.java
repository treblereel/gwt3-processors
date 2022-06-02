package org.treblereel.j2cl.translation;

import com.google.j2cl.junit.apt.J2clTestInput;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@J2clTestInput(TranslationTest.class)
public class TranslationTest {


    @Test
    public void test() {
        MyTranslationBundle bundle = new MyTranslationBundleImpl();
        MyAnotherTranslationBundle bundle2 = new MyAnotherTranslationBundleImpl();


        assertEquals("Je suppose que quelque chose s'est passé", bundle.somethingHappened());
        assertEquals("le salut", bundle.hello());
        assertEquals("Au revoir Billy !", bundle2.bye("Billy"));
        assertEquals("Billy and Billy", bundle2.dups("Billy"));
    }
}
