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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.util.Collections;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.tools.FileObject;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.treblereel.j2cl.processors.exception.GenerationException;

class MavenArtifactDownloader {

  private final File tempDir;
  private final MavenArtifact mavenArtifact;

  private Artifact artifact;

  MavenArtifactDownloader(File tempDir, MavenArtifact mavenArtifact) {
    this.tempDir = tempDir;
    this.mavenArtifact = mavenArtifact;
  }

  void copyResourceTo(String path, FileObject dst, boolean unzip) {
    if (artifact == null) {
      download();
    }
    try (ZipFile zipFile = new ZipFile(artifact.getFile())) {
      ZipEntry entry = zipFile.getEntry(path);
      if (entry != null) {
        if (unzip && !checkIfZip(zipFile.getInputStream(entry))) {
          throw new GenerationException("Resource with a path " + path + " isn't a GZIP");
        }
        try (BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        unzip
                            ? new GZIPInputStream(zipFile.getInputStream(entry))
                            : zipFile.getInputStream(entry)));
            BufferedWriter bufferedWriter =
                new BufferedWriter(new OutputStreamWriter(dst.openOutputStream()))) {

          char[] buffer = new char[1024];
          int charsRead;
          while ((charsRead = reader.read(buffer)) != -1) {
            bufferedWriter.write(buffer, 0, charsRead);
          }
          bufferedWriter.flush();
        }
      } else {
        throw new GenerationException("Unable to find resource " + path + " at " + artifact);
      }
    } catch (Exception e) {
      throw new GenerationException(e);
    }
  }

  private boolean checkIfZip(InputStream inputStream) {
    if (!(inputStream instanceof PushbackInputStream)) {
      inputStream = new PushbackInputStream(inputStream, 2);
    }

    byte[] signature = new byte[2];
    try {
      int bytesRead = inputStream.read(signature);
      ((PushbackInputStream) inputStream).unread(signature, 0, bytesRead);
      return bytesRead == 2 && signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b;
    } catch (IOException e) {
      throw new GenerationException(e);
    }
  }

  private void download() {
    RepositorySystem system = newRepositorySystem();
    RepositorySystemSession session = newSession(system);
    Artifact artifact =
        new DefaultArtifact(
            String.format(
                "%s:%s:%s",
                mavenArtifact.getGroupId(),
                mavenArtifact.getArtifactId(),
                mavenArtifact.getVersion()));

    ArtifactRequest request = new ArtifactRequest();
    request.setArtifact(artifact);
    request.setRepositories(
        Collections.singletonList(
            new RemoteRepository.Builder(
                    mavenArtifact.getRepositoryId(),
                    mavenArtifact.getRepositoryType(),
                    mavenArtifact.getRepositoryUrl())
                .build()));

    try {
      ArtifactResult artifactResult = system.resolveArtifact(session, request);
      this.artifact = artifactResult.getArtifact();
    } catch (ArtifactResolutionException e) {
      throw new GenerationException(e);
    } finally {
      system.shutdown();
    }
  }

  private RepositorySystem newRepositorySystem() {
    RepositorySystemSupplier supplier = new RepositorySystemSupplier();
    return supplier.get();
  }

  private RepositorySystemSession newSession(RepositorySystem system) {
    DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
    LocalRepository localRepo = new LocalRepository(tempDir);
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
    session.setTransferListener(new ConsoleTransferListener());
    session.setRepositoryListener(new ConsoleRepositoryListener());
    return session;
  }

  private static class ConsoleTransferListener extends AbstractTransferListener {}

  private static class ConsoleRepositoryListener extends AbstractRepositoryListener {}
}
