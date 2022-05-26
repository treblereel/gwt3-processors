# gwt3-processors

## What is it ?

It's a set of helper processors that can speed up j2cl development by generation boiler-plate code.

The latest version: https://search.maven.org/search?q=g:org.treblereel.j2cl.processors

It contains the following features:

* *@GWT3EntryPoint* acts pretty much the same as *Gwt2 EntryPoint*, the annotated method ll be called on application startup.
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
  
Take a look at tests for more details.
