/*
 * Copyright 2008 Google Inc.
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
package org.treblereel.j2cl.processors.generator.resources.context;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import org.treblereel.j2cl.processors.common.resources.ClientBundleWithLookup;
import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.context.AptContext;
import org.treblereel.j2cl.processors.generator.resources.ext.GeneratorContext;
import org.treblereel.j2cl.processors.generator.resources.ext.ResourceGenerator;
import org.treblereel.j2cl.processors.generator.resources.ext.UnableToCompleteException;
import org.treblereel.j2cl.processors.generator.resources.rg.Generator;
import org.treblereel.j2cl.processors.generator.resources.rg.util.MoreTypeUtils;
import org.treblereel.j2cl.processors.logger.TreeLogger;

/**
 * The base class for creating new ClientBundle implementations.
 *
 * <p>The general structure of the generated class is as follows:
 *
 * <pre>
 * private void resourceInitializer() {
 *   resource = new Resource();
 * }
 * private static class cellTreeClosedItemInitializer {
 *   // Using a static initializer so the compiler can optimize clinit calls.
 *   // Refers back to an instance method. See comment below.
 *   static {
 *     _instance0.resourceInitializer();
 *   }
 *   static ResourceType get() {
 *     return resource;
 *   }
 * }
 * public ResourceType resource() {
 *   return cellTreeClosedItemInitializer.get();
 * }
 * // Other ResourceGenerator-defined fields
 * private static ResourceType resource;
 * private static HashMap&lt;String, ResourcePrototype&gt; resourceMap;
 * public ResourcePrototype[] getResources() {
 *   return new ResourcePrototype[] { resource() };
 * }
 * public ResourcePrototype getResource(String name) {
 *   if (GWT.isScript()) {
 *     return getResourceNative(name);
 *   } else {
 *     if (resourceMap == null) {
 *       resourceMap = new HashMap<String, ResourcePrototype>();
 *       resourceMap.put("resource", resource());
 *     }
 *     return resourceMap.get(name);
 *   }
 * }
 * private native ResourcePrototype getResourceNative(String name) /-{
 *   switch (name) {
 *     case 'resource': return this.@...::resource()();
 *   }
 *   return null;
 * }-/
 * </pre>
 *
 * The instantiation of the individual ResourcePrototypes is done in the content of an instance of
 * the ClientBundle type so that resources can refer to one another by simply emitting a call to
 * <code>resource()</code>.
 */
public abstract class AbstractClientBundleGenerator extends Generator {
  private static final String INSTANCE_NAME = "_instance0";
  private Map<TypeElement, Map<Class<? extends ResourceGenerator>, List<ExecutableElement>>>
      taskListByTypeElement = new HashMap<>();
  private Map<TypeElement, FieldsImpl> typeElementFieldsMap = new HashMap<>();
  private Map<TypeElement, AbstractResourceContext> typeElementResourceContextsMap =
      new HashMap<>();
  private Map<TypeElement, Map<ResourceGenerator, List<ExecutableElement>>>
      typeElementResourceGeneratorsMap = new HashMap<>();

  private AptContext aptContext;

  @Override
  public void generate(
          TreeLogger logger, GeneratorContext generatorContext, Set<TypeElement> bundles)
      throws UnableToCompleteException {
    this.aptContext = generatorContext.getAptContext();

    // default resources
    for (TypeElement bundle : bundles) {
      initAndPrepare(logger, generatorContext, bundle, null);
    }
    for (TypeElement bundle : bundles) {
      process(logger, generatorContext, bundle, null);
    }
  }

