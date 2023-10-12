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

import java.util.Objects;
import javax.tools.FileObject;
import org.treblereel.j2cl.processors.common.resources.ClientBundle;

class MavenArtifact {

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String repositoryId;
  private final String repositoryType;
  private final String repositoryUrl;

  private MavenArtifactDownloader mavenArtifactDownloader;

  MavenArtifact(ClientBundle.MavenArtifactSource mavenArtifactSource) {
    this.groupId = mavenArtifactSource.group();
    this.artifactId = mavenArtifactSource.artifact();
    this.version = mavenArtifactSource.version();
    this.repositoryId = mavenArtifactSource.repositoryId();
    this.repositoryType = mavenArtifactSource.repositoryType();
    this.repositoryUrl = mavenArtifactSource.repositoryUrl();
  }

  void setMavenArtifactDownloader(MavenArtifactDownloader mavenArtifactDownloader) {
    this.mavenArtifactDownloader = mavenArtifactDownloader;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenArtifact that = (MavenArtifact) o;
    return Objects.equals(groupId, that.groupId)
        && Objects.equals(artifactId, that.artifactId)
        && Objects.equals(version, that.version)
        && Objects.equals(repositoryId, that.repositoryId)
        && Objects.equals(repositoryType, that.repositoryType)
        && Objects.equals(repositoryUrl, that.repositoryUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, version, repositoryId, repositoryType, repositoryUrl);
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public String getRepositoryId() {
    return repositoryId;
  }

  public String getRepositoryType() {
    return repositoryType;
  }

  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  void copyResourceTo(String path, FileObject dst) {
    mavenArtifactDownloader.copyResourceTo(path, dst);
  }

  @Override
  public String toString() {
    return "MavenArtifact{"
        + "groupId='"
        + groupId
        + '\''
        + ", artifactId='"
        + artifactId
        + '\''
        + ", version='"
        + version
        + '\''
        + ", repositoryId='"
        + repositoryId
        + '\''
        + ", repositoryType='"
        + repositoryType
        + '\''
        + ", repositoryUrl='"
        + repositoryUrl
        + '}';
  }
}
