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
package org.treblereel.j2cl.processors.resource.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.FilerException;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.resource.ResourceOracle;

public class ResourceOracleImpl implements ResourceOracle {
  private final AptContext aptContext;

  private final List<Location> locations =
      Arrays.asList(
          StandardLocation.SOURCE_PATH,
          StandardLocation.SOURCE_OUTPUT,
          StandardLocation.CLASS_PATH,
          StandardLocation.CLASS_OUTPUT,
          StandardLocation.ANNOTATION_PROCESSOR_PATH);

  public ResourceOracleImpl(AptContext context) {
    this.aptContext = context;
  }

  @Override
  public URL findResource(CharSequence path) {
    String packageName = "";
    String relativeName = path.toString();

    int index = relativeName.lastIndexOf('/');
    if (index >= 0) {
      packageName = relativeName.substring(0, index).replace('/', '.');
      relativeName = relativeName.substring(index + 1);
    }

    return findResource(packageName, relativeName);
  }

  /**
   * Locates a resource by searching multiple locations.
   *
   * <p>Searches in the order of
   *
   * <ul>
   *   <li>{@link StandardLocation#SOURCE_PATH}
   *   <li>{@link StandardLocation#CLASS_PATH}
   *   <li>{@link StandardLocation#CLASS_OUTPUT}
   * </ul>
   *
   * @return FileObject or null if file is not found.
   */
  private URL findResource(CharSequence pkg, CharSequence relativeName) {
    URL resource;
    String pathTo = pkg.length() > 0 ? String.valueOf(pkg).replaceAll("\\.", "/") + "/" : "";

    resource = getUrlClassLoader(pathTo + relativeName);
    if (resource != null) {
      return resource;
    }
    return doFindResource(locations, pkg, relativeName);
  }

  /**
   * Locates a resource by searching multiple locations.
   *
   * @return FileObject or null if file is not found in given locations.
   */
  private URL doFindResource(
      List<Location> searchLocations, CharSequence pkg, CharSequence relativeName) {
    if (searchLocations == null || searchLocations.isEmpty()) {
      return null;
    }
    for (Location location : searchLocations) {
      String path = "";
      if (pkg.length() > 0) {
        path = String.valueOf(pkg).replace('.', '/') + '/';
      }
      URL candidate = findResource(location, path + relativeName);
      if (candidate != null) {
        return candidate;
      }
    }
    // unable to locate, return null.
    return null;
  }

  private URL findResource(Location location, String relativeName) {
    try {
      FileObject fileObject =
          aptContext.getProcessingEnv().getFiler().getResource(location, "", relativeName);
      if (new File(fileObject.getName()).exists()) {
        return fileObject.toUri().toURL();
      }
    } catch (FilerException ignored) {
      File openedfile =
          new File(ignored.getMessage().replace("Attempt to reopen a file for path ", ""));
      if (openedfile.exists()) {
        try {
          return openedfile.toURI().toURL();
        } catch (MalformedURLException e) {
          // ignored
        }
      }
      // ignored
    } catch (IOException ignored) {
      // ignored
    }
    return null;
  }

  private URL getUrlClassLoader(String path) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(path);
    if (resource != null) {
      return resource;
    }
    return null;
  }
}