  private void initAndPrepare(
      TreeLogger logger, GeneratorContext generatorContext, TypeElement bundle, String locale)
      throws UnableToCompleteException {
    // Ensure that the requested type exists
    if (bundle == null) {
      logger.log(TreeLogger.ERROR, "Could not find requested typeName");
      throw new UnableToCompleteException();
    } else if (!bundle.getKind().isInterface()) {
      // The incoming type wasn't a plain interface, we don't support
      // abstract base classes
      logger.log(
          TreeLogger.ERROR,
          MoreTypeUtils.getQualifiedSourceName(bundle) + " is not an interface.",
          null);
      throw new UnableToCompleteException();
    }

    /*
     * This associates the methods to implement with the ResourceGenerator class
     * that will generate the implementations of those methods.
     */
    Map<Class<? extends ResourceGenerator>, List<ExecutableElement>> taskList =
        createTaskList(logger, bundle, generatorContext);
    taskListByTypeElement.put(bundle, taskList);
    /*
     * Additional objects that hold state during the generation process.
     */
    AbstractResourceContext resourceContext =
        createResourceContext(logger, generatorContext, bundle, locale);
    typeElementResourceContextsMap.put(bundle, resourceContext);
    FieldsImpl fields = new FieldsImpl();
    typeElementFieldsMap.put(bundle, fields);
    doAddFields(logger, generatorContext, fields);

    /* Initialize the ResourceGenerators and prepare them for subsequent code
     * generation.
     */
    Map<ResourceGenerator, List<ExecutableElement>> generators =
        initAndPrepare(logger, taskList, resourceContext, locale);
    typeElementResourceGeneratorsMap.put(bundle, generators);
  }

  /**
   * Create the ResourceContext object that will be used by {@link ResourceGenerator} subclasses.
   * This is the primary way to implement custom logic in the resource generation pass.
   */
  protected abstract AbstractResourceContext createResourceContext(
      TreeLogger logger, GeneratorContext context, TypeElement resourceBundleType, String locale)
      throws UnableToCompleteException;

  /**
   * Provides a hook for subtypes to add additional fields or requirements to the bundle.
   *
   * @param logger a TreeLogger
   * @param context the GeneratorContext
   * @param fields ClentBundle fields
   */
  protected void doAddFields(TreeLogger logger, GeneratorContext context, FieldsImpl fields) {}

  /**
   * Given a ClientBundle subtype, compute which ResourceGenerators should implement which methods.
   * The data returned from this method should be stable across identical modules.
   */
  private Map<Class<? extends ResourceGenerator>, List<ExecutableElement>> createTaskList(
      TreeLogger logger, TypeElement sourceType, GeneratorContext generatorContext)
      throws UnableToCompleteException {
    Types types = generatorContext.getAptContext().types;
    Elements elements = generatorContext.getAptContext().elements;
    logger = logger.branch(TreeLogger.DEBUG, "Processing " + sourceType);
    Map<Class<? extends ResourceGenerator>, List<ExecutableElement>> toReturn =
        new LinkedHashMap<>();

    TypeElement bundleWithLookupType =
        MoreTypeUtils.getTypeElementFromClass(ClientBundleWithLookup.class, aptContext.elements);
    assert bundleWithLookupType != null;

    TypeElement resourcePrototypeType =
        MoreTypeUtils.getTypeElementFromClass(ResourcePrototype.class, aptContext.elements);
    assert resourcePrototypeType != null;

    // Accumulate as many errors as possible before failing
    boolean throwException = false;

    // Using overridable methods allows composition of interface types
    ImmutableSet<ExecutableElement> methods =
        MoreElements.getLocalAndInheritedMethods(sourceType, types, elements);
    for (ExecutableElement method : methods) {
      TypeMirror theReturn = method.getReturnType();
      if (MoreTypes.isTypeOf(ClientBundleWithLookup.class, method.getEnclosingElement().asType())) {
        // Methods that we must generate, but that are not resources
        continue;
      } else if (!MoreTypeUtils.isAbstract(method)) {
        // Covers the case of an abstract class base type
        continue;
      } else if (theReturn == null) {
        // Primitives and random other abstract methods
        logger.log(
            TreeLogger.ERROR,
            "Unable to process method '"
                + method.getSimpleName().toString()
                + "' from "
                + method.getEnclosingElement()
                + " because "
                + method.getReturnType()
                + " does not derive from "
                + MoreTypeUtils.getQualifiedSourceName(resourcePrototypeType));
        throwException = true;
        continue;
      }

      try {
        Class<? extends ResourceGenerator> clazz = findResourceGenerator(logger, method);
        List<ExecutableElement> generatorMethods;
        if (toReturn.containsKey(clazz)) {
          generatorMethods = toReturn.get(clazz);
        } else {
          generatorMethods = new ArrayList<>();
          toReturn.put(clazz, generatorMethods);
        }

        generatorMethods.add(method);
      } catch (UnableToCompleteException e) {
        throwException = true;
      }
    }

    if (throwException) {
      throw new UnableToCompleteException();
    }

    return toReturn;
  }

