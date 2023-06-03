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
package org.treblereel.j2cl.processors.generator.resources.ext;

import org.treblereel.gwt.resources.context.AptContext;
import org.treblereel.gwt.resources.rg.resource.impl.ResourceOracleImpl;
import org.treblereel.gwt.resources.rg.util.DiskCache;
import org.treblereel.gwt.resources.rg.util.GeneratedUnit;
import org.treblereel.gwt.resources.rg.util.Util;

import javax.annotation.processing.FilerException;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;

/** @author Dmitrii Tikhomirov Created by treblereel 11/12/18 */
public class StandardGeneratorContext implements GeneratorContext {
  private static DiskCache diskCache = DiskCache.INSTANCE;
  private final AptContext aptContext;
  private final Map<String, PendingResource> pendingResources = new HashMap<>();
  private final Map<PrintWriter, Generated> uncommittedGeneratedCupsByPrintWriter =
      new IdentityHashMap<>();
  private final Map<String, GeneratedUnit> committedGeneratedCups = new HashMap<>();
  private final Set<String> newlyGeneratedTypeNames = new HashSet<>();
  private final ResourceOracle resourceOracle;

  public StandardGeneratorContext(AptContext aptContext) {
    this.aptContext = aptContext;
    this.resourceOracle = new ResourceOracleImpl(aptContext);
  }

  @Override
  public boolean checkRebindRuleAvailable(String sourceTypeName) {
    return false;
  }

  @Override
  public void commit(TreeLogger logger, PrintWriter pw) {
    Generated gcup = uncommittedGeneratedCupsByPrintWriter.get(pw);
    if (gcup == null) {
      logger.log(TreeLogger.WARN, "Generator attempted to commit an unknown PrintWriter", null);
      return;
    }
    gcup.commit(logger);
    uncommittedGeneratedCupsByPrintWriter.remove(pw);
    committedGeneratedCups.put(gcup.getTypeName(), gcup);
  }

  @Override
  public void commitResource(TreeLogger logger, OutputStream os) throws UnableToCompleteException {
    PendingResource pendingResource = null;
    String partialPath = null;
    if (os instanceof PendingResource) {
      pendingResource = (PendingResource) os;
      partialPath = pendingResource.getPartialPath();
      // Make sure it's ours by looking it up in the map.
      if (pendingResource != pendingResources.get(partialPath)) {
        pendingResource = null;
      }
    }
    if (pendingResource == null) {
      logger.log(TreeLogger.WARN, "Generator attempted to commit an unknown OutputStream", null);
      throw new UnableToCompleteException();
    }
  }

  @Override
  public ResourceOracle getResourcesOracle() {
    return resourceOracle;
  }

  @Override
  public AptContext getAptContext() {
    return aptContext;
  }

  @Override
  public PrintWriter tryCreate(TreeLogger logger, String packageName, String simpleTypeName)
      throws UnableToCompleteException {
    String typeName;
    if (packageName.length() == 0) {
      typeName = simpleTypeName;
    } else {
      typeName = packageName + '.' + simpleTypeName;
    }

    if (newlyGeneratedTypeNames.contains(typeName)) {
      return null;
    }

    // The type isn't there, so we can let the caller create it. Remember that
    // it is pending so another attempt to create the same type will fail.
    Generated gcup;
    StringWriter sw = new StringWriter();
    PrintWriter pw;
    try {
      JavaFileObject builderFile =
          aptContext.filer.createSourceFile(packageName + "." + simpleTypeName);
      pw =
          new PrintWriter(builderFile.openWriter(), true) {
            /**
             * Overridden to force unix-style line endings for consistent behavior across platforms.
             */
            @Override
            public void println() {
              super.print('\n');
              super.flush();
            }
          };
    } catch (FilerException filerException) {
      pw = null;
    } catch (IOException e) {
      logger.log(TreeLogger.Type.ERROR, "Unable to create a Class : " + e.getMessage());
      throw new UnableToCompleteException();
    }

    gcup = new GeneratedUnitImpl(sw, typeName);
    uncommittedGeneratedCupsByPrintWriter.put(pw, gcup);
    return pw;
  }

