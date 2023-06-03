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
package org.treblereel.j2cl.processors.generator.resources.rg;

import org.treblereel.j2cl.processors.common.resources.utils.UriUtils;
import org.treblereel.j2cl.processors.generator.resources.ext.*;
import org.treblereel.j2cl.processors.generator.resources.rg.util.SourceWriter;
import org.treblereel.j2cl.processors.generator.resources.rg.util.StringSourceWriter;
import org.treblereel.j2cl.processors.generator.resources.rg.util.Util;
import org.treblereel.j2cl.processors.generator.resources.rg.util.tools.Utility;
import org.treblereel.j2cl.processors.logger.TreeLogger;
import org.w3c.dom.Node;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.lang.model.element.ExecutableElement;

/** @author Dmitrii Tikhomirov Created by treblereel 11/15/18 */
public class ImageResourceGenerator extends AbstractResourceGenerator {

  /*
   * Only PNG is supported right now. In the future, we may be able to infer the
   * best output type, and get rid of this constant.
   */
  static final String BUNDLE_FILE_TYPE = "png";
  private static final int IMAGE_MAX_SIZE =
      Integer.getInteger("gwt.imageResource.maxBundleSize", 256);

  private CachedState shared;

  @Override
  public String createAssignment(
          TreeLogger logger, ResourceContext context, ExecutableElement method, String locale) {
    String name = method.getSimpleName().toString();

    ImageResourceDeclaration image = new ImageResourceDeclaration(method);
    DisplayedImage bundle = getImage(image);

    SourceWriter sw = new StringSourceWriter();
    sw.println("new " + bundle.getResourceType().getCanonicalName() + "(");
    sw.indent();
    sw.println('"' + name + "\",");

    ImageRect rect = bundle.getImageRect(image);
    if (rect == null) {
      throw new NullPointerException("No ImageRect ever computed for " + name);
    }
    rect.setHeight(image.getScaleHeight());
    rect.setWidth(image.getScaleWidth());

    String[] urlExpressions =
        new String[] {bundle.getNormalContentsFieldName(), bundle.getRtlContentsFieldName()};
    assert urlExpressions[0] != null : "No primary URL expression for " + name;

    if (urlExpressions[1] == null) {
      sw.println(UriUtils.class.getName() + ".fromTrustedString(" + urlExpressions[0] + "),");
    } else {
      sw.println(UriUtils.class.getName() + ".fromTrustedString(" + urlExpressions[0] + "),");
    }
    sw.println(
        rect.getLeft()
            + ", "
            + rect.getTop()
            + ", "
            + rect.getWidth()
            + ", "
            + rect.getHeight()
            + ", "
            + rect.isAnimated()
            + ", "
            + rect.isLossy());

    sw.outdent();
    sw.print(")");

    return sw.toString();
  }

  private ExternalImage getImage(ImageResourceDeclaration image) {
    ExternalImage toReturn = shared.externalImages.get(new BundleKey(image, true));
    if (toReturn != null) {
      return toReturn;
    }
    return null;
  }

  /**
   * We use this as a signal that we have received all image methods and can now create the bundled
   * images.
   */
  @Override
  public void createFields(TreeLogger logger, ResourceContext context, ClientBundleFields fields)
      throws UnableToCompleteException {
    renderImageMap(logger, context, fields, shared.externalImages);
  }

  private void renderImageMap(
      TreeLogger logger,
      ResourceContext context,
      ClientBundleFields fields,
      Map<BundleKey, ? extends DisplayedImage> map)
      throws UnableToCompleteException {
    for (Map.Entry<BundleKey, ? extends DisplayedImage> entry : map.entrySet()) {
      DisplayedImage bundle = entry.getValue();
      bundle.render(logger, context, fields);
    }
  }

  @Override
  public void init(TreeLogger logger, ResourceContext context) {
    String key = createCacheKey(context);
    shared = context.getCachedData(key, CachedState.class);
    if (shared != null) {
      logger.log(TreeLogger.DEBUG, "Using cached data");
    } else {
      shared = new CachedState();
      context.putCachedData(key, shared);
    }
  }

  /**
   * Creates a cache key to be used with {@link ResourceContext#putCachedData}. The key is based on
   * the ClientBundle type, support for data URLs, and the current locale.
   */
  private String createCacheKey(ResourceContext context) {
    StringBuilder sb = new StringBuilder();
    sb.append(context.getClientBundleType().getQualifiedName().toString());
    sb.append(":").append(context.supportsDataUrls());
    /*        try {
        //TODO LOCALE
        //sb.append(locale);
    } catch (BadPropertyValueException e) {
        // OK, locale isn't defined
    }*/
    return sb.toString();
  }