  /**
   * Given a ExecutableElement, find the a ResourceGenerator class that will be able to provide an
   * implementation of the method.
   */
  private Class<? extends ResourceGenerator> findResourceGenerator(
      TreeLogger logger, ExecutableElement method) throws UnableToCompleteException {
    TypeElement resourceType = MoreTypes.asTypeElement(method.getReturnType());
    if (aptContext.generators.containsKey(resourceType)) {
      return aptContext.generators.get(resourceType);
    } else {
      List<? extends TypeMirror> parents =
          new ArrayList<>(ResourceGeneratorUtil.getAllParents(resourceType));
      Collections.reverse(parents);
      for (TypeMirror p : parents) {
        Element parent = aptContext.types.asElement(p);
        if (aptContext.generators.containsKey(parent)) {
          return aptContext.generators.get(parent);
        }
      }

      /** This is a special case of ResourceGenerator that handles nested bundles. */
      if (parents.size() == 1) {
        boolean theSame =
            aptContext.types.isSameType(
                parents.get(0),
                aptContext.elements.getTypeElement(Object.class.getCanonicalName()).asType());
        if (theSame) return BundleResourceGenerator.class;
      }
    }
    logger.log(
        TreeLogger.ERROR,
        "No @"
            + ResourceGeneratorType.class.getName()
            + " was specified for type "
            + resourceType
            + " or its supertypes");
    throw new UnableToCompleteException();
  }

  private Map<ResourceGenerator, List<ExecutableElement>> initAndPrepare(
      TreeLogger logger,
      Map<Class<? extends ResourceGenerator>, List<ExecutableElement>> taskList,
      AbstractResourceContext resourceContext,
      String locale)
      throws UnableToCompleteException {

    // Try to provide as many errors as possible before failing.
    boolean success = true;
    Map<ResourceGenerator, List<ExecutableElement>> toReturn = new LinkedHashMap<>();

    // Run the ResourceGenerators to generate implementations of the methods
    for (Map.Entry<Class<? extends ResourceGenerator>, List<ExecutableElement>> entry :
        taskList.entrySet()) {
      ResourceGenerator rg = instantiateResourceGenerator(logger, entry.getKey());
      toReturn.put(rg, entry.getValue());
      success &= initAndPrepare(logger, resourceContext, rg, entry.getValue(), locale);
    }

    if (!success) {
      throw new UnableToCompleteException();
    }

    return toReturn;
  }

  private boolean initAndPrepare(
      TreeLogger logger,
      AbstractResourceContext resourceContext,
      ResourceGenerator rg,
      List<ExecutableElement> generatorMethods,
      String locale) {
    try {
      resourceContext.setCurrentResourceGenerator(rg);
      rg.init(
          logger.branch(
              TreeLogger.DEBUG,
              "Initializing ResourceGenerator " + rg.getClass().getCanonicalName()),
          resourceContext);
    } catch (UnableToCompleteException e) {
      return false;
    }

    boolean fail = false;

    // Prepare the ResourceGenerator by telling it all methods that it is
    // expected to produce.
    for (ExecutableElement m : generatorMethods) {
      try {
        rg.prepare(
            logger.branch(TreeLogger.DEBUG, "Preparing method " + m.getSimpleName().toString()),
            resourceContext,
            m,
            locale);
      } catch (UnableToCompleteException e) {
        fail = true;
      }
    }
    return !fail;
  }

  /** Utility method to construct a ResourceGenerator that logs errors. */
  private <T extends ResourceGenerator> T instantiateResourceGenerator(
      TreeLogger logger, Class<T> generatorClass) throws UnableToCompleteException {
    try {
      return generatorClass.newInstance();
    } catch (InstantiationException e) {
      logger.log(TreeLogger.ERROR, "Unable to initialize ResourceGenerator", e);
    } catch (IllegalAccessException e) {
      logger.log(
          TreeLogger.ERROR,
          "Unable to instantiate ResourceGenerator. "
              + "Does it have a public default constructor?",
          e);
    }
    throw new UnableToCompleteException();
  }

