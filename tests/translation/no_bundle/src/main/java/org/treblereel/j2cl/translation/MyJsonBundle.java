package org.treblereel.j2cl.translation;

import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;

@TranslationBundle
public interface MyJsonBundle {

  @TranslationKey(defaultValue = "Hello from JSON!")
  String jsonGreeting();

  @TranslationKey(defaultValue = "Hello {$name}!")
  String jsonHello(String name);
}
