[![GitHub license](https://img.shields.io/github/license/treblereel/gwt3-processors)](https://github.com/treblereel/gwt3-processors/blob/main/LICENSE)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/org.treblereel.j2cl.processors/processors?server=https%3A%2F%2Foss.sonatype.org&style=plastic)
[![Join the chat at https://gitter.im/vertispan/j2cl](https://img.shields.io/badge/GITTER-join%20chat-green.svg)](https://gitter.im/vertispan/j2cl?utm_source=badge)


# gwt3-processors

## What is it ?

It's a set of helper processors that can speed up J2CL development by generating boiler-plate code.

The latest version: https://search.maven.org/search?q=g:org.treblereel.j2cl.processors

It contains the following features:

* *@GWT3EntryPoint* acts pretty much the same as *Gwt2 EntryPoint*, the annotated method will be called on application startup.
  ```java
  public class App {

  @GWT3EntryPoint
  public void init() {
    HTMLButtonElement btn = (HTMLButtonElement) DomGlobal.document.createElement("button");
    btn.textContent = "PRESS ME !";
    }
  }
  ```

* *@ES6Module* allows us to use Es6 modules via JsInterop.
  ```java
  @ES6Module
  @JsType(isNative = true, namespace = "org.treblereel.j2cl.shim")
  public class ES6Test {
    public String id;
    public native boolean isTest();
   }
  ```
  ```javascript
   class ES6Test {
    constructor() {
      /** @type {string} */
      this.id = "#id"
    }
    /**
    * @return {boolean}
    */
    isTest() {
      return true;
    }
   }
  export { ES6Test };
  ```
  
* *@GWT3Export* allows a resulted JavaScript file to be called from the pure JavaScript environment.
  
  ```java
  public class ExportTestClass {
  
      @GWT3Export
      public static String test(String s) {
        return s;
      }
    
      @GWT3Export
      public Promise<String> promise() {
        return Promise.resolve("Hello world!");
      }
  }
  ```
  ```javascript
    var test = new org.treblereel.j2cl.exports.ExportTestClass();

    test.test('INSTANCE METHOD CALL');

    test.promise().then(function(result) {
       
    });
   ```

* *@TranslationBundle* j2cl-maven-plugin 0.20 brings us support of Closure's `.xtb` translation bundles that are a very effective way to translate your application.
    * Note: at the moment, `.xtb` support is only available for j2cl-maven-plugin 0.20 and above and works only in ADVANCED mode, otherwise default values will be used.

  To use `@TranslationBundle`, you need to do the following steps:

1. In the configuration section of j2cl-maven-plugin, enable `.xtb` auto discovery:

  ```xml
    <translationsFile>
      <auto>true</auto>
    </translationsFile>
  ```

2. To a set locale for current compilation, you need to add the following line to your `pom.xml`
    
  ```xml
    <defines>
      <goog.LOCALE>en</goog.LOCALE>
    </defines>
  ```

3. Create an interface _MyBundle_ annotated with `@TranslationBundle`
    
  ```java
    @TranslationBundle
    public interface MyBundle {
      ...
    }
  ```
  The processor will generate the `MyBundleImpl.java` implementation in the same package where _MyBundle_ is.

4. Create a translation property bundle for _MyBundle_ with a locale declared in the following format:

    * `MyBundle_en.properties` or `MyBundle_en_US.properties` containing key value pairs like:
    * `hello = Hello World!` or `hello = Hello {$arg}!`
    <br/><br/>
    Note: `{$arg}` is a placeholder for a string argument.

5. Declare corresponding methods in your _MyBundle_ interface.

  ```java
    @TranslationBundle
    public interface MyBundle {
    
      @TranslationKey(defaultValue = "Hello World!")
      String hello();
    
      @TranslationKey(defaultValue = "Hello {$arg}!")
      String hello(String arg);
    }
  ```

  Default value is used if no translation property value is found for a given locale and key.
  If a value contains the `{$arg}` placeholder, it will be replaced with the argument provided to the method. Placeholder is surrounded with curly brackets and a corresponding method argument must be named the same; 

6. Values can be in HTML, in this case HTML will be escaped. To unescape it, set `unescapeHtmlEntities = true` in the `@TranslationKey` annotation.

  ```java
      @TranslationKey(defaultValue = "<div>HELLO</div>", unescapeHtmlEntities = true)
      String hello();
  ```
   
7. Each method should have a corresponding key value pair in a translation property file. Otherwise, a default value will be used. A method name can be overridden in `@TranslationKey` annotation.

  ```java
      @TranslationKey(key = "greetings", defaultValue = "Hello World!")
      String hello();
  ```
  
Take a look at tests for more details.