  private void process(
      TreeLogger logger, GeneratorContext generatorContext, TypeElement bundle, String locale)
      throws UnableToCompleteException {

    Map<Class<? extends ResourceGenerator>, List<ExecutableElement>> taskList =
        taskListByTypeElement.get(bundle);
    FieldsImpl fields = typeElementFieldsMap.get(bundle);
    AbstractResourceContext resourceContext = typeElementResourceContextsMap.get(bundle);
    Map<ResourceGenerator, List<ExecutableElement>> generators =
        typeElementResourceGeneratorsMap.get(bundle);
    /*
     * Now that the ResourceGenerators have been initialized and prepared, we
     * can compute the actual name of the implementation class in order to
     * ensure that we use a distinct name between permutations.
     */
    String generatedSimpleSourceName = generateSimpleSourceName(logger, resourceContext, locale);
    String packageName = MoreElements.getPackage(bundle).getQualifiedName().toString();
    PrintWriter out = generatorContext.tryCreate(logger, packageName, generatedSimpleSourceName);
    // If an implementation already exists, we don't need to do any work
    if (out != null) {
      // There is actual work to do
      doCreateBundleForPermutation(logger, generatorContext, fields, generatedSimpleSourceName);
      // Begin writing the generated source.
      ClassSourceFileComposerFactory f =
          new ClassSourceFileComposerFactory(packageName, generatedSimpleSourceName);

      // Used by the map methods
      f.addImport(ResourcePrototype.class.getName());

      // The whole point of this exercise
      f.addImplementedInterface(Util.getQualifiedSourceName(bundle, aptContext.elements));

      // All source gets written through this Writer
      SourceWriter sw = f.createSourceWriter(generatorContext, out);
      // Set the now-calculated simple source name
      resourceContext.setSimpleSourceName(generatedSimpleSourceName);

      String hashMapStringResource = "java.util.HashMap<String, ResourcePrototype>";
      String resourceMapField = fields.define(hashMapStringResource, "resourceMap");

      // Write a static instance for use by the static initializers.
      sw.print("private static " + generatedSimpleSourceName + " ");
      sw.println(INSTANCE_NAME + " = new " + generatedSimpleSourceName + "();");

      // Write the generated code to disk
      createFieldsAndAssignments(logger, sw, generators, resourceContext, fields, locale);

      // Print the accumulated field definitions
      sw.println(fields.getCode());

      /*
       *The map -accessor methods use JSNI and need a fully - qualified class
       *name, but should not include any sub - bundles.
       */
      writeMapMethods(sw, taskList, hashMapStringResource, resourceMapField);
      sw.commit(logger);
    }

    finish(logger, resourceContext, generators.keySet());
    doFinish(logger);
  }

  /**
   * This method is called after the ClientBundleRequirements have been evaluated and a new
   * ClientBundle implementation is being created.
   *
   * @param logger a TreeLogger
   * @param generatorContext the GeneratoContext
   * @param fields ClientBundle fields
   * @param generatedSimpleSourceName a String
   */
  protected void doCreateBundleForPermutation(
      TreeLogger logger,
      GeneratorContext generatorContext,
      FieldsImpl fields,
      String generatedSimpleSourceName) {}

  /**
   * Provides a hook for finalizing generated resources.
   *
   * @param logger a TreeLogger
   */
  protected void doFinish(TreeLogger logger) {}

  /** Call finish() on several ResourceGenerators. */
  private void finish(
      TreeLogger logger, AbstractResourceContext context, Collection<ResourceGenerator> generators)
      throws UnableToCompleteException {
    boolean fail = false;
    // Finalize the ResourceGenerator
    for (ResourceGenerator rg : generators) {
      context.setCurrentResourceGenerator(rg);
      try {
        rg.finish(logger.branch(TreeLogger.DEBUG, "Finishing ResourceGenerator"), context);
      } catch (UnableToCompleteException e) {
        fail = true;
      }
    }
    if (fail) {
      throw new UnableToCompleteException();
    }
  }

