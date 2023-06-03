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
package org.treblereel.j2cl.processors.generator.resources.rg.resource.impl;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import org.treblereel.gwt.resources.api.client.ClientBundle;
import org.treblereel.gwt.resources.api.ext.DefaultExtensions;
import org.treblereel.gwt.resources.context.AptContext;
import org.treblereel.gwt.resources.ext.ResourceGeneratorUtil;
import org.treblereel.gwt.resources.ext.ResourceOracle;
import org.treblereel.gwt.resources.ext.TreeLogger;
import org.treblereel.gwt.resources.ext.UnableToCompleteException;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author Dmitrii Tikhomirov <chani.liet@gmail.com> Created by treblereel on 10/8/18.
 *
 *
 * @implNote if resouce is null, check :
 *
 * <code>
 *
 *   <build>
 *     <resources>
 *       <resource>
 *         <directory>src/main/java</directory>
 *         <includes>
 *           <include>&#42;&#42;/&#42;.java</include>
 *         </includes>
 *       </resource>
 *       <resource>
 *         <directory>src/main/resources</directory>
 *       </resource>
 *     </resources>
 *
 * <code/>
 *
 * */
public class ResourceOracleImpl implements ResourceOracle {
  private final AptContext aptContext;

  public ResourceOracleImpl(AptContext context) {
    this.aptContext = context;
  }

  private URL[] getResourcesByExtensions(
      ExecutableElement method, String[] extensions, String locale)
      throws UnableToCompleteException {
    String[] paths = new String[extensions.length];
    for (int i = 0; i < extensions.length; i++) {
      StringBuffer sb = new StringBuffer();
      sb.append(method.getSimpleName().toString()).append(extensions[i]);
      paths[i] = sb.toString();
    }
    return findResources(
        MoreElements.getPackage(method).getQualifiedName().toString(), paths, locale);
  }

  @Override
  public URL[] findResources(CharSequence packageName, CharSequence[] pathName, String locale) {
    List<URL> result = new ArrayList<>();
    for (int i = 0; i < pathName.length; i++) {
      URL resource = findResource(packageName, pathName[i], locale);
      if (resource != null) {
        result.add(resource);
      } else {
        resource = findResource(pathName[i], locale);
        if (resource != null) {
          result.add(resource);
        }
      }
    }
    if (result.size() > 0) {
      return result.toArray(new URL[result.size()]);
    }
    return null;
  }

  @Override
  public URL findResource(CharSequence path, String locale) {
    String packageName = "";
    String relativeName = path.toString();

    int index = relativeName.lastIndexOf('/');
    if (index >= 0) {
      packageName = relativeName.substring(0, index).replace('/', '.');
      relativeName = relativeName.substring(index + 1);
    }

    return findResource(packageName, relativeName, locale);
  }

  @Override
  public URL[] findResources(TreeLogger logger, ExecutableElement method, String locale)
      throws UnableToCompleteException {
    TypeElement returnType = (TypeElement) MoreTypes.asElement(method.getReturnType());
    assert returnType.getKind().isInterface() || returnType.getKind().isClass();
    DefaultExtensions annotation =
        ResourceGeneratorUtil.findDefaultExtensionsInClassHierarcy(returnType);
    String[] extensions;
    if (annotation != null) {
      extensions = annotation.value();
    } else {
      extensions = new String[0];
    }
    return findResources(logger, method, extensions, locale);
  }

  @Override
  public URL[] findResources(
      TreeLogger logger, ExecutableElement method, String[] defaultSuffixes, String locale)
      throws UnableToCompleteException {
    boolean error = false;
    ClientBundle.Source resourceAnnotation = method.getAnnotation(ClientBundle.Source.class);
    URL[] toReturn = null;

    if (resourceAnnotation == null) {
      if (defaultSuffixes != null) {

        for (String extension : defaultSuffixes) {
          if (logger.isLoggable(TreeLogger.SPAM)) {
            logger.log(TreeLogger.SPAM, "Trying default extension " + extension);
          }
          String url =
              (MoreElements.getPackage(method) + "." + method.getSimpleName()).replace('.', '/')
                  + extension;
          URL resourceUrl = findResource(url, locale);

          // Take the first match
          if (resourceUrl != null) {
            return new URL[] {resourceUrl};
          }
        }
      }
      logger.log(
          TreeLogger.ERROR,
          "No "
              + ClientBundle.Source.class.getName()
              + " annotation and no resources found with default extensions");
      error = true;
    } else {
      // The user has put an @Source annotation on the accessor method
      String[] resources = resourceAnnotation.value();
      toReturn =
          findResources(
              MoreElements.getPackage(method.getEnclosingElement()).getQualifiedName().toString(),
              resources,
              locale);
      if (toReturn == null) {
        error = true;
        logger.log(
            TreeLogger.ERROR,
            "Resource for "
                + method
                + " in "
                + method.getEnclosingElement()
                + " not found. Is the name specified as ClassLoader.getResource()"
                + " would expect?");
      }
    }

    if (error) {
      throw new UnableToCompleteException();
    }

    return toReturn;
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
  @Override
  public URL findResource(CharSequence pkg, CharSequence relativeName, String locale) {
    URL resource = null;
    String pathTo = pkg.length() > 0 ? String.valueOf(pkg).replaceAll("\\.", "/") + "/" : "";

    if (locale != null) {
      // Convert language_country_variant to independent pieces
      String[] localeSegments = locale.split("_");
      int lastDot = relativeName.toString().lastIndexOf(".");
      String prefix =
          lastDot == -1 ? relativeName.toString() : relativeName.toString().substring(0, lastDot);
      String extension = lastDot == -1 ? "" : relativeName.toString().substring(lastDot);

      for (int i = localeSegments.length - 1; i >= -1; i--) {
        String localeInsert = "";
        for (int j = 0; j <= i; j++) {
          localeInsert += "_" + localeSegments[j];
        }

        String qualifiedName = prefix + localeInsert + extension;
        resource = getUrlClassLoader(pathTo + qualifiedName);
        if (resource != null) {
          return resource;
        }

        resource =
            doFindResource(
                Arrays.asList(
                    StandardLocation.SOURCE_PATH,
                    StandardLocation.SOURCE_OUTPUT,
                    StandardLocation.CLASS_PATH,
                    StandardLocation.CLASS_OUTPUT,
                    StandardLocation.ANNOTATION_PROCESSOR_PATH),
                pkg,
                qualifiedName);
        if (resource != null) {
          return resource;
        }
      }
    } else {
      resource = getUrlClassLoader(pathTo + relativeName);
      if (resource != null) {
        return resource;
      }
      resource =
          doFindResource(
              Arrays.asList(
                  StandardLocation.SOURCE_PATH,
                  StandardLocation.SOURCE_OUTPUT,
                  StandardLocation.CLASS_PATH,
                  StandardLocation.CLASS_OUTPUT,
                  StandardLocation.ANNOTATION_PROCESSOR_PATH),
              pkg,
              relativeName);
    }

    return resource;
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
      FileObject fileObject = aptContext.filer.getResource(location, "", relativeName);
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
