/*
 * Copyright © 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.treblereel.j2cl.translation;

import org.treblereel.j2cl.processors.annotations.TranslationBundle;
import org.treblereel.j2cl.processors.annotations.TranslationKey;

@TranslationBundle
public interface MyTranslationBundle {

  @TranslationKey(defaultValue = "I guess something happened!", unescapeHtmlEntities = true)
  String somethingHappened();

  @TranslationKey(defaultValue = "Hello", key = "greeting")
  String hello();

  @TranslationKey(defaultValue = "defaultValue")
  String defaultValue();
}