  /**
   * Given a user-defined type name, determine the type name for the generated class based on
   * accumulated requirements.
   */
  public String generateSimpleSourceName(
      TreeLogger logger, ResourceContext context, String locale) {
    return ResourceGeneratorUtil.generateSimpleSourceName(
        logger, context.getClientBundleType(), locale);
  }

  /** Create fields and assignments for multiple ResourceGenerators. */
  private void createFieldsAndAssignments(
      TreeLogger logger,
      SourceWriter sw,
      Map<ResourceGenerator, List<ExecutableElement>> generators,
      AbstractResourceContext resourceContext,
      ClientBundleFields fields,
      String locale)
      throws UnableToCompleteException {
    // Try to provide as many errors as possible before failing.
    boolean success = true;

    // Run the ResourceGenerators to generate implementations of the methods
    for (Map.Entry<ResourceGenerator, List<ExecutableElement>> entry : generators.entrySet()) {
      success &=
          createFieldsAndAssignments(
              logger, resourceContext, entry.getKey(), entry.getValue(), sw, fields, locale);
    }

    if (!success) {
      throw new UnableToCompleteException();
    }
  }

  /** Create fields and assignments for a single ResourceGenerator. */
  private boolean createFieldsAndAssignments(
      TreeLogger logger,
      AbstractResourceContext resourceContext,
      ResourceGenerator rg,
      List<ExecutableElement> generatorMethods,
      SourceWriter sw,
      ClientBundleFields fields,
      String locale) {
    // Defer failure until this phase has ended
    boolean fail = false;
    resourceContext.setCurrentResourceGenerator(rg);
    // Write all field values
    try {
      rg.createFields(logger.branch(TreeLogger.DEBUG, "Creating fields"), resourceContext, fields);
    } catch (UnableToCompleteException e) {
      return false;
    }

    // Create the instance variables in the IRB subclass by calling
    // writeAssignment() on the ResourceGenerator
    for (ExecutableElement m : generatorMethods) {
      String rhs;

      try {
        rhs =
            rg.createAssignment(
                logger.branch(
                    TreeLogger.DEBUG,
                    "Creating assignment for " + m.getSimpleName().toString() + "()"),
                resourceContext,
                m,
                locale);
      } catch (UnableToCompleteException e) {
        fail = true;
        continue;
      }

      // Define a field that will hold the ResourcePrototype
      TypeElement theReturn = (TypeElement) MoreTypes.asElement(m.getReturnType());
      String ident = fields.define(theReturn, m.getSimpleName().toString(), null, true, false);
      /*
       * Create an initializer method in the context of an instance so that
       * resources can refer to one another by simply emitting a call to
       * <code>resource()</code>.
       */
      String initializerName = m.getSimpleName() + "Initializer";
      sw.println("private void " + initializerName + "() {");
      sw.indentln(ident + " = " + rhs + ";");
      sw.println("}");

      /*
       * Create a static Initializer class to lazily initialize the field on
       * first access. The compiler can efficiently optimize this static class
       * using clinits.
       */
      sw.println("private static class " + initializerName + " {");

      sw.indent();
      sw.println("static {");
      sw.indentln(INSTANCE_NAME + "." + initializerName + "();");
      sw.println("}");

      sw.print("static ");
      sw.print(
          Util.getQualifiedSourceName(MoreTypes.asElement(m.getReturnType()), aptContext.elements));
      sw.println(" get() {");
      sw.indentln("return " + ident + ";");
      sw.println("}");

      sw.outdent();
      sw.println("}");

      // Strip off all but the access modifiers
      sw.print("public ");
      sw.print(
          Util.getQualifiedSourceName(MoreTypes.asElement(m.getReturnType()), aptContext.elements));
      sw.print(" ");
      sw.print(m.toString());
      sw.println(" {");
      sw.indentln("return " + initializerName + ".get();");
      sw.println("}");
    }

    return !fail;
  }

