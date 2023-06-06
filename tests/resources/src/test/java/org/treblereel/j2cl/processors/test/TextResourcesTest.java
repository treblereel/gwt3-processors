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

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ResourcesTest {

  @Test
  public void testSmallTxt() {
    String content = readFileAsString("small.txt");
    assertEquals(content, TextTestResourceImpl.INSTANCE.getSmall().getText());
  }

  @Test
  public void testEscape() {
    String content = readFileAsString("escape.txt");
    assertEquals(content, TextTestResourceImpl.INSTANCE.escape().getText());
  }

  @Test
  public void testBigTxt() {
    String content = readFileAsString("bigtextresource.txt");
    assertEquals(content, TextTestResourceImpl.INSTANCE.getBig().getText());
  }

  @Test
  public void testFromResourceFolder() {
    String content = readFileAsString("test.js");
    assertEquals(content, TextTestResourceImpl.INSTANCE.getFromResourceFolder().getText());
  }

  private String readFileAsString(String fileName) {
    String file = this.getClass().getResource(fileName).getPath();
    try {
      return new String(Files.readAllBytes(Paths.get(file)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}