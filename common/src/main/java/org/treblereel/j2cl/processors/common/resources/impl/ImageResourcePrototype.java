/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.treblereel.j2cl.processors.common.resources.impl;

import static elemental2.dom.DomGlobal.document;

import elemental2.dom.Image;
import org.treblereel.j2cl.processors.common.resources.ImageResource;

/**
 * This is part of an implementation of the ImageBundle optimization implemented with ClientBundle.
 */
public class ImageResourcePrototype implements ImageResource {

  private final String name;
  private final String url;
  private final int width;
  private final int height;

  /** Only called by generated code. */
  public ImageResourcePrototype(String name, String url, int width, int height) {
    this.name = name;
    this.height = height;
    this.width = width;
    this.url = url;
  }

  /** Exists for testing purposes, not part of the ImageResource interface. */
  public int getHeight() {
    return height;
  }

  /** Exists for testing purposes, not part of the ImageResource interface. */
  public int getWidth() {
    return width;
  }

  /** Returns the Image */
  @Override
  public Image getImage() {
    Image image = (Image) document.createElement("img");
    image.src = url;
    image.name = name;
    image.width = width;
    image.height = height;
    return image;
  }

  public String getName() {
    return name;
  }

  public String getURL() {
    return url;
  }
}
