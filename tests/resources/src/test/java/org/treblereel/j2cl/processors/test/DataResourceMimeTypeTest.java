/*
 * Copyright 2010 Google Inc.
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
package org.treblereel.j2cl.processors.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treblereel.j2cl.processors.annotations.GWT3Resource;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.common.resources.DataResource;
import org.treblereel.j2cl.processors.common.resources.DataResource.MimeType;

/** Tests for {@link MimeType @MimeType} resource annotations. */
public class DataResourceMimeTypeTest {

  private static final DataResourceMimeTypeTest_DataResourceMimeTypeImpl impl =
      DataResourceMimeTypeTest_DataResourceMimeTypeImpl.INSTANCE;

  @GWT3Resource
  interface DataResourceMimeType extends ClientBundle {

    /**
     * This is a binary file containing four 0x00 bytes, which is small enough to be embeddable, and
     * contains insufficient information for a determination of a recognizable MIME Type.
     */
    String FOUR_ZEROS_SOURCE = "fourZeros.dat";

    /** A simple MIME Type as per RFC 1521. */
    String MIME_TYPE_AUDIO_OGG = "audio/ogg";

    /** MIME Type with a single codecs specification as per RFC 4281. */
    String MIME_TYPE_WITH_CODECS = "audio/3gpp; codecs=samr";

    /** MIME Type with a multiple codecs specification as per RFC 4281. */
    String MIME_TYPE_WITH_QUOTED_CODECS_LIST = "video/3gpp; codecs=\"s263, samr\"";

    // Purposely missing a @MimeType annotation
    @Source(FOUR_ZEROS_SOURCE)
    DataResource resourceMimeTypeNoAnnotation();

    @MimeType(MIME_TYPE_AUDIO_OGG)
    @Source(FOUR_ZEROS_SOURCE)
    DataResource resourceMimeTypeAnnotationAudioOgg();

    @MimeType(MIME_TYPE_WITH_CODECS)
    @Source(FOUR_ZEROS_SOURCE)
    DataResource resourceMimeTypeAnnotationWithCodecs();

    @MimeType(MIME_TYPE_WITH_QUOTED_CODECS_LIST)
    @Source(FOUR_ZEROS_SOURCE)
    DataResource resourceMimeTypeAnnotationWithQuotedCodecsList();

    @Source("largeLossy.jpg")
    DataResource largeLossy();
  }

  @Test
  public void testMimeTypeAnnotationMissingDefaultsToContentUnknown() {
    String url = impl.resourceMimeTypeNoAnnotation().asString();
    assertEquals("data:content/unknown;base64,AAAAAA==", url);
  }

  @Test
  public void testMimeTypeAnnotationOverridesDefaultMimeType() {
    String url = impl.resourceMimeTypeAnnotationAudioOgg().asString();
    assertEquals("data:audio/ogg;base64,AAAAAA==", url);
  }

  @Test
  public void testMimeTypeAnnotationWithCodecs() {
    String url = impl.resourceMimeTypeAnnotationWithCodecs().asString();
    assertEquals("data:audio/3gpp; codecs=samr;base64,AAAAAA==", url);
  }

  @Test
  public void testMimeTypeAnnotationWithQuotedCodecsList() {
    String url = impl.resourceMimeTypeAnnotationWithQuotedCodecsList().asString();
    assertEquals("data:video/3gpp; codecs=\"s263, samr\";base64,AAAAAA==", url);
  }
}
