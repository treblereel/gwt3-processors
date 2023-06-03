/*
 *
 * Copyright © ${year} ${name}
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
package org.treblereel.j2cl.processors.generator.resources.context;

import org.treblereel.gwt.resources.ext.GeneratorContext;
import org.treblereel.gwt.resources.ext.TreeLogger;
import org.treblereel.gwt.resources.ext.UnableToCompleteException;

import javax.lang.model.element.TypeElement;

/** @author Dmitrii Tikhomirov Created by treblereel 11/11/18 */
class InlineResourceContext extends AbstractResourceContext {
  /** String constants in Java have a maximum limit that we must obey. */
  public static final int MAX_ENCODED_SIZE = (2 << 15) - 1;

  InlineResourceContext(
      TreeLogger logger,
      GeneratorContext context,
      TypeElement resourceBundleType,
      ClientBundleContext clientBundleCtx) {
    super(logger, context, resourceBundleType, clientBundleCtx);
  }

  @Override
  public String deploy(
      String suggestedFileName, String mimeType, byte[] data, boolean forceExternal)
      throws UnableToCompleteException {
    TreeLogger logger = getLogger();

    // data: URLs are not compatible with XHRs on FF and Safari browsers
    if ((!forceExternal) && (data.length < MAX_INLINE_SIZE)) {
      logger.log(TreeLogger.DEBUG, "Inlining", null);

      String base64Contents = toBase64(data);

      // CHECKSTYLE_OFF
      String encoded =
          "\"data:" + mimeType.replaceAll("\"", "\\\\\"") + ";base64," + base64Contents + "\"";
      // CHECKSTYLE_ON

      /*
       * We know that the encoded format will be one byte per character, since
       * we're using only ASCII characters.
       */
      if (encoded.length() < MAX_ENCODED_SIZE) {
        return encoded;
      }
    }

    throw new Error("Max file " + suggestedFileName + " size exceeded " + MAX_ENCODED_SIZE);
  }

  @Override
  public boolean supportsDataUrls() {
    return true;
  }
}
