/*
 * Copyright © 2025 Treblereel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.treblereel.j2cl.processors.common.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSPHash {

  Type value();

  Algorithm algorithm() default Algorithm.SHA_256;

  enum Type {
    SCRIPT,
    STYLE
  }

  enum Algorithm {
    SHA_256("sha256", "SHA-256"),
    SHA_384("sha384", "SHA-384"),
    SHA_512("sha512", "SHA-512");

    private final String prefix;
    private final String digestName;

    Algorithm(String prefix, String digestName) {
      this.prefix = prefix;
      this.digestName = digestName;
    }

    public String prefix() {
      return prefix;
    }

    public String digestName() {
      return digestName;
    }
  }
}
