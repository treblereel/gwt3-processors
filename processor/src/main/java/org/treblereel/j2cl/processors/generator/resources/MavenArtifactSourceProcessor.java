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

package org.treblereel.j2cl.processors.generator.resources;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.exception.GenerationException;

class MavenArtifactSourceProcessor {

  private final AptContext context;

  MavenArtifactSourceProcessor(AptContext context) {
    this.context = context;
  }

  void process(Set<ExecutableElement> withMavenArtifact) {
    final File tempDir = Files.createTempDir();
    try {
      final Map<MavenArtifact, Set<ExecutableElement>> mavenArtifacts = new HashMap<>();
      processAnnotations(withMavenArtifact, mavenArtifacts);
      downloadMavenArtifacts(tempDir, mavenArtifacts);
      copyResources(mavenArtifacts);
    } catch (Exception e) {
      throw new GenerationException(e);
    } finally {
      delete(tempDir);
    }
  }

  private void processAnnotations(
      Set<ExecutableElement> withMavenArtifact,
      Map<MavenArtifact, Set<ExecutableElement>> mavenArtifacts) {
    for (ExecutableElement executableElement : withMavenArtifact) {
      ClientBundle.MavenArtifactSource mavenArtifactSource =
          executableElement.getAnnotation(ClientBundle.MavenArtifactSource.class);
      MavenArtifact mavenArtifact = new MavenArtifact(mavenArtifactSource);
      if (!mavenArtifacts.containsKey(mavenArtifact)) {
        mavenArtifacts.put(mavenArtifact, new HashSet<>());
      }
      mavenArtifacts.get(mavenArtifact).add(executableElement);
    }
  }

  private void downloadMavenArtifacts(
      File tempDir, Map<MavenArtifact, Set<ExecutableElement>> mavenArtifacts) {
    for (MavenArtifact mavenArtifact : mavenArtifacts.keySet()) {
      mavenArtifact.setMavenArtifactDownloader(new MavenArtifactDownloader(tempDir, mavenArtifact));
    }
  }

  private void copyResources(Map<MavenArtifact, Set<ExecutableElement>> mavenArtifacts) {
    for (Map.Entry<MavenArtifact, Set<ExecutableElement>> mavenArtifactSetEntry :
        mavenArtifacts.entrySet()) {
      for (ExecutableElement executableElement : mavenArtifactSetEntry.getValue()) {
        ClientBundle.MavenArtifactSource artifactSource =
            executableElement.getAnnotation(ClientBundle.MavenArtifactSource.class);
        String path = artifactSource.path();
        String copyTo = artifactSource.copyTo().equals("<auto>") ? path : artifactSource.copyTo();

        if (artifactSource.copyTo().equals("<auto>")) {
          checkPath(path, "path", executableElement);
        } else {
          checkPath(copyTo, "copyTo", executableElement);
        }
        try {
          FileObject resource =
              context
                  .getProcessingEnv()
                  .getFiler()
                  .createResource(
                      StandardLocation.CLASS_OUTPUT,
                      "", // no package
                      copyTo);
          mavenArtifactSetEntry.getKey().copyResourceTo(path, resource, artifactSource.unzip());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private static final Pattern pattern =
      Pattern.compile("^(?:[a-zA-Z]+(?:\\d*[a-zA-Z_]*)*)(?:\\.[a-zA-Z]+(?:\\d*[a-zA-Z_]*)*)+$");

  private void checkPath(String path, String type, ExecutableElement method) {
    String pkg = path.substring(0, path.lastIndexOf("/")).replaceAll("/", ".");

    if (!pattern.matcher(pkg).matches()) {
      throw new GenerationException(
          "Wrong " + type + " at " + method.getEnclosingElement() + "." + method.getSimpleName());
    }
  }

  private void delete(File dir) {
    Stack<File> stack = new Stack<>();
    stack.push(dir);
    while (!stack.isEmpty()) {
      File file = stack.peek();
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        if (files.length == 0) {
          stack.pop();
          file.delete();
        } else {
          for (File subFile : files) {
            stack.push(subFile);
          }
        }
      } else {
        stack.pop();
        file.delete();
      }
    }
  }
}
