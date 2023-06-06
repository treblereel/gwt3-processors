/*
 * Copyright © 2019 The GWT Project Authors
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

import static org.treblereel.j2cl.processors.common.resources.ImageResource.*;

import org.treblereel.j2cl.processors.annotations.GWT3Resource;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.common.resources.ImageResource;

public class ImageResourceTest {

  @GWT3Resource
  interface ImageResources extends ClientBundle {
    @Source("animated.gif")
    ImageResource animated();

    /**
     * This image shouldn't be re-encoded as a PNG or it will dramatically increase in size,
     * although it's still small enough to be encoded as a data URL as-is.
     */
    ImageResource complexLossy();

    @Source("16x16.png")
    ImageResource i16x16();

    @Source("32x32.png")
    ImageResource i32x32();

    @Source("64x64.png")
    ImageResource i64x64();

    @Source("64x64.png")
    ImageResource i64x64Dup();

    @Source("64x64-dup.png")
    ImageResource i64x64Dup2();

    // Test default filename lookup while we're at it
    ImageResource largeLossless();

    // Test default filename lookup while we're at it
    ImageResource largeLossy();

    @Source("64x64.png")
    @ImageOptions(width = 32)
    ImageResource scaledDown();

    @Source("64x64.png")
    @ImageOptions(width = 128)
    ImageResource scaledUp();

    @Source("logo.png")
    ImageResource linuxLogo();
  }
}
