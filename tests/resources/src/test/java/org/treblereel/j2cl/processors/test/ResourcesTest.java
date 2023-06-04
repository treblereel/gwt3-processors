/*
 * Copyright © 2023 treblereel
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

package org.treblereel.j2cl.processors.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.treblereel.j2cl.processors.annotations.GWT3Resource;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.common.resources.TextResource;

public class ResourcesTest {

  @GWT3Resource
  interface TextTestResource extends ClientBundle {

    @Source("small.txt")
    TextResource getSmall();

    @Source("bigtextresource.txt")
    TextResource getBig();

    TextResource getNoSource();
  }

  @Test
  public void test() {
    assertTrue(false);
  }
}
