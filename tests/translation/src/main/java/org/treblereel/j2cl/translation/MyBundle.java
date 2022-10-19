package org.treblereel.j2cl.translation;

import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;

@TranslationBundle
public interface MyBundle {

    @TranslationKey(defaultValue = "<br/>{$var_1}<div id=\"this\">inner text {$var_2}</div>", unescapeHtmlEntities = true)
    String hello(String var_1, String var_2);

    @TranslationKey(defaultValue = "@TranslationKey {$arg} !")
    String test(String arg);

    @TranslationKey(defaultValue = "{$arg}")
    String test2(String arg);

    @TranslationKey(defaultValue = "{$arg}&amp;{$arg_1}", unescapeHtmlEntities = true)
    String test7(String arg, String arg_1);

    @TranslationKey(defaultValue = "<div id=\"WOW\">3{$arg}-!!!!!!!!!!!!!!!!!</div>", unescapeHtmlEntities = true)
    String test11(String arg);

    @TranslationKey(defaultValue = "<br/>{$arg}<div id=\"this\">TranslationKey{$arg_1}</div>", unescapeHtmlEntities = true)
    String test6(String arg,String arg_1);


    @TranslationKey(defaultValue = "<div id=\"this\">!@#$%^*(((</div>{$arg}&amp;{$arg_1}@#$%^&amp;*(<div>{$arg_2}</div>", unescapeHtmlEntities = true)
    String test9(String arg, String arg_1, String arg_2);


    @TranslationKey(defaultValue = "{$arg}TranslationKey{$arg_1}TranslationKey{$arg_2}")
    String test5(String arg, String arg_1, String arg_2);

    @TranslationKey(defaultValue = "{$arg}{$arg_1}")
    String test3(String arg, String arg_1);

    @TranslationKey(defaultValue = "<div id=\"this\">!@#$%^*(((</div>{$arg}&amp;{$arg_1}", unescapeHtmlEntities = true)
    String test8(String arg, String arg_1);

    @TranslationKey(defaultValue = "&amp;&lt;div id=\"{$arg}\"&gt;@TranslationKey&lt;/div&gt;", unescapeHtmlEntities = true)
    String test16(String arg);

    @TranslationKey(defaultValue = "7128890306670950348232162507662243061846645994402114603180927379544880168678174568758504050459062584")
    String test15();


    @TranslationKey(defaultValue = "Tests run: {$arg}, Failures: {$arg_1}, Errors: {$arg_2}, Skipped: {$arg_3}, Time elapsed: {$arg_4} sec")
    String test4(String arg, String arg_1, String arg_2, String arg_3, String arg_4);


    @TranslationKey(defaultValue = "<div id=\"someId\">some content<br /><a href=\"#someRef\">{$arg}</a>,</div>", unescapeHtmlEntities = true)
    String test10(String arg);


    @TranslationKey(defaultValue = "<div id=\"div1\"><div id=\"div2\"><div id=\"div3\">RRRRRRR{$arg}></div><div id=\"div4\"></div></div></div>", unescapeHtmlEntities = true)
    String test14(String arg);

    @TranslationKey(defaultValue = "<div id=\"div1\"><div id=\"div2\"><div id=\"div3\"></div><div id=\"div4\"></div></div></div>", unescapeHtmlEntities = true)
    String test12();


    @TranslationKey(defaultValue = "<div>TranslationKey</div>", unescapeHtmlEntities = true)
    String test13();


    @TranslationKey(defaultValue = "<br/>{$var_1}<div id=\"this\">inner text {$var_2}</div>")
    String test17(String var_1, String var_2);

    @TranslationKey(defaultValue = "<div id=\"_this\">!@#$%^*(((</div>{$arg}&amp;{$arg_1}")
    String test18(String arg, String arg_1);

    @TranslationKey(defaultValue = "<div id=\"_this\">!@#$%^*(((</div>{$arg}&amp;{$arg_1}@#$%^&amp;*(<div>{$arg_2}</div>")
    String test19(String arg, String arg_1,String arg_2);

    @TranslationKey(defaultValue = "<div id=\"_someId\">some content<br /><a href=\"#someRef\">{$arg}</a>,</div>")
    String test20(String arg);

    @TranslationKey(defaultValue = "<div id=\"div_id\">3{$arg}-!!!!!!!!!!!!!!!!!</div>")
    String test21(String arg);

    @TranslationKey(defaultValue = "<div id=\"div21\"><div id=\"div22\"><div id=\"div23\"></div><div id=\"div14\"></div></div></div>")
    String test22();

    @TranslationKey(defaultValue = "<div>inner content</div>")
    String test23();

    @TranslationKey(defaultValue = "<div id=\"div21\"><div id=\"div22\"><div id=\"div23\">RRRRRRR{$arg}></div><div id=\"div4\"></div></div></div>")
    String test24(String arg);
}
