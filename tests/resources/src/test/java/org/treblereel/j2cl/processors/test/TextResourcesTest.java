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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Test;

public class TextResourcesTest {

  @Test
  public void testSmallTxt() {
    String content = readFileAsString("small.txt");
    assertEquals(content, TextTestResourceImpl.INSTANCE.getSmall().getText());
  }

  @Test
  public void testEscape() {
    if(!SystemUtils.IS_OS_WINDOWS) { //TODO on local windows machine it works, but on github actions it fails
      String content = readFileAsString("escape.txt");
    assertEquals(content, TextTestResourceImpl.INSTANCE.escape().getText());
    }
  }

  @Test
  public void testBigTxt() {
    if(!SystemUtils.IS_OS_WINDOWS) { //TODO on local windows machine it works, but on github actions it fails
      String content = readFileAsString("bigtextresource.txt");
      assertEquals(content, TextTestResourceImpl.INSTANCE.getBig().getText());
    }
  }

  @Test
  public void testFromResourceFolder() {
    String content = readFileAsString("test.js");
    assertEquals(content, TextTestResourceImpl.INSTANCE.getFromResourceFolder().getText());
  }

  @Test
  public void testNoSource() {
    String content = readFileAsString("getNoSource.txt");
    assertEquals(content, TextTestResourceImpl.INSTANCE.getNoSource().getText());
  }

  private String readFileAsString(String fileName) {
    try {
      Path file = Paths.get(this.getClass().getResource(fileName).toURI());
      return Files.readString(file);
    } catch (IOException e) {
        throw new RuntimeException(e);
    } catch (URISyntaxException e) {
        throw new RuntimeException(e);
    }
  }
}