  /**
   * Process each image method. This will either assign the image to an ImageBundleBuilder or
   * reencode an external image.
   */
  @Override
  public void prepare(
      TreeLogger logger, ResourceContext context, ExecutableElement method, String locale)
      throws UnableToCompleteException {

    ImageResourceDeclaration image = new ImageResourceDeclaration(method);

    LocalizedImage localized = LocalizedImage.create(logger, context, image, locale);

    ResourceOracle resourceOracle = context.getGeneratorContext().getResourcesOracle();

    URL[] resources = resourceOracle.findResources(logger, image.getMethod(), locale);

    if (resources.length != 1) {
      logger.log(TreeLogger.ERROR, "Exactly one image may be specified", null);
      throw new UnableToCompleteException();
    }

    URL resource = resources[0];

    ImageRect rect = addImage(logger, resource.getFile(), resource);

    if (rect.isAnimated() || rect.isLossy()) {
      // Don't re-encode
    } else {
      /*
       * Try to re-compress the image, but only use the re-compressed bytes if
       * they actually offer a space-savings.
       */
      try {
        int originalSize = getContentLength(resource);
        // Re-encode the data
        URL reencodedContents = reencodeToTempFile(logger, rect);
        int newSize = getContentLength(reencodedContents);

        // But only use it if we did a better job on compression

        if (newSize < originalSize) {
          //  if (logger.isLoggable(TreeLogger.SPAM)) {
          logger.log(
              TreeLogger.SPAM, "Reencoded image and saved " + (originalSize - newSize) + " bytes");
          //    }
          localized = new LocalizedImage(localized, reencodedContents);
        }
      } catch (IOException e2) {
        // Non-fatal, but weird
        logger.log(
            TreeLogger.WARN,
            "Unable to determine before/after size when re-encoding image " + "data",
            e2);
      }
    }

    ExternalImage externalImage = new ExternalImage(image, localized, rect);
    shared.externalImages.put(new BundleKey(image, true), externalImage);
  }

  private ImageRect addImage(TreeLogger logger, String imageName, URL imageUrl)
      throws UnableToCompleteException {

    logger = logger.branch(TreeLogger.TRACE, "Adding image '" + imageName + "'", null);
    BufferedImage image = null;
    // Be safe by default and assume that the incoming image is lossy
    boolean lossy = true;
    ImageRect animated = null;
    // Load the image
    try (InputStream is = imageUrl.openStream();
        MemoryCacheImageInputStream imageInputStream = new MemoryCacheImageInputStream(is)) {
      /*
       * ImageIO uses an SPI pattern API. We don't care about the particulars of
       * the implementation, so just choose the first ImageReader.
       */
      Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputStream);
      readers:
      while (it.hasNext()) {
        ImageReader reader = it.next();
        reader.setInput(imageInputStream);

        int numImages = reader.getNumImages(true);
        if (numImages == 0) {
          // Fall through

        } else if (numImages == 1) {
          try {
            image = reader.read(0);
            IIOMetadata metadata = reader.getImageMetadata(0);
            if (metadata != null && metadata.isStandardMetadataFormatSupported()) {
              // http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
              Node data = metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
              metadata:
              for (int i = 0, j = data.getChildNodes().getLength(); i < j; i++) {
                Node child = data.getChildNodes().item(i);
                if (child.getLocalName().equalsIgnoreCase("compression")) {
                  for (int k = 0, l = child.getChildNodes().getLength(); k < l; k++) {
                    Node child2 = child.getChildNodes().item(k);
                    if (child2.getLocalName().equalsIgnoreCase("lossless")) {
                      Node value = child2.getAttributes().getNamedItem("value");
                      if (value == null) {
                        // The default is true, according to the DTD
                        lossy = false;
                      } else {
                        lossy = !Boolean.parseBoolean(value.getNodeValue());
                      }
                      break metadata;
                    }
                  }
                }
              }
            }
          } catch (Exception e) {
            // Hope we have another reader that can handle the image
            continue readers;
          }

        } else {
          // Read all contained images
          BufferedImage[] images = new BufferedImage[numImages];

          try {
            for (int i = 0; i < numImages; i++) {
              images[i] = reader.read(i);
            }
          } catch (Exception e) {
            // Hope we have another reader that can handle the image
            continue readers;
          }

          animated = new ImageRect(imageName, images);
          animated.setLossy(false);
        }
      }
    } catch (IllegalArgumentException iex) {
      if (imageName.toLowerCase(Locale.ROOT).endsWith("png")
          && iex.getMessage() != null
          && iex.getStackTrace()[0]
              .getClassName()
              .equals("javax.imageio.ImageTypeSpecifier$Indexed")) {
        logger.log(
            TreeLogger.ERROR,
            "Unable to read image. The image may not be in valid PNG format. "
                + "This problem may also be due to a bug in versions of the "
                + "JRE prior to 1.6. See "
                + "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5098176 "
                + "for more information. If this bug is the cause of the "
                + "error, try resaving the image using a different image "
                + "program, or upgrade to a newer JRE.",
            null);
        throw new UnableToCompleteException();
      } else {
        throw iex;
      }
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Unable to read image resource", e);
      throw new UnableToCompleteException();
    }

