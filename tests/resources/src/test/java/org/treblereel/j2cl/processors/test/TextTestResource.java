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

import org.treblereel.j2cl.processors.annotations.GWT3Resource;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.common.resources.TextResource;

@GWT3Resource
interface TextTestResource extends ClientBundle {

  @Source("small.txt")
  TextResource getSmall();

  @Source("bigtextresource.txt")
  TextResource getBig();

  @Source("test.js")
  TextResource getFromResourceFolder();

  @Source("/io/qwerty/test.txt")
  TextResource getFQDNPath();

  TextResource escape();

  TextResource getNoSource();

  @MavenArtifactSource(
      group = "org.uberfire",
      artifact = "uberfire-workbench-client-views-patternfly",
      version = "7.74.1.Final",
      path = "org/uberfire/client/views/static/css/patternfly.css")
  TextResource externalResource();

  @MavenArtifactSource(
      group = "org.uberfire",
      artifact = "uberfire-workbench-client-views-patternfly",
      version = "7.74.1.Final",
      path = "org/uberfire/client/views/static/css/patternfly.css",
      copyTo = "org/qwerty/css/test.css")
  TextResource externalResourceRename();

  @MavenArtifactSource(
      group = "org.webjars.npm",
      artifact = "jquery",
      version = "3.7.1",
      path = "META-INF/resources/webjars/jquery/3.7.1/src/css/support.js",
      copyTo = "resources/webjars/jquery/src/css/support.js")
  TextResource externalResourceWebJar();

  @MavenArtifactSource(
      group = "org.webjars.npm",
      artifact = "jquery",
      version = "3.7.1",
      path = "META-INF/resources/webjars/jquery/3.7.1/src/css/support.js",
      copyTo = "org/test/support.js.back")
  TextResource externalResourceWebJarRename();

  @MavenArtifactSource(
      group = "org.webjars.npm",
      artifact = "jquery",
      version = "3.7.1",
      path = "META-INF/resources/webjars/jquery/3.7.1/src/css/support.js",
      copyTo = "org/test/support-old.js.back")
  TextResource externalResourceWebJarRenameDashInFile();

  @MavenArtifactSource(
      group = "org.webjars",
      artifact = "bootstrap",
      version = "3.4.1",
      path = "META-INF/resources/webjars/bootstrap/3.4.1/js/bootstrap.min.js.gz",
      copyTo = "org/test/bootstrap.min.js.back",
      unzip = true)
  TextResource externalResourceWebJarGZIP();
}