  @Override
  public OutputStream tryCreateResource(TreeLogger logger, String partialPath)
      throws UnableToCompleteException {
    logger =
        logger.branch(
            TreeLogger.DEBUG, "Preparing pending output resource '" + partialPath + "'", null);

    // Disallow null or empty names.
    if (partialPath == null || partialPath.trim().equals("")) {
      logger.log(TreeLogger.ERROR, "The resource name must be a non-empty string", null);
      throw new UnableToCompleteException();
    }

    // Disallow absolute paths.
    if (new File(partialPath).isAbsolute()) {
      logger.log(
          TreeLogger.ERROR,
          "Resource paths are intended to be relative to the compiled output directory and cannot be absolute",
          null);
      throw new UnableToCompleteException();
    }

    // Disallow backslashes (to promote consistency in calling code).
    if (partialPath.indexOf('\\') >= 0) {
      logger.log(
          TreeLogger.ERROR,
          "Resource paths must contain forward slashes (not backslashes) to denote subdirectories",
          null);
      throw new UnableToCompleteException();
    }

    // See if the file is pending.
    if (pendingResources.containsKey(partialPath)) {
      // It is already pending.
      logger.log(
          TreeLogger.DEBUG, "The file '" + partialPath + "' is already a pending resource", null);
      return null;
    }
    PendingResource pendingResource = new PendingResource(partialPath);
    pendingResources.put(partialPath, pendingResource);
    return pendingResource;
  }

  /** Extras added to {@link GeneratedUnit}. */
  private interface Generated extends GeneratedUnit {
    void abort();

    void commit(TreeLogger logger);
  }

  /**
   * This generated unit acts as a normal generated unit as well as a buffer into which generators
   * can write their source. A controller should ensure that source isn't requested until the
   * generator has finished writing it. This version is backed by {@link
   * StandardGeneratorContext#diskCache}.
   */
  public static class GeneratedUnitImpl implements Generated {

    private final String typeName;
    /** A token to retrieve this object's bytes from the disk cache. */
    protected long sourceToken = -1;

    private String strongHash; // cache so that refreshes work correctly
    private StringWriter sw;

    public GeneratedUnitImpl(StringWriter sw, String typeName) {
      this.typeName = typeName;
      this.sw = sw;
    }

    @Override
    public void abort() {
      sw = null;
    }

    /** Finalizes the source and adds this generated unit to the host. */
    @Override
    public void commit(TreeLogger logger) {
      String source = sw.toString();
      strongHash = Util.computeStrongName(Util.getBytes(source));
      sourceToken = diskCache.writeString(source);
      sw = null;
    }

    @Override
    public String getSource() {
      if (sw != null) {
        throw new IllegalStateException("source not committed");
      }
      return diskCache.readString(sourceToken);
    }

    @Override
    public String getSourceMapPath() {
      return "gen/" + getTypeName().replace('.', '/') + ".java";
    }

    @Override
    public String getTypeName() {
      return typeName;
    }

    @Override
    public long getSourceToken() {
      if (sw != null) {
        throw new IllegalStateException("source not committed");
      }
      return sourceToken;
    }

    @Override
    public String getStrongHash() {
      return strongHash;
    }

    @Override
    public String optionalFileLocation() {
      return null;
    }
  }

  /** Manages a resource that is in the process of being created by a generator. */
  private static class PendingResource extends OutputStream {

    private final String partialPath;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public PendingResource(String partialPath) {
      this.partialPath = partialPath;
    }

    public void abort() {
      baos = null;
    }

    public String getPartialPath() {
      return partialPath;
    }

    public byte[] takeBytes() {
      byte[] result = baos.toByteArray();
      baos = null;
      return result;
    }

    @Override
    public void write(byte[] b) throws IOException {
      if (baos == null) {
        throw new IOException("stream closed");
      }
      baos.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      if (baos == null) {
        throw new IOException("stream closed");
      }
      baos.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
      if (baos == null) {
        throw new IOException("stream closed");
      }
      baos.write(b);
    }
  }
}