  /**
   * Emits getResources() and getResourceMap() implementations.
   *
   * @param sw the output writer
   * @param taskList the list of methods to map by name
   * @param resourceMapField field containing the Java String to Resource map
   */
  private void writeMapMethods(
      SourceWriter sw,
      Map<Class<? extends ResourceGenerator>, List<ExecutableElement>> taskList,
      String resourceMapType,
      String resourceMapField) {

    // Complete the IRB contract
    sw.println("public ResourcePrototype[] getResources() {");
    sw.indent();
    sw.println("return new ResourcePrototype[] {");
    sw.indent();
    for (List<ExecutableElement> methods : taskList.values()) {
      for (ExecutableElement method : methods) {
        // ignore ClientBundles TODO
        TypeElement methodType = (TypeElement) MoreTypes.asElement(method.getReturnType());
        if (methodType.getAnnotation(Resource.class) == null)
          sw.println(method.getSimpleName() + "(), ");
      }
    }
    sw.outdent();
    sw.println("};");
    sw.outdent();
    sw.println("}");

    // Map implementation for dev mode.
    sw.println("public ResourcePrototype getResource(String name) {");
    sw.indent();
    sw.indent();
    sw.println("if (" + resourceMapField + " == null) {");
    sw.indent();
    sw.println(resourceMapField + " = new " + resourceMapType + "();");
    for (List<ExecutableElement> list : taskList.values()) {
      for (ExecutableElement method : list) {
        // ignore ClientBundles TODO
        TypeElement methodType = (TypeElement) MoreTypes.asElement(method.getReturnType());
        if (methodType.getAnnotation(Resource.class) == null) {
          sw.println(
              resourceMapField
                  + ".put(\""
                  + method.getSimpleName()
                  + "\", "
                  + method.getSimpleName()
                  + "());");
        }
      }
    }
    sw.outdent();
    sw.println("}");
    sw.println("return resourceMap.get(name);");
    sw.outdent();
    sw.outdent();
    sw.println("}");
  }

  /** An implementation of ClientBundleFields. */
  protected static class FieldsImpl implements ClientBundleFields {
    private final NameFactory factory = new NameFactory();
    /** It is necessary to maintain order in case one field refers to another. */
    private final Map<String, String> fieldsToDeclarations = new LinkedHashMap<>();

    private final Map<String, String> fieldsToInitializers = new HashMap<>();

    @Override
    public String define(String rowType, String name) {
      return define(rowType, name, null, true, false);
    }

    @Override
    public String define(
        String rowType, String name, String initializer, boolean isStatic, boolean isFinal) {
      assert Util.isValidJavaIdent(name) : name + " is not a valid Java identifier";

      String ident = factory.createName(name);

      StringBuilder sb = new StringBuilder();
      sb.append("private ");

      if (isStatic) {
        sb.append("static ");
      }

      if (isFinal) {
        sb.append("final ");
      }

      sb.append(rowType);
      sb.append(" ");
      sb.append(ident);

      fieldsToDeclarations.put(ident, sb.toString());

      if (initializer != null) {
        fieldsToInitializers.put(ident, initializer);
      }

      return ident;
    }

    @Override
    public String define(TypeElement type, String name) {
      return define(type, name, null, true, false);
    }

    @Override
    public String define(
        TypeElement type, String name, String initializer, boolean isStatic, boolean isFinal) {
      return define(type.toString(), name, initializer, isStatic, isFinal);
    }

    @Override
    public String define(
        ArrayType array, String name, String initializer, boolean isStatic, boolean isFinal) {
      return define(array.toString(), name, initializer, isStatic, isFinal);
    }

    /**
     * This can be called to reset the initializer expression on an already-defined field.
     *
     * @param ident an identifier previously returned by {@link #define}
     * @param initializer a Java expression that will be used to initialize the field
     */
    public void setInitializer(String ident, String initializer) {
      assert fieldsToDeclarations.containsKey(ident) : ident + " not defined";
      fieldsToInitializers.put(ident, initializer);
    }

    private String getCode() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> entry : fieldsToDeclarations.entrySet()) {
        String ident = entry.getKey();
        sb.append(entry.getValue());

        String initializer = fieldsToInitializers.get(ident);
        if (initializer != null) {
          sb.append(" = ").append(initializer);
        }
        sb.append(";\n");
      }
      return sb.toString();
    }
  }
}