    if (image == null && animated == null) {
      logger.log(TreeLogger.ERROR, "Unrecognized image file format", null);
      throw new UnableToCompleteException();
    }

    ImageRect toReturn;
    if (animated == null) {
      toReturn = new ImageRect(imageName, image);
      toReturn.setLossy(lossy);
    } else {
      toReturn = animated;
    }

    return toReturn;
  }

  /**
   * Helper method to read the contentLength of a given URL, automatically closing the InputStream
   * that is opened as a side effect.
   */
  private int getContentLength(URL url) throws IOException {
    URLConnection conn = url.openConnection();
    try {
      return conn.getContentLength();
    } finally {
      Utility.close(conn.getInputStream());
    }
  }

  /** Re-encode an image as a PNG to strip random header data. */
  private URL reencodeToTempFile(TreeLogger logger, ImageRect rect)
      throws UnableToCompleteException {
    try {
      byte[] imageBytes = toPng(logger, rect);

      if (imageBytes == null) {
        return null;
      }

      File file = File.createTempFile(ImageResourceGenerator.class.getSimpleName(), ".png");
      file.deleteOnExit();
      Util.writeBytesToFile(logger, file, imageBytes);
      return file.toURI().toURL();
    } catch (IOException ex) {
      logger.log(TreeLogger.ERROR, "Unable to write re-encoded PNG", ex);
      throw new UnableToCompleteException();
    }
  }

  public static byte[] toPng(TreeLogger logger, ImageRect rect) throws UnableToCompleteException {
    // Create the bundled image.
    BufferedImage bundledImage =
        new BufferedImage(rect.getWidth(), rect.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D g2d = bundledImage.createGraphics();
    setBetterRenderingQuality(g2d);

    g2d.drawImage(rect.getImage(), rect.transform(), null);
    g2d.dispose();

    byte[] imageBytes = createImageBytes(logger, bundledImage);
    return imageBytes;
  }

  /** Write the bundled image into a byte array, so that we can compute its strong name. */
  private static byte[] createImageBytes(TreeLogger logger, BufferedImage bundledImage)
      throws UnableToCompleteException {
    byte[] imageBytes;

    try {
      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
      boolean writerAvailable = ImageIO.write(bundledImage, BUNDLE_FILE_TYPE, byteOutputStream);
      if (!writerAvailable) {
        logger.log(TreeLogger.ERROR, "No " + BUNDLE_FILE_TYPE + " writer available");
        throw new UnableToCompleteException();
      }
      imageBytes = byteOutputStream.toByteArray();
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "An error occurred while trying to write the image bundle.", e);
      throw new UnableToCompleteException();
    }
    return imageBytes;
  }

  private static void setBetterRenderingQuality(Graphics2D g2d) {
    g2d.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2d.setRenderingHint(
        RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
  }

  /**
   * This key is used to determine which DisplayedImage a given set of image bytes should be
   * associated with.
   */
  static class BundleKey extends StringKey {

    public BundleKey(ImageResourceDeclaration image, boolean isExternal) {
      super(key(image, isExternal));
    }

    private static String key(ImageResourceDeclaration image, boolean isExternal) {
      return "Unbundled: " + image.get();
    }

    public boolean isExternal() {
      return false;
    }
  }

  /** This is client that can be client across permutations for a given ClientBundle . */
  static class CachedState {
    public final Map<BundleKey, ExternalImage> externalImages = new LinkedHashMap<>();
  }

  /** Represents a file that contains exactly one image. */
  static class ExternalImage extends DisplayedImage {
    private final ImageResourceDeclaration image;
    private final LocalizedImage localized;
    private final ImageRect rect;
    private boolean isRtl;

    /** Create an unbundled image. */
    public ExternalImage(ImageResourceDeclaration image, LocalizedImage localized, ImageRect rect) {
      this.image = image;
      this.localized = localized;
      this.rect = rect;
    }

    @Override
    public Class<?> getResourceType() {
      return ImageResourcePrototype.class;
    }

    @Override
    public ImageRect getImageRect(ImageResourceDeclaration image) {
      return this.image.equals(image) ? rect : null;
    }

    @Override
    public void setRtlImage(LocalizedImage image) {
      if (this.localized.equals(localized)) {
        isRtl = true;
      }
    }

    @Override
    public void render(TreeLogger logger, ResourceContext context, ClientBundleFields fields)
        throws UnableToCompleteException {

      String contentsExpression = context.deploy(localized.getUrl(), null, false);
      normalContentsFieldName =
          fields.define("String", "externalImage", contentsExpression, true, true);

      if (isRtl) {
        // Create a transformation to mirror about the Y-axis and translate
        AffineTransform tx = new AffineTransform();
        tx.setTransform(-1, 0, 0, 1, rect.getWidth(), 0);
        rect.setTransform(tx);

        byte[] rtlData = toPng(logger, rect);
        String rtlContentsUrlExpression =
            context.deploy(image.getName() + "_rtl.png", "image/png", rtlData, false);
        rtlContentsFieldName =
            fields.define("String", "externalImage_rtl", rtlContentsUrlExpression, true, true);
      }
    }
  }

  /**
   * This represent how the user described the image in the original Java source. Its identity is
   * based on the ImageResource JMethod.
   */
  static class ImageResourceDeclaration extends StringKey {
    private final String name;
    private final ExecutableElement method;
    private final ImageOptions options;

    public ImageResourceDeclaration(ExecutableElement method) {
      super(key(method));
      this.name = method.getSimpleName().toString();
      this.method = method;
      this.options = method.getAnnotation(ImageOptions.class);
    }

    private static String key(ExecutableElement method) {
      return method.getEnclosingElement() + "." + method.getSimpleName().toString();
    }

    public ExecutableElement getMethod() {
      return method;
    }

    public String getName() {
      return name;
    }

    public int getScaleHeight() {
      return options == null ? -1 : options.height();
    }

    public int getScaleWidth() {
      return options == null ? -1 : options.width();
    }
  }

  /** Represents a file that contains image data. */
  abstract static class DisplayedImage {
    protected String normalContentsFieldName;
    protected String rtlContentsFieldName;

    public abstract Class<?> getResourceType();

    public abstract ImageRect getImageRect(ImageResourceDeclaration image);

    /** Only valid after calling {@link #render}. */
    public String getNormalContentsFieldName() {
      return normalContentsFieldName;
    }

    /**
     * Only valid after calling {@link #render}, may be <code>null</code> if there is no RTL version
     * of the image.
     */
    public String getRtlContentsFieldName() {
      return rtlContentsFieldName;
    }

    public abstract void setRtlImage(LocalizedImage image);

    abstract void render(TreeLogger logger, ResourceContext context, ClientBundleFields fields)
        throws UnableToCompleteException;
  }

  /** The rectangle at which the original image is placed into the composite image. */
  static class ImageRect {

    private final int intrinsicHeight, intrinsicWidth;
    private final BufferedImage[] images;
    private final String name;
    private final AffineTransform transform = new AffineTransform();
    private boolean hasBeenPositioned, lossy;
    private int height, width;
    private int left, top;

    /** Copy constructor. */
    public ImageRect(ImageRect other) {
      this.name = other.getName();
      this.height = other.height;
      this.width = other.width;
      this.images = other.getImages();
      this.left = other.getLeft();
      this.top = other.getTop();
      this.intrinsicHeight = other.intrinsicHeight;
      this.intrinsicWidth = other.intrinsicWidth;
      setTransform(other.getTransform());
    }

    public BufferedImage[] getImages() {
      return images;
    }

    public int getLeft() {
      return left;
    }

    public String getName() {
      return name;
    }

    public int getTop() {
      return top;
    }

    public AffineTransform getTransform() {
      return new AffineTransform(transform);
    }

    public void setTransform(AffineTransform transform) {
      this.transform.setTransform(transform);
    }

    public ImageRect(String name, BufferedImage... images) {
      this.name = name;
      this.images = images;
      this.intrinsicWidth = images[0].getWidth();
      this.intrinsicHeight = images[0].getHeight();
      this.height = this.width = -1;
    }

    public BufferedImage getImage() {
      return images[0];
    }

    public boolean hasBeenPositioned() {
      return hasBeenPositioned;
    }

    public boolean isAnimated() {
      return images.length > 1;
    }

    public boolean isLossy() {
      return lossy;
    }

    public void setLossy(boolean lossy) {
      this.lossy = lossy;
    }

    public void setPosition(int left, int top) {
      hasBeenPositioned = true;
      this.left = left;
      this.top = top;
    }

    public AffineTransform transform() {
      AffineTransform toReturn = new AffineTransform();

      // Translate
      toReturn.translate(left, top);

      // Scale
      assert height > 0 == width > 0;
      if (height > 0) {
        toReturn.scale((double) height / intrinsicHeight, (double) width / intrinsicWidth);
      }

      // Use the base concatenation
      toReturn.concatenate(transform);

      assert checkTransform(toReturn);
      return toReturn;
    }

    private boolean checkTransform(AffineTransform tx) {
      double[] in = {0, 0, intrinsicWidth, intrinsicHeight};
      double[] out = {0, 0, 0, 0};

      tx.transform(in, 0, out, 0, 2);

      // Sanity check on bounds
      assert out[0] >= 0;
      assert out[1] >= 0;
      assert out[2] >= 0;
      assert out[3] >= 0;

      // Check scaling
      assert getWidth() == Math.round(Math.abs(out[0] - out[2]))
          : "Width " + getWidth() + " != " + Math.round(Math.abs(out[0] - out[2]));
      assert getHeight() == Math.round(Math.abs(out[1] - out[3]))
          : "Height " + getHeight() + "!=" + Math.round(Math.abs(out[1] - out[3]));

      return true;
    }

    public int getHeight() {
      return height > 0 ? height : intrinsicHeight;
    }

    public void setHeight(int height) {
      this.height = height;
      if (width <= 0) {
        width = (int) Math.round((double) height / intrinsicHeight * intrinsicWidth);
      }
    }

    public int getWidth() {
      return width > 0 ? width : intrinsicWidth;
    }

    public void setWidth(int width) {
      this.width = width;
      if (height <= 0) {
        height = (int) Math.round((double) width / intrinsicWidth * intrinsicHeight);
      }
    }
  }

  /** Used to return the size of the resulting image from the method */
  static class Size {
    private final int width, height;

    Size(int width, int height) {
      this.width = width;
      this.height = height;
    }
  }

  /**
   * This represents the particular collections of bits associated with a localized resource that a
   * permutation will use. Its identity is based on the content hash of the resolved data and any
   * transformations that will be applied to the data.
   */
  static class LocalizedImage {
    private final ImageResourceDeclaration image;
    private final URL url;

    public LocalizedImage(LocalizedImage other, URL alternateUrl) {
      this(other.image, alternateUrl);
    }

    private LocalizedImage(ImageResourceDeclaration image, URL url) {
      this.image = image;
      this.url = url;
    }

    private static String key(ImageResourceDeclaration image, URL url) {
      return Util.computeStrongName(Util.readURLAsBytes(url))
          + ":"
          + image.getScaleHeight()
          + ":"
          + image.getScaleWidth();
    }

    public static LocalizedImage create(
        TreeLogger logger, ResourceContext context, ImageResourceDeclaration image, String locale)
        throws UnableToCompleteException {

      ResourceOracle resourceOracle = context.getGeneratorContext().getResourcesOracle();

      URL[] resources = resourceOracle.findResources(logger, image.getMethod(), locale);

      if (resources.length != 1) {
        logger.log(
            TreeLogger.ERROR, "Exactly one image may be specified 1 " + resources.length, null);
        throw new UnableToCompleteException();
      }

      URL resource = resources[0];

      LocalizedImage toReturn = new LocalizedImage(image, resource);
      return toReturn;
    }

    public URL getUrl() {
      return url;
    }
  }
}
