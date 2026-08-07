/*
 * Copyright © 2024
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

package org.treblereel.j2cl.processors.utils;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

class MangledNameTestProcessor extends AbstractProcessor {

  private final BiConsumer<ProcessingEnvironment, RoundEnvironment> testCallback;
  private boolean processed = false;

  MangledNameTestProcessor(BiConsumer<ProcessingEnvironment, RoundEnvironment> testCallback) {
    this.testCallback = testCallback;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!processed && !roundEnv.processingOver()) {
      processed = true;
      testCallback.accept(processingEnv, roundEnv);
    }
    return false;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton("*");
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
